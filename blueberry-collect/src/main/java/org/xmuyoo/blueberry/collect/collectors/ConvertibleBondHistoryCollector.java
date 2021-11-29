package org.xmuyoo.blueberry.collect.collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.xmuyoo.blueberry.collect.domains.ConvertibleBondCode;
import org.xmuyoo.blueberry.collect.domains.ConvertibleBondHistory;
import org.xmuyoo.blueberry.collect.domains.DataSchema;
import org.xmuyoo.blueberry.collect.http.HttpClient;
import org.xmuyoo.blueberry.collect.http.Request;
import org.xmuyoo.blueberry.collect.storage.PgClient;

@Slf4j
public class ConvertibleBondHistoryCollector extends BasicCollector {

    private static final String CONVERTIBLE_BOND_HISTORY = "convertible_bond_history";

    private static final String HISTORY_DATA_URL_FMT =
            "https://www.jisilu.cn/data/cbnew/detail_hist/%s?___jsl=LST___t=%s";
    private static final ZoneOffset TIME_ZONE_OFFSET = ZoneOffset.ofHours(8);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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

    private final HttpClient http;
    private final String cookie;

    public ConvertibleBondHistoryCollector(PgClient pgClient, HttpClient httpClient) {
        super(CONVERTIBLE_BOND_HISTORY, pgClient);
        this.http = httpClient;

        Config jisiluCfg = ConfigFactory.load("jisilu");
        this.cookie = jisiluCfg.getString("cookie");
    }

    @Override
    protected boolean isAvailable() {
        return true;
    }

    @SneakyThrows
    @Override
    protected boolean collect() {
        List<ConvertibleBondCode> convertibleBondCodeList = this.storage.queryList(
                "SELECT code, name FROM convertible_bond_code", ConvertibleBondCode.class);
        int total = convertibleBondCodeList.size();
        int idx = 1;
        for (ConvertibleBondCode convertibleBondCode : convertibleBondCodeList) {
            log.info("Collect convertible bond history for [{}/{}] {}",
                    idx, total, convertibleBondCode.name());
            String url = String.format(HISTORY_DATA_URL_FMT, convertibleBondCode.code(), System.currentTimeMillis());
            Request request = new Request();
            request.url(url);
            request.method(HttpMethod.GET);
            Map<String, String> headers = new HashMap<>(JISILU_HEADERS);
            headers.put("Cookie", cookie);
            request.headers(headers);

            try {
                List<ConvertibleBondHistory> historyList = http.sync(request, resp -> {
                    if (resp.total() <= 0) {
                        return new ArrayList<>();
                    }

                    List<Row> rows = resp.rows();
                    if (null == rows) {
                        return new ArrayList<>();
                    }

                    return rows.stream()
                               .map(row -> {
                                   Cell cell = row.cell();
                                   ConvertibleBondHistory history = new ConvertibleBondHistory();
                                   history.code(convertibleBondCode.code());
                                   LocalDate localDate = LocalDate.parse(cell.lastChgDt(), DATE_FORMATTER);
                                   long recordTime = localDate.toEpochSecond(LocalTime.of(0, 0), TIME_ZONE_OFFSET);
                                   history.recordTime(recordTime);
                                   history.ytmRt(getDoubleValue(cell.ytmRt()));
                                   history.premiumRt(getDoubleValue(cell.premiumRt()));
                                   history.convertValue(getDoubleValue(cell.convertValue()));
                                   history.price(getDoubleValue(cell.price()));
                                   history.volume(getDoubleValue(cell.volume()));
                                   history.stockVolume(getLongValue(cell.stockVolume()));
                                   history.currIssAmt(getDoubleValue(cell.currIssAmt()));
                                   history.turnoverRt(getDoubleValue(cell.turnoverRt()));

                                   return history;
                               })
                               .collect(Collectors.toList());
                }, ConvertibleBondResponse.class);
                this.storage.saveIgnoreDuplicated(historyList, ConvertibleBondHistory.class);
            } catch (Exception e) {
                log.warn("Failed to collect convertible bond history for {}", convertibleBondCode.name(), e);
            }
            TimeUnit.SECONDS.sleep(1);
            idx++;
        }

        return true;
    }

    private static Long getLongValue(Object value) {
        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof String) {
            String val = (String) value;
            if (val.equals("-")) {
                return null;
            } else {
                return Long.parseLong(val);
            }
        } else {
            return null;
        }
    }

    private static Double getDoubleValue(String value) {
        if (value.equals("-")) {
            return null;
        } else if (value.contains("%")) {
            return getDoubleFromPercentStr(value);
        } else {
            return Double.parseDouble(value);
        }
    }

    private static Double getDoubleFromPercentStr(String valueWithPercent) {
        return Double.parseDouble(valueWithPercent.substring(0, valueWithPercent.indexOf("%")));
    }

    @Override
    protected List<DataSchema> getDataSchemaList() {
        return toSchemaList(ConvertibleBondHistory.class);
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ConvertibleBondResponse {
        @JsonProperty
        private int page;
        @JsonProperty
        private List<Row> rows;
        @JsonProperty
        private int total;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Row {
        @JsonProperty
        private String id;

        @JsonProperty
        private Cell cell;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Cell {
        @JsonProperty("bond_id")
        private String bondId;
        @JsonProperty("last_chg_dt")
        private String lastChgDt;
        @JsonProperty("ytm_rt")
        private String ytmRt;
        @JsonProperty("premium_rt")
        private String premiumRt;
        @JsonProperty("convert_value")
        private String convertValue;
        @JsonProperty
        private String price;
        @JsonProperty
        private String volume;
        @JsonProperty("stock_volume")
        private Object stockVolume;
        @JsonProperty("curr_iss_amt")
        private String currIssAmt;
        @JsonProperty("turnover_rt")
        private String turnoverRt;
    }
}
