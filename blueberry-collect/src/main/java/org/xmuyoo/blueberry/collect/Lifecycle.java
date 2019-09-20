package org.xmuyoo.blueberry.collect;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public interface Lifecycle {

    void start();

    void shutdown();

    default void shutdownExecutorService(ExecutorService executorService) {
        if (null != executorService) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();

                    if (!executorService.awaitTermination(10, TimeUnit.SECONDS))
                        executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
