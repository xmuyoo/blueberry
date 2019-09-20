package org.xmuyoo.blueberry.collect;

import com.alibaba.druid.pool.DruidDataSource;
import com.typesafe.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.xmuyoo.blueberry.collect.http.HttpClient;
import org.xmuyoo.blueberry.collect.storage.TimescaleDBClient;

@Configuration
@PropertySource("classpath:application.properties")
public class SpringConfig {

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public TimescaleDBClient timescaleDBClient() {
        Config config = Configs.databaseConfig();

        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setDriverClassName(config.getString("driver.class"));
        druidDataSource.setUrl(config.getString("url"));
        druidDataSource.setUsername(config.getString("user"));
        druidDataSource.setPassword(config.getString("password"));
        druidDataSource.setInitialSize(2);
        druidDataSource.setMinIdle(2);
        druidDataSource.setMaxActive(20);
        druidDataSource.setMaxWait(10000);

        return new TimescaleDBClient(druidDataSource);
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public CollectorManager collectorManager(HttpClient httpClient,
                                           TimescaleDBClient timescaleDBClient) {

        return new CollectorManager(httpClient, timescaleDBClient);
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public HttpClient httpClient() {
        return new HttpClient();
    }
}
