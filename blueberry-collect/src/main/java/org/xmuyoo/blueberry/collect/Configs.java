package org.xmuyoo.blueberry.collect;

import com.google.common.base.Splitter;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.List;

public class Configs {
    public static final Splitter COMMA_SPLITTER = Splitter.on(",").trimResults();
    public static final Splitter LINE_SPLITTER = Splitter.on("-").trimResults();

    private static final String COLLECTOR_LIST = "collector.list";
    private static final String COLLECTOR_CONFIG_PREFIX = "collector.";
    private static final String NETWORK_CONFIG = "network";
    private static final String PUBLISHER_CONFIG = "pulsar";
    private static final String META_BASE_CONFIG = "metaBase";
    private static final String DATA_WAREHOUSE_CONFIG = "dataWarehouse";

    private static final Config config = ConfigFactory.load("application");
    private static List<String> collectors =
            COMMA_SPLITTER.splitToList(config.getString(COLLECTOR_LIST));

    public static List<String> collectors() {
        return collectors;
    }

    public static Config config(String configKey) {
        return config.getConfig(COLLECTOR_CONFIG_PREFIX + configKey);
    }

    public static Config networkConfig() {
        return config.hasPath(NETWORK_CONFIG) ? config.getConfig(NETWORK_CONFIG) :
                ConfigFactory.empty();
    }

    public static Config publisherConfig() {
        return config.hasPath(PUBLISHER_CONFIG) ? config.getConfig(PUBLISHER_CONFIG) :
                ConfigFactory.empty();
    }

    public static Config metaBaseConfig() {
        return config.hasPath(META_BASE_CONFIG) ? config.getConfig(META_BASE_CONFIG) :
                ConfigFactory.empty();
    }

    public static Config dataWarehouseConfig() {
        return config.hasPath(DATA_WAREHOUSE_CONFIG) ? config.getConfig(DATA_WAREHOUSE_CONFIG) :
                ConfigFactory.empty();
    }
}
