package org.xmuyoo.blueberry.collect.collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
import org.xmuyoo.blueberry.collect.collectors.data.source.RemoteDataSource;
import org.xmuyoo.blueberry.collect.collectors.data.source.RemoteDataSourceFactory;
import org.xmuyoo.blueberry.collect.domains.ConvertibleBondCode;
import org.xmuyoo.blueberry.collect.domains.ConvertibleBondHistory;
import org.xmuyoo.blueberry.collect.domains.DataSchema;
import org.xmuyoo.blueberry.collect.http.HttpClient;
import org.xmuyoo.blueberry.collect.http.Request;
import org.xmuyoo.blueberry.collect.storage.PgClient;

@Slf4j
public class ConvertibleBondHistoryCollector extends BasicCollector<ConvertibleBondHistory> {

    private static final String CONVERTIBLE_BOND_HISTORY = "convertible_bond_history";

    private static final String HISTORY_DATA_URL_FMT =
            "https://www.jisilu.cn/data/cbnew/detail_hist/%s?___jsl=LST___t=%s";
    private static final ZoneOffset TIME_ZONE_OFFSET = ZoneOffset.ofHours(8);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final HttpClient http;
    private final RemoteDataSource jisilu;
    private final int collectIntervalMs;

    public ConvertibleBondHistoryCollector(PgClient pgClient, HttpClient httpClient) {
        super(CONVERTIBLE_BOND_HISTORY, pgClient, ConvertibleBondHistory.class);
        this.http = httpClient;
        this.jisilu = RemoteDataSourceFactory.getDataSource(RemoteDataSourceFactory.DataSourceType.Jisilu);
        this.collectIntervalMs = jisilu.getCollectIntervalMilliseconds();
    }

    @Override
    protected boolean needCreateEntityTable() {
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
            Map<String, String> headers = new HashMap<>(jisilu.getRemoteHTTPRequestHeaders());
            headers.put("Cookie", jisilu.getRemoteHTTPRequestCookie());
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
            TimeUnit.MILLISECONDS.sleep(this.collectIntervalMs);
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
