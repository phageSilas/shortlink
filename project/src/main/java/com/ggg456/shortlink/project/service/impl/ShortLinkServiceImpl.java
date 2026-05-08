package com.ggg456.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ggg456.shortlink.project.common.convention.exception.ServiceException;
import com.ggg456.shortlink.project.common.enums.VailDateTypeEnum;
import com.ggg456.shortlink.project.dao.entity.ShortLinkDO;
import com.ggg456.shortlink.project.dao.mapper.LinkMapper;
import com.ggg456.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.ggg456.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.ggg456.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.ggg456.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.ggg456.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.ggg456.shortlink.project.service.ShortLinkService;
import com.ggg456.shortlink.project.toolkit.HashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<LinkMapper, ShortLinkDO> implements ShortLinkService {

    private final RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter;


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
        try {
            baseMapper.insert(shortLinkDO);
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

        shortUriCreateCachePenetrationBloomFilter.add(shortLinkDO.getShortUri()); // 添加到布隆过滤器中

        return ShortLinkCreateRespDTO.builder()
                .fullShortUrl(shortLinkDO.getFullShortUrl())
                .gid(shortLinkDO.getGid())
                .originUrl(shortLinkDO.getOriginUrl())
                .build();
    }

    /**
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
