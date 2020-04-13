package org.xmuyoo.blueberry.collect.collectors;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
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

@Slf4j
public class FinancialReportCollector extends BasicCollector {

    private static final String FINANCIAL_REPORT = "financial_report";
    private static final Splitter LINE_SPLITTER = Splitter.on("\n")
                                                          .trimResults()
                                                          .omitEmptyStrings();
    private static final Splitter COMMA_SPLITTER = Splitter.on(",")
                                                           .trimResults()
                                                           .omitEmptyStrings();
    private static final Map<String, Pair<String, String>> INDICATOR_SCHEMA_NAME_MAPPING =
            ImmutableMap.<String, Pair<String, String>>builder()
                    .put("报告日期", Pair.of("datetime", "datetime"))
                    .put("销售商品、提供劳务收到的现金(万元)", Pair.of("cr_from_sg_ps",
                            "Cash received from selling goods and providing services"))
                    .put("客户存款和同业存放款项净增加额(万元)", Pair.of("ni_in_cd_dwb",
                            "Net increase in customer deposits and deposits with banks"))
                    .put("向中央银行借款净增加额(万元", Pair.of("ni_in_bftcb",
                            "Net increase in borrowings from the central bank"))
                    .put("向其他金融机构拆入资金净增加额(万元)", Pair.of("ni_in_bfofi",
                            "Net increase in borrowings from other financial institutions"))
                    .put("收到原保险合同保费取得的现金(万元)", Pair.of("cr_from_pooic",
                            "Cash received from premium of original insurance contract"))
                    .put("收到再保险业务现金净额(万元)",
                            Pair.of("ncr_from_rb", "Net cash received from reinsurance business"))
                    .put("保户储金及投资款净增加额(万元)", Pair.of("ni_in_daiooti",
                            "Net increase in deposit and investment of the insured"))
                    .put("处置交易性金融资产净增加额(万元)", Pair.of("ni_in_dotfa",
                            "Net increase in disposal of trading financial assets"))
                    .put("收取利息、手续费及佣金的现金(万元)", Pair.of("cr_for_ihcac",
                            "Cash received for interest, handling charge and commission"))
                    .put("拆入资金净增加额(万元)", Pair.of("ni_in_bf", "Net increase in borrowed funds"))
                    .put("回购业务资金净增加额(万元)",
                            Pair.of("ni_in_pbf", "Net increase in repurchase business funds"))
                    .put("收到的税费返还(万元)", Pair.of("rot", "Refunds of taxes"))
                    .put("收到的其他与经营活动有关的现金(万元)", Pair.of("ocrrtoa",
                            "Other cash received related to operating activities"))
                    .put("经营活动现金流入小计(万元)", Pair.of("s_of_cifoa",
                            "Subtotal of cash inflow from operating activities"))
                    .put("购买商品、接受劳务支付的现金(万元)",
                            Pair.of("cp_for_gas", "Cash paid for goods and services"))
                    .put("客户贷款及垫款净增加额(万元)",
                            Pair.of("ni_in_claa", "Net increase in customer loans and advances"))
                    .put("存放中央银行和同业款项净增加额(万元)", Pair.of("ni_in_dwtcbaob",
                            "Net increase in deposits with the central bank and other banks"))
                    .put("支付原保险合同赔付款项的现金(万元)", Pair.of("cp_for_cutoic",
                            "Cash paid for compensation under the original insurance contract"))
                    .put("支付利息、手续费及佣金的现金(万元)", Pair.of("cp_forihcac",
                            "Cash paid for interest, handling charge and commission"))
                    .put("支付保单红利的现金(万元)", Pair.of("cp_for_pd", "Cash paid for policy dividend"))
                    .put("支付给职工以及为职工支付的现金(万元)",
                            Pair.of("cp_to_and_for_e", "Cash paid to and for employees"))
                    .put("支付的各项税费(万元)", Pair.of("tp", "Taxes paid"))
                    .put("支付的其他与经营活动有关的现金(万元)", Pair.of("ocpr_to_oa",
                            "Other cash paid related to operating activities"))
                    .put("经营活动现金流出小计(万元)", Pair.of("soco_from_oa",
                            "Subtotal of cash outflow from operating activities"))
                    .put("经营活动产生的现金流量净额(万元)",
                            Pair.of("ncf_from_oa", "Net cash flow from operating activities"))
                    .put("经营活动产生现金流量净额(万元)",
                            Pair.of("ncf_from_oa", "Net cash flow from operating activities"))
                    .put("收回投资所收到的现金(万元)",
                            Pair.of("cr_from_ir", "Cash received from investment recovery"))
                    .put("取得投资收益所收到的现金(万元)",
                            Pair.of("cr_from_ii", "Cash received from investment income"))
                    .put("处置固定资产、无形资产和其他长期资产所收回的现金净额(万元)", Pair.of("ncr_from_dofaiaaola",
                            "Net cash received from disposal of fixed assets, intangible assets and other long-term assets"))
                    .put("处置子公司及其他营业单位收到的现金净额(万元)", Pair.of("ncr_from_dosaobu",
                            "Net cash received from disposal of subsidiaries and other business units"))
                    .put("收到的其他与投资活动有关的现金(万元)", Pair.of("ocrr_to_ia",
                            "Other cash received related to investment activities"))
                    .put("减少质押和定期存款所收到的现金(万元)", Pair.of("cr_from_pafdr",
                            "Cash received from pledge and fixed deposit reduction"))
                    .put("投资活动现金流入小计(万元)", Pair.of("soci_from_ia",
                            "Subtotal of cash inflow from investment activities"))
                    .put("购建固定资产、无形资产和其他长期资产所支付的现金(万元)", Pair.of("cp_for_aacofaiaaola",
                            "Cash paid for acquisition and construction of fixed assets, intangible assets and other long-term assets"))
                    .put("投资所支付的现金(万元)", Pair.of("cp_for_i", "Cash paid for investment"))
                    .put("质押贷款净增加额(万元)", Pair.of("ni_in_pl", "Net increase in pledged loans"))
                    .put("取得子公司及其他营业单位支付的现金净额(万元)", Pair.of("ncp_by_saobu",
                            "Net cash paid by subsidiaries and other business units"))
                    .put("支付的其他与投资活动有关的现金(万元)", Pair.of("ocpr_to_ia",
                            "Other cash paid related to investment activities"))
                    .put("增加质押和定期存款所支付的现金(万元)", Pair.of("cp_for_iopafd",
                            "Cash paid for increase of pledge and fixed deposit"))
                    .put("投资活动现金流出小计(万元)", Pair.of("soco_from_ia",
                            "Subtotal of cash outflow from investment activities"))
                    .put("投资活动产生的现金流量净额(万元)",
                            Pair.of("ncf_from_ia", "Net cash flow from investment activities"))
                    .put("吸收投资收到的现金(万元)",
                            Pair.of("cr_from_ia", "Cash received from investment absorption"))
                    .put("其中：子公司吸收少数股东投资收到的现金(万元)", Pair.of("including_cr_from_ms_ibs",
                            "Including: cash received from minority shareholders' investment by subsidiaries"))
                    .put("取得借款收到的现金(万元)", Pair.of("cr_from_b", "Cash received from borrowing"))
                    .put("发行债券收到的现金(万元)", Pair.of("cr_from_bi", "Cash received from bond issuance"))
                    .put("收到其他与筹资活动有关的现金(万元)",
                            Pair.of("cr_from_ofa", "Cash received from other financing activities"))
                    .put("筹资活动现金流入小计(万元)", Pair.of("soci_from_fa",
                            "Subtotal of cash inflow from financing activities"))
                    .put("偿还债务支付的现金(万元)", Pair.of("cp_for_dr", "Cash paid for debt repayment"))
                    .put("分配股利、利润或偿付利息所支付的现金(万元)", Pair.of("cp_for_dodpoi",
                            "Cash paid for distribution of dividends, profits or interest"))
                    .put("其中：子公司支付给少数股东的股利、利润(万元)", Pair.of("including_dappbstms",
                            "Including: dividends and profits paid by subsidiaries to minority shareholders"))
                    .put("支付其他与筹资活动有关的现金(万元)",
                            Pair.of("cp_for_ofa", "Cash paid for other financing activities"))
                    .put("筹资活动现金流出小计(万元)", Pair.of("socr_from_fa",
                            "Subtotal of cash outflow from financing activities"))
                    .put("筹资活动产生的现金流量净额(万元)",
                            Pair.of("ncf_from_fa", "Net cash flow from financing activities"))
                    .put("汇率变动对现金及现金等价物的影响(万元)", Pair.of("eoerc_on_cace",
                            "Effect of exchange rate changes on cash and cash equivalents"))
                    .put("现金及现金等价物净增加额(万元)",
                            Pair.of("ni_in_cace", "Net increase in cash and cash equivalents"))
                    .put("现金及现金等价物的净增加额(万元)",
                            Pair.of("ni_in_cace", "Net increase in cash and cash equivalents"))
                    .put("加:期初现金及现金等价物余额(万元)", Pair.of("add_bofcace_at_tbotp",
                            "Add: balance of cash and cash equivalents at the beginning of the period"))
                    .put("期末现金及现金等价物余额(万元)", Pair.of("bocace_at_teotp",
                            "Balance of cash and cash equivalents at the end of the period"))
                    .put("净利润(万元)", Pair.of("np", "Net Profit"))
                    .put("少数股东损益(万元)", Pair.of("mi", "Minority interest"))
                    .put("未确认的投资损失(万元)", Pair.of("uil", "Unrecognized investment loss"))
                    .put("资产减值准备(万元)", Pair.of("p_for_ioa", "Provision for impairment of assets"))
                    .put("固定资产折旧、油气资产折耗、生产性物资折旧(万元)", Pair.of("dofa_dooaga_dopm",
                            "Depreciation of fixed assets, depletion of oil and gas assets, depreciation of productive materials"))
                    .put("无形资产摊销(万元)", Pair.of("a_of_ia", "Amortization of intangible assets"))
                    .put("长期待摊费用摊销(万元)",
                            Pair.of("a_of_lue", "Amortization of long-term unamortized expense"))
                    .put("待摊费用的减少(万元)", Pair.of("d_of_ue", "Decrease of unamortized expense"))
                    .put("预提费用的增加(万元)", Pair.of("i_in_ae", "Increase in accrued expense"))
                    .put("处置固定资产、无形资产和其他长期资产的损失(万元)", Pair.of("lod_of_fa_ia_and_ola",
                            "Loss on disposal of fixed assets, intangible assets and other long-term asset"))
                    .put("固定资产报废损失(万元)", Pair.of("l_on_rofa", "Loss on retirement of fixed asset"))
                    .put("公允价值变动损失(万元)", Pair.of("l_from_cifv", "Loss from changes in fair value"))
                    .put("递延收益增加(减：减少)(万元)",
                            Pair.of("i_in_di", "Increase in deferred income (minus: decrease)"))
                    .put("预计负债(万元)", Pair.of("al", "Accrued liabilities"))
                    .put("财务费用(万元)", Pair.of("fe", "Financial Expense"))
                    .put("投资损失(万元)", Pair.of("il", "Investment loss"))
                    .put("递延所得税资产减少(万元)",
                            Pair.of("d_of_dita", "Decrease of deferred income tax assets"))
                    .put("递延所得税负债增加(万元)",
                            Pair.of("i_in_ditl", "Increase in deferred income tax liabilities"))
                    .put("存货的减少(万元)", Pair.of("d_in_i", "Decrease in inventories"))
                    .put("经营性应收项目的减少(万元)", Pair.of("d_of_or", "Decrease of operating receivables"))
                    .put("经营性应付项目的增加(万元)", Pair.of("i_in_op", "Increase in operating payables"))
                    .put("已完工尚未结算款的减少(减:增加)(万元)", Pair.of("d_of_cbuf",
                            "Decrease of completed but unsettled funds (minus: increase)"))
                    .put("已结算尚未完工款的增加(减:减少)(万元)", Pair.of("i_of_sbncp",
                            "Increase of settled but not completed payment  (minus: decrease)"))
                    .put("其他(万元)", Pair.of("others", "Others"))
                    .put("债务转为资本(万元)", Pair.of("d_to_c", "Debt to capital"))
                    .put("一年内到期的可转换公司债券(万元)",
                            Pair.of("cbd_within_oy", "Convertible bonds due within one year"))
                    .put("融资租入固定资产(万元)", Pair.of("faufl", "Fixed assets under financing leas"))
                    .put("现金的期末余额(万元)", Pair.of("cb_of_c", "Closing balance of cash"))
                    .put("现金的期初余额(万元)", Pair.of("ob_of_c", "Opening balance of cash"))
                    .put("现金等价物的期末余额(万元)",
                            Pair.of("cb_of_ce", "Closing balance of cash equivalents"))
                    .put("现金等价物的期初余额(万元)",
                            Pair.of("ob_of_ce", "Opening balance of cash equivalents"))
                    .build();
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

            for (StockCode stockCode : stockCodeList) {
                Request request = Requests.newFinancialReportRequest(stockCode.code());
                httpClient.async(request, (req, response) -> {
                    if (response.status() != HttpStatus.OK.value()) {
                        log.warn("Failed to get financial report for {}", req.fullUrl());
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
        if (lines.size() < INDICATOR_SCHEMA_NAME_MAPPING.size())
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
            if (!INDICATOR_SCHEMA_NAME_MAPPING.containsKey(indicatorOption)) {
                log.warn("Unknown indicator option: {}", indicatorOption);
                continue;
            }

            Pair<String, String> indicatorInfo = INDICATOR_SCHEMA_NAME_MAPPING.get(indicatorOption);
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