package org.xmuyoo.blueberry.collect.collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.xmuyoo.blueberry.collect.domains.DataSchema;
import org.xmuyoo.blueberry.collect.domains.FinancialIndicator;
import org.xmuyoo.blueberry.collect.domains.StockCode;
import org.xmuyoo.blueberry.collect.http.HttpClient;
import org.xmuyoo.blueberry.collect.http.Request;
import org.xmuyoo.blueberry.collect.storage.PgClient;

@Slf4j
public class FinancialIndicatorsCollector extends BasicCollector<FinancialIndicator> {

    private final HttpClient http;

    private final String incomeIndicatorUrl;
    private final Map<String, String> fixedIndicatorParameters;
    private final ExecutorService executorService;
    private String cookie;

    public FinancialIndicatorsCollector(PgClient storage, HttpClient httpClient) {
        super(FinancialIndicator.FINANCIAL_INDICATORS, storage, FinancialIndicator.class);
        this.http = httpClient;

        Config config = ConfigFactory.load(FinancialIndicator.FINANCIAL_INDICATORS);
        this.incomeIndicatorUrl = config.getString("url");
        Config xueqiuCfg = ConfigFactory.load("xueqiu");
        this.cookie = xueqiuCfg.getString("cookie");

        this.fixedIndicatorParameters = ImmutableMap.of(
                "count", String.valueOf(config.getInt("page.count")),
                "is_detail", String.valueOf(config.getBoolean("is.detail")),
                "type", config.getString("type"),
                "timestamp", String.valueOf(System.currentTimeMillis()));

        this.executorService = Executors.newCachedThreadPool();

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
        for (StockCode stockCode : stockCodeList) {
            log.info("Collect financial indicators for {} {}", stockCode.code(), stockCode.name());
            String parameters = getParameters(stockCode);
            String stockUrl = this.incomeIndicatorUrl + "?" + parameters;
            Request request = new Request();
            request.url(stockUrl);
            request.method(HttpMethod.GET);
            request.setFullCookie(this.cookie);

            List<FinancialIndicator> financialIndicatorList =
                    http.sync(request, fResp -> {
                        List<FinancialIndicator> indicators = new ArrayList<>();
                        if (fResp.data == null) {
                            log.error("Failed to collect financial indicators for {} {}, error: {}, {}",
                                    stockCode.code(), stockCode.name(), fResp.errorCode(), fResp.errorMessage());
                            return indicators;
                        }

                        IndicatorData indicatorData = fResp.data();
                        if (indicatorData.reportList() == null || indicatorData.reportList().isEmpty()) {
                            log.error("Financial indicators of {} {} is empty.", stockCode.code(), stockCode.name());
                            return indicators;
                        }

                        for (IndicatorReport report : indicatorData.reportList()) {
                            FinancialIndicator fi = new FinancialIndicator();
                            fi.code(stockCode.code());
                            fi.reportName(report.reportName());
                            fi.reportDate(report.reportDate());
                            fi.avgRoe(report.avgRoe()[0]);
                            fi.npPerShare(report.npPerShare()[0]);
                            fi.operateCashFlowPs(report.operateCashFlowPs()[0]);
                            fi.basicEps(report.basicEps()[0]);
                            fi.capitalReserve(report.capitalReserve()[0]);
                            fi.undistriProfitPs(report.undistriProfitPs()[0]);
                            fi.netInterestOfTotalAssets(report.netInterestOfTotalAssets()[0]);
                            fi.netSellingRate(report.netSellingRate()[0]);
                            fi.grossSellingRate(report.grossSellingRate()[0]);
                            fi.totalRevenue(report.totalRevenue()[0]);
                            fi.operatingIncomeYoy(report.operatingIncomeYoy()[0]);
                            fi.netProfitAtsopc(report.netProfitAtsopc()[0]);
                            fi.netProfitAtsopcYoy(report.netProfitAtsopcYoy()[0]);
                            fi.netProfitAfterNrgalAtsolc(report.netProfitAfterNrgalAtsolc()[0]);
                            fi.npAtsopcNrgalYoy(report.npAtsopcNrgalYoy()[0]);
                            fi.oreDlt(report.oreDlt()[0]);
                            fi.rop(report.rop()[0]);
                            fi.assetLiabRatio(report.assetLiabRatio()[0]);
                            fi.currentRatio(report.currentRatio()[0]);
                            fi.quickRatio(report.quickRatio()[0]);
                            fi.equityMultiplier(report.equityMultiplier()[0]);
                            fi.equityRatio(report.equityRatio()[0]);
                            fi.holderEquity(report.holderEquity()[0]);
                            fi.ncfFromOaToTotalLiab(report.ncfFromOaToTotalLiab()[0]);
                            fi.inventoryTurnoverDays(report.inventoryTurnoverDays()[0]);
                            fi.receivableTurnoverDays(report.receivableTurnoverDays()[0]);
                            fi.accountsPayableTurnoverDays(report.accountsPayableTurnoverDays()[0]);
                            fi.cashCycle(report.cashCycle()[0]);
                            fi.operatingCycle(report.operatingCycle()[0]);
                            fi.totalCapitalTurnover(report.totalCapitalTurnover()[0]);
                            fi.inventoryTurnover(report.inventoryTurnover()[0]);
                            fi.accountReceivableTurnover(report.accountReceivableTurnover()[0]);
                            fi.accountsPayableTurnover(report.accountsPayableTurnover()[0]);
                            fi.currentAssetTurnoverRate(report.currentAssetTurnoverRate()[0]);
                            fi.fixedAssetTurnoverRatio(report.fixedAssetTurnoverRatio()[0]);

                            indicators.add(fi);
                        }

                        return indicators;
                    }, FinancialIndicatorResponse.class);

            executorService.submit(
                    () -> storage.saveIgnoreDuplicated(financialIndicatorList, FinancialIndicator.class));

            TimeUnit.MILLISECONDS.sleep(100);
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

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FinancialIndicatorResponse {

        @JsonProperty
        private IndicatorData data;

        @JsonProperty("error_code")
        private int errorCode;

        @JsonProperty("error_message")
        private String errorMessage;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IndicatorData {
        @JsonProperty("list")
        private List<IndicatorReport> reportList;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IndicatorReport {

        @JsonProperty("report_date")
        private Long reportDate;

        @JsonProperty("report_name")
        private String reportName;

        @JsonProperty("avg_roe")
        private Double[] avgRoe;

        @JsonProperty("np_per_share")
        private Double[] npPerShare;
        @JsonProperty("operate_cash_flow_ps")
        private Double[] operateCashFlowPs;
        @JsonProperty("basic_eps")
        private Double[] basicEps;
        @JsonProperty("capital_reserve")
        private Double[] capitalReserve;
        @JsonProperty("undistri_profit_ps")
        private Double[] undistriProfitPs;
        @JsonProperty("net_interest_of_total_assets")
        private Double[] netInterestOfTotalAssets;
        @JsonProperty("net_selling_rate")
        private Double[] netSellingRate;
        @JsonProperty("gross_selling_rate")
        private Double[] grossSellingRate;
        @JsonProperty("total_revenue")
        private Double[] totalRevenue;
        @JsonProperty("operating_income_yoy")
        private Double[] operatingIncomeYoy;
        @JsonProperty("net_profit_atsopc")
        private Double[] netProfitAtsopc;
        @JsonProperty("net_profit_atsopc_yoy")
        private Double[] netProfitAtsopcYoy;
        @JsonProperty("net_profit_after_nrgal_atsolc")
        private Double[] netProfitAfterNrgalAtsolc;
        @JsonProperty("np_atsopc_nrgal_yoy")
        private Double[] npAtsopcNrgalYoy;
        @JsonProperty("ore_dlt")
        private Double[] oreDlt;
        @JsonProperty("rop")
        private Double[] rop;
        @JsonProperty("asset_liab_ratio")
        private Double[] assetLiabRatio;
        @JsonProperty("current_ratio")
        private Double[] currentRatio;
        @JsonProperty("quick_ratio")
        private Double[] quickRatio;
        @JsonProperty("equity_multiplier")
        private Double[] equityMultiplier;
        @JsonProperty("equity_ratio")
        private Double[] equityRatio;
        @JsonProperty("holder_equity")
        private Double[] holderEquity;
        @JsonProperty("ncf_from_oa_to_total_liab")
        private Double[] ncfFromOaToTotalLiab;
        @JsonProperty("inventory_turnover_days")
        private Double[] inventoryTurnoverDays;
        @JsonProperty("receivable_turnover_days")
        private Double[] receivableTurnoverDays;
        @JsonProperty("accounts_payable_turnover_days")
        private Double[] accountsPayableTurnoverDays;
        @JsonProperty("cash_cycle")
        private Double[] cashCycle;
        @JsonProperty("operating_cycle")
        private Double[] operatingCycle;
        @JsonProperty("total_capital_turnover")
        private Double[] totalCapitalTurnover;
        @JsonProperty("inventory_turnover")
        private Double[] inventoryTurnover;
        @JsonProperty("account_receivable_turnover")
        private Double[] accountReceivableTurnover;
        @JsonProperty("accounts_payable_turnover")
        private Double[] accountsPayableTurnover;
        @JsonProperty("current_asset_turnover_rate")
        private Double[] currentAssetTurnoverRate;
        @JsonProperty("fixed_asset_turnover_ratio")
        private Double[] fixedAssetTurnoverRatio;
    }
}
