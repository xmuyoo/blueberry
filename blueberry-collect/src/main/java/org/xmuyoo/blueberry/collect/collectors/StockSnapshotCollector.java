package org.xmuyoo.blueberry.collect.collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.xmuyoo.blueberry.collect.domains.DataSchema;
import org.xmuyoo.blueberry.collect.domains.StockCode;
import org.xmuyoo.blueberry.collect.domains.StockSnapshot;
import org.xmuyoo.blueberry.collect.http.HttpClient;
import org.xmuyoo.blueberry.collect.http.Request;
import org.xmuyoo.blueberry.collect.storage.PgClient;
import org.xmuyoo.blueberry.collect.utils.Utils;

@Slf4j
public class StockSnapshotCollector extends BasicCollector<StockSnapshot> {

    private static final String STOCK_SNAPSHOT = "stock_snapshot";

    private final HttpClient http;
    private final String xqUrlFmt;
    private String cookie;

    public StockSnapshotCollector(PgClient storage, HttpClient httpClient) {
        super(STOCK_SNAPSHOT, storage, StockSnapshot.class);
        this.http = httpClient;

        Config stockSnapshotConfig = ConfigFactory.load("stock-snapshot");
        this.xqUrlFmt = stockSnapshotConfig.getString("xq.snapshot.url.format");

        Config xueqiuCfg = ConfigFactory.load("xueqiu");
        this.cookie = xueqiuCfg.getString("cookie");
    }

    @Override
    protected boolean needCreateEntityTable() {
        return true;
    }

    @SneakyThrows
    @Override
    protected boolean collect() {
        List<StockCode> stockCodeList =
                this.storage.queryList("SELECT code, name, exchange FROM stock_code", StockCode.class);

        List<StockSnapshot> stockSnapshotList = new ArrayList<>();
        int idx = 1;
        for (StockCode stockCode : stockCodeList) {
            log.info("StockSnapshot at {}: {}", idx, stockCode.name());
            String identifier = stockCode.exchange().name() + stockCode.code();
            String url = String.format(this.xqUrlFmt, identifier);

            Request request = new Request();
            request.url(url);
            request.method(HttpMethod.GET);
            request.headers(ImmutableMap.of("Cookie", cookie));

            StockSnapshot stockSnapshot = http.sync(request, response -> {
                try {
                    if (null != response.body()) {
                        byte[] content = response.body().bytes();
                        if (content.length == 0) {
                            return null;
                        }

                        XqStockSnapshotResponse resp =
                                Utils.deserialize(content, XqStockSnapshotResponse.class);
                        Data data = resp.data();
                        return mapToEntity(data.quote(), StockSnapshot.class);
                    } else {
                        return null;
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse response data for {}: ", stockCode, e);
                    return null;
                }
            });

            if (null != stockSnapshot) {
                stockSnapshotList.add(stockSnapshot);
            }
            TimeUnit.MILLISECONDS.sleep(10);

            if (idx % 100 == 0) {
                this.storage.saveOrUpdate(stockSnapshotList, StockSnapshot.class);
                stockSnapshotList.clear();
            }

            idx++;
        }

        this.storage.saveOrUpdate(stockSnapshotList, StockSnapshot.class);

        return true;
    }

    @Override
    protected List<DataSchema> getDataSchemaList() {
        return toSchemaList(StockSnapshot.class);
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class XqStockSnapshotResponse {
        @JsonProperty("data")
        private Data data;

        @JsonProperty("error_code")
        private int errorCode;

        @JsonProperty("error_description")
        private String errorDescription;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data {
        @JsonProperty("quote")
        private Map<String, Object> quote;
    }
}
