package org.xmuyoo.blueberry.collect.collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.xmuyoo.blueberry.collect.domains.ConvertibleBondCode;
import org.xmuyoo.blueberry.collect.domains.DataSchema;
import org.xmuyoo.blueberry.collect.http.HttpClient;
import org.xmuyoo.blueberry.collect.http.Request;
import org.xmuyoo.blueberry.collect.storage.PgClient;

@Slf4j
public class ConvertibleBondCodeCollector extends BasicCollector<ConvertibleBondCode> {

    private static final String CONVERTIBLE_BOND_CODE = "convertible_bond_code";
    private static final String ACTIVE_PARAMETERS_BODY = Joiner.on("&").join(
            ImmutableSet.<String>builder()
                    .add("is_search=N")
                    .add(URLEncoder.encode("market_cd[]", StandardCharsets.UTF_8) + "=shmb")
                    .add(URLEncoder.encode("market_cd[]", StandardCharsets.UTF_8) + "=shkc")
                    .add(URLEncoder.encode("market_cd[]", StandardCharsets.UTF_8) + "=szmb")
                    .add(URLEncoder.encode("market_cd[]", StandardCharsets.UTF_8) + "=szkc")
                    .add("bType=C")
                    .add("listed=Y")
                    .add("qflag=N")
                    .add("rp=50")
                    .add("page=1")
                    .build()
    );

    private final String delistedUrl;
    private final String activeUrl;
    private final String cookie;
    private final HttpClient http;

    public ConvertibleBondCodeCollector(PgClient storage, HttpClient http) {
        super(CONVERTIBLE_BOND_CODE, storage, ConvertibleBondCode.class);
        this.http = http;
        this.delistedUrl = "http://www.jisilu.cn/data/cbnew/delisted/?___jsl=LST___t=" + System.currentTimeMillis();
        this.activeUrl = "https://www.jisilu.cn/data/cbnew/cb_list_new/?___jsl=LST___t=" + System.currentTimeMillis();

        Config config = ConfigFactory.load("jisilu");
        this.cookie = config.getString("cookie");
    }

    @Override
    protected boolean needCreateEntityTable() {
        return true;
    }

    @Override
    protected boolean collect() {
        try {
            log.info("Start collecting active code list");
            collectActiveList();
            log.info("Start collecting delisted code list");
            collectDeliestedList();
            return true;
        } catch (Exception e) {
            log.error("Failed to collect convertible bond code list", e);
            return false;
        }
    }

    @Override
    protected List<DataSchema> getDataSchemaList() {
        return toSchemaList(ConvertibleBondCode.class);
    }

    private void collectActiveList() throws Exception {
        Request request = new Request();
        request.url(this.activeUrl);
        request.method(HttpMethod.POST);
        request.headers(ImmutableMap.of("Cookie", this.cookie));
        request.body(ACTIVE_PARAMETERS_BODY.getBytes(StandardCharsets.UTF_8));

        List<ConvertibleBondCode> activeCodeList = http.sync(request, cbcResponse -> {
            List<ConvertibleBondCode> codeList = null;
            if (null != cbcResponse.rows() && !cbcResponse.rows().isEmpty()) {
                codeList = cbcResponse.rows()
                                      .stream()
                                      .map(row -> {
                                          Cell cell = row.cell();
                                          if (null == cell) {
                                              return null;
                                          }

                                          ConvertibleBondCode code = new ConvertibleBondCode();
                                          code.code(cell.bondId());
                                          code.name(cell.bondName());
                                          code.active(true);
                                          code.delisted(false);
                                          code.stockCode(cell.stockId());
                                          code.stockName(cell.stockName());
                                          code.bondRating(cell.ratingCd());
                                          code.conversionPrice(cell.convertPrice());
                                          code.conversionValue(cell.convertValue());
                                          code.ytmRt(cell.ytmRt());
                                          code.convertAmtRatio(cell.convertAmtRatio());
                                          code.adjCnt(cell.adjCnt());
                                          code.expireDate("20" + cell.shortMaturityDt());
                                          code.forceRedemptionTriggerPrice(cell.forceRedeemPrice());
                                          code.resaleTriggerPrice(cell.putConvertPrice());

                                          return code;
                                      })
                                      .filter(Objects::nonNull)
                                      .collect(Collectors.toList());
            }

            return codeList;
        }, ConvertibleBondCodeResponse.class);

        if (null != activeCodeList) {
            storage.saveOrUpdate(activeCodeList, ConvertibleBondCode.class);
        }
    }

    private void collectDeliestedList() throws Exception {
        Request request = new Request();
        request.url(this.delistedUrl);
        request.method(HttpMethod.POST);
        request.headers(ImmutableMap.of("Cookie", this.cookie));
        request.body("".getBytes(StandardCharsets.UTF_8));

        List<ConvertibleBondCode> delistedCodeList = http.sync(request, cbcResponse -> {
            List<ConvertibleBondCode> codeList = null;
            if (null != cbcResponse.rows() && !cbcResponse.rows().isEmpty()) {
                codeList = cbcResponse.rows()
                                      .stream()
                                      .map(row -> {
                                          Cell cell = row.cell();
                                          if (null == cell) {
                                              return null;
                                          }
                                          ConvertibleBondCode code = new ConvertibleBondCode();
                                          code.code(cell.bondId());
                                          code.name(cell.bondName());
                                          code.marketCd(cell.marketCd());
                                          code.active(false);
                                          code.delisted(true);
                                          code.stockCode(cell.stockId());
                                          code.stockName(cell.stockName());
                                          code.lastPrice(cell.price());
                                          code.lastingYears(cell.listedYears());
                                          code.delistReason(cell.delistNotes());
                                          code.delistDate(cell.delistDate());

                                          return code;
                                      })
                                      .filter(Objects::nonNull)
                                      .collect(Collectors.toList());
            }

            return codeList;
        }, ConvertibleBondCodeResponse.class);

        if (null != delistedCodeList) {
            storage.saveOrUpdate(delistedCodeList, ConvertibleBondCode.class);
        }
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ConvertibleBondCodeResponse {

        @JsonProperty
        private int page;

        @JsonProperty
        private List<BondCode> rows;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class BondCode {

        @JsonProperty
        private String id;

        @JsonProperty
        private Cell cell;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Cell {

        @JsonProperty("bond_id")
        private String bondId;

        @JsonProperty("bond_nm")
        private String bondName;

        @JsonProperty("market_cd")
        private String marketCd;

        @JsonProperty("price")
        private Double price;

        @JsonProperty("stock_id")
        private String stockId;

        @JsonProperty("stock_nm")
        private String stockName;

        @JsonProperty("rating_cd")
        private String ratingCd;

        @JsonProperty("short_maturity_dt")
        private String shortMaturityDt;

        // 强赎触发价
        @JsonProperty("force_redeem_price")
        private Double forceRedeemPrice;

        // 回收触发价
        @JsonProperty("put_convert_price")
        private Double putConvertPrice;

        // 到期税前收益
        @JsonProperty("ytm_rt")
        private Double ytmRt;

        // 转股价
        @JsonProperty("convert_price")
        private Double convertPrice;

        // 转股价值
        @JsonProperty("convert_value")
        private Double convertValue;

        // 转债占比
        @JsonProperty("convert_amt_ratio")
        private Double convertAmtRatio;

        // 下修转股价次数
        @JsonProperty("adj_cnt")
        private Double adjCnt;

        @JsonProperty("delist_notes")
        private String delistNotes;

        @JsonProperty("listed_years")
        private Double listedYears;

        @JsonProperty("delist_dt")
        private String delistDate;
    }
}
