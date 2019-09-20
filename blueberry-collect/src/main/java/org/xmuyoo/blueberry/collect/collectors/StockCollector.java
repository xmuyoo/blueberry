package org.xmuyoo.blueberry.collect.collectors;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.HttpStatus;
import org.xmuyoo.blueberry.collect.Configs;
import org.xmuyoo.blueberry.collect.domains.SeriesData;
import org.xmuyoo.blueberry.collect.domains.StockCode;
import org.xmuyoo.blueberry.collect.domains.StockRealtimePrice;
import org.xmuyoo.blueberry.collect.http.Request;
import org.xmuyoo.blueberry.collect.http.Requests;
import org.xmuyoo.blueberry.collect.streams.Publisher;
import org.xmuyoo.blueberry.collect.utils.Utils;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class StockCollector extends SimpleBatchCollector {

    private static final String PERIOD_MINUTES = "period.minutes";
    private static final String TOPIC = "topic";

    private static final String STOCK_LIST_URL = "http://quote.eastmoney.com/stock_list.html";
    private static final Pattern STOCK_HREF_PATTERN = Pattern.compile(".*/([a-z]+)([0-9]+).html");
    private static final Pattern STOCK_NAME_PATTERN = Pattern.compile("(.*)\\([0-9]+\\)");
    private static final int STANDARD_STOCK_CODE_LENGTH = 6;
    private static final Pattern STOCK_CONTENT_PATTERN = Pattern.compile(".*=\"(.*)\"");

    private static final String DATABASE_TABLE_NAME = "stock_realtime_price";

    private ScheduledExecutorService executors;
    private List<StockCode> stockCodes;
    private int collectorPeriodMinutes;
    private List<Pair<LocalTime, LocalTime>> availableTimeRanges;
    private Publisher<SeriesData> publisher;

    public StockCollector() {
        super();

        ThreadFactory factory =
                new ThreadFactoryBuilder().setNameFormat("stock-collector-%s").build();
        executors = Executors.newSingleThreadScheduledExecutor(factory);
        executors.scheduleAtFixedRate(this::loadStockObjects, 1, 1, TimeUnit.DAYS);

        Config config = Configs.config("stock");
        this.collectorPeriodMinutes = config.getInt(PERIOD_MINUTES);
        this.availableTimeRanges = new ArrayList<>();

        List<String> timeRangeList =
                Configs.COMMA_SPLITTER.splitToList(config.getString("available.time.range"));
        for (String range : timeRangeList) {
            List<String> timePoints = Configs.LINE_SPLITTER.splitToList(range);
            LocalTime start = Utils.toLocalTime(timePoints.get(0));
            LocalTime end = Utils.toLocalTime(timePoints.get(1));

            this.availableTimeRanges.add(Pair.of(start, end));
        }

        publisher = Publisher.getInstance(config.getString(TOPIC), SeriesData.class);
    }

    @Override
    public void run() {
        while (isRunning) {
            if (null == stockCodes || stockCodes.isEmpty()) {
                waitMinutes(collectorPeriodMinutes);
            }

            if (!isAvailable())
                waitMinutes(collectorPeriodMinutes);

            for (StockCode stockCode : this.stockCodes) {
                Request request = Requests.newStockCodeRequest(stockCode.code(), stockCode.type());
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

                    timescaleDBClient.saveSeriesData(DATABASE_TABLE_NAME, seriesDataList);
                    publisher.publish(seriesDataList, (msgId, e) -> {
                        if (null != e)
                            log.error("Failed to send stock realtime price", e);
                    });
                });
            }

            waitMinutes(collectorPeriodMinutes);
        }
    }

    @Override
    public void start() {
        loadStockObjects();
    }

    @Override
    public void shutdown() {
        shutdownExecutorService(executors);
    }

    private void loadStockObjects() {
        List<StockCode> stockCodes = new ArrayList<>();
        try {
            Document document = Jsoup.connect(STOCK_LIST_URL).get();
            Elements elements = document.select("div[id=quotesearch]").select("a[target]");
            for (Element element : elements) {
                String href = element.attr("href");
                String text = element.text();

                StockCode stockCode = new StockCode();
                Matcher matcher = STOCK_HREF_PATTERN.matcher(href);
                if (!matcher.find()) {
                    log.warn("Can not find stock code for {}", element.toString());
                    continue;
                }

                StockCode.Type type = StockCode.getType(matcher.group(1));
                String code = matcher.group(2);
                stockCode.type(type);
                if (code.length() > STANDARD_STOCK_CODE_LENGTH)
                    code = code.substring(1);
                stockCode.code(code);

                Matcher nameMatcher = STOCK_NAME_PATTERN.matcher(text);
                if (!nameMatcher.find())
                    stockCode.name(text);
                else
                    stockCode.name(nameMatcher.group(1));

                stockCodes.add(stockCode);
            }
        } catch (Exception e) {
            log.error("Failed to load stock codes", e);
            return;
        }

        timescaleDBClient.saveIgnoreDuplicated(stockCodes, StockCode.class);

        this.stockCodes = stockCodes;
    }

    private boolean isAvailable() {
        LocalTime now = LocalTime.now();
        for (Pair<LocalTime, LocalTime> range : this.availableTimeRanges) {
            LocalTime start = range.getLeft();
            LocalTime end = range.getRight();
            if (start.compareTo(now) <= 0 && now.compareTo(end) <= 0)
                return true;
        }

        return false;
    }
}
