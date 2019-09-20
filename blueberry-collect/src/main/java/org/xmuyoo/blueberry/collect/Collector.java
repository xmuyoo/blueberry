package org.xmuyoo.blueberry.collect;

import com.typesafe.config.Config;
import lombok.NonNull;

public interface Collector extends Lifecycle {

    /**
     * To run a collect task. The task will run only one time.
     */
    void run();

    /**
     * Returns the name of this collector. The name of collector should be unique.
     *
     * @return
     */
    @NonNull
    String name();

    void setStop(boolean stop);

    /**
     * Add config to the collector. If there is no configuration for the collector, set null then;
     * Or the collector don't need a configuration, the collector just leave this function empty.
     * @param config Configuration of the collector
     */
    void setConfig(Config config);
}
