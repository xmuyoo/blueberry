package org.xmuyoo.blueberry.collect.domains;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.xmuyoo.blueberry.collect.storage.Persistent;
import org.xmuyoo.blueberry.collect.storage.PersistentProperty;
import org.xmuyoo.blueberry.collect.storage.ValueType;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Persistent(name = "collect_record")
public class CollectRecord {

    @PersistentProperty(name = "id", valueType = ValueType.Text, isUnique = true)
    private String id;

    @PersistentProperty(name = "collected_datetime", valueType = ValueType.Datetime)
    private LocalDateTime collectedDatetime;

    @PersistentProperty(name = "success", valueType = ValueType.Boolean)
    private boolean success;

    @PersistentProperty(name = "collect_task_id", valueType = ValueType.Text)
    private String collectTaskId;
}
