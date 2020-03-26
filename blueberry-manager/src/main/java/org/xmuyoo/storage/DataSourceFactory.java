package org.xmuyoo.storage;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSourceFactory {

    public static final String TIMESCALE_DB = "timescale";

    @Value("${timescaleDB.url}")
    private String timescaleDBUrl;
    @Value("${timescaleDB.user}")
    private String timescaleDBUser;
    @Value("${timescaleDB.password}")
    private String timescaleDBPassword;
    @Value("${timescaleDB.driver.class}")
    private String timescaleDBDriver;
    @Value("${timescaleDB.maxActive}")
    private int maxActive;

    @Bean(name = TIMESCALE_DB, initMethod = "init", destroyMethod = "close")
    public DruidDataSource timescale() {
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setDriverClassName(timescaleDBDriver);
        druidDataSource.setUrl(timescaleDBUrl);
        druidDataSource.setUsername(timescaleDBUser);
        druidDataSource.setPassword(timescaleDBPassword);
        druidDataSource.setInitialSize(2);
        druidDataSource.setMinIdle(2);
        druidDataSource.setMaxActive(maxActive);
        druidDataSource.setMaxWait(10000);

        return druidDataSource;
    }
}