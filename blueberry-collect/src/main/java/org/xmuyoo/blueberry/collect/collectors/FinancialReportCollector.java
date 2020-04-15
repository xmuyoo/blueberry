package org.xmuyoo.blueberry.collect.collectors;

import com.google.common.base.Splitter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpStatus;
import org.xmuyoo.blueberry.collect.TaskDefinition;
import org.xmuyoo.blueberry.collect.domains.DataSchema;
import org.xmuyoo.blueberry.collect.domains.FinancialReport;
import org.xmuyoo.blueberry.collect.domains.SeriesData;
import org.xmuyoo.blueberry.collect.domains.StockCode;
import org.xmuyoo.blueberry.collect.http.Request;
import org.xmuyoo.blueberry.collect.http.Requests;
import org.xmuyoo.blueberry.collect.storage.PersistentProperty;
import org.xmuyoo.blueberry.collect.storage.ValueType;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class FinancialReportCollector extends BasicCollector {

    private static final String FINANCIAL_REPORT = "financial_report";
    private static final Splitter LINE_SPLITTER = Splitter.on("\n")
                                                          .trimResults()
                                                          .omitEmptyStrings();
    private static final Splitter COMMA_SPLITTER = Splitter.on(",")
                                                           .trimResults()
                                                           .omitEmptyStrings();

    private static final String LAST_RECORDS_SQL = String.format(
            "SELECT last(record_time, record_time) AS \"record_time\", stock_code AS \"stockCode\" " +
                    "FROM %s GROUP BY \"stockCode\"",
            FINANCIAL_REPORT
    );
    private static final String EMPTY_VALUE = "--";
    private static final ZoneId ASIA_SHANGHAI = ZoneId.of("Asia/Shanghai");

    public FinancialReportCollector(TaskDefinition taskDefinition) {
        super(taskDefinition);
    }

    @Override
    protected boolean isAvailable() {
        return true;
    }

    @Override
    protected boolean collect() {
        try {
            List<StockCode> stockCodeList = dataWarehouse.queryList(
                    "SELECT code, name, exchange FROM stock_code",
                    StockCode.class);
            if (stockCodeList.isEmpty()) {
                log.warn("StockCode list is empty. Skip collecting financial reports.");

                return true;
            }

            List<ReportRecord> latestReports =
                    dataWarehouse.queryList(LAST_RECORDS_SQL, ReportRecord.class);
            Map<String, Long> latestReportsTime = new HashMap<>();
            latestReports.forEach(
                    r -> latestReportsTime.put(r.stockCode(), r.recordTime().getTime()));

            List<StockCode> targetCodes = stockCodeList
                    .stream()
                    .filter(s -> !s.code().startsWith("5"))
                    .collect(Collectors.toList());
            log.info("Start to collect financial reports for {} stocks", targetCodes.size());
            for (StockCode stockCode : targetCodes) {
                TimeUnit.MICROSECONDS.sleep(100);

                Request request = Requests.newFinancialReportRequest(stockCode.code());
                httpClient.async(request, (req, response) -> {
                    if (response.status() != HttpStatus.OK.value()) {
                        log.warn("Failed to get financial report for {}, [{}]",
                                req.fullUrl(), stockCode);

                        return;
                    }

                    Map<Long, Map<String, Double>> reportIndicators = parseReport(response.data());
                    if (reportIndicators.isEmpty())
                        return;

                    Long lastReportRecordTime =
                            latestReportsTime.getOrDefault(stockCode.code(), 0L);
                    for (Map.Entry<Long, Map<String, Double>> entry : reportIndicators.entrySet()) {
                        Long time = entry.getKey();
                        if (time <= lastReportRecordTime)
                            continue;

                        Map<String, Double> indicators = entry.getValue();
                        FinancialReport report = FinancialReport.of(
                                LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ASIA_SHANGHAI),
                                stockCode,
                                indicators);

                        List<SeriesData> seriesData;
                        try {
                            seriesData = report.toSeriesData();
                        } catch (IllegalAccessException e) {
                            log.error("Failed to transform financial report to series data list",
                                    e);
                            continue;
                        }
                        dataWarehouse.saveSeriesData(FINANCIAL_REPORT, seriesData);
                    }
                });
            }

            log.info("Complete collecting financial reports");
            return true;
        } catch (Exception e) {
            log.error("", e);
            return false;
        }
    }

    @Override
    protected List<DataSchema> getDataSchemaList() {
        DataSchema dateTime = new DataSchema(FINANCIAL_REPORT, "record_time", ValueType.Datetime,
                "报告日期", taskId());
        DataSchema value = new DataSchema(FINANCIAL_REPORT, "value", ValueType.Number,
                "指标值", taskId());
        DataSchema metric = new DataSchema(FINANCIAL_REPORT, "metric", ValueType.Text,
                "指标名称", taskId());
        DataSchema stockCode = new DataSchema(FINANCIAL_REPORT, "stock_code", ValueType.Text,
                "公司股票代码", taskId());
        DataSchema stockName = new DataSchema(FINANCIAL_REPORT, "stock_name", ValueType.Text,
                "公司股票名称", taskId());
        DataSchema description = new DataSchema(FINANCIAL_REPORT, "description", ValueType.Text,
                "指标描述", taskId());

        return Arrays.asList(dateTime, value, metric, stockCode, stockName, description);
    }

    private Map<Long, Map<String, Double>> parseReport(byte[] data) {
        Map<Long, Map<String, Double>> parsedContent = new HashMap<>();

        String content;
        try {
            content = new String(
                    new String(data, "GBK").getBytes(StandardCharsets.UTF_8));
        } catch (UnsupportedEncodingException e) {
            log.error("Unsupported encoding content", e);

            return parsedContent;
        }

        List<String> lines = LINE_SPLITTER.splitToList(content);
        if (lines.size() < FinancialReport.INDICATOR_SCHEMA_NAME_MAPPING.size())
            return parsedContent;

        String firstLine = lines.get(0).trim();
        Map<Integer, Long> datetimeIndexMapping = new HashMap<>();
        List<String> datetimeOptList = COMMA_SPLITTER.splitToList(firstLine);

        int dateIdx = 1;
        for (String dateString : datetimeOptList.subList(1, datetimeOptList.size())) {
            Long datetime = TimeUnit.DAYS.toMillis(LocalDate.parse(dateString).toEpochDay());
            datetimeIndexMapping.put(dateIdx, datetime);
            parsedContent.put(datetime, new HashMap<>());

            dateIdx++;
        }

        for (String line : lines.subList(1, lines.size())) {
            List<String> items = COMMA_SPLITTER.splitToList(line);
            String indicatorOption = items.get(0);
            if (!FinancialReport.INDICATOR_SCHEMA_NAME_MAPPING.containsKey(indicatorOption)) {
                log.warn("Unknown indicator option: {}", indicatorOption);
                continue;
            }

            Pair<String, String> indicatorInfo =
                    FinancialReport.INDICATOR_SCHEMA_NAME_MAPPING.get(indicatorOption);
            // Loop datetime values
            for (int i = 1; i < items.size(); i++) {
                Long datetime = datetimeIndexMapping.get(i);
                Map<String, Double> indicators = parsedContent.get(datetime);
                if (EMPTY_VALUE.equals(items.get(i)))
                    continue;

                indicators.put(indicatorInfo.getKey(), Double.parseDouble(items.get(i)));
            }
        }

        return parsedContent;
    }

    @NoArgsConstructor
    @Getter
    @Setter
    public static class ReportRecord {
        @PersistentProperty(name = "record_time", valueType = ValueType.Datetime)
        private Timestamp recordTime;

        @PersistentProperty(name = "stock_code", valueType = ValueType.Text)
        private String stockCode;
    }
}