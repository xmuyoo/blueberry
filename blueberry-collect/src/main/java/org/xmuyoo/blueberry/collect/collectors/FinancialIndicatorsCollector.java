package org.xmuyoo.blueberry.collect.collectors;

import com.google.common.collect.ImmutableMap;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.xmuyoo.blueberry.collect.domains.DataSchema;
import org.xmuyoo.blueberry.collect.domains.FinancialIndicator;
import org.xmuyoo.blueberry.collect.domains.StockCode;
import org.xmuyoo.blueberry.collect.http.HttpClient;
import org.xmuyoo.blueberry.collect.storage.PgClient;

public class FinancialIndicatorsCollector extends BasicCollector<FinancialIndicator> {

    private final HttpClient http;

    private final String indicatorUrl;
    private final Map<String, String> fixedIndicatorParameters;

    public FinancialIndicatorsCollector(PgClient storage, HttpClient httpClient) {
        super(FinancialIndicator.FINANCIAL_INDICATORS, storage, FinancialIndicator.class);
        this.http = httpClient;

        Config config = ConfigFactory.load(FinancialIndicator.FINANCIAL_INDICATORS);
        this.indicatorUrl = config.getString("indicator.url");

        this.fixedIndicatorParameters = ImmutableMap.of(
                "count", String.valueOf(config.getInt("page.count")),
                "is_detail", String.valueOf(config.getBoolean("is.detail")),
                "type", config.getString("type"),
                "timestamp", String.valueOf(System.currentTimeMillis()));
    }

    @Override
    protected List<DataSchema> getDataSchemaList() {
        return toSchemaList(FinancialIndicator.class);
    }

    @Override
    protected boolean needCreateEntityTable() {
        return true;
    }

    @SneakyThrows
    @Override
    protected boolean collect() {
        List<StockCode> stockCodeList = this.storage.queryList(
                "SELECT code, name, exchange FROM stock_code", StockCode.class);
        int totalCnt = stockCodeList.size();
        int alreadyCollected = 1;
        for (StockCode stockCode : stockCodeList) {
            String parameters = getParameters(stockCode);
            String stockUrl = this.indicatorUrl + "?" + parameters;
        }

        return true;
    }

    private String getParameters(StockCode stockCode) {
        Map<String, String> parameters = new HashMap<>(this.fixedIndicatorParameters);
        parameters.put("symbol", stockCode.exchange().name() + stockCode.code());

        return parameters.entrySet()
                         .stream()
                         .map(p -> p.getKey() + "=" + p.getValue())
                         .collect(Collectors.joining("&"));
    }
}
