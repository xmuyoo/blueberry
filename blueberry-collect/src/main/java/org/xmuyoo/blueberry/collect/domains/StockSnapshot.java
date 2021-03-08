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

    public static final String CODE = "code";
    public static final String NAME = "name";
    public static final String PE_LYR = "pe_lyr";
    public static final String PB = "pb";
    public static final String DIVIDEND_YIELD = "dividend_yield";
    public static final String MARKET_CAPITAL = "market_capital";
    public static final String TOTAL_SHARES = "total_shares";
    public static final String NAVPS = "navps";
    public static final String LAST_CLOSE = "last_close";


    @PersistentProperty(name = "code", valueType = ValueType.Text, isUnique = true)
    private String code;

    @PersistentProperty(name = "name", valueType = ValueType.Text)
    private String name;

    @PersistentProperty(name = "pe_lyr", valueType = ValueType.Number)
    private Double peLyr;

    @PersistentProperty(name = "pb", valueType = ValueType.Number)
    private Double pb;

    @PersistentProperty(name = "dividend_yield", valueType = ValueType.Number)
    private Double dividendYield;

    @PersistentProperty(name = "market_capital", valueType = ValueType.Number)
    private Double marketCapital;

    @PersistentProperty(name = "total_shares", valueType = ValueType.Number)
    private Number totalShares;

    @PersistentProperty(name = "navps", valueType = ValueType.Number)
    private Double navps;

    @PersistentProperty(name = "last_close", valueType = ValueType.Number)
    private Double lastClose;
}
