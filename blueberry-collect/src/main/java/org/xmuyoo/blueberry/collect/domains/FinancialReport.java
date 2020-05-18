package org.xmuyoo.blueberry.collect.domains;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Getter
@Setter
@ToString(callSuper = true)
@Slf4j
public class FinancialReport extends Data {

    private static final String CR_FROM_SG_PS_ZH = "销售商品、提供劳务收到的现金(万元)";
    private static final String CR_FROM_SG_PS_EN = "Cash received from selling goods and providing services";
    private static final String NI_IN_CD_DWB_ZH = "客户存款和同业存放款项净增加额(万元)";
    private static final String NI_IN_CD_DWB_EN = "Net increase in customer deposits and deposits with banks";
    private static final String NI_IN_BFTCB_ZH = "向中央银行借款净增加额(万元";
    private static final String NI_IN_BFTCB_EN = "Net increase in borrowings from the central bank";
    private static final String NI_IN_BFOFI_ZH = "向其他金融机构拆入资金净增加额(万元)";
    private static final String NI_IN_BFOFI_EN = "Net increase in borrowings from other financial institutions";
    private static final String CR_FROM_POOIC_ZH = "收到原保险合同保费取得的现金(万元)";
    private static final String CR_FROM_POOIC_EN = "Cash received from premium of original insurance contract";
    private static final String NCR_FROM_RB_ZH = "收到再保险业务现金净额(万元)";
    private static final String NCR_FROM_RB_EN = "Net cash received from reinsurance business";
    private static final String NI_IN_DAIOOTI_ZH = "保户储金及投资款净增加额(万元)";
    private static final String NI_IN_DAIOOTI_EN = "Net increase in deposit and investment of the insured";
    private static final String NI_IN_DOTFA_ZH = "处置交易性金融资产净增加额(万元)";
    private static final String NI_IN_DOTFA_EN = "Net increase in disposal of trading financial assets";
    private static final String CR_FOR_IHCAC_ZH = "收取利息、手续费及佣金的现金(万元)";
    private static final String CR_FOR_IHCAC_EN = "Cash received for interest, handling charge and commission";
    private static final String NI_IN_BF_ZH = "拆入资金净增加额(万元)";
    private static final String NI_IN_BF_EN = "Net increase in borrowed funds";
    private static final String NI_IN_PBF_ZH = "回购业务资金净增加额(万元)";
    private static final String NI_IN_PBF_EN = "Net increase in repurchase business funds";
    private static final String ROT_ZH = "收到的税费返还(万元)";
    private static final String ROT_EN = "Refunds of taxes";
    private static final String OCRRTOA_ZH = "收到的其他与经营活动有关的现金(万元)";
    private static final String OCRRTOA_EN = "Other cash received related to operating activities";
    private static final String S_OF_CIFOA_ZH = "经营活动现金流入小计(万元)";
    private static final String S_OF_CIFOA_EN = "Subtotal of cash inflow from operating activities";
    private static final String CP_FOR_GAS_ZH = "购买商品、接受劳务支付的现金(万元)";
    private static final String CP_FOR_GAS_EN = "Cash paid for goods and services";
    private static final String NI_IN_CLAA_ZH = "客户贷款及垫款净增加额(万元)";
    private static final String NI_IN_CLAA_EN = "Net increase in customer loans and advances";
    private static final String NI_IN_DWTCBAOB_ZH = "存放中央银行和同业款项净增加额(万元)";
    private static final String NI_IN_DWTCBAOB_EN = "Net increase in deposits with the central bank and other banks";
    private static final String CP_FOR_CUTOIC_ZH = "支付原保险合同赔付款项的现金(万元)";
    private static final String CP_FOR_CUTOIC_EN = "Cash paid for compensation under the original insurance contract";
    private static final String CP_FOR_IHCAC_ZH = "支付利息、手续费及佣金的现金(万元)";
    private static final String CP_FOR_IHCAC_EN = "Cash paid for interest, handling charge and commission";
    private static final String CP_FOR_PD_ZH = "支付保单红利的现金(万元)";
    private static final String CP_FOR_PD_EN = "Cash paid for policy dividend";
    private static final String CP_TO_AND_FOR_E_ZH = "支付给职工以及为职工支付的现金(万元)";
    private static final String CP_TO_AND_FOR_E_EN = "Cash paid to and for employees";
    private static final String TP_ZH = "支付的各项税费(万元)";
    private static final String TP_EN = "Taxes paid";
    private static final String OCPR_TO_OA_ZH = "支付的其他与经营活动有关的现金(万元)";
    private static final String OCPR_TO_OA_EN = "Other cash paid related to operating activities";
    private static final String SOCO_FROM_OA_ZH = "经营活动现金流出小计(万元)";
    private static final String SOCO_FROM_OA_EN = "Subtotal of cash outflow from operating activities";
    private static final String NCF_FROM_OA_ZH = "经营活动产生的现金流量净额(万元)";
    private static final String NCF_FROM_OA_EN = "Net cash flow from operating activities";
    private static final String CR_FROM_IR_ZH = "收回投资所收到的现金(万元)";
    private static final String CR_FROM_IR_EN = "Cash received from investment recovery";
    private static final String CR_FROM_II_ZH = "取得投资收益所收到的现金(万元)";
    private static final String CR_FROM_II_EN = "Cash received from investment income";
    private static final String NCR_FROM_DOFAIAAOLA_ZH = "处置固定资产、无形资产和其他长期资产所收回的现金净额(万元)";
    private static final String NCR_FROM_DOFAIAAOLA_EN = "Net cash received from disposal of fixed assets, intangible assets and other long-term assets";
    private static final String NCR_FROM_DOSAOBU_ZH = "处置子公司及其他营业单位收到的现金净额(万元)";
    private static final String NCR_FROM_DOSAOBU_EN = "Net cash received from disposal of subsidiaries and other business units";
    private static final String OCRR_TO_IA_ZH = "收到的其他与投资活动有关的现金(万元)";
    private static final String OCRR_TO_IA_EN = "Other cash received related to investment activities";
    private static final String CR_FROM_PAFDR_ZH = "减少质押和定期存款所收到的现金(万元)";
    private static final String CR_FROM_PAFDR_EN = "Cash received from pledge and fixed deposit reduction";
    private static final String SOCI_FROM_IA_ZH = "投资活动现金流入小计(万元)";
    private static final String SOCI_FROM_IA_EN = "Subtotal of cash inflow from investment activities";
    private static final String CP_FOR_AACOFAIAAOLA_ZH = "购建固定资产、无形资产和其他长期资产所支付的现金(万元)";
    private static final String CP_FOR_AACOFAIAAOLA_EN = "Cash paid for acquisition and construction of fixed assets, intangible assets and other long-term assets";
    private static final String CP_FOR_I_ZH = "投资所支付的现金(万元)";
    private static final String CP_FOR_I_EN = "Cash paid for investment";
    private static final String NI_IN_PL_ZH = "质押贷款净增加额(万元)";
    private static final String NI_IN_PL_EN = "Net increase in pledged loans";
    private static final String NCP_BY_SAOBU_ZH = "取得子公司及其他营业单位支付的现金净额(万元)";
    private static final String NCP_BY_SAOBU_EN = "Net cash paid by subsidiaries and other business units";
    private static final String OCPR_TO_IA_ZH = "支付的其他与投资活动有关的现金(万元)";
    private static final String OCPR_TO_IA_EN = "Other cash paid related to investment activities";
    private static final String CP_FOR_IOPAFD_ZH = "增加质押和定期存款所支付的现金(万元)";
    private static final String CP_FOR_IOPAFD_EN = "Cash paid for increase of pledge and fixed deposit";
    private static final String SOCO_FROM_IA_ZH = "投资活动现金流出小计(万元)";
    private static final String SOCO_FROM_IA_EN = "Subtotal of cash outflow from investment activities";
    private static final String NCF_FROM_IA_ZH = "投资活动产生的现金流量净额(万元)";
    private static final String NCF_FROM_IA_EN = "Net cash flow from investment activities";
    private static final String CR_FROM_IA_ZH = "吸收投资收到的现金(万元)";
    private static final String CR_FROM_IA_EN = "Cash received from investment absorption";
    private static final String INCLUDING_CR_FROM_MS_IBD_ZH = "其中：子公司吸收少数股东投资收到的现金(万元)";
    private static final String INCLUDING_CR_FROM_MS_IBD_EN = "Including: cash received from minority shareholders' investment by subsidiaries";
    private static final String CR_FROM_B_ZH = "取得借款收到的现金(万元)";
    private static final String CR_FROM_B_EN = "Cash received from borrowing";
    private static final String CR_FROM_BI_ZH = "发行债券收到的现金(万元)";
    private static final String CR_FROM_BI_EN = "Cash received from bond issuance";
    private static final String CR_FROM_OFA_ZH = "收到其他与筹资活动有关的现金(万元)";
    private static final String CR_FROM_OFA_EN = "Cash received from other financing activities";
    private static final String SOCI_FROM_FA_ZH = "筹资活动现金流入小计(万元)";
    private static final String SOCI_FROM_FA_EN = "Subtotal of cash inflow from financing activities";
    private static final String CP_FOR_DR_ZH = "偿还债务支付的现金(万元)";
    private static final String CP_FOR_DR_EN = "Cash paid for debt repayment";
    private static final String CP_FOR_DODPOI_ZH = "分配股利、利润或偿付利息所支付的现金(万元)";
    private static final String CP_FOR_DODPOI_EN = "Cash paid for distribution of dividends, profits or interest";
    private static final String INCLUDING_DAPPBSTMS_ZH = "其中：子公司支付给少数股东的股利、利润(万元)";
    private static final String INCLUDING_DAPPBSTMS_EN = "Including: dividends and profits paid by subsidiaries to minority shareholders";
    private static final String CP_FOR_OFA_ZH = "支付其他与筹资活动有关的现金(万元)";
    private static final String CP_FOR_OFA_EN = "Cash paid for other financing activities";
    private static final String SOCR_FROM_FA_ZH = "筹资活动现金流出小计(万元)";
    private static final String SOCR_FROM_FA_EN = "Subtotal of cash outflow from financing activities";
    private static final String NCF_FROM_FA_ZH = "筹资活动产生的现金流量净额(万元)";
    private static final String NCF_FROM_FA_EN = "Net cash flow from financing activities";
    private static final String EOERC_ON_CACE_ZH = "汇率变动对现金及现金等价物的影响(万元)";
    private static final String EOERC_ON_CACE_EN = "Effect of exchange rate changes on cash and cash equivalents";
    private static final String NI_IN_CACE_ZH = "现金及现金等价物净增加额(万元)";
    private static final String NI_IN_CACE_EN = "Net increase in cash and cash equivalents";
    private static final String ADD_BOFCACE_AT_TBOTP_ZH = "加:期初现金及现金等价物余额(万元)";
    private static final String ADD_BOFCACE_AT_TBOTP_EN = "Add: balance of cash and cash equivalents at the beginning of the period";
    private static final String BOCACE_AT_TEOTP_ZH = "期末现金及现金等价物余额(万元)";
    private static final String BOCACE_AT_TEOTP_EN = "Balance of cash and cash equivalents at the end of the period";
    private static final String NP_ZH = "净利润(万元)";
    private static final String NP_EN = "Net Profit";
    private static final String MI_ZH = "少数股东损益(万元)";
    private static final String MI_EN = "Minority interest";
    private static final String UIL_ZH = "未确认的投资损失(万元)";
    private static final String UIL_EN = "Unrecognized investment loss";
    private static final String P_FOR_IOA_ZH = "资产减值准备(万元)";
    private static final String P_FOR_IOA_EN = "Provision for impairment of assets";
    private static final String DOFA_DOOAGA_DOPM_ZH = "固定资产折旧、油气资产折耗、生产性物资折旧(万元)";
    private static final String DOFA_DOOAGA_DOPM_EN = "Depreciation of fixed assets, depletion of oil and gas assets, depreciation of productive materials";
    private static final String A_OF_IA_ZH = "无形资产摊销(万元)";
    private static final String A_OF_IA_EN = "Amortization of intangible assets";
    private static final String A_OF_LUE_ZH = "长期待摊费用摊销(万元)";
    private static final String A_OF_LUE_EN = "Amortization of long-term unamortized expense";
    private static final String D_OF_UE_ZH = "待摊费用的减少(万元)";
    private static final String D_OF_UE_EN = "Decrease of unamortized expense";
    private static final String I_IN_AE_ZH = "预提费用的增加(万元)";
    private static final String I_IN_AE_EN = "Increase in accrued expense";
    private static final String LOD_OF_FA_IA_AND_OLA_ZH = "处置固定资产、无形资产和其他长期资产的损失(万元)";
    private static final String LOD_OF_FA_IA_AND_OLA_EN = "Loss on disposal of fixed assets, intangible assets and other long-term asset";
    private static final String L_ON_ROFA_ZH = "固定资产报废损失(万元)";
    private static final String L_ON_ROFA_EN = "Loss on retirement of fixed asset";
    private static final String L_FROM_CIFY_ZH = "公允价值变动损失(万元)";
    private static final String L_FROM_CIFY_EN = "Loss from changes in fair value";
    private static final String I_IN_DI_ZH = "递延收益增加(减：减少)(万元)";
    private static final String I_IN_DI_EN = "Increase in deferred income (minus: decrease)";
    private static final String AL_ZH = "预计负债(万元)";
    private static final String AL_EN = "Accrued liabilities";
    private static final String FE_ZH = "财务费用(万元)";
    private static final String FE_EN = "Financial Expense";
    private static final String IL_ZH = "投资损失(万元)";
    private static final String IL_EN = "Investment loss";
    private static final String D_OF_DITA_ZH = "递延所得税资产减少(万元)";
    private static final String D_OF_DITA_EN = "Decrease of deferred income tax assets";
    private static final String I_IN_DITL_ZH = "递延所得税负债增加(万元)";
    private static final String I_IN_DITL_EN = "Increase in deferred income tax liabilities";
    private static final String D_IN_I_ZH = "存货的减少(万元)";
    private static final String D_IN_I_EN = "Decrease in inventories";
    private static final String D_OF_OR_ZH = "经营性应收项目的减少(万元)";
    private static final String D_OF_OR_EN = "Decrease of operating receivables";
    private static final String I_IN_OP_ZH = "经营性应付项目的增加(万元)";
    private static final String I_IN_OP_EN = "Increase in operating payables";
    private static final String D_OF_CBUF_ZH = "已完工尚未结算款的减少(减:增加)(万元)";
    private static final String D_OF_CBUF_EN = "Decrease of completed but unsettled funds (minus: increase)";
    private static final String I_OF_SBNCP_ZH = "已结算尚未完工款的增加(减:减少)(万元)";
    private static final String I_OF_SBNCP_EN = "Increase of settled but not completed payment  (minus: decrease)";
    private static final String OTHERS_ZH = "其他(万元)";
    private static final String OTHERS_EN = "Others";
    private static final String D_TO_C_ZH = "债务转为资本(万元)";
    private static final String D_TO_C_EN = "Debt to capital";
    private static final String CBD_WITHIN_OY_ZH = "一年内到期的可转换公司债券(万元)";
    private static final String CBD_WITHIN_OY_EN = "Convertible bonds due within one year";
    private static final String FAUFL_ZH = "融资租入固定资产(万元)";
    private static final String FAUFL_EN = "Fixed assets under financing leas";
    private static final String CB_OF_C_ZH = "现金的期末余额(万元)";
    private static final String CB_OF_C_EN = "Closing balance of cash";
    private static final String OB_OF_C_ZH = "现金的期初余额(万元)";
    private static final String OB_OF_C_EN = "Opening balance of cash";
    private static final String CB_OF_CE_ZH = "现金等价物的期末余额(万元)";
    private static final String CB_OF_CE_EN = "Closing balance of cash equivalents";
    private static final String OB_OF_CE_ZH = "现金等价物的期初余额(万元)";
    private static final String OB_OF_CE_EN = "Opening balance of cash equivalents";

    private static final String TOI_ZH = "营业总收入(万元)";
    private static final String TOI_EN = "Total Operating Income";
    private static final String REVENUE_ZH = "营业收入(万元)";
    private static final String REVENUE_EN = "Revenue";
    private static final String II_ZH = "利息收入(万元)";
    private static final String II_EN = "Interest Income";
    private static final String EP_ZH = "已赚保费(万元)";
    private static final String EP_EN = "Earned premium";
    private static final String F_AND_CI_ZH = "手续费及佣金收入(万元)";
    private static final String F_AND_CI_EN = "Fees and commission income";
    private static final String RESR_ZH = "房地产销售收入(万元)";
    private static final String RESR_EN = "Real estate sales revenue";
    private static final String OOI_ZH = "其他业务收入(万元)";
    private static final String OOI_EN = "Other operating income";
    private static final String TC_ZH = "营业总成本(万元)";
    private static final String TC_EN = "Total cost";
    private static final String C_OF_GS_ZH = "营业成本(万元)";
    private static final String C_OF_GS_EN = "Cost of goods sold";
    private static final String IE_ZH = "利息支出(万元)";
    private static final String IE_EN = "Interest expense";
    private static final String F_AND_CE_ZH = "手续费及佣金支出(万元)";
    private static final String F_AND_CE_EN = "Fees and commission expense";
    private static final String C_OF_RES_ZH = "房地产销售成本(万元)";
    private static final String C_OF_RES_EN = "cost of real estate sold";
    private static final String RESEARCH_EXPENSE_ZH = "研发费用(万元)";
    private static final String RESEARCH_EXPENSE_EN = "Research expense";
    private static final String SURRENDERS_ZH = "退保金(万元)";
    private static final String SURRENDERS_EN = "Surrenders";
    private static final String NCP_ZH = "赔付支出净额(万元)";
    private static final String NCP_EN = "Net claims paid";
    private static final String NR_ON_IC_ZH = "提取保险合同准备金净额(万元)";
    private static final String NR_ON_IC_EN = "Net reserves on insurance contracts";
    private static final String ED_ZH = "保单红利支出(万元)";
    private static final String ED_EN = "Expenditure dividends";
    private static final String REINSURANCE_EXPENSE_ZH = "分保费用(万元)";
    private static final String REINSURANCE_EXPENSE_EN = "Reinsurance expenses";
    private static final String OOC_ZH = "其他业务成本(万元)";
    private static final String OOC_EN = "Other operating cost";
    private static final String T_AND_S_ZH = "营业税金及附加(万元)";
    private static final String T_AND_S_EN = "Taxes and surcharges";
    private static final String SE_ZH = "销售费用(万元)";
    private static final String SE_EN = "Sales expense";
    private static final String AE_ZH = "管理费用(万元)";
    private static final String AE_EN = "Administration expense";
    private static final String I_FROM_CIFV_ZH = "公允价值变动收益(万元)";
    private static final String I_FROM_CIFV_EN = "Income from changes in fair value";
    private static final String I_FROM_I_ZH = "投资收益(万元)";
    private static final String I_FROM_I_EN = "Income from investments";
    private static final String I_FROM_IIA_AND_JV_ZH = "对联营企业和合营企业的投资收益(万元)";
    private static final String I_FROM_IIA_AND_JV_EN = "Income from investment in associates and joint ventures";
    private static final String EG_ZH = "汇兑收益(万元)";
    private static final String EG_EN = "Exchange gains";
    private static final String G_ON_F_ZH = "期货损益(万元)";
    private static final String G_ON_F_EN = "Gains on futures";
    private static final String DEPOSITS_ZH = "托管收益(万元)";
    private static final String DEPOSITS_EN = "Deposits";
    private static final String SI_ZH = "补贴收入(万元)";
    private static final String SI_EN = "Subsidy income";
    private static final String OOP_ZH = "其他业务利润(万元)";
    private static final String OOP_EN = "Other operating profit";
    private static final String OP_ZH = "营业利润(万元)";
    private static final String OP_EN = "Operating profit";
    private static final String NOI_ZH = "营业外收入(万元)";
    private static final String NOI_EN = "Non operating income";
    private static final String NOE_ZH = "营业外支出(万元)";
    private static final String NOE_EN = "Non operating expense";
    private static final String L_ON_DONCA_ZH = "非流动资产处置损失(万元)";
    private static final String L_ON_DONCA_EN = "Loss on disposal of non current assets";
    private static final String TOTAL_PROFIT_ZH = "利润总额(万元)";
    private static final String TOTAL_PROFIT_EN = "Total profit";
    private static final String IT_ZH = "所得税费用(万元)";
    private static final String IT_EN = "Income tax";
    private static final String NPA_TO_TOOTPC_ZH = "归属于母公司所有者的净利润(万元)";
    private static final String NPA_TO_TOOTPC_EN = "Net profit attributable to the owner of the parent company";
    private static final String NP_OF_TMPBTM_ZH = "被合并方在合并前实现净利润(万元)";
    private static final String NP_OF_TMPBTM_EN = "Net profit of the merged party before the merger";
    private static final String BEPS_ZH = "基本每股收益";
    private static final String BEPS_EN = "Basic earnings per share";
    private static final String DEPS_ZH = "稀释每股收益";
    private static final String DEPS_EN = "Diluted earnings per share";
    private static final String IMPAIRMENT_LOSS_ZH = "资产减值损失(万元)";
    private static final String IMPAIRMENT_LOSS_EN = "Impairment Loss";

    public static final Map<String, Pair<String, String>> PROFIT_INDICATORS_SCHEMA =
            ImmutableMap.<String, Pair<String, String>>builder()
                    .put(TOI_ZH, Pair.of("toi", TOI_EN))
                    .put(REVENUE_ZH, Pair.of("revenue", REVENUE_EN))
                    .put(II_ZH, Pair.of("ii", II_EN))
                    .put(EP_ZH, Pair.of("ep", EP_EN))
                    .put(F_AND_CI_ZH, Pair.of("f_and_ci", F_AND_CI_EN))
                    .put(RESR_ZH, Pair.of("resr", RESR_EN))
                    .put(OOI_ZH, Pair.of("ooi", OOI_EN))
                    .put(TC_ZH, Pair.of("tc", TC_EN))
                    .put(C_OF_GS_ZH, Pair.of("c_of_gs", C_OF_GS_EN))
                    .put(IE_ZH, Pair.of("ie", IE_EN))
                    .put(F_AND_CE_ZH, Pair.of("f_and_ce", F_AND_CE_EN))
                    .put(C_OF_RES_ZH, Pair.of("c_of_res", C_OF_RES_EN))
                    .put(RESEARCH_EXPENSE_ZH, Pair.of("research_expense", RESEARCH_EXPENSE_EN))
                    .put(SURRENDERS_ZH, Pair.of("surrenders", SURRENDERS_EN))
                    .put(NCP_ZH, Pair.of("ncp", NCP_EN))
                    .put(NR_ON_IC_ZH, Pair.of("nr_on_ic", NR_ON_IC_EN))
                    .put(ED_ZH, Pair.of("ed", ED_EN))
                    .put(REINSURANCE_EXPENSE_ZH,
                            Pair.of("reinsurance_expense", REINSURANCE_EXPENSE_EN))
                    .put(OOC_ZH, Pair.of("ooc", OOC_EN))
                    .put(T_AND_S_ZH, Pair.of("t_and_s", T_AND_S_EN))
                    .put(SE_ZH, Pair.of("se", SE_EN))
                    .put(AE_ZH, Pair.of("ae", AE_EN))
                    .put(I_FROM_CIFV_ZH, Pair.of("i_from_cifv", I_FROM_CIFV_EN))
                    .put(I_FROM_I_ZH, Pair.of("i_from_i", I_FROM_I_EN))
                    .put(I_FROM_IIA_AND_JV_ZH, Pair.of("i_from_iia_and_jv", I_FROM_IIA_AND_JV_EN))
                    .put(EG_ZH, Pair.of("eg", EG_EN))
                    .put(G_ON_F_ZH, Pair.of("g_on_f_zh", G_ON_F_EN))
                    .put(DEPOSITS_ZH, Pair.of("deposits", DEPOSITS_EN))
                    .put(SI_ZH, Pair.of("si", SI_EN))
                    .put(OOP_ZH, Pair.of("opp", OOP_EN))
                    .put(OP_ZH, Pair.of("op", OP_EN))
                    .put(NOI_ZH, Pair.of("noi", NOI_EN))
                    .put(NOE_ZH, Pair.of("noe", NOE_EN))
                    .put(L_ON_DONCA_ZH, Pair.of("l_on_donca", L_ON_DONCA_EN))
                    .put(TOTAL_PROFIT_ZH, Pair.of("total_profit", TOTAL_PROFIT_EN))
                    .put(IT_ZH, Pair.of("it", IT_EN))
                    .put(NPA_TO_TOOTPC_ZH, Pair.of("npa_to_tootpc", NPA_TO_TOOTPC_EN))
                    .put(NP_OF_TMPBTM_ZH, Pair.of("np_of_tmpbtm", NP_OF_TMPBTM_EN))
                    .put(BEPS_ZH, Pair.of("bers", BEPS_EN))
                    .put(DEPS_ZH, Pair.of("deps", DEPS_EN))
                    .put(FE_ZH, Pair.of("fe", FE_EN))
                    .put(NP_ZH, Pair.of("np", NP_EN))
                    .put(MI_ZH, Pair.of("mi", MI_EN))
                    .put("未确认投资损失(万元)", Pair.of("uil", UIL_EN))
                    .put(IMPAIRMENT_LOSS_ZH, Pair.of("impairment_loss", IMPAIRMENT_LOSS_EN))
                    .build();

    public static final Map<String, Pair<String, String>> CASH_FLOW_INDICATORS_SCHEMA =
            ImmutableMap.<String, Pair<String, String>>builder()
                    .put("报告日期", Pair.of("datetime", "datetime"))
                    .put(CR_FROM_SG_PS_ZH, Pair.of("cr_from_sg_ps", CR_FROM_SG_PS_EN))
                    .put(NI_IN_CD_DWB_ZH, Pair.of("ni_in_cd_dwb", NI_IN_CD_DWB_EN))
                    .put(NI_IN_BFTCB_ZH, Pair.of("ni_in_bftcb", NI_IN_BFTCB_EN))
                    .put(NI_IN_BFOFI_ZH, Pair.of("ni_in_bfofi", NI_IN_BFOFI_EN))
                    .put(CR_FROM_POOIC_ZH, Pair.of("cr_from_pooic", CR_FROM_POOIC_EN))
                    .put(NCR_FROM_RB_ZH, Pair.of("ncr_from_rb", NCR_FROM_RB_EN))
                    .put(NI_IN_DAIOOTI_ZH, Pair.of("ni_in_daiooti", NI_IN_DAIOOTI_EN))
                    .put(NI_IN_DOTFA_ZH, Pair.of("ni_in_dotfa", NI_IN_DOTFA_EN))
                    .put(CR_FOR_IHCAC_ZH, Pair.of("cr_for_ihcac", CR_FOR_IHCAC_EN))
                    .put(NI_IN_BF_ZH, Pair.of("ni_in_bf", NI_IN_BF_EN))
                    .put(NI_IN_PBF_ZH, Pair.of("ni_in_pbf", NI_IN_PBF_EN))
                    .put(ROT_ZH, Pair.of("rot", ROT_EN))
                    .put(OCRRTOA_ZH, Pair.of("ocrrtoa", OCRRTOA_EN))
                    .put(S_OF_CIFOA_ZH, Pair.of("s_of_cifoa", S_OF_CIFOA_EN))
                    .put(CP_FOR_GAS_ZH, Pair.of("cp_for_gas", CP_FOR_GAS_EN))
                    .put(NI_IN_CLAA_ZH, Pair.of("ni_in_claa", NI_IN_CLAA_EN))
                    .put(NI_IN_DWTCBAOB_ZH, Pair.of("ni_in_dwtcbaob", NI_IN_DWTCBAOB_EN))
                    .put(CP_FOR_CUTOIC_ZH, Pair.of("cp_for_cutoic", CP_FOR_CUTOIC_EN))
                    .put(CP_FOR_IHCAC_ZH, Pair.of("cp_for_ihcac", CP_FOR_IHCAC_EN))
                    .put(CP_FOR_PD_ZH, Pair.of("cp_for_pd", CP_FOR_PD_EN))
                    .put(CP_TO_AND_FOR_E_ZH, Pair.of("cp_to_and_for_e", CP_TO_AND_FOR_E_EN))
                    .put(TP_ZH, Pair.of("tp", TP_EN))
                    .put(OCPR_TO_OA_ZH, Pair.of("ocpr_to_oa", OCPR_TO_OA_EN))
                    .put(SOCO_FROM_OA_ZH, Pair.of("soco_from_oa", SOCO_FROM_OA_EN))
                    .put(NCF_FROM_OA_ZH, Pair.of("ncf_from_oa", NCF_FROM_OA_EN))
                    .put("经营活动产生现金流量净额(万元)", Pair.of("ncf_from_oa", NCF_FROM_OA_EN))
                    .put(CR_FROM_IR_ZH, Pair.of("cr_from_ir", CR_FROM_IR_EN))
                    .put(CR_FROM_II_ZH, Pair.of("cr_from_ii", CR_FROM_II_EN))
                    .put(NCR_FROM_DOFAIAAOLA_ZH,
                            Pair.of("ncr_from_dofaiaaola", NCR_FROM_DOFAIAAOLA_EN))
                    .put(NCR_FROM_DOSAOBU_ZH, Pair.of("ncr_from_dosaobu", NCR_FROM_DOSAOBU_EN))
                    .put(OCRR_TO_IA_ZH, Pair.of("ocrr_to_ia", OCRR_TO_IA_EN))
                    .put(CR_FROM_PAFDR_ZH, Pair.of("cr_from_pafdr", CR_FROM_PAFDR_EN))
                    .put(SOCI_FROM_IA_ZH, Pair.of("soci_from_ia", SOCI_FROM_IA_EN))
                    .put(CP_FOR_AACOFAIAAOLA_ZH,
                            Pair.of("cp_for_aacofaiaaola", CP_FOR_AACOFAIAAOLA_EN))
                    .put(CP_FOR_I_ZH, Pair.of("cp_for_i", CP_FOR_I_EN))
                    .put(NI_IN_PL_ZH, Pair.of("ni_in_pl", NI_IN_PL_EN))
                    .put(NCP_BY_SAOBU_ZH, Pair.of("ncp_by_saobu", NCP_BY_SAOBU_EN))
                    .put(OCPR_TO_IA_ZH, Pair.of("ocpr_to_ia", OCPR_TO_IA_EN))
                    .put(CP_FOR_IOPAFD_ZH, Pair.of("cp_for_iopafd", CP_FOR_IOPAFD_EN))
                    .put(SOCO_FROM_IA_ZH, Pair.of("soco_from_ia", SOCO_FROM_IA_EN))
                    .put(NCF_FROM_IA_ZH, Pair.of("ncf_from_ia", NCF_FROM_IA_EN))
                    .put(CR_FROM_IA_ZH, Pair.of("cr_from_ia", CR_FROM_IA_EN))
                    .put(INCLUDING_CR_FROM_MS_IBD_ZH,
                            Pair.of("including_cr_from_ms_ibs", INCLUDING_CR_FROM_MS_IBD_EN))
                    .put(CR_FROM_B_ZH, Pair.of("cr_from_b", CR_FROM_B_EN))
                    .put(CR_FROM_BI_ZH, Pair.of("cr_from_bi", CR_FROM_BI_EN))
                    .put(CR_FROM_OFA_ZH, Pair.of("cr_from_ofa", CR_FROM_OFA_EN))
                    .put(SOCI_FROM_FA_ZH, Pair.of("soci_from_fa", SOCI_FROM_FA_EN))
                    .put(CP_FOR_DR_ZH, Pair.of("cp_for_dr", CP_FOR_DR_EN))
                    .put(CP_FOR_DODPOI_ZH, Pair.of("cp_for_dodpoi", CP_FOR_DODPOI_EN))
                    .put(INCLUDING_DAPPBSTMS_ZH,
                            Pair.of("including_dappbstms", INCLUDING_DAPPBSTMS_EN))
                    .put(CP_FOR_OFA_ZH, Pair.of("cp_for_ofa", CP_FOR_OFA_EN))
                    .put(SOCR_FROM_FA_ZH, Pair.of("socr_from_fa", SOCR_FROM_FA_EN))
                    .put(NCF_FROM_FA_ZH, Pair.of("ncf_from_fa", NCF_FROM_FA_EN))
                    .put(EOERC_ON_CACE_ZH, Pair.of("eoerc_on_cace", EOERC_ON_CACE_EN))
                    .put(NI_IN_CACE_ZH, Pair.of("ni_in_cace", NI_IN_CACE_EN))
                    .put("现金及现金等价物的净增加额(万元)", Pair.of("ni_in_cace", NI_IN_CACE_EN))
                    .put(ADD_BOFCACE_AT_TBOTP_ZH,
                            Pair.of("add_bofcace_at_tbotp", ADD_BOFCACE_AT_TBOTP_EN))
                    .put(BOCACE_AT_TEOTP_ZH, Pair.of("bocace_at_teotp", BOCACE_AT_TEOTP_EN))
                    .put(NP_ZH, Pair.of("np", NP_EN))
                    .put(MI_ZH, Pair.of("mi", MI_EN))
                    .put(UIL_ZH, Pair.of("uil", UIL_EN))
                    .put("未确认投资损失(万元)", Pair.of("uil", UIL_EN))
                    .put(P_FOR_IOA_ZH, Pair.of("p_for_ioa", P_FOR_IOA_EN))
                    .put(DOFA_DOOAGA_DOPM_ZH, Pair.of("dofa_dooaga_dopm", DOFA_DOOAGA_DOPM_EN))
                    .put(A_OF_IA_ZH, Pair.of("a_of_ia", A_OF_IA_EN))
                    .put(A_OF_LUE_ZH, Pair.of("a_of_lue", A_OF_LUE_EN))
                    .put(D_OF_UE_ZH, Pair.of("d_of_ue", D_OF_UE_EN))
                    .put(I_IN_AE_ZH, Pair.of("i_in_ae", I_IN_AE_EN))
                    .put(LOD_OF_FA_IA_AND_OLA_ZH,
                            Pair.of("lod_of_fa_ia_and_ola", LOD_OF_FA_IA_AND_OLA_EN))
                    .put(L_ON_ROFA_ZH, Pair.of("l_on_rofa", L_ON_ROFA_EN))
                    .put(L_FROM_CIFY_ZH, Pair.of("l_from_cifv", L_FROM_CIFY_EN))
                    .put(I_IN_DI_ZH, Pair.of("i_in_di", I_IN_DI_EN))
                    .put(AL_ZH, Pair.of("al", AL_EN))
                    .put(FE_ZH, Pair.of("fe", FE_EN))
                    .put(IL_ZH, Pair.of("il", IL_EN))
                    .put(D_OF_DITA_ZH, Pair.of("d_of_dita", D_OF_DITA_EN))
                    .put(I_IN_DITL_ZH, Pair.of("i_in_ditl", I_IN_DITL_EN))
                    .put(D_IN_I_ZH, Pair.of("d_in_i", D_IN_I_EN))
                    .put(D_OF_OR_ZH, Pair.of("d_of_or", D_OF_OR_EN))
                    .put(I_IN_OP_ZH, Pair.of("i_in_op", I_IN_OP_EN))
                    .put(D_OF_CBUF_ZH, Pair.of("d_of_cbuf", D_OF_CBUF_EN))
                    .put(I_OF_SBNCP_ZH, Pair.of("i_of_sbncp", I_OF_SBNCP_EN))
                    .put(OTHERS_ZH, Pair.of("others", OTHERS_EN))
                    .put(D_TO_C_ZH, Pair.of("d_to_c", D_TO_C_EN))
                    .put(CBD_WITHIN_OY_ZH, Pair.of("cbd_within_oy", CBD_WITHIN_OY_EN))
                    .put(FAUFL_ZH, Pair.of("faufl", FAUFL_EN))
                    .put(CB_OF_C_ZH, Pair.of("cb_of_c", CB_OF_C_EN))
                    .put(OB_OF_C_ZH, Pair.of("ob_of_c", OB_OF_C_EN))
                    .put(CB_OF_CE_ZH, Pair.of("cb_of_ce", CB_OF_CE_EN))
                    .put(OB_OF_CE_ZH, Pair.of("ob_of_ce", OB_OF_CE_EN))
                    .build();

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

    @SeriesProperty(description = CR_FROM_SG_PS_ZH + " " + CR_FROM_SG_PS_EN)
    private double cr_from_sg_ps;

    @SeriesProperty(description = NI_IN_CD_DWB_ZH + " " + NI_IN_BF_EN)
    private double ni_in_cd_dwb;

    @SeriesProperty(description = NI_IN_BFTCB_ZH + " " + NI_IN_BFTCB_EN)
    private double ni_in_bftcb;

    @SeriesProperty(description = NI_IN_BFOFI_ZH + " " + NI_IN_BFOFI_EN)
    private double ni_in_bfofi;

    @SeriesProperty(description = CR_FROM_POOIC_ZH + " " + CR_FROM_POOIC_EN)
    private double cr_from_pooic;

    @SeriesProperty(description = NCR_FROM_RB_ZH + " " + NCR_FROM_RB_EN)
    private double ncr_from_rb;

    @SeriesProperty(description = NI_IN_DAIOOTI_ZH + " " + NI_IN_DAIOOTI_EN)
    private double ni_in_daiooti;

    @SeriesProperty(description = NI_IN_DOTFA_ZH + " " + NI_IN_DOTFA_EN)
    private double ni_in_dotfa;

    @SeriesProperty(description = CR_FOR_IHCAC_ZH + " " + CR_FOR_IHCAC_EN)
    private double cr_for_ihcac;

    @SeriesProperty(description = NI_IN_BF_ZH + " " + NI_IN_BF_EN)
    private double ni_in_bf;

    @SeriesProperty(description = NI_IN_PBF_ZH + " " + NI_IN_PBF_EN)
    private double ni_in_pbf;

    @SeriesProperty(description = ROT_ZH + " " + ROT_EN)
    private double rot;

    @SeriesProperty(description = OCRRTOA_ZH + " " + OCRRTOA_EN)
    private double ocrrtoa;

    @SeriesProperty(description = S_OF_CIFOA_ZH + " " + S_OF_CIFOA_EN)
    private double s_of_cifoa;

    @SeriesProperty(description = CP_FOR_GAS_ZH + " " + CP_FOR_GAS_EN)
    private double cp_for_gas;

    @SeriesProperty(description = NI_IN_CLAA_ZH + " " + NI_IN_CLAA_EN)
    private double ni_in_claa;

    @SeriesProperty(description = NI_IN_DWTCBAOB_ZH + " " + NI_IN_DWTCBAOB_EN)
    private double ni_in_dwtcbaob;

    @SeriesProperty(description = CP_FOR_CUTOIC_ZH + " " + CP_FOR_CUTOIC_EN)
    private double cp_for_cutoic;

    @SeriesProperty(description = CP_FOR_IHCAC_ZH + " " + CP_FOR_IHCAC_EN)
    private double cp_for_ihcac;

    @SeriesProperty(description = CP_FOR_PD_ZH + " " + CP_FOR_PD_EN)
    private double cp_for_pd;

    @SeriesProperty(description = CP_TO_AND_FOR_E_ZH + " " + CP_TO_AND_FOR_E_EN)
    private double cp_to_and_for_e;

    @SeriesProperty(description = TP_ZH + " " + TP_EN)
    private double tp;

    @SeriesProperty(description = OCPR_TO_OA_ZH + " " + OCPR_TO_OA_EN)
    private double ocpr_to_oa;

    @SeriesProperty(description = SOCO_FROM_OA_ZH + " " + SOCO_FROM_OA_EN)
    private double soco_from_oa;

    @SeriesProperty(description = NCF_FROM_OA_ZH + " " + NCF_FROM_OA_EN)
    private double ncf_from_oa;

    @SeriesProperty(description = CR_FROM_IR_ZH + " " + CR_FROM_IR_EN)
    private double cr_from_ir;

    @SeriesProperty(description = CR_FROM_II_ZH + " " + CR_FROM_II_EN)
    private double cr_from_ii;

    @SeriesProperty(description = NCR_FROM_DOFAIAAOLA_ZH + " " + NCR_FROM_DOFAIAAOLA_EN)
    private double ncr_from_dofaiaaola;

    @SeriesProperty(description = NCR_FROM_DOSAOBU_ZH + " " + NCR_FROM_DOSAOBU_EN)
    private double ncr_from_dosaobu;

    @SeriesProperty(description = OCRR_TO_IA_ZH + " " + OCRR_TO_IA_EN)
    private double ocrr_to_ia;

    @SeriesProperty(description = CR_FROM_PAFDR_ZH + " " + CR_FROM_PAFDR_EN)
    private double cr_from_pafdr;

    @SeriesProperty(description = SOCI_FROM_IA_ZH + " " + SOCI_FROM_IA_EN)
    private double soci_from_ia;

    @SeriesProperty(description = CP_FOR_AACOFAIAAOLA_ZH + " " + CP_FOR_AACOFAIAAOLA_EN)
    private double cp_for_aacofaiaaola;

    @SeriesProperty(description = CP_FOR_I_ZH + " " + CP_FOR_I_EN)
    private double cp_for_i;

    @SeriesProperty(description = NI_IN_PL_ZH + " " + NI_IN_PL_EN)
    private double ni_in_pl;

    @SeriesProperty(description = NCP_BY_SAOBU_ZH + " " + NCP_BY_SAOBU_EN)
    private double ncp_by_saobu;

    @SeriesProperty(description = OCPR_TO_IA_ZH + " " + OCPR_TO_IA_EN)
    private double ocpr_to_ia;

    @SeriesProperty(description = CP_FOR_IOPAFD_ZH + " " + CP_FOR_IOPAFD_EN)
    private double cp_for_iopafd;

    @SeriesProperty(description = SOCO_FROM_IA_ZH + " " + SOCO_FROM_IA_EN)
    private double soco_from_ia;

    @SeriesProperty(description = NCF_FROM_IA_ZH + " " + NCF_FROM_IA_EN)
    private double ncf_from_ia;

    @SeriesProperty(description = CR_FROM_IA_ZH + " " + CR_FROM_IA_EN)
    private double cr_from_ia;

    @SeriesProperty(description = INCLUDING_CR_FROM_MS_IBD_ZH + " " + INCLUDING_CR_FROM_MS_IBD_EN)
    private double including_cr_from_ms_ibs;

    @SeriesProperty(description = CR_FROM_B_ZH + " " + CR_FROM_B_EN)
    private double cr_from_b;

    @SeriesProperty(description = CR_FROM_BI_ZH + " " + CR_FROM_BI_EN)
    private double cr_from_bi;

    @SeriesProperty(description = CR_FROM_OFA_ZH + " " + CR_FROM_OFA_EN)
    private double cr_from_ofa;

    @SeriesProperty(description = SOCI_FROM_FA_ZH + " " + SOCI_FROM_FA_EN)
    private double soci_from_fa;

    @SeriesProperty(description = CP_FOR_DR_ZH + " " + CP_FOR_DR_EN)
    private double cp_for_dr;

    @SeriesProperty(description = CP_FOR_DODPOI_ZH + " " + CP_FOR_DODPOI_EN)
    private double cp_for_dodpoi;

    @SeriesProperty(description = INCLUDING_DAPPBSTMS_ZH + " " + INCLUDING_DAPPBSTMS_EN)
    private double including_dappbstms;

    @SeriesProperty(description = CP_FOR_OFA_ZH + " " + CP_FOR_OFA_EN)
    private double cp_for_ofa;

    @SeriesProperty(description = SOCR_FROM_FA_ZH + " " + SOCR_FROM_FA_EN)
    private double socr_from_fa;

    @SeriesProperty(description = NCF_FROM_FA_ZH + " " + NCF_FROM_FA_EN)
    private double ncf_from_fa;

    @SeriesProperty(description = EOERC_ON_CACE_ZH + " " + EOERC_ON_CACE_EN)
    private double eoerc_on_cace;

    @SeriesProperty(description = NI_IN_CACE_ZH + " " + NI_IN_CACE_EN)
    private double ni_in_cace;

    @SeriesProperty(description = ADD_BOFCACE_AT_TBOTP_ZH + " " + ADD_BOFCACE_AT_TBOTP_EN)
    private double add_bofcace_at_tbotp;

    @SeriesProperty(description = BOCACE_AT_TEOTP_ZH + " " + BOCACE_AT_TEOTP_EN)
    private double bocace_at_teotp;

    @SeriesProperty(description = NP_ZH + " " + NP_EN)
    private double np;

    @SeriesProperty(description = MI_ZH + " " + MI_EN)
    private double mi;

    @SeriesProperty(description = UIL_ZH + " " + UIL_EN)
    private double uil;

    @SeriesProperty(description = P_FOR_IOA_ZH + " " + P_FOR_IOA_EN)
    private double p_for_ioa;

    @SeriesProperty(description = DOFA_DOOAGA_DOPM_ZH + " " + DOFA_DOOAGA_DOPM_EN)
    private double dofa_dooaga_dopm;

    @SeriesProperty(description = A_OF_IA_ZH + " " + A_OF_IA_EN)
    private double a_of_ia;

    @SeriesProperty(description = A_OF_LUE_ZH + " " + A_OF_LUE_EN)
    private double a_of_lue;

    @SeriesProperty(description = D_OF_UE_ZH + " " + D_OF_UE_EN)
    private double d_of_ue;

    @SeriesProperty(description = I_IN_AE_ZH + " " + I_IN_AE_EN)
    private double i_in_ae;

    @SeriesProperty(description = LOD_OF_FA_IA_AND_OLA_ZH + " " + LOD_OF_FA_IA_AND_OLA_EN)
    private double lod_of_fa_ia_and_ola;

    @SeriesProperty(description = L_ON_ROFA_ZH + " " + L_ON_ROFA_EN)
    private double l_on_rofa;

    @SeriesProperty(description = L_FROM_CIFY_ZH + " " + L_FROM_CIFY_EN)
    private double l_from_cifv;

    @SeriesProperty(description = I_IN_DI_ZH + " " + I_IN_DI_EN)
    private double i_in_di;

    @SeriesProperty(description = AL_ZH + " " + AL_EN)
    private double al;

    @SeriesProperty(description = FE_ZH + " " + FE_EN)
    private double fe;

    @SeriesProperty(description = IL_ZH + " " + IL_EN)
    private double il;

    @SeriesProperty(description = D_OF_DITA_ZH + " " + D_OF_DITA_EN)
    private double d_of_dita;

    @SeriesProperty(description = I_IN_DITL_ZH + " " + I_IN_DITL_EN)
    private double i_in_ditl;

    @SeriesProperty(description = D_IN_I_ZH + " " + D_IN_I_EN)
    private double d_in_i;

    @SeriesProperty(description = D_OF_OR_ZH + " " + D_OF_OR_EN)
    private double d_of_or;

    @SeriesProperty(description = I_IN_OP_ZH + " " + I_IN_OP_EN)
    private double i_in_op;

    @SeriesProperty(description = D_OF_CBUF_ZH + " " + D_OF_CBUF_EN)
    private double d_of_cbuf;

    @SeriesProperty(description = I_OF_SBNCP_ZH + " " + I_OF_SBNCP_EN)
    private double i_of_sbncp;

    @SeriesProperty(description = OTHERS_ZH + " " + OTHERS_EN)
    private double others;

    @SeriesProperty(description = D_TO_C_ZH + " " + D_TO_C_EN)
    private double d_to_c;

    @SeriesProperty(description = CBD_WITHIN_OY_ZH + " " + CBD_WITHIN_OY_EN)
    private double cbd_within_oy;

    @SeriesProperty(description = FAUFL_ZH + " " + FAUFL_EN)
    private double faufl;

    @SeriesProperty(description = CB_OF_C_ZH + " " + CB_OF_C_EN)
    private double cb_of_c;

    @SeriesProperty(description = OB_OF_C_ZH + " " + OB_OF_C_EN)
    private double ob_of_c;

    @SeriesProperty(description = CB_OF_CE_ZH + " " + CB_OF_CE_EN)
    private double cb_of_ce;

    @SeriesProperty(description = OB_OF_CE_ZH + " " + OB_OF_CE_EN)
    private double ob_of_ce;

    /**
     * Profit Indicators
     */
    @SeriesProperty(description = TOI_ZH + " " + TOI_EN)
    private double toi;
    @SeriesProperty(description = REVENUE_ZH + " " + REVENUE_EN)
    private double revenue;
    @SeriesProperty(description = II_ZH + " " + II_EN)
    private double ii;
    @SeriesProperty(description = EP_ZH + " " + EP_EN)
    private double ep;
    @SeriesProperty(description = F_AND_CI_ZH + " " + F_AND_CI_EN)
    private double f_and_ci;
    @SeriesProperty(description = RESR_ZH + " " + RESR_EN)
    private double resr;
    @SeriesProperty(description = OOI_ZH + " " + OOI_EN)
    private double ooi;
    @SeriesProperty(description = TC_ZH + " " + TC_EN)
    private double tc;
    @SeriesProperty(description = C_OF_GS_ZH + " " + C_OF_GS_EN)
    private double c_of_gs;
    @SeriesProperty(description = IE_ZH + " " + IE_EN)
    private double ie;
    @SeriesProperty(description = F_AND_CE_ZH + " " + F_AND_CE_EN)
    private double f_and_ce;
    @SeriesProperty(description = C_OF_RES_ZH + " " + C_OF_RES_EN)
    private double c_of_res;
    @SeriesProperty(description = RESEARCH_EXPENSE_ZH + " " + RESEARCH_EXPENSE_EN)
    private double research_expense;
    @SeriesProperty(description = SURRENDERS_ZH + " " + SURRENDERS_EN)
    private double surrenders;
    @SeriesProperty(description = NCP_ZH + " " + NCP_EN)
    private double ncp;
    @SeriesProperty(description = NR_ON_IC_ZH + " " + NR_ON_IC_EN)
    private double nr_on_ic;
    @SeriesProperty(description = ED_ZH + " " + ED_EN)
    private double ed;
    @SeriesProperty(description = REINSURANCE_EXPENSE_ZH + " " + REINSURANCE_EXPENSE_EN)
    private double reinsurance_expense;
    @SeriesProperty(description = OOC_ZH + " " + OOC_EN)
    private double ooc;
    @SeriesProperty(description = T_AND_S_ZH + " " + T_AND_S_EN)
    private double t_and_s;
    @SeriesProperty(description = SE_ZH + " " + SE_EN)
    private double se;
    @SeriesProperty(description = AE_ZH + " " + AE_EN)
    private double ae;
    @SeriesProperty(description = I_FROM_CIFV_ZH + " " + I_FROM_CIFV_EN)
    private double i_from_cifv;
    @SeriesProperty(description = I_FROM_I_ZH + " " + I_FROM_I_EN)
    private double i_from_i;
    @SeriesProperty(description = I_FROM_IIA_AND_JV_ZH + " " + I_FROM_IIA_AND_JV_EN)
    private double i_from_iia_and_jv;
    @SeriesProperty(description = EG_ZH + " " + EG_EN)
    private double eg;
    @SeriesProperty(description = G_ON_F_ZH + " " + G_ON_F_EN)
    private double g_on_f_zh;
    @SeriesProperty(description = DEPOSITS_ZH + " " + DEPOSITS_EN)
    private double deposits;
    @SeriesProperty(description = SI_ZH + " " + SI_EN)
    private double si;
    @SeriesProperty(description = OOP_ZH + " " + OOP_EN)
    private double opp;
    @SeriesProperty(description = OP_ZH + " " + OP_EN)
    private double op;
    @SeriesProperty(description = NOI_ZH + " " + NOI_EN)
    private double noi;
    @SeriesProperty(description = NOE_ZH + " " + NOE_EN)
    private double noe;
    @SeriesProperty(description = L_ON_DONCA_ZH + " " + L_ON_DONCA_EN)
    private double l_on_donca;
    @SeriesProperty(description = TOTAL_PROFIT_ZH + " " + TOTAL_PROFIT_EN)
    private double total_profit;
    @SeriesProperty(description = IT_ZH + " " + IT_EN)
    private double it;
    @SeriesProperty(description = NPA_TO_TOOTPC_ZH + " " + NPA_TO_TOOTPC_EN)
    private double npa_to_tootpc;
    @SeriesProperty(description = NP_OF_TMPBTM_ZH + " " + NP_OF_TMPBTM_EN)
    private double np_of_tmpbtm;
    @SeriesProperty(description = BEPS_ZH + " " + BEPS_EN)
    private double bers;
    @SeriesProperty(description = DEPS_ZH + " " + DEPS_EN)
    private double deps;
    @SeriesProperty(description = IMPAIRMENT_LOSS_ZH + IMPAIRMENT_LOSS_EN)
    private double impairment_loss;

    @Override
    protected Map<String, String> generateTags() {
        return null;
    }
}
