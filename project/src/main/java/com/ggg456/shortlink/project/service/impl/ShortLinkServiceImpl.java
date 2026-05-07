package com.ggg456.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ggg456.shortlink.project.dao.entity.ShortLinkDO;
import com.ggg456.shortlink.project.dao.mapper.LinkMapper;
import com.ggg456.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.ggg456.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.ggg456.shortlink.project.service.ShortLinkService;
import com.ggg456.shortlink.project.toolkit.HashUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ShortLinkServiceImpl extends ServiceImpl<LinkMapper, ShortLinkDO> implements ShortLinkService {


    /**
     * 创建短链接
     * @param reqParam
     * @return 短链接创建结果
     */
    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO reqParam) {
        String shortLinkSuffix = generateSuffix(reqParam);
        ShortLinkDO shortLinkDO = BeanUtil.toBean(reqParam, ShortLinkDO.class);
        shortLinkDO.setFullShortUrl(reqParam.getDomain()+ "/" + shortLinkSuffix);
        shortLinkDO.setShortUri(shortLinkSuffix);
        shortLinkDO.setEnableStatus(1);
        shortLinkDO.setCreatedType(reqParam.getCreatedType());
        baseMapper.insert(shortLinkDO);


        return ShortLinkCreateRespDTO.builder()
                .fullShortUrl(shortLinkDO.getFullShortUrl())
                .gid(shortLinkDO.getGid())
                .originUrl(shortLinkDO.getOriginUrl())
                .build();
    }

    private String generateSuffix(ShortLinkCreateReqDTO reqParam) {
        String originUrl = reqParam.getOriginUrl();
        return HashUtil.hashToBase62(originUrl);// 生成短链接后缀
        //因为sql的默认编码不区分大小写,所以这里需要在数据库中(查询控制台)将短链接后缀的编码设置为utf8_bin
        //ALTER TABLE t_link MODIFY short_uri VARCHAR(8) CHARACTER SET utf8 COLLATE utf8_bin;
    }



}
