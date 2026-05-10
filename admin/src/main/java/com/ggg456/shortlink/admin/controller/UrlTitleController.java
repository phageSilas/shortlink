package com.ggg456.shortlink.admin.controller;


import com.ggg456.shortlink.admin.common.convention.result.Result;
import com.ggg456.shortlink.admin.common.convention.result.Results;
import com.ggg456.shortlink.admin.service.UrlTitleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class UrlTitleController {
    private final UrlTitleService urlTitleService;

    @GetMapping("/api/short-link/admin/v1/title")
    public Result<String> getTitleByUrl(@RequestParam("url") String url) throws IOException {
        return Results.success(urlTitleService.getTitleByUrl(url));
    }
}
