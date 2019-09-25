package org.xmuyoo.blueberry.collect.domains;

import lombok.Getter;
import org.xmuyoo.blueberry.collect.storage.Persistent;
import org.xmuyoo.blueberry.collect.storage.PersistentProperty;
import org.xmuyoo.blueberry.collect.storage.UniqueConstraint;
import org.xmuyoo.blueberry.collect.storage.ValueType;
import org.xmuyoo.blueberry.collect.utils.Utils;

@Getter
@Persistent(name = "data_schema", uniqueConstraints = {@UniqueConstraint({"id"}),
        @UniqueConstraint({"namespace", "name", "user_id"})})
public class DataSchema {

    @PersistentProperty(name = "id", valueType = ValueType.Number)
    private Long id;

    @PersistentProperty(name = "namespace", valueType = ValueType.Text)
    private String namespace;

    @PersistentProperty(name = "name", valueType = ValueType.Text)
    private String name;

    @PersistentProperty(name = "user_id", valueType = ValueType.Number)
    private Long userId;

    @PersistentProperty(name = "type", valueType = ValueType.Text)
    private String type;

    @PersistentProperty(name = "description", valueType = ValueType.Text)
    private String description;

    public DataSchema(String namespace, String name, Long userId, ValueType valueType,
                      String desc) {

        this.id = Utils.MURMUR3.hashBytes(String.format("%s:%s:%s", namespace, name, userId).getBytes())
                .asLong();
        this.namespace = namespace;
        this.name = name;
        this.userId = userId;
        this.type = valueType.name().toLowerCase();
        this.description = desc;
    }
}
