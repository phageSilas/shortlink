package com.ggg456.shortlink.project.service;

import java.io.IOException;

public interface UrlTitleService {

    /**
     * 根据url获取标题
     *
     * @param url
     * @return
     */
    String getTitleByUrl(String url) throws IOException;
}
