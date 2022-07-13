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
@Persistent(name = "stock_snapshot")
public class StockSnapshot {

    @PersistentProperty(name = "code", valueType = ValueType.Text, isUnique = true, description = "股票代码")
    private String code;

    @PersistentProperty(name = "name", valueType = ValueType.Text, description = "股票名称")
    private String name;

    @PersistentProperty(name = "pe_lyr", valueType = ValueType.Double, updateWhenConflict = true, description = "市盈率（静）")
    private Double peLyr;

    @PersistentProperty(name = "pe_ttm", valueType = ValueType.Double, updateWhenConflict = true, description = "市盈率（TTM）")
    private Double peTtm;

    @PersistentProperty(name = "pb", valueType = ValueType.Double, updateWhenConflict = true, description = "市净率")
    private Double pb;

    @PersistentProperty(name = "dividend_yield", valueType = ValueType.Double, updateWhenConflict = true, description = "股息率")
    private Double dividendYield;

    @PersistentProperty(name = "market_capital", valueType = ValueType.Double, updateWhenConflict = true, description = "总市值")
    private Double marketCapital;

    @PersistentProperty(name = "total_shares", valueType = ValueType.BigInt, updateWhenConflict = true, description = "总股本")
    private Long totalShares;

    @PersistentProperty(name = "navps", valueType = ValueType.Double, updateWhenConflict = true, description = "每股净资产")
    private Double navps;

    @PersistentProperty(name = "last_close", valueType = ValueType.Double, updateWhenConflict = true, description = "昨日收盘价")
    private Double lastClose;

    @PersistentProperty(name = "amount", valueType = ValueType.Double, updateWhenConflict = true, description = "成交额")
    private Double amount;

    @PersistentProperty(name = "volume", valueType = ValueType.Double, updateWhenConflict = true, description = "成交量")
    private Double volume;

    @PersistentProperty(name = "volume_ratio", valueType = ValueType.Double, updateWhenConflict = true, description = "量比")
    private Double volumeRatio;

    @PersistentProperty(name = "float_market_capital", valueType = ValueType.Double, updateWhenConflict = true, description = "流通市值")
    private Double floatMarketCapital;

    @PersistentProperty(name = "high52w", valueType = ValueType.Double, updateWhenConflict = true, description = "52 周最高")
    private Double high52w;

    @PersistentProperty(name = "low52w", valueType = ValueType.Double, updateWhenConflict = true, description = "52周最低")
    private Double low52w;

    @PersistentProperty(name = "turnover_rate", valueType = ValueType.Double, updateWhenConflict = true, description = "换手率")
    private Double turnoverRate;
}
