package org.xmuyoo.blueberry.collect.collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.xmuyoo.blueberry.collect.domains.DataSchema;
import org.xmuyoo.blueberry.collect.domains.StockCode;
import org.xmuyoo.blueberry.collect.domains.StockKLine;
import org.xmuyoo.blueberry.collect.http.HttpClient;
import org.xmuyoo.blueberry.collect.http.Request;
import org.xmuyoo.blueberry.collect.storage.ChClient;
import org.xmuyoo.blueberry.collect.storage.PgClient;

@Slf4j
public class StockKLineCollector extends BasicCollector<StockKLine> {

    private static final String STOCK_K_LINE = "stock_k_line";
    private static final String K_LINE_URL =
            "https://stock.xueqiu.com/v5/stock/chart/kline.json";
    private static final Map<String, String> K_LINE_URL_FIX_PARAMS = ImmutableMap.of(
            "indicator", "chg,kline,pe,pb,ps,pcf,market_capital,agt,ggt,balance",
            "period", "day",
            "type", "before",
            "count", "-1000");

    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final Long ONE_DAY_MS = 86400L * 1000;

    private static final String CREATE_STOCK_K_LINE_CLICKHOUSE_SQL =
            "CREATE TABLE IF NOT EXISTS stock_k_line (\n" +
            "    code String,\n" +
            "    name String,\n" +
            "    record_time Int64,\n" +
            "    record_date Date MATERIALIZED toDate(toDateTime(record_time / 1000, 'Asia/Shanghai')),\n" +
            "    volume Int64,\n" +
            "    open Float64,\n" +
            "    close Float64,\n" +
            "    high Float64,\n" +
            "    low Float64,\n" +
            "    chg Float64,\n" +
            "    percent Float64,\n" +
            "    turn_overrate Float64,\n" +
            "    amount Float64,\n" +
            "    volume_post Int64,\n" +
            "    amount_post Float64,\n" +
            "    pe Float64,\n" +
            "    pb Float64,\n" +
            "    ps Float64,\n" +
            "    pcf Float64,\n" +
            "    market_capital Int64,\n" +
            "    balance Float64,\n" +
            "    hold_volume_cn Float64,\n" +
            "    hold_ratio_cn Float64,\n" +
            "    net_volume_cn Float64,\n" +
            "    hold_volume_hk Float64,\n" +
            "    hold_ratio_hk Float64,\n" +
            "    net_volume_hk Float64\n" +
            ") ENGINE = ReplacingMergeTree()\n" +
            "PARTITION BY (code)\n" +
            "ORDER BY (code, record_time)";

    final private ChClient kLineStorage;
    final private HttpClient http;
    final private String xueqiuCookie;
    final private Long defaultBeginFromTs;

    public StockKLineCollector(PgClient stockCodeStorage, ChClient kLineStorage, HttpClient http) {
        super(STOCK_K_LINE, stockCodeStorage, StockKLine.class);
        this.http = http;
        this.kLineStorage = kLineStorage;

        Config kLineCfg = ConfigFactory.load("stock_k_line");
        this.defaultBeginFromTs = kLineCfg.getLong("default.begin.from.ts");

        Config xueqiuConfig = ConfigFactory.load("xueqiu");
        this.xueqiuCookie = xueqiuConfig.getString("cookie");
    }

    @Override
    protected List<DataSchema> getDataSchemaList() {
        return toSchemaList(StockKLine.class);
    }

    @Override
    public void init() {
        // Create ClickHouse table with ReplacingMergeTree engine
        // PARTITION BY (code, record_date), ORDER BY (code, record_time)
        kLineStorage.execute(CREATE_STOCK_K_LINE_CLICKHOUSE_SQL);
        log.info("ClickHouse stock_k_line table ensured.");
    }

    @SneakyThrows
    @Override
    protected boolean collect() {
        long nowTs = LocalDateTime.ofInstant(Instant.now(), ZoneId.of("Asia/Shanghai"))
                                  .withHour(0)
                                  .withMinute(0)
                                  .withSecond(0)
                                  .toEpochSecond(ZoneOffset.ofHours(8)) * 1000;
        // Collect stock k line data
        List<StockCode> stockCodeList = this.storage.queryList(
                "SELECT code, name, exchange FROM stock_code", StockCode.class);
        int totalCnt = stockCodeList.size();
        int alreadyCollected = 1;
        for (StockCode stockCode : stockCodeList) {
            try {
                Long earliestTs = getEarliestTime(stockCode);
                while (earliestTs != null && earliestTs > defaultBeginFromTs) {
                    earliestTs =
                            collectSingleData(stockCode.exchange(), stockCode.code(), stockCode.name(), earliestTs);
                    if (earliestTs == null) {
                        break;
                    }
                    earliestTs -= ONE_DAY_MS;
                }
                Long latestTs = getLatestTime(stockCode);
                if (nowTs - latestTs > ONE_DAY_MS) {
                    Long nextEndTs = nowTs;
                    while (nextEndTs > latestTs) {
                        nextEndTs =
                                collectSingleData(stockCode.exchange(), stockCode.code(), stockCode.name(), nextEndTs);
                        if (nextEndTs == null) {
                            break;
                        }
                        nextEndTs -= ONE_DAY_MS;
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to collect stock k line for: {}", stockCode.name(), e);
            }
            if (alreadyCollected % 100 == 0) {
                log.info("Collect stocks processing: {}/{}", alreadyCollected, totalCnt);
            }
            alreadyCollected++;
        }

        return true;
    }

    private Long getLatestTime(StockCode code) throws Exception {
        Long latestTs = kLineStorage.queryOne(
                "SELECT MAX(record_time) FROM stock_k_line WHERE code = '" + code.code() + "'");
        if (null == latestTs) {
            latestTs = defaultBeginFromTs;
        }

        return latestTs;
    }

    private Long getEarliestTime(StockCode stockCode) throws Exception {
        final String sql = "SELECT MIN(record_time) FROM stock_k_line WHERE code = '" + stockCode.code() + "'";
        return kLineStorage.queryOne(sql);
    }

    private Long collectSingleData(StockCode.Exchange exchange, String code, String name, Long beginFrom)
            throws Exception {
        Request request = new Request();
        request.url(K_LINE_URL);
        Map<String, String> parameters = new HashMap<>(K_LINE_URL_FIX_PARAMS);
        parameters.put("symbol", exchange.name() + code);
        parameters.put("begin", String.valueOf(beginFrom));
        request.parameters(parameters);
        request.setFullCookie(this.xueqiuCookie);

        List<StockKLine> kLineData = http.sync(request, stockKLineResp -> {
            if (stockKLineResp.errorCode() != 0) {
                return null;
            }

            KLineData data = stockKLineResp.data();
            if (null == data || null == data.item()) {
                return null;
            }
            return data.item().stream()
                       .map(item -> toStockKLine(code, name, item))
                       .collect(Collectors.toList());
        }, StockKLineResponse.class);

        if (null == kLineData) {
            log.warn("Failed to collect stock K line data for: {}", code);
            return null;
        }

        if (kLineData.isEmpty()) {
            return null;
        }

        kLineStorage.saveIgnoreDuplicated(kLineData, StockKLine.class);
        kLineData.sort(Comparator.comparingLong(StockKLine::recordTime));

        return kLineData.get(0).recordTime();
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class StockKLineResponse {
        @JsonProperty
        private KLineData data;

        @JsonProperty("error_code")
        private Integer errorCode;

        @JsonProperty("error_description")
        private String errorDescription;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class KLineData {
        @JsonProperty
        private String symbol;

        @JsonProperty
        private List<List<Double>> item;
    }

    private static StockKLine toStockKLine(String code, String name, List<Double> dataItem) {
        StockKLine stockKLine = new StockKLine();
        stockKLine.code(code);
        stockKLine.name(name);
        stockKLine.recordTime(getLongValue(dataItem.get(0)));
        stockKLine.volume(getLongValue(dataItem.get(1)));
        stockKLine.open(dataItem.get(2));
        stockKLine.high(dataItem.get(3));
        stockKLine.low(dataItem.get(4));
        stockKLine.close(dataItem.get(5));
        stockKLine.chg(dataItem.get(6));
        stockKLine.percent(dataItem.get(7));
        stockKLine.turnOverrate(dataItem.get(8));
        stockKLine.amount(dataItem.get(9));
        stockKLine.volumePost(getLongValue(dataItem.get(10)));
        stockKLine.amountPost(dataItem.get(11));
        stockKLine.pe(dataItem.get(12));
        stockKLine.pb(dataItem.get(13));
        stockKLine.ps(dataItem.get(14));
        stockKLine.pcf(dataItem.get(15));
        stockKLine.marketCapital(getLongValue(dataItem.get(16)));
        stockKLine.balance(dataItem.get(17));
        stockKLine.holdVolumeCn(dataItem.get(18));
        stockKLine.holdRatioCn(dataItem.get(19));
        stockKLine.netVolumeCn(dataItem.get(20));
        stockKLine.holdVolumeHk(dataItem.get(21));
        stockKLine.holdRatioHk(dataItem.get(22));
        stockKLine.netVolumeHk(dataItem.get(23));

        return stockKLine;
    }

    private static Long getLongValue(Double value) {
        return null == value ? null : value.longValue();
    }
}
