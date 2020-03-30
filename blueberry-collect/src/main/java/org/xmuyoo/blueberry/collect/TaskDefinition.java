package org.xmuyoo.blueberry.collect;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;
import org.xmuyoo.blueberry.collect.storage.Persistent;
import org.xmuyoo.blueberry.collect.storage.PersistentProperty;
import org.xmuyoo.blueberry.collect.storage.ValueType;
import org.xmuyoo.blueberry.collect.utils.Utils;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Persistent(name = "task_definition")
public class TaskDefinition {

    public static final String TIME_UNIT_WEEK = "week";
    public static final String TIME_UNIT_DAY = "day";
    public static final String TIME_UNIT_HOUR = "hour";
    public static final String TIME_UNIT_MINUTE = "minute";

    @PersistentProperty(name = "id", valueType = ValueType.Text)
    private String id;

    @PersistentProperty(name = "description", valueType = ValueType.Text)
    private String description;

    @PersistentProperty(name = "source_name", valueType = ValueType.Text)
    private String sourceName;

    @PersistentProperty(name = "source_url", valueType = ValueType.Text)
    private String sourceUrl;

    @PersistentProperty(name = "source_type", valueType = ValueType.Text)
    private String sourceType;

    @PersistentProperty(name = "time_ranges", valueType = ValueType.Text)
    private String timeRanges;

    @PersistentProperty(name = "period", valueType = ValueType.Text)
    private String period;

    @PersistentProperty(name = "body_pattern", valueType = ValueType.Text)
    private String bodyPattern;

    @PersistentProperty(name = "active", valueType = ValueType.Boolean)
    private Boolean active;

    @PersistentProperty(name = "http_method", valueType = ValueType.Text)
    private String httpMethod;

    @PersistentProperty(name = "collector_name", valueType = ValueType.Text)
    private String collectorName;

    @PersistentProperty(name = "collector_alias", valueType = ValueType.Text)
    private String collectorAlias;

    @PersistentProperty(name = "collector_driver", valueType = ValueType.Text)
    private String collectorDriver;

    public Pair<Long, TimeUnit> getPeriod() {
        String[] items = this.period.split(" ");
        Long time = Long.valueOf(items[0]);
        TimeUnit unit;
        switch (items[1]) {
            case TIME_UNIT_WEEK:
                unit = TimeUnit.DAYS;
                time = time * 7;
                break;
            case TIME_UNIT_DAY:
                unit = TimeUnit.DAYS;
                break;
            case TIME_UNIT_HOUR:
                unit = TimeUnit.HOURS;
                break;
            case TIME_UNIT_MINUTE:
                unit = TimeUnit.MINUTES;
                break;
            default:
                unit = TimeUnit.DAYS;
                break;
        }

        return Pair.of(time, unit);
    }

    public List<Pair<LocalTime, LocalTime>> getTimeRangeList() {
        List<Pair<LocalTime, LocalTime>> ranges = new ArrayList<>();

        List<String> timeRangeList =
                Configs.COMMA_SPLITTER.splitToList(this.timeRanges);
        for (String range : timeRangeList) {
            List<String> timePoints = Configs.LINE_SPLITTER.splitToList(range);
            LocalTime start = Utils.toLocalTime(timePoints.get(0));
            LocalTime end = Utils.toLocalTime(timePoints.get(1));

            ranges.add(Pair.of(start, end));
        }

        return ranges;
    }
}
