package org.xmuyoo.blueberry.collect.collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.springframework.http.HttpMethod;
import org.xmuyoo.blueberry.collect.domains.DataSchema;
import org.xmuyoo.blueberry.collect.domains.StockCode;
import org.xmuyoo.blueberry.collect.domains.StockSnapshot;
import org.xmuyoo.blueberry.collect.http.HttpClient;
import org.xmuyoo.blueberry.collect.http.Request;
import org.xmuyoo.blueberry.collect.storage.PgClient;
import org.xmuyoo.blueberry.collect.storage.ValueType;
import org.xmuyoo.blueberry.collect.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Slf4j
public class StockSnapshotCollector extends BasicCollector {

    private static final String STOCK_SNAPSHOT = "stock_snapshot";
    private static final String COOKIE_STRING = "Hm_lvt_1db88642e346389874251b5a1eded6e3=1634136017,1635863052; device_id=94aee6be8ada348a95176a08545f9fea; s=c212bco9yi; xq_a_token=84594e5ed31d2073c04e20e6d5f6025ca3c9412e; xqat=84594e5ed31d2073c04e20e6d5f6025ca3c9412e; xq_r_token=7db1253145a2bfc99a82e274223cf9c06d8e9097; xq_id_token=eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJ1aWQiOjEwMzkyMzQ0NTMsImlzcyI6InVjIiwiZXhwIjoxNjM4NDU1MDc1LCJjdG0iOjE2MzU4NjMwNzUzNjgsImNpZCI6ImQ5ZDBuNEFadXAifQ.WSdP1AufwTqD8I_lRJc4EABADJpdUHqfFxJ58YZYkXDcwTH_LZm_Oa2NR7yV4sySPOCKksH32u7Prnwbc7Th8gDKWxC5nYLWs3QVN1jNys61D1WROVAGhEqYm-jaDNrn1iprDHl0P3LNkEMA2gG2h1o75PmBJngKehU1-lZXGblr4vdLoCWaF0m2FrgvrXG5f_Wx4C-BugnBOGfVjDDH6R9xvq502RKKIEfSNxR58LQI3QeLVlHyymnRXSK4GJdUQmky5Oz8jN0z4WgADXPcXwlDeSm1wR_VAiSMgm2TTsM7keKSrsfTlRQFbR4jOpMgIHaQT-YFBpL67c7A8Ss9Fg; xq_is_login=1; u=1039234453; is_overseas=0; Hm_lpvt_1db88642e346389874251b5a1eded6e3=1635863077";

    private final HttpClient http;
    private final String xqUrlFmt;

    public StockSnapshotCollector(PgClient storage, HttpClient httpClient) {
        super(STOCK_SNAPSHOT, storage);
        this.http = httpClient;

        Config config = ConfigFactory.load("stock-snapshot");
        this.xqUrlFmt = config.getString("xq.snapshot.url.format");
    }

    @Override
    protected boolean isAvailable() {
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
            request.headers(ImmutableMap.of("Cookie", COOKIE_STRING));

            StockSnapshot stockSnapshot = http.sync(request, new Function<Response, StockSnapshot>() {
                @Override
                public StockSnapshot apply(Response response) {
                    try {
                        XqStockSnapshotResponse resp =
                                Utils.deserialize(response.body().bytes(), XqStockSnapshotResponse.class);

                        Data data = resp.data();
                        Double peLyr = data.getValue(StockSnapshot.PE_LYR, Double.class, 0.0);
                        Double pb = data.getValue(StockSnapshot.PB, Double.class, 0.0);
                        Double dividendYield = data.getValue(StockSnapshot.DIVIDEND_YIELD, Double.class, 0.0);
                        Double marketCapital = data.getValue(StockSnapshot.MARKET_CAPITAL, Double.class, 0.0);
                        Number totalShares = data.getValue(StockSnapshot.TOTAL_SHARES, Number.class, 0L);
                        Double lastClose = data.getValue(StockSnapshot.LAST_CLOSE, Double.class, 0.0);
                        Double navps = data.getValue(StockSnapshot.NAVPS, Double.class, 0.0);

                        StockSnapshot snapshot = new StockSnapshot();
                        snapshot.code(stockCode.code());
                        snapshot.name(stockCode.name());
                        snapshot.peLyr(peLyr);
                        snapshot.pb(pb);
                        snapshot.totalShares(totalShares);
                        snapshot.marketCapital(marketCapital);
                        snapshot.dividendYield(dividendYield);
                        snapshot.lastClose(lastClose);
                        snapshot.navps(navps);

                        return snapshot;
                    } catch (Exception e) {
                        log.warn("Failed to parse response data for {}: ", stockCode, e);
                        return null;
                    }
                }
            });

            if (null != stockSnapshot) {
                stockSnapshotList.add(stockSnapshot);
            }
            TimeUnit.MILLISECONDS.sleep(10);

            if (idx % 100 == 0) {
                this.storage.saveIgnoreDuplicated(stockSnapshotList, StockSnapshot.class);
                stockSnapshotList.clear();
            }

            idx++;
        }

        this.storage.saveIgnoreDuplicated(stockSnapshotList, StockSnapshot.class);

        return true;
    }

    @Override
    protected List<DataSchema> getDataSchemaList() {
        return ImmutableList.<DataSchema>builder()
                .add(new DataSchema(STOCK_SNAPSHOT, StockSnapshot.CODE, ValueType.Text, "股票代码", STOCK_SNAPSHOT))
                .add(new DataSchema(STOCK_SNAPSHOT, StockSnapshot.NAME, ValueType.Text, "股票名称", STOCK_SNAPSHOT))
                .add(new DataSchema(STOCK_SNAPSHOT, StockSnapshot.DIVIDEND_YIELD, ValueType.Number, "股息率", STOCK_SNAPSHOT))
                .add(new DataSchema(STOCK_SNAPSHOT, StockSnapshot.MARKET_CAPITAL, ValueType.Number, "总市值", STOCK_SNAPSHOT))
                .add(new DataSchema(STOCK_SNAPSHOT, StockSnapshot.PE_LYR, ValueType.Number, "市盈率（静）", STOCK_SNAPSHOT))
                .add(new DataSchema(STOCK_SNAPSHOT, StockSnapshot.PB, ValueType.Number, "市净率", STOCK_SNAPSHOT))
                .add(new DataSchema(STOCK_SNAPSHOT, StockSnapshot.NAVPS, ValueType.Number, "每股净资产", STOCK_SNAPSHOT))
                .add(new DataSchema(STOCK_SNAPSHOT, StockSnapshot.TOTAL_SHARES, ValueType.Number, "总股本", STOCK_SNAPSHOT))
                .add(new DataSchema(STOCK_SNAPSHOT, StockSnapshot.LAST_CLOSE, ValueType.Number, "昨日收盘价", STOCK_SNAPSHOT))
                .build();
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

        public <T> T getValue(String key, Class<T> clz, T defaultValue) {
            if (null == this.quote) {
                return null;
            }

            Object val = this.quote.get(key);
            if (null == val) {
                return defaultValue;
            }

            return clz.cast(val);
        }
    }
}
