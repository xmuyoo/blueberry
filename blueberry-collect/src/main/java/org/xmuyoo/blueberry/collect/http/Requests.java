package org.xmuyoo.blueberry.collect.http;

import org.springframework.http.HttpMethod;

import java.util.Map;

public class Requests {


    public static Request newGetRequest(String urlPattern, Map<String, String> params) {
        Request request = new Request();
        request.protocol(Request.HttpProtocol.V1_1);
        request.host(formatUrl(urlPattern, params));
        request.method(HttpMethod.GET);

        return request;
    }

    private static String formatUrl(String urlPattern, Map<String, String> parameters) {
        String formattedUrl = urlPattern;
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            formattedUrl = formattedUrl.replaceAll(String.format("\\$\\{%s\\}", key), value);
        }

        return formattedUrl;
    }
}
