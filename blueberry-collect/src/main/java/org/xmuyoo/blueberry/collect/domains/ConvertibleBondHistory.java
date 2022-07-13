package org.xmuyoo.blueberry.collect.domains;

import lombok.Getter;
import lombok.Setter;
import org.xmuyoo.blueberry.collect.storage.Persistent;
import org.xmuyoo.blueberry.collect.storage.PersistentProperty;
import org.xmuyoo.blueberry.collect.storage.UniqueConstraint;

import static org.xmuyoo.blueberry.collect.storage.ValueType.*;

@Getter
@Setter
@Persistent(name = "convertible_bond_history",
        uniqueConstraints = {@UniqueConstraint(value = {"code", "record_time"})})
public class ConvertibleBondHistory {

    @PersistentProperty(name = "code", valueType = Text, description = "可转债代码")
    private String code;

    @PersistentProperty(name = "record_time", valueType = BigInt, description = "日期时间戳")
    private Long recordTime;

    @PersistentProperty(name = "ytm_rt", valueType = Double, description = "到期税前收益率")
    private Double ytmRt;
    @PersistentProperty(name = "premium_rt", valueType = Double, description = "转股溢价率")
    private Double premiumRt;
    @PersistentProperty(name = "convert_value", valueType = Double, description = "转股价值")
    private Double convertValue;
    @PersistentProperty(name = "price", valueType = Double, description = "价格")
    private Double price;
    @PersistentProperty(name = "volume", valueType = BigInt, description = "成交额（万元）")
    private Double volume;
    @PersistentProperty(name = "stock_volume", valueType = BigInt, description = "成交量")
    private Long stockVolume;
    @PersistentProperty(name = "curr_iss_amt", valueType = Double, description = "剩余规模（亿元）")
    private Double currIssAmt;
    @PersistentProperty(name = "turnover_rt", valueType = Double, description = "换手率")
    private Double turnoverRt;
}
