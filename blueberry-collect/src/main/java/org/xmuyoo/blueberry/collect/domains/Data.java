package org.xmuyoo.blueberry.collect.domains;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSortedMap;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.xmuyoo.blueberry.collect.utils.Utils;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PROTECTED)
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Data {

    public static final String ID = "id";
    public static final String DATA_TYPE = "dataType";
    public static final String CREATED_TIME = "createdTime";
    public static final String METRIC = "metric";
    public static final String DESCRIPTION = "description";

    @JsonProperty("id")
    private String id;

    @JsonProperty("dataType")
    private DataType dataType;

    @JsonProperty("createdTime")
    private LocalDateTime createdTime;

    public Data(String id, DataType dataType, LocalDateTime dateTime) {
        this.id = id;
        this.dataType = dataType;
        this.createdTime = dateTime;
    }

    protected abstract Map<String, String> generateTags();

    /**
     * A {@link Data} object may contains a number of metric values.
     *
     * @return Series data of values and tags.
     * @throws IllegalAccessException If any tag is not a String,
     *                                it throws an {@link IllegalArgumentException}.
     */
    public List<SeriesData> toSeriesData() throws IllegalAccessException {
        Map<String, String> defaultTags = generateTags();
        defaultTags = null != defaultTags ? defaultTags : new HashMap<>();

        // Deal with tags fields firstly.
        // Tag values can only be String, which is enough.
        // Tag keys and values should be enumerable.
        // Multiple levels of tags is not supported because any tags can be described
        // in only one level; Tags are not configurations.
        Field[] declaredFields = this.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            if (!isAvailableTag(field))
                continue;

            field.setAccessible(true);
            String fieldName = field.getName();
            Tag tagAnnotation = field.getAnnotation(Tag.class);
            Object tagValue = field.get(this);
            if (!(tagValue instanceof String)) {
                throw new IllegalArgumentException(
                        String.format("Tag value can only be String: [%s.%s]",
                                      this.getClass().getSimpleName(), fieldName));
            }
            if (StringUtils.isNotBlank(tagAnnotation.name())) {
                defaultTags.put(tagAnnotation.name(), (String) tagValue);
            } else {
                defaultTags.put(fieldName, (String) tagValue);
            }
        }

        // Deal with values fields
        List<SeriesData> seriesDataList = new ArrayList<>();
        for (Field field : declaredFields) {
            if (!isAvailableValue(field))
                continue;

            field.setAccessible(true);
            Map<String, String> fullTags = new TreeMap<>(defaultTags);
            if (fullTags.containsKey(METRIC) || fullTags.containsKey(DATA_TYPE))
                log.warn("{} and {} tags may be override", METRIC, DATA_TYPE);

            fullTags.put(METRIC, field.getName());
            fullTags.put(DATA_TYPE, this.dataType.toString());

            SeriesData seriesData = new SeriesData();
            Object value = field.get(this);
            if (!(value instanceof Number))
                continue;

            SeriesProperty seriesProperty = field.getAnnotation(SeriesProperty.class);
            String description = seriesProperty.description();
            if (StringUtils.isNotBlank(description))
                fullTags.put(DESCRIPTION, description);

            seriesData.value(field.getDouble(this));
            // To milliseconds
            seriesData.createdTime(
                    this.createdTime.toEpochSecond(Utils.ZONE_OFFSET_8_HOURS) * 1000);
            seriesData.tagId(fullTags.hashCode());
            seriesData.tags(fullTags);

            seriesDataList.add(seriesData);
        }

        return seriesDataList;
    }

    private boolean isAvailableTag(Field field) {
        SeriesIgnore seriesIgnore = field.getAnnotation(SeriesIgnore.class);
        if (null != seriesIgnore)
            return false;

        Tag tagAnnotation = field.getAnnotation(Tag.class);
        return null != tagAnnotation;

    }

    private boolean isAvailableValue(Field field) {
        SeriesIgnore seriesIgnore = field.getAnnotation(SeriesIgnore.class);
        if (null != seriesIgnore)
            return false;

        Tag tagAnnotation = field.getAnnotation(Tag.class);
        if (null != tagAnnotation)
            return false;

        SeriesProperty seriesProperty = field.getAnnotation(SeriesProperty.class);
        if (null == seriesProperty)
            return false;

        return seriesProperty.value();
    }
}
