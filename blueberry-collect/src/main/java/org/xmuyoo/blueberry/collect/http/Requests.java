package org.xmuyoo.blueberry.collect.http;

import com.google.common.collect.ImmutableMap;
import org.springframework.http.HttpMethod;
import org.xmuyoo.blueberry.collect.domains.StockCode;

public class Requests {

    private static final String STOCK_PRICE_HOST = "hq.sinajs.cn";

    public static Request newStockCodeRequest(String code, StockCode.Type type) {
        Request request = new Request();
        request.protocol(Request.HttpProtocol.V1_1);
        request.host(STOCK_PRICE_HOST);
        request.method(HttpMethod.GET);
        request.parameters(ImmutableMap.of("list", type.toString() + code));

        return request;
    }
}
