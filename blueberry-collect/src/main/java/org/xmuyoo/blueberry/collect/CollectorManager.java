package org.xmuyoo.blueberry.collect;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Configurable;
import org.xmuyoo.blueberry.collect.http.HttpClient;
import org.xmuyoo.blueberry.collect.storage.TimescaleDBClient;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configurable
public class CollectorManager implements Lifecycle {

    private static final String COLLECTOR_CLASS = "class";

    private ExecutorService collectorExecutors;
    private Map<String, Collector> collectors;
    private List<String> collectorNames;
    private HttpClient httpClient;
    private TimescaleDBClient timescaleDBClient;

    public CollectorManager(HttpClient httpClient, TimescaleDBClient timescaleDBClient) {
        this.httpClient = httpClient;
        this.timescaleDBClient = timescaleDBClient;

        collectorNames = Configs.collectors();
        ThreadFactory factory = new ThreadFactoryBuilder().setNameFormat("collector-mgr-%s").build();
        this.collectorExecutors =
                Executors.newFixedThreadPool(collectorNames.size(), factory);
        collectors = new HashMap<>();
    }

    @Override
    public void start() {
        loadCollectors();
        for (Map.Entry<String, Collector> entry : collectors.entrySet()) {
            Collector collector = entry.getValue();
            collector.start();
            collectorExecutors.submit(collector::run);
        }

        while (!Thread.currentThread().isInterrupted()) {
            loadCollectors();
            try {
                TimeUnit.DAYS.sleep(7);
            } catch (InterruptedException e) {
                log.error("CollectorManager is interrupted", e);
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
    }

    private void loadCollectors() {
        for (String collectorName : collectorNames) {
            Config collectorConfig = Configs.config(collectorName);
            String collectorClass = collectorConfig.getString(COLLECTOR_CLASS);
            try {
                Collector collector = (Collector) Class.forName(collectorClass).newInstance();
                collector.setConfig(collectorConfig);
                Class superClz = collector.getClass().getSuperclass();
                for (Field field : superClz.getDeclaredFields()) {
                    String fieldClassName = field.getType().getSimpleName();
                    if (fieldClassName.equals(HttpClient.class.getSimpleName())) {
                        field.setAccessible(true);
                        field.set(collector, httpClient);
                    } else if (fieldClassName.equals(TimescaleDBClient.class.getSimpleName())) {
                        field.setAccessible(true);
                        field.set(collector, timescaleDBClient);
                    }
                }
                collectors.put(collectorName, collector);
            } catch (Exception e) {
                log.error(String.format("Can not load collector %s", collectorClass), e);
                throw new RuntimeException(String.format("Can not load collector %s", collectorClass));
            }
        }
    }
}
