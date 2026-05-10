package com.ggg456.shortlink.admin.service.impl;


import com.ggg456.shortlink.admin.service.UrlTitleService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class UrlTitleServiceImpl implements UrlTitleService {

    /**
     *
     * @param url
     * @return
     */
    @Override
    public String getTitleByUrl(String url) throws IOException {

            // 建立连接，设置超时时间并可模拟浏览器 User-Agent
            Document doc = Jsoup.connect(url)
                    .timeout(5000)           // 连接超时 5 秒
                    .userAgent("Mozilla/5.0") // 设置 User-Agent 避免被拒
                    .get();
            // 获取 <title> 标签内的文本
            return doc.title();

    }
}
