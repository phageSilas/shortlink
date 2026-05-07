package com.ggg456.shortlink.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ggg456.shortlink.project.common.convention.exception.ServiceException;
import com.ggg456.shortlink.project.dao.entity.ShortLinkDO;
import com.ggg456.shortlink.project.dao.mapper.LinkMapper;
import com.ggg456.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.ggg456.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.ggg456.shortlink.project.service.ShortLinkService;
import com.ggg456.shortlink.project.toolkit.HashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

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
