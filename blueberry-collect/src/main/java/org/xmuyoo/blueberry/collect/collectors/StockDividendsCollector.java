package org.xmuyoo.blueberry.collect.collectors;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xmuyoo.blueberry.collect.domains.DataSchema;
import org.xmuyoo.blueberry.collect.domains.StockCode;
import org.xmuyoo.blueberry.collect.domains.StockDividends;
import org.xmuyoo.blueberry.collect.http.HttpClient;
import org.xmuyoo.blueberry.collect.http.Request;
import org.xmuyoo.blueberry.collect.storage.ChClient;
import org.xmuyoo.blueberry.collect.storage.PgClient;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
@Slf4j
public class StockDividendsCollector extends BasicCollector<StockDividends> {

    private static final String STOCK_DIVIDENDS = "stock_dividends";
    private static final String DIVIDENDS_URL_TEMPLATE =
            "https://data.10jqka.com.cn/ajax/sgpx/op/code/code/%s/ajax/1/free/1/";

    // Column indexes in the returned HTML table (0-based)
    private static final int COL_CODE  = 1;   // 股票代码
    private static final int COL_NAME  = 2;   // 股票简称
    private static final int COL_PRICE = 4;   // 预计除权除息价
    private static final int COL_DATE  = 12;  // 除权除息日

    private static final String CREATE_STOCK_DIVIDENDS_CLICKHOUSE_SQL =
            "CREATE TABLE IF NOT EXISTS stock_dividends (\n" +
            "    code String,\n" +
            "    name String,\n" +
            "    price Float64,\n" +
            "    record_date Date\n" +
            ") ENGINE = ReplacingMergeTree()\n" +
            "PARTITION BY (code)\n" +
            "ORDER BY (code, record_date)";

    final private ChClient dividendStorage;
    final private HttpClient http;
    final private String cookieTonghuashun;

    public StockDividendsCollector(PgClient stockCodeStorage, ChClient dividendStorage,
                                   HttpClient http) {
        super(STOCK_DIVIDENDS, stockCodeStorage, StockDividends.class);
        this.http = http;
        this.dividendStorage = dividendStorage;

        Config cfg10jqka = ConfigFactory.load("tonghuashun");
        this.cookieTonghuashun = cfg10jqka.getString("cookie");
    }

    @Override
    protected List<DataSchema> getDataSchemaList() {
        return toSchemaList(StockDividends.class);
    }

    @Override
    public void init() {
        dividendStorage.execute(CREATE_STOCK_DIVIDENDS_CLICKHOUSE_SQL);
        log.info("ClickHouse stock_dividends table ensured.");
    }

    @Override
    protected boolean collect() {
        List<StockCode> stockCodeList = null;
        try {
            stockCodeList = this.storage.queryList(
                    "SELECT code, name FROM stock_code", StockCode.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        int totalCnt = stockCodeList.size();
        int alreadyCollected = 0;

        for (StockCode stockCode : stockCodeList) {
            try {
                collectSingleData(stockCode.code(), stockCode.name());
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {
                log.warn("Failed to collect dividends for: {}", stockCode.name(), e);
            }

//            alreadyCollected++;
//            if (alreadyCollected % 10 == 0) {
//                try {
//                    TimeUnit.MILLISECONDS.sleep(100);
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                    break;
//                }
//            }
//            if (alreadyCollected % 100 == 0) {
//                log.info("Collect dividends processing: {}/{}", alreadyCollected, totalCnt);
//            }
        }

        log.info("Dividends collection finished. Total: {}", alreadyCollected);
        return true;
    }

    private void collectSingleData(String code, String name) throws Exception {
        String url = String.format(DIVIDENDS_URL_TEMPLATE, code);
        Request request = new Request();
        request.url(url);
        request.setFullCookie(this.cookieTonghuashun);

        String html = http.sync(request, response -> {
            if (!response.isSuccessful()) {
                log.warn("Failed to fetch dividends for {}: HTTP {}", code, response.code());
                return null;
            }
            try {
                return response.body() != null ? response.body().string() : null;
            } catch (Exception e) {
                log.warn("Failed to read response body for {}", code, e);
                return null;
            }
        });

        if (html == null || html.isEmpty()) {
            return;
        }

        List<StockDividends> dividendsList = parseHtml(code, name, html);
        if (dividendsList.isEmpty()) {
            return;
        }

        dividendStorage.saveIgnoreDuplicated(dividendsList, StockDividends.class);
    }

    List<StockDividends> parseHtml(String code, String name, String html) {
        List<StockDividends> result = new ArrayList<>();
        Document doc = Jsoup.parse(html);

        // Only extract data rows from <tbody>, skip the header row in <thead>
        Elements rows = doc.select("table tbody tr");
        for (Element row : rows) {
            Elements cells = row.select("td");
            if (cells.size() <= COL_DATE) {
                continue; // insufficient columns
            }

            String rowCode = cells.get(COL_CODE).text().trim();
            String rowName = cells.get(COL_NAME).text().trim();
            String priceText = cells.get(COL_PRICE).text().trim();
            String dateText = cells.get(COL_DATE).text().trim();

            // Skip rows with no ex-dividend date
            if ("--".equals(dateText) || dateText.isEmpty()) {
                continue;
            }

            // Skip rows with no valid ex-dividend price ("-" means unassigned)
            if ("-".equals(priceText) || "--".equals(priceText) || priceText.isEmpty()) {
                continue;
            }

            StockDividends dividends = new StockDividends();
            dividends.code(rowCode);
            dividends.name(rowName);
            dividends.recordDate(dateText);

            try {
                dividends.price(Double.parseDouble(priceText));
            } catch (NumberFormatException e) {
                log.debug("Failed to parse price '{}' for {}: {}", priceText, code, e.getMessage());
                continue;
            }

            result.add(dividends);
        }

        return result;
    }
}
