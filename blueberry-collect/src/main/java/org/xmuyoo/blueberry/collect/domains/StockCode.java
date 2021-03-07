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
        SH, SZ;

        @Override
        public String toString() {
            return this.name().toLowerCase();
        }
    }

    @PersistentProperty(name = "code", valueType = ValueType.Text, isUnique = true)
    private String code;

    @PersistentProperty(name = "name", valueType = ValueType.Text)
    private String name;

    @PersistentProperty(name = "exchange", valueType = ValueType.Text)
    private Exchange exchange;

    @PersistentProperty(name = "category", valueType = ValueType.Text)
    private String category;

    @PersistentProperty(name = "location", valueType = ValueType.Text)
    private String location;

    public static Exchange getExchange(String typeName) {
        if (Exchange.SH.toString().equals(typeName.toLowerCase()))
            return Exchange.SH;
        else if (Exchange.SZ.toString().equals(typeName.toLowerCase()))
            return Exchange.SZ;
        else
            return null;
    }
}
