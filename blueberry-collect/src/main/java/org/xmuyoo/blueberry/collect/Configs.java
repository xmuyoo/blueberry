package org.xmuyoo.blueberry.collect;

import com.google.common.base.Splitter;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.AccessLevel;
import lombok.Setter;

public class Configs {
    public static final Splitter COMMA_SPLITTER = Splitter.on(",").trimResults();
    public static final Splitter LINE_SPLITTER = Splitter.on("-").trimResults();

    private static final String NETWORK_CONFIG = "network";
    private static final String PUBLISHER_CONFIG = "pulsar";
    private static final String META_BASE_CONFIG = "metaBase";
    private static final String DATA_WAREHOUSE_CONFIG = "dataWarehouse";
    private static final String TASKS_SWITCH = "tasks.switch";

    private static final Config applicationConfig = ConfigFactory.load("application");

    @Setter(AccessLevel.PRIVATE)
    private Config jobConfig;

    @Setter(AccessLevel.PRIVATE)
    private Config tasksSwitchConfig;

    public static Configs of(Config config) {
        Configs configs = new Configs();
        configs.tasksSwitchConfig(applicationConfig.getConfig("tasks.switch"));

        return configs;
    }

    public static Config of(String configName) {
        return ConfigFactory.load(configName);
    }

    public static Configs applicationConfig() {
        return of(applicationConfig);
    }

    public String getString(String key, String defaultValue) {
        if (this.jobConfig.hasPath(key))
            return this.jobConfig.getString(key);
        else
            return defaultValue;
    }

    public static Config networkConfig() {
        return applicationConfig.hasPath(NETWORK_CONFIG) ? applicationConfig.getConfig(NETWORK_CONFIG) :
                ConfigFactory.empty();
    }

    public static Config publisherConfig() {
        return applicationConfig.hasPath(PUBLISHER_CONFIG) ? applicationConfig.getConfig(PUBLISHER_CONFIG) :
                ConfigFactory.empty();
    }

    public static Config metaBaseConfig() {
        return applicationConfig.hasPath(META_BASE_CONFIG) ? applicationConfig.getConfig(META_BASE_CONFIG) :
                ConfigFactory.empty();
    }

    public static Config dataWarehouseConfig() {
        return applicationConfig.hasPath(DATA_WAREHOUSE_CONFIG) ? applicationConfig.getConfig(DATA_WAREHOUSE_CONFIG) :
                ConfigFactory.empty();
    }

    public boolean runStockCodeList() {
        return this.tasksSwitchConfig.getBoolean("stock.code.list");
    }

    public boolean runStockSnapshot() {
        return this.tasksSwitchConfig.getBoolean("stock.snapshot");
    }

    public boolean runConvertBondCodeList() {
        return this.tasksSwitchConfig.getBoolean("convert.bond.code.list");
    }
}
