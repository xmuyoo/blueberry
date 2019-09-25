package org.xmuyoo.blueberry.collect.collectors;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xmuyoo.blueberry.collect.TaskDefinition;
import org.xmuyoo.blueberry.collect.domains.DataSchema;
import org.xmuyoo.blueberry.collect.domains.StockCode;
import org.xmuyoo.blueberry.collect.storage.ValueType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class StockCodeCollector extends BasicCollector {

    private static final Pattern STOCK_HREF_PATTERN = Pattern.compile(".*/([a-z]+)([0-9]+).html");
    private static final Pattern STOCK_NAME_PATTERN = Pattern.compile("(.*)\\([0-9]+\\)");
    private static final int STANDARD_STOCK_CODE_LENGTH = 6;

    private String stockCodeListUrl;

    public StockCodeCollector(TaskDefinition taskDefinition) {
        super(taskDefinition);
        this.stockCodeListUrl = taskDefinition.sourceUrl();
    }

    @Override
    protected boolean isAvailable() {
        return true;
    }

    @Override
    protected boolean collect() {
        log.info("Collect stock codes of Chinese exchanges");

        List<StockCode> stockCodes = new ArrayList<>();
        try {
            Document document = Jsoup.connect(stockCodeListUrl).get();
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

                StockCode.Exchange exchange = StockCode.getExchange(matcher.group(1));
                String code = matcher.group(2);
                stockCode.exchange(exchange);
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

            dataWarehouse.saveIgnoreDuplicated(stockCodes, StockCode.class);
        } catch (Exception e) {
            log.error("Failed to load stock codes", e);
            return false;
        }

        return true;
    }

    @Override
    protected List<DataSchema> getDataSchemaList() {
        DataSchema stockCode =
                new DataSchema("stock_code", "code", userId(), ValueType.Text,
                               "中国上交所和深交所股票代码");
        DataSchema stockName =
                new DataSchema("stock_code", "name", userId(), ValueType.Text,
                               "中国上交所和深交所股票名称");
        DataSchema stockExchange =
                new DataSchema("stock_code", "exchange", userId(), ValueType.Text,
                               "中国股票所属交易所");
        return Arrays.asList(stockCode, stockName, stockExchange);
    }
}
