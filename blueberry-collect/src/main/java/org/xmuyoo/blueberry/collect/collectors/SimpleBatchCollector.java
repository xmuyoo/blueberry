package org.xmuyoo.blueberry.collect.collectors;

import com.typesafe.config.Config;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.xmuyoo.blueberry.collect.Collector;
import org.xmuyoo.blueberry.collect.http.HttpClient;
import org.xmuyoo.blueberry.collect.storage.TimescaleDBClient;

import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class SimpleBatchCollector implements Collector {

    protected volatile boolean isRunning = true;
    protected volatile String name;

    protected HttpClient httpClient;
    protected TimescaleDBClient timescaleDBClient;

    @Getter(AccessLevel.PROTECTED)
    private Config config;

    @Override
    public void setStop(boolean stop) {
        this.isRunning = stop;
    }

    @Override
    public void setConfig(Config config) {
        this.config = config;
    }

    @Override
    public String name() {
        if (StringUtils.isEmpty(this.name))
            this.name = this.getClass().getSimpleName();

        return this.name;
    }

    protected void waitMinutes(long minutes) {
        try {
            TimeUnit.MINUTES.sleep(minutes);
        } catch (Exception e) {
            log.error(
                    String.format("Thread [%s] is interrupted", Thread.currentThread().getName()),
                    e);
            Thread.currentThread().interrupt();
        }
    }
}
