package org.xmuyoo.blueberry.collect;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.xmuyoo.blueberry.collect.domains.StockCode;
import org.xmuyoo.blueberry.collect.http.HttpClient;
import org.xmuyoo.blueberry.collect.http.Request;
import org.xmuyoo.blueberry.collect.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class TShareData {

    private static final String TU_SHARE_API_HOST = "api.waditu.com";

    private final String token;
    private final HttpClient httpClient;

    public TShareData(HttpClient httpClient) {
        Config config = ConfigFactory.load("application");
        this.token = config.getString("tu.share.token");
        this.httpClient = httpClient;
    }

    @SneakyThrows
    public List<StockCode> stockCodeList() {
        Request request = new Request();
        request.host(TU_SHARE_API_HOST);
        request.method(HttpMethod.POST);
        request.body(Utils.serialize(apiBody("stock_basic",
                ImmutableMap.of("list_status", "L"),
                Collections.emptyList())));
        List<StockCode> stockCodes = httpClient.sync(request, new Function<Response, List<StockCode>>() {

            @SneakyThrows
            @Override
            public List<StockCode> apply(Response response) {
                TuShareResponse tResp = Utils.deserialize(response.body().bytes(), TuShareResponse.class);
                TData tData = tResp.data();

                List<StockCode> codeList = new ArrayList<>();
                for (List<Object> item : tData.items()) {
                    String exchange = ((String) item.get(0)).split("\\.")[1];
                    String code = (String) item.get(1);
                    String name = (String) item.get(2);
                    String location = (String) item.get(3);
                    if (StringUtils.isBlank(location))
                        location = "NA";

                    String category = (String) item.get(4);
                    if (StringUtils.isBlank(category))
                        category = "NA";

                    StockCode stockCode = new StockCode();
                    stockCode.exchange(StockCode.Exchange.valueOf(exchange.toUpperCase()));
                    stockCode.code(code);
                    stockCode.name(name);
                    stockCode.location(location);
                    stockCode.category(category);

                    codeList.add(stockCode);
                }

                return codeList;
            }
        });

        return stockCodes;
    }

    private Map<String, Object> apiBody(String apiName, Map<String, Object> parameters, List<String> fields) {
        return ImmutableMap.of(
                "api_name", apiName,
                "token", this.token,
                "parameters", parameters,
                "fields", String.join(",", fields));
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class TuShareResponse {

        @JsonProperty("request_id")
        private String requestId;

        @JsonProperty("code")
        private int code;

        @JsonProperty
        private String msg;

        @JsonProperty("data")
        private TData data;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class TData {

        @JsonProperty
        private List<String> fields;

        @JsonProperty
        private List<List<Object>> items;
    }
}
