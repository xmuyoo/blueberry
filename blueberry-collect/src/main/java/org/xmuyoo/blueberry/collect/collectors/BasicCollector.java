package org.xmuyoo.blueberry.collect.collectors;

import com.google.common.collect.ImmutableMap;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.xmuyoo.blueberry.collect.Collector;
import org.xmuyoo.blueberry.collect.domains.DataSchema;
import org.xmuyoo.blueberry.collect.storage.Persistent;
import org.xmuyoo.blueberry.collect.storage.PersistentProperty;
import org.xmuyoo.blueberry.collect.storage.PgClient;
import org.xmuyoo.blueberry.collect.storage.UniqueConstraint;
import org.xmuyoo.blueberry.collect.storage.ValueType;

@Slf4j
public abstract class BasicCollector<T> implements Collector {

    protected String collectorName;
    protected volatile boolean isRunning = true;
    protected volatile String name;

    protected PgClient storage;
    private Class<T> entityClz;

    public BasicCollector(String collectorName, PgClient storage) {
        this.collectorName = collectorName;
        this.storage = storage;
    }

    public BasicCollector(String collectorName, PgClient storage, Class<T> entityClz) {
        this.collectorName = collectorName;
        this.storage = storage;
        this.entityClz = entityClz;
    }

    @Override
    public void setStop(boolean stop) {
        this.isRunning = stop;
    }

    @Override
    public String name() {
        if (StringUtils.isEmpty(this.name)) {
            this.name = this.getClass().getSimpleName();
        }

        return this.name;
    }

    @Override
    public void init() {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void run() {
        log.info("Start running collector: {}", this.collectorName);
        registerDataSchema();
        createEntityTableIfNotExists();
        init();

        try {
            collect();
        } catch (Throwable t) {
            log.error(String.format("Error in collector: [%s:%s]", name(),
                    collectorName), t);
        }

        shutdown();
        log.info("Collecting completed. Shutdown {}", this.collectorName);
    }

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
        createEntityTable(DataSchema.class);
        List<DataSchema> dataSchemaList = getDataSchemaList();
        try {
            storage.saveOrUpdate(dataSchemaList, DataSchema.class);
        } catch (Exception e) {
            log.error(String.format("Failed to register data schema for collector [%s:%s]",
                    name(), collectorName), e);
        }
    }

    private void createEntityTableIfNotExists() {
        if (!needCreateEntityTable() || entityClz == null) {
            return;
        }

        createEntityTable(entityClz);
    }

    @SneakyThrows
    private <ENTITY> void createEntityTable(Class<ENTITY> entityClz) {
        StringBuilder builder = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        Persistent persistent = entityClz.getAnnotation(Persistent.class);
        String tableName = persistent.name();
        builder.append(tableName).append("(\n");

        List<String> columnDefinitionList = new ArrayList<>();
        List<String> uniqueColumns = new ArrayList<>();
        ImmutableMap<ValueType, String> typeMapping = this.storage.getStorgeTypeMapping();
        for (Field field : entityClz.getDeclaredFields()) {
            PersistentProperty property = field.getAnnotation(PersistentProperty.class);
            if (null == property) {
                continue;
            }

            String columnName = property.name();
            ValueType valueType = property.valueType();
            String description = property.description();

            String columnDefinition = "-- " + description + "\n";
            columnDefinition += String.format("%s %s", columnName, typeMapping.get(valueType));
            columnDefinitionList.add(columnDefinition);

            if (property.isUnique()) {
                uniqueColumns.add(columnName);
            }
        }

        String createSql = builder.append(String.join(",", columnDefinitionList)).append(")").toString();
        this.storage.execute(createSql);

        String uniqueIdxDefinitionFmt = "CREATE UNIQUE INDEX IF NOT EXISTS %s__%s_idx ON %s(%s)";
        for (String uniqueCol : uniqueColumns) {
            String createUniqueIdxSql = String.format(uniqueIdxDefinitionFmt,
                    tableName, uniqueCol, tableName, uniqueCol);
            this.storage.execute(createUniqueIdxSql);
        }

        UniqueConstraint[] uniqueConstraints = persistent.uniqueConstraints();
        if (null != uniqueConstraints && uniqueConstraints.length > 0) {
            for (UniqueConstraint constraint : uniqueConstraints) {
                String[] cols = constraint.value();
                if (cols != null && cols.length > 0) {
                    List<String> multiColsUniqueIndex = new ArrayList<>(Arrays.asList(cols));
                    String multiIndexDefinition = String.format(uniqueIdxDefinitionFmt,
                            tableName, String.join("_", multiColsUniqueIndex),
                            tableName, String.join(",", multiColsUniqueIndex));

                    this.storage.execute(multiIndexDefinition);
                }
            }
        }
    }

    protected boolean needCreateEntityTable() {
        return false;
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
            switch (valueType) {
                case Double:
                    field.set(entity, Double.parseDouble(value.toString()));
                    break;
                case BigInt:
                    field.set(entity, Long.parseLong(value.toString()));
                    break;
                case Text:
                    field.set(entity, value);
                    break;
                default:
                    if (Double.class.equals(valueClz)) {
                        field.set(entity, Double.parseDouble(value.toString()));
                    } else if (Long.class.equals(valueClz)) {
                        field.set(entity, Long.parseLong(value.toString()));
                    } else {
                        field.set(entity, value);
                    }
                    break;
            }
        }

        return entity;
    }
}