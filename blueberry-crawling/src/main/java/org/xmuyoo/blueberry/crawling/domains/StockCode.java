package org.xmuyoo.blueberry.crawling.domains;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.xmuyoo.blueberry.crawling.storage.Persistent;
import org.xmuyoo.blueberry.crawling.storage.PersistentProperty;
import org.xmuyoo.blueberry.crawling.storage.ValueType;

@Getter
@Setter
@ToString
@Persistent(name = "stock_code")
public class StockCode {

    public enum Type {
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

    @PersistentProperty(name = "type", valueType = ValueType.Text)
    private Type type;

    public static Type getType(String typeName) {
        if (Type.SH.toString().equals(typeName.toLowerCase()))
            return Type.SH;
        else if (Type.SZ.toString().equals(typeName.toLowerCase()))
            return Type.SZ;
        else
            return null;
    }
}
