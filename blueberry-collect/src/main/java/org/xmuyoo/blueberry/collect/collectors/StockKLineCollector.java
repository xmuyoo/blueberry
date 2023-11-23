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

    final private HttpClient http;
    final private String xueqiuCookie;
    final private Long defaultBeginFromTs;

    public StockKLineCollector(PgClient storage, HttpClient http) {
        super(STOCK_K_LINE, storage, StockKLine.class);
        this.http = http;

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
    protected boolean needCreateEntityTable() {
        return true;
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
        Long latestTs = storage.queryOne(
                "SELECT MAX(record_time) FROM stock_k_line WHERE code = '" + code.code() + "'");
        if (null == latestTs) {
            latestTs = defaultBeginFromTs;
        }

        return latestTs;
    }

    private Long getEarliestTime(StockCode stockCode) throws Exception {
        final String sql = "SELECT MIN(record_time) FROM stock_k_line WHERE code = '" + stockCode.code() + "'";
        return storage.queryOne(sql);
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

        storage.saveIgnoreDuplicated(kLineData, StockKLine.class);
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
