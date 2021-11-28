package org.xmuyoo.blueberry.collect.domains;

import lombok.Getter;
import org.xmuyoo.blueberry.collect.storage.Persistent;
import org.xmuyoo.blueberry.collect.storage.PersistentProperty;
import org.xmuyoo.blueberry.collect.storage.UniqueConstraint;
import org.xmuyoo.blueberry.collect.storage.ValueType;
import org.xmuyoo.blueberry.collect.utils.Utils;

@Getter
@Persistent(name = "data_schema", uniqueConstraints = {@UniqueConstraint({"id"}),
        @UniqueConstraint({"namespace", "name"})})
public class DataSchema {

    @PersistentProperty(name = "id", valueType = ValueType.Text, isUnique = true)
    private String id;

    @PersistentProperty(name = "namespace", valueType = ValueType.Text)
    private String namespace;

    @PersistentProperty(name = "name", valueType = ValueType.Text)
    private String name;

    @PersistentProperty(name = "type", valueType = ValueType.Text)
    private String type;

    @PersistentProperty(name = "description", valueType = ValueType.Text)
    private String description;

    @PersistentProperty(name = "collect_task_id", valueType = ValueType.Text)
    private String collectTaskId;

    public DataSchema(String namespace, String name, ValueType valueType,
                      String desc, String collectTaskId) {

        this.id = Utils.MURMUR3.hashBytes(String.format("%s:%s", namespace, name).getBytes())
                               .toString();
        this.namespace = namespace;
        this.name = name;
        this.type = valueType.name().toLowerCase();
        this.description = desc;
        this.collectTaskId = collectTaskId;
    }
}
