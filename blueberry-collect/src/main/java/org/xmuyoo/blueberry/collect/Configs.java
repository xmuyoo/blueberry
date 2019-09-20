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
    private static final String DATABASE_CONFIG = "database";

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

    public static Config databaseConfig() {
        return config.hasPath(DATABASE_CONFIG) ? config.getConfig(DATABASE_CONFIG) :
                ConfigFactory.empty();
    }
}
