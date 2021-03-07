package org.xmuyoo.blueberry.collect.collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.xmuyoo.blueberry.collect.Collector;
import org.xmuyoo.blueberry.collect.domains.DataSchema;
import org.xmuyoo.blueberry.collect.storage.PgClient;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class BasicCollector implements Collector {

    private static final String DATA_SCHEMA_MAPPING_SQL_FORMAT =
            "INSERT INTO data_schema(collect_task_id, data_schema_id) " +
                    "VALUES('%s', '%s') " +
                    "ON CONFLICT(collect_task_id, data_schema_id) DO NOTHING";

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
//            storage.saveIgnoreDuplicated(dataSchemaList, DataSchema.class);
//            for (DataSchema dataSchema : dataSchemaList) {
//                String sql = String.format(DATA_SCHEMA_MAPPING_SQL_FORMAT, collectorName, dataSchema.id());
//                storage.execute(sql);
//            }
        } catch (Exception e) {
            log.error(String.format("Failed to register data schema for collector [%s:%s]",
                    name(), collectorName), e);
        }
    }
}