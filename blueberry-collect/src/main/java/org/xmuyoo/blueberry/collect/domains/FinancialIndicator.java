package org.xmuyoo.blueberry.collect.domains;

import lombok.Getter;
import lombok.Setter;
import org.xmuyoo.blueberry.collect.storage.Persistent;
import org.xmuyoo.blueberry.collect.storage.PersistentProperty;
import org.xmuyoo.blueberry.collect.storage.UniqueConstraint;

import static org.xmuyoo.blueberry.collect.storage.ValueType.BigInt;
import static org.xmuyoo.blueberry.collect.storage.ValueType.Double;
import static org.xmuyoo.blueberry.collect.storage.ValueType.Text;

@Getter
@Setter
@Persistent(name = "financial_indicators",
        uniqueConstraints = {@UniqueConstraint(value = {"code", "report_date"})})
public class FinancialIndicator {

    public static final String FINANCIAL_INDICATORS = "financial_indicators";

    @PersistentProperty(name = "code", valueType = Text, description = "股票代码")
    private String code;

    @PersistentProperty(name = "report_date", valueType = BigInt, description = "财报日期时间戳")
    private Long reportDate;

    @PersistentProperty(name = "report_name", valueType = Text, description = "财报名称")
    private String reportName;

    @PersistentProperty(name = "avg_roe", valueType = Double, description = "净资产收益率")
    private Double avgRoe;

    @PersistentProperty(name = "np_per_share", valueType = Double, description = "每股净资产")
    private Double npPerShare;

    @PersistentProperty(name = "operate_cash_flow_ps", valueType = Double, description = "每股经营现金流")
    private Double operateCashFlowPs;

    @PersistentProperty(name = "basic_eps", valueType = Double, description = "每股收益")
    private Double basicEps;

    @PersistentProperty(name = "capital_reserve", valueType = Double, description = "每股资本公积金")
    private Double capitalReserve;

    @PersistentProperty(name = "undistri_profit_ps", valueType = Double, description = "每股未分配利润")
    private Double undistriProfitPs;

    @PersistentProperty(name = "net_interest_of_total_assets", valueType = Double, description = "总资产报酬率")
    private Double netInterestOfTotalAssets;

    @PersistentProperty(name = "net_selling_rate", valueType = Double, description = "销售净利率")
    private Double netSellingRate;

    @PersistentProperty(name = "gross_selling_rate", valueType = Double, description = "销售毛利率")
    private Double grossSellingRate;

    @PersistentProperty(name = "total_revenue", valueType = Double, description = "营业收入（元）")
    private Double totalRevenue;

    @PersistentProperty(name = "operating_income_yoy", valueType = Double, description = "营业收入同比增长")
    private Double operatingIncomeYoy;

    @PersistentProperty(name = "net_profit_atsopc", valueType = Double, description = "净利润（元）")
    private Double netProfitAtsopc;

    @PersistentProperty(name = "net_profit_atsopc_yoy", valueType = Double, description = "净利润同比增长")
    private Double netProfitAtsopcYoy;

    @PersistentProperty(name = "net_profit_after_nrgal_atsolc", valueType = Double, description = "扣非净利润")
    private Double netProfitAfterNrgalAtsolc;

    @PersistentProperty(name = "np_atsopc_nrgal_yoy", valueType = Double, description = "扣非净利润同比增长")
    private Double npAtsopcNrgalYoy;

    @PersistentProperty(name = "ore_dlt", valueType = Double, description = "净资产收益率-摊薄")
    private Double oreDlt;

    @PersistentProperty(name = "rop", valueType = Double, description = "人力投入回报率")
    private Double rop;

    @PersistentProperty(name = "asset_liab_ratio", valueType = Double, description = "资产负债率")
    private Double assetLiabRatio;

    @PersistentProperty(name = "current_ratio", valueType = Double, description = "流动比率")
    private Double currentRatio;

    @PersistentProperty(name = "quick_ratio", valueType = Double, description = "速动比率")
    private Double quickRatio;

    @PersistentProperty(name = "equity_multiplier", valueType = Double, description = "权益乘数")
    private Double equityMultiplier;

    @PersistentProperty(name = "equity_ratio", valueType = Double, description = "产权比率")
    private Double equityRatio;

    @PersistentProperty(name = "holder_equity", valueType = Double, description = "股东权益比率")
    private Double holderEquity;

    @PersistentProperty(name = "ncf_from_oa_to_total_liab", valueType = Double, description = "现金流量比率")
    private Double ncfFromOaToTotalLiab;

    @PersistentProperty(name = "inventory_turnover_days", valueType = Double, description = "存货周转天数")
    private Double inventoryTurnoverDays;

    @PersistentProperty(name = "receivable_turnover_days", valueType = Double, description = "应收账款周转天数")
    private Double receivableTurnoverDays;

    @PersistentProperty(name = "accounts_payable_turnover_days", valueType = Double, description = "应付账款周转天数")
    private Double accountsPayableTurnoverDays;

    @PersistentProperty(name = "cash_cycle", valueType = Double, description = "现金循环周期（天）")
    private Double cashCycle;

    @PersistentProperty(name = "operating_cycle", valueType = Double, description = "营业周期（天）")
    private Double operatingCycle;

    @PersistentProperty(name = "total_capital_turnover", valueType = Double, description = "总资产周转率（次）")
    private Double totalCapitalTurnover;

    @PersistentProperty(name = "inventory_turnover", valueType = Double, description = "存货周转率（次）")
    private Double inventoryTurnover;

    @PersistentProperty(name = "account_receivable_turnover", valueType = Double, description = "应收账款周转率（次）")
    private Double accountReceivableTurnover;

    @PersistentProperty(name = "accounts_payable_turnover", valueType = Double, description = "应付账款周转率（次）")
    private Double accountsPayableTurnover;

    @PersistentProperty(name = "current_asset_turnover_rate", valueType = Double, description = "流动资产周转率（次）")
    private Double currentAssetTurnoverRate;

    @PersistentProperty(name = "fixed_asset_turnover_ratio", valueType = Double, description = "固定资产周转率（次）")
    private Double fixedAssetTurnoverRatio;
}
