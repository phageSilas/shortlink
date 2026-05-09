package com.ggg456.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ggg456.shortlink.project.common.constant.RedisKeyConstant;
import com.ggg456.shortlink.project.common.convention.exception.ServiceException;
import com.ggg456.shortlink.project.common.enums.VailDateTypeEnum;
import com.ggg456.shortlink.project.dao.entity.ShortLinkDO;
import com.ggg456.shortlink.project.dao.entity.ShortLinkGotoDO;
import com.ggg456.shortlink.project.dao.mapper.LinkMapper;
import com.ggg456.shortlink.project.dao.mapper.ShortLinkGotoMapper;
import com.ggg456.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.ggg456.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.ggg456.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.ggg456.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.ggg456.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.ggg456.shortlink.project.service.ShortLinkService;
import com.ggg456.shortlink.project.toolkit.HashUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.ggg456.shortlink.project.toolkit.LinkUtil.getLinkCacheValidTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<LinkMapper, ShortLinkDO> implements ShortLinkService {

    private final RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter;
    private final ShortLinkGotoMapper shortLinkGotoMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;


    /**
     * 创建短链接
     * @param reqParam
     * @return 短链接创建结果
     */
    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO reqParam) {
        String shortLinkSuffix = generateSuffix(reqParam);// 生成短链接后缀
        String fullShortUrl = reqParam.getDomain() + "/" + shortLinkSuffix;// 生成完整的短链接

        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .id(null)
                .domain(reqParam.getDomain())
                .gid(reqParam.getGid())
                .originUrl(reqParam.getOriginUrl())
                .validDateType(reqParam.getValidDateType())
                .validDate(reqParam.getValidDate())
                .describe(reqParam.getDescribe())
                .fullShortUrl(fullShortUrl)
                .shortUri(shortLinkSuffix)
                .enableStatus(0)
                .createdType(reqParam.getCreatedType())
                .build();
       // ShortLinkDO shortLinkDO = BeanUtil.toBean(reqParam, ShortLinkDO.class); // 将请求参数转换为DO//  shortLinkDO.setFullShortUrl(fullShortUrl); // 设置完整的短链接
      //  shortLinkDO.setShortUri(shortLinkSuffix); // 设置短链接后缀
      //  shortLinkDO.setEnableStatus(0); // 设置启用状态为0
      //  shortLinkDO.setCreatedType(reqParam.getCreatedType()); // 设置创建类型
//
        ShortLinkGotoDO shortLinkGotoDO = ShortLinkGotoDO.builder()
                .gid(reqParam.getGid())
                .fullShortUrl(fullShortUrl)
                .build();
        try {
            baseMapper.insert(shortLinkDO); // 插入短链接
            shortLinkGotoMapper.insert(shortLinkGotoDO); // 插入短链接跳转表
        } catch (DuplicateKeyException e) { // 插入失败, 说明已经存在
            LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl, fullShortUrl);// 查询完整的短链接
            ShortLinkDO hasShortLinkDO = baseMapper.selectOne(queryWrapper);
            if (hasShortLinkDO != null) {
                // TODO 已经误判的短链接如何处理
                log.warn("创建短链接失败, 短链接: {}已存在",fullShortUrl);
                throw new ServiceException("创建短链接失败, 短链接: " + fullShortUrl + "已存在");
            }


        }

        //将新建的短链接缓存到Redis中(缓存预热)
        stringRedisTemplate.opsForValue().set(String.format(RedisKeyConstant.GOTO_SHORT_LINK_KEY, fullShortUrl), reqParam.getOriginUrl(), getLinkCacheValidTime(reqParam.getValidDate()), TimeUnit.MILLISECONDS);

        shortUriCreateCachePenetrationBloomFilter.add(shortLinkDO.getShortUri()); // 添加到布隆过滤器中

        return ShortLinkCreateRespDTO.builder()
                .fullShortUrl("http://"+shortLinkDO.getFullShortUrl())
                .gid(shortLinkDO.getGid())
                .originUrl(shortLinkDO.getOriginUrl())
                .build();
    }

    /**W
     * 分页查询短链接
     * @param reqParam
     * @return
     */
    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO reqParam) {
        Wrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, reqParam.getGid())
                .eq(ShortLinkDO::getEnableStatus, 0)
                .eq(ShortLinkDO::getDelFlag, 0)
                .orderByDesc(ShortLinkDO::getCreateTime);
        IPage<ShortLinkDO> resultPage = baseMapper.selectPage(reqParam, queryWrapper);
      return  resultPage.convert(each -> BeanUtil.toBean(each, ShortLinkPageRespDTO.class));

    }

    /**
     * 更新短链接
     * @param reqParam
     * @return
     */
    @Override
    public void updateShortLink(ShortLinkUpdateReqDTO reqParam) {
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, reqParam.getGid())
                .eq(ShortLinkDO::getFullShortUrl, reqParam.getFullShortUrl())
                /*.eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getEnableStatus, 0)*/;
                //todo: 关于删除状态和启用状态的查询修改
        ShortLinkDO hasShortLinkDO = baseMapper.selectOne(queryWrapper); //根据前端传进来的数据查到要修改的目标链接

        if (hasShortLinkDO == null) {
            throw new ServiceException("该短链接不存在");
        }
        // 将前端传进来的数据更新到数据库中
        ShortLinkDO updateShortLinkDO = ShortLinkDO.builder() //注意:gid不在本次修改数据中
                .domain(hasShortLinkDO.getDomain()) //不允许修改域名
                .shortUri(hasShortLinkDO.getShortUri()) //不允许修改短链接后缀
                .clickNum(hasShortLinkDO.getClickNum()) //不允许修改点击次数

                .originUrl(reqParam.getOriginUrl()) //允许修改原链接
                .validDateType(reqParam.getValidDateType()) //允许修改有效期类型
                .validDate(reqParam.getValidDate()) //允许修改有效期
                .describe(reqParam.getDescribe()) //允许修改描述
                .enableStatus(0)
                .build();

        if (Objects.equals(hasShortLinkDO.getGid(), reqParam.getGid())) { //如果前端传入的gid和数据库中的gid一致,则直接修改其他数据
            Wrapper<ShortLinkDO> updateWrapperGidIsEqual = Wrappers.lambdaUpdate(ShortLinkDO.class)
                    .eq(ShortLinkDO::getGid, reqParam.getGid())
                    .eq(ShortLinkDO::getFullShortUrl, reqParam.getFullShortUrl())
                    //.eq(ShortLinkDO::getDelFlag, 0)
                    //.eq(ShortLinkDO::getEnableStatus, 0)
                    .set(Objects.equals(reqParam.getValidDateType(), VailDateTypeEnum.PERMANENT.getType()), ShortLinkDO::getValidDate, 0);

            baseMapper.update(updateShortLinkDO, updateWrapperGidIsEqual);

        } else { //如果前端传入的gid和数据库中的gid不一致,则先删除数据库中的数据,再插入新的数据
            Wrapper<ShortLinkDO> updateWrapperGidNotEqual = Wrappers.lambdaUpdate(ShortLinkDO.class)
                    .eq(ShortLinkDO::getGid, reqParam.getGid())
                    .eq(ShortLinkDO::getFullShortUrl, reqParam.getFullShortUrl())
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0);

            baseMapper.delete(updateWrapperGidNotEqual);//删除对应的字段
            updateShortLinkDO.setGid(reqParam.getGid());//设置新的gid
            baseMapper.update(updateShortLinkDO, updateWrapperGidNotEqual);//更新数据库中字段
        }


    }

    /**
     * 短链接跳转原始链接
     * @param shortUrl
     * @param request
     * @param response
     */
    @SneakyThrows // 忽略异常
    @Override
    public void restoreUrl(String shortUrl, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String serverName = request.getServerName();
        String fullShortUrl = serverName+ "/"+shortUrl;

        String originalUrl = stringRedisTemplate.opsForValue().get(RedisKeyConstant.GOTO_SHORT_LINK_KEY + fullShortUrl);//尝试从Redis中获取原始链接，如果存在则重定向

        if (StrUtil.isNotBlank(originalUrl)) { //弱redis缓存中存在查询的链接,则重定向
            ((HttpServletResponse) response).sendRedirect(originalUrl);
            return;
        }
        //若Redis中不存在链接,则尝试从数据库中获取原始链接
        //该锁是用来减少缓存击穿的,当多个请求同时发现缓存不存在时, 只有一个请求能获得锁去查数据库
        //其他请求等待锁释放后，通过"双重检查"直接从缓存获取
        RLock lock = redissonClient.getLock(String.format(RedisKeyConstant.LOCK_GOTO_SHORT_LINK_KEY, fullShortUrl));
        lock.lock();
           try {
               originalUrl = stringRedisTemplate.opsForValue().get(RedisKeyConstant.GOTO_SHORT_LINK_KEY + fullShortUrl);//再次尝试从Redis中获取原始链接，如果存在则重定向
               if (StrUtil.isNotBlank(originalUrl)) { //如果Redis缓存中存在查询的链接,则重定向
                   response.sendRedirect(originalUrl);
                   return;
               }

               //检查布隆过滤器中是否有该链接,若不包含,则证明该链接一定不存在,直接返回
               boolean isContains = shortUriCreateCachePenetrationBloomFilter.contains(fullShortUrl);
               //注意布隆过滤器中的元素无法删除(删除成本太高)
               if (!isContains) {
                   return;
               }

               LambdaQueryWrapper<ShortLinkGotoDO> linkGotoQueryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                       .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);
               ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(linkGotoQueryWrapper); //根据传进来的完整短链接在Goto表中查询对应的行
               if (shortLinkGotoDO == null) {
                   stringRedisTemplate .opsForValue().set(String.format(RedisKeyConstant.GOTO_SHORT_LINK_KEY, fullShortUrl), "-",30, TimeUnit.MINUTES);//在数据库未查询到对应的链接,将该次请求缓存进Redis,且值为"-"(可以认为是空值),防止缓存穿透
                   throw new ServiceException("短链接不存在");
               }

               LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                       .eq(ShortLinkDO::getGid, shortLinkGotoDO.getGid()) //根据Goto表中查到的数据锁定其对应的Gid,然后根据这个Gid去数据库中查询对应的原始链接
                       .eq(ShortLinkDO::getFullShortUrl, fullShortUrl)
                       .eq(ShortLinkDO::getDelFlag, 0)
                       .eq(ShortLinkDO::getEnableStatus, 0);
               ShortLinkDO shortLinkDO = baseMapper.selectOne(queryWrapper);

               if (shortLinkDO != null) { //如果数据库中存在这条数据,并且经过第一个if判断后,可知redis中没有该数据,那么则将原始链接写入Redis缓存中,并重定向
                   if (shortLinkDO.getValidDate() != null && shortLinkDO.getValidDate().before(new Date())) { //如果该短链接的生效时间不为空且该时间小于当前时间(蔽日有效期2026.1 是before 2026.5的),则判断该短链接已失效,返回"短链接已失效"
                       stringRedisTemplate .opsForValue().set(String.format(RedisKeyConstant.GOTO_SHORT_LINK_KEY, fullShortUrl), "-",30, TimeUnit.MINUTES);//将该失效的链接也缓存个空值,将该次请求缓存进Redis,且值为"-"(可以认为是空值),防止缓存穿透
                        return;
                   }
                   stringRedisTemplate.opsForValue().set(String.format(RedisKeyConstant.GOTO_SHORT_LINK_KEY, fullShortUrl), shortLinkDO.getOriginUrl(), getLinkCacheValidTime(shortLinkDO.getValidDate()), TimeUnit.MILLISECONDS);
                   response.sendRedirect(shortLinkDO.getOriginUrl());
               }
           } finally {
               lock.unlock();
           }
        }



    private String generateSuffix(ShortLinkCreateReqDTO reqParam) {
        int customGenerateCount =0;
        String shortUri;
        while (true) {
            if (customGenerateCount > 10) {
                throw new ServiceException("生成短链接失败, 请稍后再试");
            }
            String originUrl = reqParam.getOriginUrl();
            originUrl += System.currentTimeMillis();// 添加时间戳, 降低短链接重复概率
            shortUri = HashUtil.hashToBase62(originUrl);// 生成短链接后缀
            if (!shortUriCreateCachePenetrationBloomFilter.contains(shortUri)) { // 判断短链接后缀是否在布隆过滤器中
                break;
            }
            customGenerateCount++;

        }

        return shortUri;
        //因为sql的默认编码不区分大小写,所以这里需要在数据库中(查询控制台)将短链接后缀的编码设置为utf8_bin
        //ALTER TABLE t_link MODIFY short_uri VARCHAR(8) CHARACTER SET utf8 COLLATE utf8_bin;
    }



}
