package org.xmuyoo.blueberry.collect.domains;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.xmuyoo.blueberry.collect.storage.Persistent;
import org.xmuyoo.blueberry.collect.storage.PersistentProperty;
import org.xmuyoo.blueberry.collect.storage.UniqueConstraint;

import static org.xmuyoo.blueberry.collect.storage.ValueType.*;

@Getter
@Setter
@ToString
@Persistent(name = "stock_k_line", uniqueConstraints = {@UniqueConstraint(value = {"code", "record_time"})})
public class StockKLine {

    @PersistentProperty(name = "code", valueType = Text, description = "代码")
    private String code;

    @PersistentProperty(name = "name", valueType = Text, description = "名称")
    private String name;

    @PersistentProperty(name = "record_time", valueType = BigInt, description = "日期时间戳")
    private Long recordTime;

    @PersistentProperty(name = "volume", valueType = BigInt, description = "成交量")
    private Long volume;

    @PersistentProperty(name = "open", valueType = Double, description = "开盘价")
    private Double open;

    @PersistentProperty(name = "close", valueType = Double, description = "收盘价")
    private Double close;

    @PersistentProperty(name = "high", valueType = Double, description = "最高")
    private Double high;

    @PersistentProperty(name = "low", valueType = Double, description = "最低")
    private Double low;

    @PersistentProperty(name = "chg", valueType = Double, description = "涨跌额")
    private Double chg;

    @PersistentProperty(name = "percent", valueType = Double, description = "涨跌幅")
    private Double percent;

    @PersistentProperty(name = "turn_overrate", valueType = Double, description = "换手率")
    private Double turnOverrate;

    @PersistentProperty(name = "amount", valueType = Double, description = "成交额")
    private Double amount;

    @PersistentProperty(name = "volume_post", valueType = Double, description = "盘后成交量（股）")
    private Long volumePost;

    @PersistentProperty(name = "amount_post", valueType = Double, description = "盘后成交额")
    private Double amountPost;

    @PersistentProperty(name = "pe", valueType = Double, description = "市盈率(TTM)")
    private Double pe;

    @PersistentProperty(name = "pb", valueType = Double, description = "市净率")
    private Double pb;

    @PersistentProperty(name = "ps", valueType = Double, description = "")
    private Double ps;

    @PersistentProperty(name = "pcf", valueType = Double, description = "")
    private Double pcf;

    @PersistentProperty(name = "market_capital", valueType = Double, description = "总市值")
    private Long marketCapital;

    @PersistentProperty(name = "balance", valueType = Double, description = "")
    private Double balance;

    @PersistentProperty(name = "hold_volume_cn", valueType = Double, description = "")
    private Double holdVolumeCn;

    @PersistentProperty(name = "hold_ratio_cn", valueType = Double, description = "")
    private Double holdRatioCn;

    @PersistentProperty(name = "net_volume_cn", valueType = Double, description = "")
    private Double netVolumeCn;

    @PersistentProperty(name = "hold_volume_hk", valueType = Double, description = "")
    private Double holdVolumeHk;

    @PersistentProperty(name = "hold_ratio_hk", valueType = Double, description = "")
    private Double holdRatioHk;

    @PersistentProperty(name = "net_volume_hk", valueType = Double, description = "")
    private Double netVolumeHk;
}
