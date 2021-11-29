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
@Persistent(name = "stock_code")
public class StockCode {

    public enum Exchange {
        SH, SZ, BJ;

        @Override
        public String toString() {
            return this.name().toLowerCase();
        }
    }

    @PersistentProperty(name = "code", valueType = ValueType.Text, isUnique = true, description = "股票代码")
    private String code;

    @PersistentProperty(name = "name", valueType = ValueType.Text, description = "股票名称")
    private String name;

    @PersistentProperty(name = "exchange", valueType = ValueType.Text, description = "交易所代码")
    private Exchange exchange;

    @PersistentProperty(name = "category", valueType = ValueType.Text, description = "分类")
    private String category;

    @PersistentProperty(name = "location", valueType = ValueType.Text, description = "地区")
    private String location;
}
