package com.ggg456.shortlink.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ggg456.shortlink.project.dao.entity.ShortLinkDO;
import com.ggg456.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.ggg456.shortlink.project.dto.resp.ShortLinkCreateRespDTO;

public interface ShortLinkService extends IService<ShortLinkDO> {
    /**
     * 创建短链接
     *
     * @param reqParam
     * @return
     */
    ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO reqParam);
}
