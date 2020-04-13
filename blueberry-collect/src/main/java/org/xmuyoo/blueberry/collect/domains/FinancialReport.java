package org.xmuyoo.blueberry.collect.domains;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Getter
@Setter
@ToString(callSuper = true)
@Slf4j
public class FinancialReport extends Data {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-mm-dd");

    private FinancialReport(String id, DataType dataType, LocalDateTime dateTime) {
        super(id, dataType, dateTime);
    }

    public static FinancialReport of(LocalDateTime recordTime, StockCode stockCode,
                                     Map<String, Double> indicators) {

        String id = String.format("%s-%s",
                stockCode.code(), recordTime.format(DATE_TIME_FORMATTER));
        FinancialReport financialReport =
                new FinancialReport(id, DataType.FinancialReport, recordTime);
        financialReport.stockCode(stockCode.code()).stockName(stockCode.name());

        for (Field field : financialReport.getClass().getDeclaredFields()) {
            String fieldName = field.getName();
            if (!indicators.containsKey(fieldName))
                continue;

            field.setAccessible(true);
            try {
                field.setDouble(financialReport, indicators.get(fieldName));
            } catch (IllegalAccessException e) {
                String msg = "Failed to set filed: " + fieldName;
                log.error(msg, e);
                throw new RuntimeException(msg);
            }
        }

        return financialReport;
    }

    @Tag
    private String stockCode;

    @Tag
    private String stockName;

    @SeriesProperty(description = "Cash received from selling goods and providing services")
    private double cr_from_sg_ps;

    @SeriesProperty(description = "Net increase in customer deposits and deposits with banks")
    private double ni_in_cd_dwb;

    @SeriesProperty(description = "Net increase in borrowings from the central bank")
    private double ni_in_bftcb;

    @SeriesProperty(description = "Net increase in borrowings from other financial institutions")
    private double ni_in_bfofi;

    @SeriesProperty(description = "Cash received from premium of original insurance contract")
    private double cr_from_pooic;

    @SeriesProperty(description = "Net cash received from reinsurance business")
    private double ncr_from_rb;

    @SeriesProperty(description = "Net increase in deposit and investment of the insured")
    private double ni_in_daiooti;

    @SeriesProperty(description = "Net increase in disposal of trading financial assets")
    private double ni_in_dotfa;

    @SeriesProperty(description = "Cash received for interest, handling charge and commission")
    private double cr_for_ihcac;

    @SeriesProperty(description = "Net increase in borrowed funds")
    private double ni_in_bf;

    @SeriesProperty(description = "Net increase in repurchase business funds")
    private double ni_in_pbf;

    @SeriesProperty(description = "Refunds of taxes")
    private double rot;

    @SeriesProperty(description = "Other cash received related to operating activities")
    private double ocrrtoa;

    @SeriesProperty(description = "Subtotal of cash inflow from operating activities")
    private double s_of_cifoa;

    @SeriesProperty(description = "Cash paid for goods and services")
    private double cp_for_gas;

    @SeriesProperty(description = "Net increase in customer loans and advances")
    private double ni_in_claa;

    @SeriesProperty(description = "Net increase in deposits with the central bank and other banks")
    private double ni_in_dwtcbaob;

    @SeriesProperty(description = "Cash paid for compensation under the original insurance contract")
    private double cp_for_cutoic;

    @SeriesProperty(description = "Cash paid for interest, handling charge and commission")
    private double cp_forihcac;

    @SeriesProperty(description = "Cash paid for policy dividend")
    private double cp_for_pd;

    @SeriesProperty(description = "Cash paid to and for employees")
    private double cp_to_and_for_e;

    @SeriesProperty(description = "Taxes paid")
    private double tp;

    @SeriesProperty(description = "Other cash paid related to operating activities")
    private double ocpr_to_oa;

    @SeriesProperty(description = "Subtotal of cash outflow from operating activities")
    private double soco_from_oa;

    @SeriesProperty(description = "Net cash flow from operating activities")
    private double ncf_from_oa;

    @SeriesProperty(description = "Cash received from investment recovery")
    private double cr_from_ir;

    @SeriesProperty(description = "Cash received from investment income")
    private double cr_from_ii;

    @SeriesProperty(description = "Net cash received from disposal of fixed assets, intangible assets and other long-term assets")
    private double ncr_from_dofaiaaola;

    @SeriesProperty(description = "Net cash received from disposal of subsidiaries and other business units")
    private double ncr_from_dosaobu;

    @SeriesProperty(description = "Other cash received related to investment activities")
    private double ocrr_to_ia;

    @SeriesProperty(description = "Cash received from pledge and fixed deposit reduction")
    private double cr_from_pafdr;

    @SeriesProperty(description = "Subtotal of cash inflow from investment activities")
    private double soci_from_ia;

    @SeriesProperty(description = "Cash paid for acquisition and construction of fixed assets, intangible assets and other long-term assets")
    private double cp_for_aacofaiaaola;

    @SeriesProperty(description = "Cash paid for investment")
    private double cp_for_i;

    @SeriesProperty(description = "Net increase in pledged loans")
    private double ni_in_pl;

    @SeriesProperty(description = "Net cash paid by subsidiaries and other business units")
    private double ncp_by_saobu;

    @SeriesProperty(description = "Other cash paid related to investment activities")
    private double ocpr_to_ia;

    @SeriesProperty(description = "Cash paid for increase of pledge and fixed deposit")
    private double cp_for_iopafd;

    @SeriesProperty(description = "Subtotal of cash outflow from investment activities")
    private double soco_from_ia;

    @SeriesProperty(description = "Net cash flow from investment activities")
    private double ncf_from_ia;

    @SeriesProperty(description = "Cash received from investment absorption")
    private double cr_from_ia;

    @SeriesProperty(description = "Including: cash received from minority shareholders' investment by subsidiaries")
    private double including_cr_from_ms_ibs;

    @SeriesProperty(description = "Cash received from borrowing")
    private double cr_from_b;

    @SeriesProperty(description = "Cash received from bond issuance")
    private double cr_from_bi;

    @SeriesProperty(description = "Cash received from other financing activities")
    private double cr_from_ofa;

    @SeriesProperty(description = "Subtotal of cash inflow from financing activities")
    private double soci_from_fa;

    @SeriesProperty(description = "Cash paid for debt repayment")
    private double cp_for_dr;

    @SeriesProperty(description = "Cash paid for distribution of dividends, profits or interest")
    private double cp_for_dodpoi;

    @SeriesProperty(description = "Including: dividends and profits paid by subsidiaries to minority shareholders")
    private double including_dappbstms;

    @SeriesProperty(description = "Cash paid for other financing activities")
    private double cp_for_ofa;

    @SeriesProperty(description = "Subtotal of cash outflow from financing activities")
    private double socr_from_fa;

    @SeriesProperty(description = "Net cash flow from financing activities")
    private double ncf_from_fa;

    @SeriesProperty(description = "Effect of exchange rate changes on cash and cash equivalents")
    private double eoerc_on_cace;

    @SeriesProperty(description = "Net increase in cash and cash equivalents")
    private double ni_in_cace;

    @SeriesProperty(description = "Add: balance of cash and cash equivalents at the beginning of the period")
    private double add_bofcace_at_tbotp;

    @SeriesProperty(description = "Balance of cash and cash equivalents at the end of the period")
    private double bocace_at_teotp;

    @SeriesProperty(description = "Net Profit")
    private double np;

    @SeriesProperty(description = "Minority interest")
    private double mi;

    @SeriesProperty(description = "Unrecognized investment loss")
    private double uil;

    @SeriesProperty(description = "Provision for impairment of assets")
    private double p_for_ioa;

    @SeriesProperty(description = "Depreciation of fixed assets, depletion of oil and gas assets, depreciation of productive materials")
    private double dofa_dooaga_dopm;

    @SeriesProperty(description = "Amortization of intangible assets")
    private double a_of_ia;

    @SeriesProperty(description = "Amortization of long-term unamortized expense")
    private double a_of_lue;

    @SeriesProperty(description = "Decrease of unamortized expense")
    private double d_of_ue;

    @SeriesProperty(description = "Increase in accrued expense")
    private double i_in_ae;

    @SeriesProperty(description = "Loss on disposal of fixed assets, intangible assets and other long-term asset")
    private double lod_of_fa_ia_and_ola;

    @SeriesProperty(description = "Loss on retirement of fixed asset")
    private double l_on_rofa;

    @SeriesProperty(description = "Loss from changes in fair value")
    private double l_from_cifv;

    @SeriesProperty(description = "Increase in deferred income (minus: decrease)")
    private double i_in_di;

    @SeriesProperty(description = "Accrued liabilities")
    private double al;

    @SeriesProperty(description = "Financial Expense")
    private double fe;

    @SeriesProperty(description = "Investment loss")
    private double il;

    @SeriesProperty(description = "Decrease of deferred income tax assets")
    private double d_of_dita;

    @SeriesProperty(description = "Increase in deferred income tax liabilities")
    private double i_in_ditl;

    @SeriesProperty(description = "Decrease in inventories")
    private double d_in_i;

    @SeriesProperty(description = "Decrease of operating receivables")
    private double d_of_or;

    @SeriesProperty(description = "Increase in operating payables")
    private double i_in_op;

    @SeriesProperty(description = "Decrease of completed but unsettled funds (minus: increase)")
    private double d_of_cbuf;

    @SeriesProperty(description = "Increase of settled but not completed payment  (minus: decrease)")
    private double i_of_sbncp;

    @SeriesProperty(description = "Others")
    private double others;

    @SeriesProperty(description = "Debt to capital")
    private double d_to_c;

    @SeriesProperty(description = "Convertible bonds due within one year")
    private double cbd_within_oy;

    @SeriesProperty(description = "Fixed assets under financing leas")
    private double faufl;

    @SeriesProperty(description = "Closing balance of cash")
    private double cb_of_c;

    @SeriesProperty(description = "Opening balance of cash")
    private double ob_of_c;

    @SeriesProperty(description = "Closing balance of cash equivalents")
    private double cb_of_ce;

    @SeriesProperty(description = "Opening balance of cash equivalents")
    private double ob_of_ce;

    @Override
    protected Map<String, String> generateTags() {
        return null;
    }
}
