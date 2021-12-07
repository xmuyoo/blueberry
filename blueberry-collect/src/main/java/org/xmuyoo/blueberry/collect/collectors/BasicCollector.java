package org.xmuyoo.blueberry.collect.collectors;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.xmuyoo.blueberry.collect.Collector;
import org.xmuyoo.blueberry.collect.domains.DataSchema;
import org.xmuyoo.blueberry.collect.storage.PersistentProperty;
import org.xmuyoo.blueberry.collect.storage.PgClient;
import org.xmuyoo.blueberry.collect.storage.ValueType;

@Slf4j
public abstract class BasicCollector implements Collector {

    protected String collectorName;
    protected volatile boolean isRunning = true;
    protected volatile String name;

    protected PgClient storage;

    public BasicCollector(String collectorName, PgClient storage) {
        this.collectorName = collectorName;
        this.storage = storage;
    }

    @Override
    public void setStop(boolean stop) {
        this.isRunning = stop;
    }

    @Override
    public String name() {
        if (StringUtils.isEmpty(this.name))
            this.name = this.getClass().getSimpleName();

        return this.name;
    }

    @Override
    public void start() {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void run() {
        log.info("Start running collector: {}", this.collectorName);
        registerDataSchema();
        start();

        if (isAvailable()) {
            try {
                collect();
            } catch (Throwable t) {
                log.error(String.format("Error in collector: [%s:%s]", name(),
                        collectorName), t);
            }
        }

        shutdown();
        log.info("Collecting completed. Shutdown {}", this.collectorName);
    }

    protected abstract boolean isAvailable();

    protected abstract boolean collect();

    protected abstract List<DataSchema> getDataSchemaList();

    protected void wait(long periodTime, TimeUnit periodTimeUnit) {
        try {
            periodTimeUnit.sleep(periodTime);
        } catch (Exception e) {
            log.error(
                    String.format("Thread [%s] is interrupted", Thread.currentThread().getName()),
                    e);
            Thread.currentThread().interrupt();
        }
    }

    private void registerDataSchema() {
        List<DataSchema> dataSchemaList = getDataSchemaList();
        try {
            storage.saveOrUpdate(dataSchemaList, DataSchema.class);
        } catch (Exception e) {
            log.error(String.format("Failed to register data schema for collector [%s:%s]",
                    name(), collectorName), e);
        }
    }

    protected DataSchema textOf(String name, String description) {
        return new DataSchema(collectorName, name, ValueType.Text, description, collectorName);
    }

    protected DataSchema numberOf(String name, String description) {
        return new DataSchema(collectorName, name, ValueType.Number, description, collectorName);
    }

    protected DataSchema datetimeOf(String name, String description) {
        return new DataSchema(collectorName, name, ValueType.Datetime, description, collectorName);
    }

    protected DataSchema booleanOf(String name, String description) {
        return new DataSchema(collectorName, name, ValueType.Boolean, description, collectorName);
    }

    protected DataSchema jsonOf(String name, String description) {
        return new DataSchema(collectorName, name, ValueType.Json, description, collectorName);
    }

    protected List<DataSchema> toSchemaList(Class<?> clz) {
        Field[] fields = clz.getDeclaredFields();
        List<DataSchema> schemas = new ArrayList<>();
        for (Field field : fields) {
            PersistentProperty property = field.getAnnotation(PersistentProperty.class);
            if (null == property) {
                continue;
            }

            schemas.add(new DataSchema(collectorName,
                    property.name(), property.valueType(), property.description(), collectorName));
        }

        return schemas;
    }

    @SneakyThrows
    public static <T> T mapToEntity(Map<String, Object> mapData, Class<T> clz) {
        Constructor<T> constructor = clz.getConstructor();
        T entity = constructor.newInstance();
        for (Field field : clz.getDeclaredFields()) {
            PersistentProperty persistentProperty = field.getAnnotation(PersistentProperty.class);
            if (null == persistentProperty) {
                continue;
            }

            field.setAccessible(true);
            final String persistentName = persistentProperty.name();
            Object value = mapData.get(persistentName);
            if (null == value) {
                continue;
            }

            final ValueType valueType = persistentProperty.valueType();
            final Class<?> valueClz = field.getType();
            if (ValueType.Number.equals(valueType)) {
                if (Double.class.equals(valueClz)) {
                    field.set(entity, Double.parseDouble(value.toString()));
                } else if (Long.class.equals(valueClz)) {
                    field.set(entity, Long.parseLong(value.toString()));
                }
            } else {
                field.set(entity, value);
            }
        }

        return entity;
    }
}