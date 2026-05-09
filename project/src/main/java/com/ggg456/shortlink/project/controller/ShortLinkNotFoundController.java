package com.ggg456.shortlink.project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ShortLinkNotFoundController {
    /**
     * 短链接不存在跳转控制器
     */
    @RequestMapping("/page/notfound")
    public String notFound() {
        return "notfound";
    }
}
