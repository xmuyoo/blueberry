package org.xmuyoo.blueberry.collect.domains;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.xmuyoo.blueberry.collect.storage.Persistent;
import org.xmuyoo.blueberry.collect.storage.PersistentProperty;
import org.xmuyoo.blueberry.collect.storage.ValueType;

@Getter
@Setter
@ToString
@Persistent(name = "convertible_bond_code")
public class ConvertibleBondCode {

    @PersistentProperty(name = "code", valueType = ValueType.Text, description = "可转债代码", isUnique = true)
    private String code;

    @PersistentProperty(name = "name", valueType = ValueType.Text, description = "可转债名称")
    private String name;

    @PersistentProperty(name = "stock_code", valueType = ValueType.Text, description = "正股代码")
    private String stockCode;

    @PersistentProperty(name = "stock_name", valueType = ValueType.Text, description = "正股名称")
    private String stockName;

    @PersistentProperty(name = "active", valueType = ValueType.Boolean, description = "是否上市")
    private Boolean active;

    @PersistentProperty(name = "bond_rating", valueType = ValueType.Text, description = "可转债评级")
    private String bondRating;

    @PersistentProperty(name = "conversion_price", valueType = ValueType.Number, description = "转股价格", updateWhenConflict = true)
    private Double conversionPrice;

    @PersistentProperty(name = "conversion_value", valueType = ValueType.Number, description = "转股价值", updateWhenConflict = true)
    private Double conversionValue;

    @PersistentProperty(name = "ytm_rt", valueType = ValueType.Number, description = "到期税前收益")
    private Double ytmRt;

    @PersistentProperty(name = "convert_amt_ratio", valueType = ValueType.Number, description = "转债占比", updateWhenConflict = true)
    private Double convertAmtRatio;

    @PersistentProperty(name = "adj_cnt", valueType = ValueType.Number, description = "下修转股价次数", updateWhenConflict = true)
    private Double adjCnt;

    @PersistentProperty(name = "resale_trigger_price", valueType = ValueType.Number, description = "回售触发价", updateWhenConflict = true)
    private Double resaleTriggerPrice;

    @PersistentProperty(name = "frt_price", valueType = ValueType.Number, description = "Strong Redemption Trigger Price，强赎触发价", updateWhenConflict = true)
    private Double forceRedemptionTriggerPrice;

    @PersistentProperty(name = "expire_date", valueType = ValueType.Text, description = "到期时间")
    private String expireDate;

    @PersistentProperty(name = "delisted", valueType = ValueType.Boolean, description = "是否退市", updateWhenConflict = true)
    private Boolean delisted;

    @PersistentProperty(name = "last_price", valueType = ValueType.Number, description = "最后交易价格，针对已退市转债")
    private Double lastPrice;

    @PersistentProperty(name = "lasting_years", valueType = ValueType.Number, description = "存续年限")
    private Double lastingYears;

    @PersistentProperty(name = "delist_reason", valueType = ValueType.Text, description = "退市原因")
    private String delistReason;

    @PersistentProperty(name = "delist_date", valueType = ValueType.Text, description = "退市日期")
    private String delistDate;
}
