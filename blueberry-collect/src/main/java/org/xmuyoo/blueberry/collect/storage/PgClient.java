package org.xmuyoo.blueberry.collect.storage;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.postgresql.util.PGobject;
import org.xmuyoo.blueberry.collect.Lifecycle;
import org.xmuyoo.blueberry.collect.domains.SeriesData;
import org.xmuyoo.blueberry.collect.utils.Utils;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class PgClient implements Lifecycle {

    private static final String INSERT_IGNORE_DATA_FORMAT =
            "INSERT INTO %s (%s) VALUES (%s) ON CONFLICT DO NOTHING";
    private static final String INSERT_SPECIFIED_UNIQUE_DATA_FORMAT =
            "INSERT INTO %s (%s) VALUES (%s) ON CONFLICT (%s) DO NOTHING";
    private static final String INSERT_SERIES_DATA_VALUES_SQL_FMT =
            "INSERT INTO values_%s (record_time, value, tag_id) VALUES(?, ?, ?)"
                    + " ON CONFLICT (record_time, tag_id) DO UPDATE SET value = excluded.value";
    private static final String INSERT_SERIES_DATA_TAGS_SQL_FMT =
            "INSERT INTO tags_%s (record_time, tag_id, tags) VALUES(?, ?, ?)"
                    + " ON CONFLICT (record_time, tag_id) DO NOTHING";

    private final DruidDataSource dataSource;

    public PgClient(DruidDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {
        if (null != dataSource)
            dataSource.close();
    }

    public <T> boolean saveIgnoreDuplicated(@NonNull List<T> dataList, Class<T> clz) {
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
        for (Field field : fields) {
            PersistentProperty property = field.getAnnotation(PersistentProperty.class);
            if (null == property)
                continue;

            propertyList.add(property);
            propertyNames.add(property.name());
            filedNames.add(field.getName());
            if (property.isUnique())
                uniqueProperties.add(property.name());
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

        return saveData(dataList, filedNames, propertyList, insertSql);
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
                    long dayInMilliseconds = Utils.toDayInMillis(seriesData.createdTime());
                    tagsStmt.setTimestamp(1, new Timestamp(dayInMilliseconds));
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
        if (!results.isEmpty())
            return results.get(0);
        else
            return null;
    }

    public <R> R queryOne(String sql, Class<R> clz) throws SQLException {
        List<R> results = execAndGetList(sql, clz, null);
        if (!results.isEmpty())
            return results.get(0);
        else
            return null;
    }

    public void execute(String sql) throws SQLException {
        execute(sql, null, null, false);
    }

    private <T> boolean saveData(List<T> dataList, List<String> fieldNames,
                                 List<PersistentProperty> properties, String insertSql) {

        try (DruidPooledConnection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSql)) {

            for (T data : dataList) {
                for (int i = 0; i < fieldNames.size(); i++) {
                    Field field = data.getClass().getDeclaredField(fieldNames.get(i));
                    field.setAccessible(true);
                    PersistentProperty property = properties.get(i);

                    switch (property.valueType()) {
                        case Json:
                            PGobject pGobject = new PGobject();
                            pGobject.setType("json");
                            pGobject.setValue(Utils.JSON.writeValueAsString(field.get(data)));

                            stmt.setObject(i + 1, pGobject);
                            break;
                        case Text:
                            stmt.setObject(i + 1, field.get(data).toString());
                            break;
                        default:
                            stmt.setObject(i + 1, field.get(data));
                            break;
                    }
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
                            if (null == property)
                                continue;
                            String name = property.name();
                            if (!columnValues.containsKey(name))
                                continue;

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
            if (null != exceptionHandler)
                exceptionHandler.apply(e);
            else
                throw e;
        }

        return results;
    }
}
