package org.xmuyoo.blueberry.crawling;

import com.typesafe.config.Config;
import lombok.NonNull;

public interface Crawler extends Lifecycle {

    /**
     * To run a crawling task. The task will run only one time.
     */
    void run();

    /**
     * Returns the name of this crawler. The name of crawler should be unique.
     *
     * @return
     */
    @NonNull
    String name();

    void setStop(boolean stop);

    /**
     * Add config to the crawler. If there is no configuration for the crawler, set null then;
     * Or the crawler don't need a configuration, the crawler just leave this function empty.
     * @param config Configuration of the crawler
     */
    void setConfig(Config config);
}
