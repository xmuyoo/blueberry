package org.xmuyoo.blueberry.collect.collectors.data.source;

import com.google.common.collect.ImmutableMap;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.Map;

public class Jisilu implements RemoteDataSource {

    private static final Map<String, String> JISILU_HEADERS = ImmutableMap.<String, String>builder()
            .put("Host", "www.jisilu.cn")
            .put("Pragma", "no-cache")
            .put("sec-ch-ua", "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"96\", \"Google Chrome\";v=\"96\"")
            .put("sec-ch-ua-mobile", "?0")
            .put("sec-ch-ua-platform", "macOS")
            .put("Sec-Fetch-Dest", "document")
            .put("Sec-Fetch-Mode", "navigate")
            .put("Sec-Fetch-Site", "none")
            .put("Sec-Fetch-User", "?1")
            .put("Upgrade-Insecure-Requests", "1")
            .put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.55 Safari/537.36")
            .put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            .put("Connection", "keep-alive")
            .put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
            .put("Accept-Encoding", "gzip, deflate, br")
            .put("Accept-Language", "zh-CN,zh;q=0.9")
            .put("Cache-Control", "no-cache")
            .build();

    private final Config config;

    Jisilu() {
        this.config = ConfigFactory.load("jisilu");
    }

    @Override
    public Map<String, String> getRemoteHTTPRequestHeaders() {
        return JISILU_HEADERS;
    }

    @Override
    public String getRemoteHTTPRequestCookie() {
        return config.getString("cookie");
    }
}
