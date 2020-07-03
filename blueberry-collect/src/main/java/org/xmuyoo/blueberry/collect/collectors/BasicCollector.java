package org.xmuyoo.blueberry.collect.collectors;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.StringUtils;
import org.xmuyoo.blueberry.collect.Collector;
import org.xmuyoo.blueberry.collect.TaskDefinition;
import org.xmuyoo.blueberry.collect.domains.CollectRecord;
import org.xmuyoo.blueberry.collect.domains.DataSchema;
import org.xmuyoo.blueberry.collect.http.HttpClient;
import org.xmuyoo.blueberry.collect.storage.PgClient;
import org.xmuyoo.blueberry.collect.utils.Utils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class BasicCollector implements Collector {

    private static final String TASK_DATA_SCHEMA_MAPPING_SQL_FORMAT =
            "INSERT INTO collect_task_data_schema(collect_task_id, data_schema_id) " +
                    "VALUES('%s', '%s') " +
                    "ON CONFLICT(collect_task_id, data_schema_id) DO NOTHING";

    protected volatile boolean isRunning = true;
    protected volatile String name;

    protected HttpClient httpClient;

    @Resource(name = "dataWarehouse")
    protected PgClient dataWarehouse;

    @Resource(name = "metaBase")
    private PgClient metaBase;

    @Getter(AccessLevel.PROTECTED)
    private TaskDefinition taskDefinition;

    @Getter(AccessLevel.PROTECTED)
    private String taskId;

    private Long periodTime;
    private TimeUnit periodTimeUnit;

    public BasicCollector(TaskDefinition taskDefinition) {
        this.taskDefinition = taskDefinition;
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
        init();
        registerDataSchema();
        start();

        while (isRunning) {
            if (!isAvailable()) {
                wait(periodTime, periodTimeUnit);
                continue;
            }

            try {
                boolean success = collect();
                LocalDateTime now = LocalDateTime.now();

                CollectRecord collectRecord = new CollectRecord();
                String recordId = Utils.MURMUR3
                        .hashBytes(String.format(
                                "%s:%s", taskDefinition.id(), now.toString()).getBytes())
                        .toString();
                collectRecord.id(recordId);
                collectRecord.status(success);
                collectRecord.collectTaskId(taskDefinition.id());
                collectRecord.collectedDatetime(now);

                metaBase.saveIgnoreDuplicated(Collections.singletonList(collectRecord),
                        CollectRecord.class);
            } catch (Throwable t) {
                log.error(String.format("Error in collector: [%s:%s]", name(),
                        taskDefinition.collectorName()), t);
            }

            wait(periodTime, periodTimeUnit);
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

    private void init() {
        Pair<Long, TimeUnit> timePair = taskDefinition.getPeriod();
        this.periodTime = timePair.getKey();
        this.periodTimeUnit = timePair.getValue();

        this.taskId = this.taskDefinition.id();
    }

    private void registerDataSchema() {
        List<DataSchema> dataSchemaList = getDataSchemaList();
        try {
            metaBase.saveIgnoreDuplicated(dataSchemaList, DataSchema.class);
            for (DataSchema dataSchema : dataSchemaList) {
                String sql = String.format(TASK_DATA_SCHEMA_MAPPING_SQL_FORMAT, taskDefinition.id(),
                        dataSchema.id());
                metaBase.execute(sql);
            }
        } catch (Exception e) {
            log.error(String.format("Failed to register data schema for collector [%s:%s]",
                    name(), taskDefinition.collectorName()), e);
        }
    }
}