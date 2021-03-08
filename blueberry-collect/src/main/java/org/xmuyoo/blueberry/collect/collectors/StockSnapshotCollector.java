package org.xmuyoo.blueberry.collect.collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
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
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Slf4j
public class StockSnapshotCollector extends BasicCollector {

    private static final String STOCK_SNAPSHOT = "stock_snapshot";

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
        for (StockCode stockCode : stockCodeList) {
            log.info("StockSnapshot: {}", stockCode.name());
            String identifier = stockCode.exchange().name() + stockCode.code();
            String url = String.format(this.xqUrlFmt, identifier);

            Request request = new Request();
            request.url(url);
            request.method(HttpMethod.GET);
            request.headers(ImmutableMap.of("Cookie", "device_id=24700f9f1986800ab4fcc880530dd0ed; s=d71une92u2; xq_a_token=caa685f305e8e80732095691828b5946340658ce; xqat=caa685f305e8e80732095691828b5946340658ce; xq_r_token=97209c89d3e8807516f4b39dd29652b843e88043; xq_id_token=eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJ1aWQiOjEwMzkyMzQ0NTMsImlzcyI6InVjIiwiZXhwIjoxNjE2ODk1NTI2LCJjdG0iOjE2MTQzMDM1MjY2NTgsImNpZCI6ImQ5ZDBuNEFadXAifQ.X5bkhiC4FirlDbw5wVBazd5Sf83N98P01DgtQUFFWIr-IAfMVNgCwvpi2ywiRxW4z8mgx7RlS1pmufimnnLRfy_kvSunVW8lf-BccogokfvpukX_XExbxIlxlByHB1Zv3KjdFbIa7AoM0Y8Dynm9YOy-fXTwarBnOyQhM2Rbt_LaHcRsFP9ouqVPrQYvcRxgDoDBgVBpSjpJ9upDox0PpmqXcWrJbSfLv1WFFYonZ6Fes8TTrLfmuZb_tujETdRj8NTcGaAaxvrENJQNDChTKcRsNqx39ftvhYGM4GHz0JoCbkMnBjqaYiklNNI60bMegwHvZ_suJ9gLEt1iZu9xQQ; xq_is_login=1; u=1039234453; snbim_minify=true; bid=73e383c861a64ba9668b2c3a7057510e_kllow2np; Hm_lvt_1db88642e346389874251b5a1eded6e3=1612923488,1614303504; Hm_lpvt_1db88642e346389874251b5a1eded6e3=1615085659"));

            StockSnapshot stockSnapshot = http.sync(request, new Function<Response, StockSnapshot>() {

                @SneakyThrows
                @Override
                public StockSnapshot apply(Response response) {
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
                }
            });

            stockSnapshotList.add(stockSnapshot);

//            TimeUnit.SECONDS.sleep(1);
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
            Object val = this.quote.get(key);
            if (null == val)
                return defaultValue;

            return clz.cast(val);
        }
    }
}
