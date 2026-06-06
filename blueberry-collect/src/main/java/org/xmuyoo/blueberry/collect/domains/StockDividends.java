package org.xmuyoo.blueberry.collect.domains;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.xmuyoo.blueberry.collect.storage.Persistent;
import org.xmuyoo.blueberry.collect.storage.PersistentProperty;

import static org.xmuyoo.blueberry.collect.storage.ValueType.Double;
import static org.xmuyoo.blueberry.collect.storage.ValueType.Text;

@Getter
@Setter
@ToString
@Persistent(name = "stock_dividends")
public class StockDividends {

    @PersistentProperty(name = "code", valueType = Text, description = "股票代码")
    private String code;

    @PersistentProperty(name = "name", valueType = Text, description = "股票简称")
    private String name;

    @PersistentProperty(name = "price", valueType = Double, description = "预计除权除息价")
    private Double price;

    @PersistentProperty(name = "record_date", valueType = Text, description = "除权除息日")
    private String recordDate;
}
