package org.xmuyoo.blueberry.collect.http;

import com.google.common.collect.ImmutableMap;
import org.springframework.http.HttpMethod;
import org.xmuyoo.blueberry.collect.domains.StockCode;

public class Requests {

    private static final String STOCK_PRICE_HOST = "hq.sinajs.cn";
    private static final String FINANCIAL_REPORT_CASH_FLOW_URL_FMT = "quotes.money.163.com/service/xjllb_%s.html";
    private static final String FINANCIAL_REPORT_REVENUE_URL_FMT = "quotes.money.163.com/service/lrb_%s.html";

    public static Request newStockCodeRequest(String code, StockCode.Exchange exchange) {
        Request request = new Request();
        request.protocol(Request.HttpProtocol.V1_1);
        request.host(STOCK_PRICE_HOST);
        request.method(HttpMethod.GET);
        request.parameters(ImmutableMap.of("list", exchange.toString() + code));

        return request;
    }

    public static Request newFinancialReportCashFlowRequest(String stockCode) {
        Request request = new Request();
        request.protocol(Request.HttpProtocol.V1_1);
        request.host(String.format(FINANCIAL_REPORT_CASH_FLOW_URL_FMT, stockCode));
        request.method(HttpMethod.GET);

        return request;
    }

    public static Request newFinancialReportRevenueRequest(String stockCode) {
        Request request = new Request();
        request.protocol(Request.HttpProtocol.V1_1);
        request.host(String.format(FINANCIAL_REPORT_REVENUE_URL_FMT, stockCode));
        request.method(HttpMethod.GET);

        return request;
    }
}
