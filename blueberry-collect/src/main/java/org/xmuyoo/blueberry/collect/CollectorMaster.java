package org.xmuyoo.blueberry.collect;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Configurable;
import org.xmuyoo.blueberry.collect.http.HttpClient;
import org.xmuyoo.blueberry.collect.storage.PgClient;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Configurable
public class CollectorMaster implements Lifecycle {

    private static final String LOAD_COLLECTOR_SQL =
            "SELECT id, description, source_name, source_url, source_type, " +
                    "time_ranges, period, body_pattern, active, http_method, " +
                    "collector_name, collector_alias, collector_driver FROM task_definition";

    private ExecutorService collectorExecutors;
    private Map<String, Collector> collectors;
    private HttpClient httpClient;
    private PgClient metaBase;
    private PgClient dataWarehouse;
    private AtomicBoolean loaded = new AtomicBoolean(false);

    public CollectorMaster(HttpClient httpClient, PgClient metaBase, PgClient dataWarehouse) {
        this.httpClient = httpClient;
        this.metaBase = metaBase;
        this.dataWarehouse = dataWarehouse;
        collectors = new HashMap<>();
        ThreadFactory factory =
                new ThreadFactoryBuilder().setNameFormat("collector-mgr-%s").build();
        this.collectorExecutors = Executors.newCachedThreadPool(factory);
    }

    @Override
    public void start() {
        loadCollectors();
        for (Map.Entry<String, Collector> entry : collectors.entrySet()) {
            Collector collector = entry.getValue();

            log.info("Try to run {}", collector.name());
            collector.start();
            collectorExecutors.submit(collector::run);
        }

        while (!Thread.currentThread().isInterrupted()) {
            loadCollectors();
            try {
                TimeUnit.MINUTES.sleep(1);
            } catch (InterruptedException e) {
                log.error("CollectorMaster is interrupted", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void shutdown() {

        shutdownExecutorService(collectorExecutors);
        for (Map.Entry<String, Collector> entry : collectors.entrySet()) {
            Collector collector = entry.getValue();
            collector.shutdown();
        }

        dataWarehouse.shutdown();
        metaBase.shutdown();
    }

    private void loadCollectors() {

        // Load collect tasks from Database
        List<TaskDefinition> taskDefinitionList;
        try {
            taskDefinitionList = metaBase.queryList(LOAD_COLLECTOR_SQL, TaskDefinition.class);
        } catch (SQLException e) {
            log.error("Failed to query task definitions", e);
            return;
        }
        if (taskDefinitionList.isEmpty()) {
            log.warn("Collector Task List is empty");
            return;
        }

        for (TaskDefinition taskDefinition : taskDefinitionList) {
            if (!taskDefinition.active())
                continue;

            String collectorName = taskDefinition.collectorName();
            String collectorDriver = taskDefinition.collectorDriver();
            try {
                Collector collector = (Collector) Class.forName(collectorDriver)
                        .getConstructor(TaskDefinition.class)
                        .newInstance(taskDefinition);
                Class superClz = collector.getClass().getSuperclass();

                for (Field field : superClz.getDeclaredFields()) {
                    String fieldClassName = field.getType().getSimpleName();
                    if (fieldClassName.equals(HttpClient.class.getSimpleName())) {
                        field.setAccessible(true);
                        field.set(collector, httpClient);
                    } else if (fieldClassName.equals(PgClient.class.getSimpleName())) {
                        field.setAccessible(true);
                        Resource resource = field.getAnnotation(Resource.class);
                        String name = resource.name();
                        if (name.equalsIgnoreCase("metaBase"))
                            field.set(collector, metaBase);
                        else
                            field.set(collector, dataWarehouse);
                    }
                }
                collectors.put(collectorName, collector);
            } catch (Exception e) {
                log.error(String.format("Can not load collector [%s:%s]",
                                        taskDefinition.collectorName(), collectorDriver), e);
                throw new RuntimeException(
                        String.format("Can not load collector [%s:%s]",
                                      taskDefinition.collectorName(), collectorDriver));
            }
        }

        loaded.set(true);
    }
}
