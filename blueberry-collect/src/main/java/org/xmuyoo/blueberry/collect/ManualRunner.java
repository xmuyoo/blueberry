package org.xmuyoo.blueberry.collect;

import com.alibaba.druid.pool.DruidDataSource;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.xmuyoo.blueberry.collect.collectors.ConvertibleBondCodeCollector;
import org.xmuyoo.blueberry.collect.collectors.ConvertibleBondHistoryCollector;
import org.xmuyoo.blueberry.collect.collectors.StockCodeCollector;
import org.xmuyoo.blueberry.collect.collectors.StockKLineCollector;
import org.xmuyoo.blueberry.collect.collectors.StockSnapshotCollector;
import org.xmuyoo.blueberry.collect.http.HttpClient;
import org.xmuyoo.blueberry.collect.storage.PgClient;

@Slf4j
public class ManualRunner {

    public static void main(String[] args) throws Exception {
        // Prepare components
        HttpClient httpClient = new HttpClient();

        Config dataWarehouseConfig = Configs.dataWarehouseConfig();
        DruidDataSource metaDataSource = new DruidDataSource();
        metaDataSource.setDriverClassName(dataWarehouseConfig.getString("driver.class"));
        metaDataSource.setUrl(dataWarehouseConfig.getString("url"));
        metaDataSource.setUsername(dataWarehouseConfig.getString("user"));
        metaDataSource.setPassword(dataWarehouseConfig.getString("password"));
        metaDataSource.setInitialSize(2);
        metaDataSource.setMinIdle(2);
        metaDataSource.setMaxActive(10);
        metaDataSource.setMaxWait(10000);

        PgClient storage = new PgClient(metaDataSource);

        Configs appConfigs = Configs.applicationConfig();
        // Stock code list
        if (appConfigs.runStockCodeList()) {
            StockCodeCollector stockCodeCollector = new StockCodeCollector(storage, httpClient);
            stockCodeCollector.run();
        }
        // Stock snapshot
        if (appConfigs.runStockSnapshot()) {
            StockSnapshotCollector stockSnapshotCollector = new StockSnapshotCollector(storage, httpClient);
            stockSnapshotCollector.run();
        }
        // Convertible bond code list
        if (appConfigs.runConvertBondCodeList()) {
            ConvertibleBondCodeCollector convertibleBondCodeCollector =
                    new ConvertibleBondCodeCollector(storage, httpClient);
            convertibleBondCodeCollector.run();
        }
        // Stock K Line
        if (appConfigs.runStockKLine()) {
            StockKLineCollector stockKLineCollector = new StockKLineCollector(storage, httpClient);
            stockKLineCollector.run();
        }
        // Convertible Bond History
        if (appConfigs.runConvertibleBondHistory()) {
            ConvertibleBondHistoryCollector convertibleBondHistoryCollector =
                    new ConvertibleBondHistoryCollector(storage, httpClient);
            convertibleBondHistoryCollector.run();
        }

        storage.shutdown();
    }
}
