package org.xmuyoo.blueberry.crawling;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Configurable;
import org.xmuyoo.blueberry.crawling.http.HttpClient;
import org.xmuyoo.blueberry.crawling.storage.TimescaleDBClient;

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
public class CrawlerManager implements Lifecycle {

    private static final String CRAWLER_CLASS = "class";

    private ExecutorService crawlerExecutors;
    private Map<String, Crawler> crawlers;
    private List<String> crawlerNames;
    private HttpClient httpClient;
    private TimescaleDBClient timescaleDBClient;

    public CrawlerManager(HttpClient httpClient, TimescaleDBClient timescaleDBClient) {
        this.httpClient = httpClient;
        this.timescaleDBClient = timescaleDBClient;

        crawlerNames = Configs.crawlers();
        ThreadFactory factory = new ThreadFactoryBuilder().setNameFormat("crawler-mgr-%s").build();
        this.crawlerExecutors =
                Executors.newFixedThreadPool(crawlerNames.size(), factory);
        crawlers = new HashMap<>();
    }

    @Override
    public void start() {
        loadCrawlers();
        for (Map.Entry<String, Crawler> entry : crawlers.entrySet()) {
            Crawler crawler = entry.getValue();
            crawler.start();
            crawlerExecutors.submit(crawler::run);
        }

        while (!Thread.currentThread().isInterrupted()) {
            loadCrawlers();
            try {
                TimeUnit.DAYS.sleep(7);
            } catch (InterruptedException e) {
                log.error("CrawlerManager is interrupted", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void shutdown() {
        shutdownExecutorService(crawlerExecutors);
        for (Map.Entry<String, Crawler> entry : crawlers.entrySet()) {
            Crawler crawler = entry.getValue();
            crawler.shutdown();
        }
    }

    private void loadCrawlers() {
        for (String crawlerName : crawlerNames) {
            Config crawlerConfig = Configs.config(crawlerName);
            String crawlerClass = crawlerConfig.getString(CRAWLER_CLASS);
            try {
                Crawler crawler = (Crawler) Class.forName(crawlerClass).newInstance();
                crawler.setConfig(crawlerConfig);
                Class superClz = crawler.getClass().getSuperclass();
                for (Field field : superClz.getDeclaredFields()) {
                    String fieldClassName = field.getType().getSimpleName();
                    if (fieldClassName.equals(HttpClient.class.getSimpleName())) {
                        field.setAccessible(true);
                        field.set(crawler, httpClient);
                    } else if (fieldClassName.equals(TimescaleDBClient.class.getSimpleName())) {
                        field.setAccessible(true);
                        field.set(crawler, timescaleDBClient);
                    }
                }
                crawlers.put(crawlerName, crawler);
            } catch (Exception e) {
                log.error(String.format("Can not load crawler %s", crawlerClass), e);
                throw new RuntimeException(String.format("Can not load crawler %s", crawlerClass));
            }
        }
    }
}
