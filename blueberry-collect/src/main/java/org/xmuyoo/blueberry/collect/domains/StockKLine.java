package org.xmuyoo.blueberry.collect.domains;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.xmuyoo.blueberry.collect.storage.Persistent;
import org.xmuyoo.blueberry.collect.storage.PersistentProperty;
import org.xmuyoo.blueberry.collect.storage.UniqueConstraint;

import static org.xmuyoo.blueberry.collect.storage.ValueType.Number;
import static org.xmuyoo.blueberry.collect.storage.ValueType.Text;

@Getter
@Setter
@ToString
@Persistent(name = "stock_k_line", uniqueConstraints = {@UniqueConstraint(value = {"code", "record_time"})})
public class StockKLine {

    @PersistentProperty(name = "code", valueType = Text, description = "代码")
    private String code;

    @PersistentProperty(name = "name", valueType = Text, description = "名称")
    private String name;

    @PersistentProperty(name = "record_time", valueType = Number, description = "日期时间戳")
    private Long recordTime;

    @PersistentProperty(name = "volume", valueType = Number, description = "成交量")
    private Long volume;

    @PersistentProperty(name = "open", valueType = Number, description = "开盘价")
    private Double open;

    @PersistentProperty(name = "close", valueType = Number, description = "收盘价")
    private Double close;

    @PersistentProperty(name = "high", valueType = Number, description = "最高")
    private Double high;

    @PersistentProperty(name = "low", valueType = Number, description = "最低")
    private Double low;

    @PersistentProperty(name = "chg", valueType = Number, description = "涨跌额")
    private Double chg;

    @PersistentProperty(name = "percent", valueType = Number, description = "涨跌幅")
    private Double percent;

    @PersistentProperty(name = "turn_overrate", valueType = Number, description = "换手率")
    private Double turnOverrate;

    @PersistentProperty(name = "amount", valueType = Number, description = "成交额")
    private Double amount;

    @PersistentProperty(name = "volume_post", valueType = Number, description = "盘后成交量（股）")
    private Long volumePost;

    @PersistentProperty(name = "amount_post", valueType = Number, description = "盘后成交额")
    private Double amountPost;

    @PersistentProperty(name = "pe", valueType = Number, description = "市盈率(TTM)")
    private Double pe;

    @PersistentProperty(name = "pb", valueType = Number, description = "市净率")
    private Double pb;

    @PersistentProperty(name = "ps", valueType = Number, description = "")
    private Double ps;

    @PersistentProperty(name = "pcf", valueType = Number, description = "")
    private Double pcf;

    @PersistentProperty(name = "market_capital", valueType = Number, description = "总市值")
    private Long marketCapital;

    @PersistentProperty(name = "balance", valueType = Number, description = "")
    private Double balance;

    @PersistentProperty(name = "hold_volume_cn", valueType = Number, description = "")
    private Double holdVolumeCn;

    @PersistentProperty(name = "hold_ratio_cn", valueType = Number, description = "")
    private Double holdRatioCn;

    @PersistentProperty(name = "net_volume_cn", valueType = Number, description = "")
    private Double netVolumeCn;

    @PersistentProperty(name = "hold_volume_hk", valueType = Number, description = "")
    private Double holdVolumeHk;

    @PersistentProperty(name = "hold_ratio_hk", valueType = Number, description = "")
    private Double holdRatioHk;

    @PersistentProperty(name = "net_volume_hk", valueType = Number, description = "")
    private Double netVolumeHk;
}
