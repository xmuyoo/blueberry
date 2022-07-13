package org.xmuyoo.blueberry.collect.storage;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.postgresql.util.PGobject;
import org.xmuyoo.blueberry.collect.Lifecycle;
import org.xmuyoo.blueberry.collect.domains.SeriesData;
import org.xmuyoo.blueberry.collect.utils.Utils;

@Slf4j
public class PgClient implements Lifecycle {

    private static final ImmutableMap<ValueType, String> VALUE_TYPE_TO_COLUMN_TYPE = ImmutableMap
            .<ValueType, String>builder()
            .put(ValueType.Text, "text")
            .put(ValueType.BigInt, "bigint")
            .put(ValueType.Double, "decimal(16, 2)")
            .put(ValueType.Datetime, "timestamp")
            .put(ValueType.Boolean, "boolean")
            .put(ValueType.Json, "json")
            .build();

    private static final String INSERT_IGNORE_DATA_FORMAT =
            "INSERT INTO %s (%s) VALUES (%s) ON CONFLICT";
    private static final String INSERT_SPECIFIED_UNIQUE_DATA_FORMAT =
            "INSERT INTO %s (%s) VALUES (%s) ON CONFLICT (%s)";

    // Series data SQL format
    private static final String INSERT_SERIES_DATA_VALUES_SQL_FMT =
            "INSERT INTO values_%s (record_time, value, tag_id) VALUES(?, ?, ?)"
                    + " ON CONFLICT (record_time, tag_id) DO NOTHING";
    private static final String INSERT_SERIES_DATA_TAGS_SQL_FMT =
            "INSERT INTO tags_%s (record_time, tag_id, tags) VALUES(?, ?, ?)"
                    + " ON CONFLICT (record_time, tag_id) DO NOTHING";

    private static final Timestamp TAG_TIMESTAMP = new Timestamp(1593532800); // 2020.07.01 00:00:00
    private static final Joiner COMMA_SPACE_JOINER = Joiner.on(" , ");
    private final DruidDataSource dataSource;

    @AllArgsConstructor
    @Getter
    private static class FieldProperty {
        private Field field;
        private PersistentProperty property;
    }

    public PgClient(DruidDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void init() {

    }

    @Override
    public void shutdown() {
        if (null != dataSource) {
            dataSource.close();
        }
    }

    public ImmutableMap<ValueType, String> getStorgeTypeMapping() {
        return VALUE_TYPE_TO_COLUMN_TYPE;
    }

    public <T> void saveIgnoreDuplicated(@NonNull List<T> dataList, Class<T> clz) {
        saveOrUpdate(dataList, clz, false);
    }

    public <T> void saveOrUpdate(@NonNull List<T> dataList, Class<T> clz) {
        saveOrUpdate(dataList, clz, true);
    }

    private <T> boolean saveOrUpdate(@NonNull List<T> dataList, Class<T> clz, boolean updateWhenConflict) {
        if (dataList.isEmpty()) {
            log.warn("Data is empty. Ignore saving.");
            return true;
        }

        String tableName;
        Persistent persistent = clz.getAnnotation(Persistent.class);
        if (null != persistent && StringUtils.isNotBlank(persistent.name())) {
            tableName = persistent.name();
        } else {
            tableName = clz.getSimpleName().toLowerCase();
        }

        Field[] fields = clz.getDeclaredFields();
        List<PersistentProperty> propertyList = new ArrayList<>();
        List<String> propertyNames = new ArrayList<>();
        Set<String> uniqueProperties = new HashSet<>();
        List<String> filedNames = new ArrayList<>();
        List<FieldProperty> updateOnConflictColumns = new ArrayList<>();
        for (Field field : fields) {
            PersistentProperty property = field.getAnnotation(PersistentProperty.class);
            if (null == property) {
                continue;
            }

            final String persistFieldName = property.name();
            propertyList.add(property);
            propertyNames.add(persistFieldName);
            filedNames.add(field.getName());
            if (property.isUnique()) {
                uniqueProperties.add(persistFieldName);
            }
            if (updateWhenConflict && property.updateWhenConflict()) {
                updateOnConflictColumns.add(new FieldProperty(field, property));
            }
        }
        if (propertyNames.isEmpty()) {
            log.warn("There is no persistent properties specified in {} and ignore the saves",
                    clz.getSimpleName());
            return false;
        }

        String placeholders =
                Utils.commaJoin(propertyNames.stream().map(p -> "?").collect(Collectors.toList()));
        String insertSql;
        if (uniqueProperties.isEmpty()) {
            insertSql = String.format(INSERT_IGNORE_DATA_FORMAT,
                    tableName, Utils.commaJoin(propertyNames), placeholders);
        } else {
            insertSql = String.format(INSERT_SPECIFIED_UNIQUE_DATA_FORMAT,
                    tableName, Utils.commaJoin(propertyNames), placeholders,
                    Utils.commaJoin(uniqueProperties));
        }

        if (updateWhenConflict) {
            insertSql += " DO UPDATE SET " + COMMA_SPACE_JOINER.join(
                    updateOnConflictColumns.stream()
                                           .map(fp -> fp.property().name() + " = ?")
                                           .collect(Collectors.toList()));
        } else {
            insertSql += " DO NOTHING";
        }

        return saveData(dataList, filedNames, propertyList, insertSql, updateOnConflictColumns);
    }

    public void saveSeriesData(String identifier, List<SeriesData> seriesDataList) {
        String insertValuesSql = String.format(INSERT_SERIES_DATA_VALUES_SQL_FMT, identifier);
        String insertTagsSql = String.format(INSERT_SERIES_DATA_TAGS_SQL_FMT, identifier);

        try (DruidPooledConnection conn = dataSource.getConnection();
             PreparedStatement valuesStmt = conn.prepareStatement(insertValuesSql);
             PreparedStatement tagsStmt = conn.prepareStatement(insertTagsSql)) {

            Set<Long> tagIds = new HashSet<>();
            for (SeriesData seriesData : seriesDataList) {
                valuesStmt.setTimestamp(1, new Timestamp(seriesData.createdTime()));
                valuesStmt.setDouble(2, seriesData.value());
                valuesStmt.setLong(3, seriesData.tagId());
                valuesStmt.addBatch();

                if (!tagIds.contains(seriesData.tagId())) {
                    tagsStmt.setTimestamp(1, TAG_TIMESTAMP);
                    tagsStmt.setLong(2, seriesData.tagId());
                    String jsonTags = Utils.JSON.writeValueAsString(seriesData.tags());
                    PGobject jsonObject = new PGobject();
                    jsonObject.setType("json");
                    jsonObject.setValue(jsonTags);
                    tagsStmt.setObject(3, jsonObject);

                    tagsStmt.addBatch();
                    tagIds.add(seriesData.tagId());
                }
            }

            valuesStmt.executeBatch();
            tagsStmt.executeBatch();
        } catch (Exception e) {
            log.error("Failed to save series data", e);
        }
    }

    public <R> List<R> queryList(String sql) throws SQLException {
        return execAndGetList(sql, null, null);
    }

    /**
     * Note: The clz can not be an inner class.
     */
    public <R> List<R> queryList(String sql, Class<R> clz) throws SQLException {
        return execAndGetList(sql, clz, null);
    }

    public <R> R queryOne(String sql) throws SQLException {
        List<R> results = execAndGetList(sql, null, null);
        if (!results.isEmpty()) {
            return results.get(0);
        } else {
            return null;
        }
    }

    public <R> R queryOne(String sql, Class<R> clz) throws SQLException {
        List<R> results = execAndGetList(sql, clz, null);
        if (!results.isEmpty()) {
            return results.get(0);
        } else {
            return null;
        }
    }

    public void execute(String sql) throws SQLException {
        execute(sql, null, null, false);
    }

    private <T> boolean saveData(List<T> dataList, List<String> fieldNames,
                                 List<PersistentProperty> properties, String insertSql,
                                 List<FieldProperty> updateOnConflictColumns) {

        try (DruidPooledConnection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSql)) {

            for (T data : dataList) {
                for (int i = 0; i < fieldNames.size(); i++) {
                    Field field = data.getClass().getDeclaredField(fieldNames.get(i));
                    setPreparedValue(field, properties.get(i), data, stmt, i + 1);
                }

                int j = fieldNames.size() + 1;
                for (FieldProperty fieldProperty : updateOnConflictColumns) {
                    String fieldName = fieldProperty.field().getName();
                    Field field = data.getClass().getDeclaredField(fieldName);
                    setPreparedValue(field, fieldProperty.property(), data, stmt, j);

                    j++;
                }

                stmt.addBatch();
            }

            stmt.executeBatch();
        } catch (Exception e) {
            log.error(String.format("Failed to save data of type %s",
                    dataList.get(0).getClass().getSimpleName()), e);
            return false;
        }

        return true;
    }

    private void setPreparedValue(Field field, PersistentProperty property, Object data, PreparedStatement stmt, int index)
            throws Exception {
        field.setAccessible(true);
        Object fieldData = field.get(data);
        switch (property.valueType()) {
            case Json:
                if (null != fieldData) {
                    PGobject pGobject = new PGobject();
                    pGobject.setType("json");
                    pGobject.setValue(Utils.JSON.writeValueAsString(fieldData));

                    stmt.setObject(index, pGobject);
                } else {
                    stmt.setObject(index, null);
                }
                break;
            case Text:
                if (null != fieldData) {
                    stmt.setObject(index, fieldData.toString());
                } else {
                    stmt.setObject(index, null);
                }
                break;
            default:
                stmt.setObject(index, fieldData);
                break;
        }
    }

    private <R> List<R> execAndGetList(String sql, Class<R> clazz,
                                       Function<Exception, Void> exceptionHandler)
            throws SQLException {
        return execute(sql, clazz, exceptionHandler, true);
    }

    private <R> List<R> execute(String sql, Class<R> clazz,
                                Function<Exception, Void> exceptionHandler, boolean isQuery)
            throws SQLException {

        List<R> results = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            if (!isQuery) {
                stmt.execute(sql);
                return null;
            }

            stmt.executeQuery(sql);
            ResultSet resultSet = stmt.getResultSet();
            ResultSetMetaData metaData = resultSet.getMetaData();

            while (resultSet.next()) {
                if (null == clazz) {
                    // noinspection unchecked
                    results.add((R) resultSet.getObject(1));
                } else {
                    Map<String, Object> columnValues = new HashMap<>();
                    try {
                        for (int i = 1; i <= metaData.getColumnCount(); i++) {
                            String columnName = metaData.getColumnName(i);
                            Object value = resultSet.getObject(i);
                            columnValues.put(columnName, value);
                        }

                        R instance = clazz.newInstance();
                        for (Field field : clazz.getDeclaredFields()) {
                            PersistentProperty property =
                                    field.getAnnotation(PersistentProperty.class);
                            if (null == property) {
                                continue;
                            }
                            String name = property.name();
                            if (!columnValues.containsKey(name)) {
                                continue;
                            }

                            field.setAccessible(true);
                            Object value = columnValues.get(name);
                            if (field.getType().isEnum()) {
                                // noinspection unchecked
                                for (Field enumField : field.getType().getDeclaredFields()) {
                                    if (enumField.getName().equalsIgnoreCase((String) value)) {
                                        Object ennumObj = enumField.get(instance);
                                        field.set(instance, ennumObj);
                                    }
                                }
                            } else {
                                field.set(instance, value);
                            }
                        }

                        results.add(instance);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            resultSet.close();
        } catch (SQLException e) {
            if (null != exceptionHandler) {
                exceptionHandler.apply(e);
            } else {
                throw e;
            }
        }

        return results;
    }
}
