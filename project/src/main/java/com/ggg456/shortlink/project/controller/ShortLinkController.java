package com.ggg456.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ggg456.shortlink.project.common.convention.result.Result;
import com.ggg456.shortlink.project.common.convention.result.Results;
import com.ggg456.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.ggg456.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.ggg456.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.ggg456.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.ggg456.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.ggg456.shortlink.project.service.ShortLinkService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class ShortLinkController {
    private final ShortLinkService shortLinkService;

    /**
     * 创建短链接
     * @param reqParam
     * @return
     */
    @PostMapping("/api/short-link/admin/v1/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO reqParam) {
        return Results.success(shortLinkService.createShortLink(reqParam));
    }


    /**
     * 分页查询短链接
     * @return
     */
    @GetMapping("/api/short-link/admin/v1/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO reqParam) {
        return Results.success(shortLinkService.pageShortLink(reqParam));
    }

    /**
     * 修改短链接
     * @param reqParam
     * @return
     */
    @PostMapping("/api/short-link/admin/v1/update")
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDTO reqParam) {
        shortLinkService.updateShortLink(reqParam);
        return Results.success();
    }

    /**
     * 短链接跳转原始链接
     * @param shortUrl
     * @param request
     * @param response
     */
    @GetMapping("/{short-url}")
    public void restoreUrl(@PathVariable("short-url") String shortUrl, HttpServletRequest request, HttpServletResponse response) throws IOException {
      shortLinkService.restoreUrl(shortUrl, request, response);
    }


}
