package org.xmuyoo.blueberry.collect;

import com.alibaba.druid.pool.DruidDataSource;
import com.typesafe.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.xmuyoo.blueberry.collect.http.HttpClient;
import org.xmuyoo.blueberry.collect.storage.PgClient;

@Configuration
@PropertySource("classpath:application.properties")
public class SpringConfig {

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public HttpClient httpClient() {
        return new HttpClient();
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public CollectorMaster collectorManager(HttpClient httpClient) {
        Config metaBaseConfig = Configs.metaBaseConfig();
        DruidDataSource metaDataSource = new DruidDataSource();
        metaDataSource.setDriverClassName(metaBaseConfig.getString("driver.class"));
        metaDataSource.setUrl(metaBaseConfig.getString("url"));
        metaDataSource.setUsername(metaBaseConfig.getString("user"));
        metaDataSource.setPassword(metaBaseConfig.getString("password"));
        metaDataSource.setInitialSize(2);
        metaDataSource.setMinIdle(2);
        metaDataSource.setMaxActive(10);
        metaDataSource.setMaxWait(10000);

        PgClient metaBase = new PgClient(metaDataSource);

        int cores = Runtime.getRuntime().availableProcessors();
        Config dataWarehouseConfig = Configs.dataWarehouseConfig();
        DruidDataSource warehouseDataSource = new DruidDataSource();
        warehouseDataSource.setDriverClassName(dataWarehouseConfig.getString("driver.class"));
        warehouseDataSource.setUrl(dataWarehouseConfig.getString("url"));
        warehouseDataSource.setUsername(dataWarehouseConfig.getString("user"));
        warehouseDataSource.setPassword(dataWarehouseConfig.getString("password"));
        warehouseDataSource.setInitialSize(cores);
        warehouseDataSource.setMinIdle(2);
        warehouseDataSource.setMaxActive(cores * 4);
        warehouseDataSource.setMaxWait(10000);

        PgClient dataWarehouse = new PgClient(warehouseDataSource);

        return new CollectorMaster(httpClient, metaBase, dataWarehouse);
    }
}
