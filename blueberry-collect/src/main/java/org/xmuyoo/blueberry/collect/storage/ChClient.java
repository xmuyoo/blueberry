package org.xmuyoo.blueberry.collect.storage;

import com.clickhouse.jdbc.ClickHouseDataSource;
import com.google.common.collect.ImmutableMap;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Slf4j
public class ChClient {

    private static final ImmutableMap<ValueType, String> VALUE_TYPE_TO_COLUMN_TYPE = ImmutableMap
            .<ValueType, String>builder()
            .put(ValueType.Text, "String")
            .put(ValueType.BigInt, "Int64")
            .put(ValueType.Double, "Float64")
            .put(ValueType.Datetime, "DateTime64(3)")
            .put(ValueType.Boolean, "UInt8")
            .put(ValueType.Json, "String")
            .build();

    private final ClickHouseDataSource dataSource;

    /**
     * @param url      JDBC URL, e.g. jdbc:clickhouse://localhost:8123/blueberry
     * @param user     ClickHouse user
     * @param password ClickHouse password
     */
    public ChClient(String url, String user, String password) {
        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", password);
        // Use HTTP protocol (port 8123)
        props.setProperty("client_name", "blueberry-collect");
        // Disable compression to avoid LZ4/ZSTD mismatch with ClickHouse 24.x
        props.setProperty("compress", "0");
        // Allow large batch inserts spanning many partitions
        props.setProperty("max_partitions_per_insert_block", "10000");
        try {
            this.dataSource = new ClickHouseDataSource(url, props);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create ClickHouse data source", e);
        }
    }

    public ImmutableMap<ValueType, String> getStorageTypeMapping() {
        return VALUE_TYPE_TO_COLUMN_TYPE;
    }

    /**
     * Execute a DDL or DML statement (CREATE TABLE, etc.)
     */
    public void execute(String sql) {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            log.error("Failed to execute SQL: {}", sql, e);
            throw new RuntimeException("ClickHouse execute failed", e);
        }
    }

    /**
     * Batch insert data. ClickHouse does not support ON CONFLICT;
     * deduplication relies on the ReplacingMergeTree engine.
     */
    public <T> void saveIgnoreDuplicated(@NonNull List<T> dataList, Class<T> clz) {
        if (dataList.isEmpty()) {
            log.warn("Data is empty. Ignore saving to ClickHouse.");
            return;
        }

        Persistent persistent = clz.getAnnotation(Persistent.class);
        String tableName = (persistent != null && StringUtils.isNotBlank(persistent.name()))
                ? persistent.name()
                : clz.getSimpleName().toLowerCase();

        Field[] fields = clz.getDeclaredFields();
        List<String> columnNames = new ArrayList<>();
        List<Field> fieldList = new ArrayList<>();

        for (Field field : fields) {
            PersistentProperty property = field.getAnnotation(PersistentProperty.class);
            if (null == property) {
                continue;
            }
            columnNames.add(property.name());
            fieldList.add(field);
        }

        if (columnNames.isEmpty()) {
            log.warn("No @PersistentProperty fields found in class: {}", clz.getName());
            return;
        }

        String placeholders = String.join(", ", columnNames.stream().map(c -> "?").toArray(String[]::new));
        String sql = String.format("INSERT INTO %s (%s) VALUES (%s)",
                tableName, String.join(", ", columnNames), placeholders);

        try (Connection conn = dataSource.getConnection()) {
            // Allow large batch inserts spanning many partitions
            try (Statement setStmt = conn.createStatement()) {
                setStmt.execute("SET max_partitions_per_insert_block = 10000");
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (T data : dataList) {
                    int index = 1;
                    for (Field field : fieldList) {
                        field.setAccessible(true);
                        try {
                            Object value = field.get(data);
                            PersistentProperty property = field.getAnnotation(PersistentProperty.class);
                            if (null == value) {
                                setDefaultParameter(stmt, index, property.valueType());
                            } else {
                                setParameter(stmt, index, property.valueType(), value);
                            }
                        } catch (IllegalAccessException e) {
                            PersistentProperty property = field.getAnnotation(PersistentProperty.class);
                            setDefaultParameter(stmt, index, property.valueType());
                        }
                        index++;
                    }
                    stmt.addBatch();
                }
                stmt.executeBatch();
                log.debug("Inserted {} rows into ClickHouse table: {}", dataList.size(), tableName);
            }
        } catch (SQLException e) {
            log.error("Failed to batch insert into ClickHouse table: {}", tableName, e);
            throw new RuntimeException("ClickHouse batch insert failed", e);
        }
    }

    /**
     * Query a single value from ClickHouse (e.g. SELECT MAX(record_time) FROM ...).
     *
     * @param sql SQL query that returns a single column, single row
     * @param <R> return type (e.g. Long)
     * @return the queried value, or null if no result
     */
    @SuppressWarnings("unchecked")
    public <R> R queryOne(String sql) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return (R) rs.getObject(1);
            }
            return null;
        }
    }

    public void shutdown() {
        // ClickHouseDataSource connections are stateless HTTP, no explicit close needed
        log.debug("ClickHouse client shutdown.");
    }

    private void setParameter(PreparedStatement stmt, int index, ValueType valueType, Object value)
            throws SQLException {
        switch (valueType) {
            case BigInt:
                if (value instanceof Double) {
                    stmt.setLong(index, ((Double) value).longValue());
                } else if (value instanceof Number) {
                    stmt.setLong(index, ((Number) value).longValue());
                } else {
                    stmt.setObject(index, value);
                }
                break;
            case Double:
                if (value instanceof Number) {
                    stmt.setDouble(index, ((Number) value).doubleValue());
                } else {
                    stmt.setObject(index, value);
                }
                break;
            case Boolean:
                if (value instanceof Boolean) {
                    stmt.setByte(index, (byte) (((Boolean) value) ? 1 : 0));
                } else if (value instanceof Number) {
                    stmt.setByte(index, ((Number) value).byteValue());
                } else {
                    stmt.setObject(index, value);
                }
                break;
            case Datetime:
                stmt.setObject(index, value);
                break;
            case Json:
                stmt.setString(index, value.toString());
                break;
            case Text:
            default:
                stmt.setString(index, value.toString());
                break;
        }
    }

    private void setDefaultParameter(PreparedStatement stmt, int index, ValueType valueType)
            throws SQLException {
        switch (valueType) {
            case BigInt:
                stmt.setLong(index, 0L);
                break;
            case Double:
                stmt.setDouble(index, 0.0);
                break;
            case Boolean:
                stmt.setByte(index, (byte) 0);
                break;
            case Datetime:
            case Json:
            case Text:
            default:
                stmt.setString(index, "");
                break;
        }
    }
}