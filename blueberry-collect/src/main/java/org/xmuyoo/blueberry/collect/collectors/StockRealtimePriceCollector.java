package org.xmuyoo.blueberry.collect.collectors;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpStatus;
import org.xmuyoo.blueberry.collect.Configs;
import org.xmuyoo.blueberry.collect.TaskDefinition;
import org.xmuyoo.blueberry.collect.domains.*;
import org.xmuyoo.blueberry.collect.http.Request;
import org.xmuyoo.blueberry.collect.http.Requests;
import org.xmuyoo.blueberry.collect.storage.ValueType;
import org.xmuyoo.blueberry.collect.utils.Utils;

import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class StockRealtimePriceCollector extends BasicCollector {

    private static final String STOCK_REALTIME_PRICE = "stock_realtime_price";
    private static final Pattern STOCK_CONTENT_PATTERN = Pattern.compile(".*=\"(.*)\"");
    private static final String SQL_LOAD_STOCK_CODES =
            "SELECT code, name, exchange FROM stock_code";

    private ScheduledExecutorService executors;
    private List<StockCode> stockCodes;
    private List<Pair<LocalTime, LocalTime>> availableTimeRanges;
//    private Publisher<SeriesData> publisher;

    public StockRealtimePriceCollector(TaskDefinition taskDefinition) {
        super(taskDefinition);

        ThreadFactory factory =
                new ThreadFactoryBuilder().setNameFormat("stock-collector-%s").build();
        executors = Executors.newSingleThreadScheduledExecutor(factory);
        this.availableTimeRanges = taskDefinition().getTimeRangeList();

//        publisher = Publisher.getInstance(config.getString(TOPIC), SeriesData.class);
    }

    @Override
    public void start() {
        executors.scheduleAtFixedRate(this::loadStockObjects, 0, 10, TimeUnit.MINUTES);
    }

    @Override
    public void shutdown() {
        shutdownExecutorService(executors);
    }

    @Override
    protected boolean isAvailable() {
        boolean emptyStockCode = null == stockCodes || stockCodes.isEmpty();
        if (emptyStockCode)
            return false;

        LocalTime now = LocalTime.now();
        for (Pair<LocalTime, LocalTime> range : this.availableTimeRanges) {
            LocalTime start = range.getLeft();
            LocalTime end = range.getRight();
            if (start.compareTo(now) <= 0 && now.compareTo(end) <= 0)
                return true;
        }

        return false;
    }

    @Override
    protected boolean collect() {

        for (StockCode stockCode : this.stockCodes) {
            Request request = Requests.newStockCodeRequest(stockCode.code(), stockCode.exchange());
            httpClient.async(request, response -> {
                if (response.status() != HttpStatus.OK.value()) {
                    log.warn("Can not request stock realtime price for {}",
                             stockCode.toString());
                    return;
                }

                String content = new String(response.data());
                Matcher matcher = STOCK_CONTENT_PATTERN.matcher(content);
                if (!matcher.find()) {
                    log.error(String.format("Failed to parse stock realtime price data: %s",
                                            new String(response.data())));
                    return;
                }
                String rawContent = matcher.group(1);
                if (StringUtils.isBlank(rawContent)) {
                    log.warn("Empty response content: {}", stockCode.toString());
                    return;
                }

                StockRealtimePrice stockRealtimePrice =
                        StockRealtimePrice.of(stockCode, rawContent);


                // Persistent and publish StockRealtimePrice, using SeriesData
                List<SeriesData> seriesDataList;
                try {
                    seriesDataList = stockRealtimePrice.toSeriesData();
                } catch (Exception e) {
                    log.error(String.format(
                            "Failed to translate StockRealtimePrice to SeriesData. " +
                                    "StockRealtimePrice: %s", stockRealtimePrice.toString()),
                              e);
                    return;
                }

                dataWarehouse.saveSeriesData(STOCK_REALTIME_PRICE, seriesDataList);
//                publisher.publish(seriesDataList, (msgId, e) -> {
//                    if (null != e)
//                        log.error("Failed to send stock realtime price", e);
//                });
            });
        }

        return true;
    }

    @Override
    protected List<DataSchema> getDataSchemaList() {
        DataSchema recordTime = new DataSchema(STOCK_REALTIME_PRICE, "record_time",
                                               userId(), ValueType.Datetime, "股票实时数据的时间点");
        DataSchema value = new DataSchema(STOCK_REALTIME_PRICE, "value", userId(), ValueType.Number,
                                          "股票实时价格的数据值");
        DataSchema metric = new DataSchema(STOCK_REALTIME_PRICE, "metric", userId(), ValueType.Text,
                                           "股票实时价格数据名称");
        DataSchema tags = new DataSchema(STOCK_REALTIME_PRICE, "tags", userId(), ValueType.Json,
                                         "股票实时价格数据的维度");
        return Arrays.asList(recordTime, value, metric, tags);
    }

    private void loadStockObjects() {
        List<StockCode> stockCodes;
        try {
            stockCodes = dataWarehouse.queryList(SQL_LOAD_STOCK_CODES, StockCode.class);
        } catch (SQLException e) {
            log.error("Failed to load stock codes", e);
            return;
        }

        this.stockCodes = stockCodes;
    }
}
