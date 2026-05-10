package com.ggg456.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ggg456.shortlink.project.dao.entity.ShortLinkDO;
import com.ggg456.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.ggg456.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.ggg456.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.ggg456.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.ggg456.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public interface ShortLinkService extends IService<ShortLinkDO> {
    /**
     * 创建短链接
     *
     * @param reqParam
     * @return
     */
    ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO reqParam) throws IOException;



    /**
     * 分页查询短链接
     *
     * @param reqParam
     * @return
     */
    IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO reqParam);

    /**
     * 更新短链接
     *
     * @param reqParam
     * @return
     */
    void updateShortLink(ShortLinkUpdateReqDTO reqParam);

    /**
     * 短链接跳转原始链接
     * @param shortUrl
     * @param request
     * @param response
     */
    void restoreUrl(String shortUrl, HttpServletRequest request, HttpServletResponse response) throws IOException;
}
