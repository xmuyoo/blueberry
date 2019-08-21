package org.xmuyoo.blueberry.crawling.streams;

import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.*;
import org.xmuyoo.blueberry.crawling.Configs;
import org.xmuyoo.blueberry.crawling.Lifecycle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

@Slf4j
public class Publisher<T> implements Lifecycle {

    private static final String PULSAR_SERVICE = "services";
    private static final String SEND_TIMEOUT = "send.timeout";
    private static final String BATCHING_DELAY = "batching.delay";

    private static final int DEFAULT_SEND_TIMEOUT = 100;
    private static final int DEFAULT_MAX_BATCHING_MESSAGES = 1000;
    private static final int DEFAULT_BATCHING_DELAY = 10;

    private static Map<String, Publisher> publishers = new HashMap<>();

    public synchronized static <T> Publisher<T> getInstance(String topic, Class<T> clz) {
        if (publishers.containsKey(topic)) {
            // noinspection unchecked
            return publishers.get(topic);
        }

        Publisher<T> publisher = new Publisher<>(topic, clz);
        publishers.put(topic, publisher);

        return publisher;
    }

    private PulsarClient client;
    private Producer<T> producer;

    private Publisher(String topic, Class<T> clz) {
        Config pulsarConfig = Configs.publisherConfig();
        try {
            client = PulsarClient.builder()
                    .serviceUrl(pulsarConfig.getString(PULSAR_SERVICE))
                    .build();
            int sendTimeout = pulsarConfig.hasPath(SEND_TIMEOUT) ?
                    pulsarConfig.getInt(SEND_TIMEOUT) : DEFAULT_SEND_TIMEOUT;
            int batchingDelay = pulsarConfig.hasPath(BATCHING_DELAY) ?
                    pulsarConfig.getInt(BATCHING_DELAY) : DEFAULT_BATCHING_DELAY;
            producer = client.newProducer(Schema.AVRO(clz))
                    .producerName("publisher-" + clz.getSimpleName())
                    .topic(String.format("persistent://public/default/%s", topic))
                    .sendTimeout(sendTimeout, TimeUnit.SECONDS)
                    .blockIfQueueFull(true)
                    .enableBatching(true)
                    .batchingMaxPublishDelay(batchingDelay, TimeUnit.MILLISECONDS)
                    .create();
        } catch (PulsarClientException e) {
            log.error("Failed to create Publisher client", e);
            throw new RuntimeException("Failed to create Publisher client");
        }
    }

    public void publish(List<T> messageList) {
        messageList.forEach(msg -> producer.sendAsync(msg));
    }

    public void publish(List<T> messageList, BiConsumer<MessageId, Throwable> consumer) {
        messageList.forEach(msg -> {
            CompletableFuture<MessageId> future = producer.sendAsync(msg);
            future.whenCompleteAsync(consumer);
        });
    }

    @Override
    public void start() {
    }

    @Override
    public void shutdown() {
        try {
            if (null != producer) {
                producer.close();
                client.shutdown();
            }
        } catch (PulsarClientException e) {
            log.error("Failed to shutdown publisher");
        }
    }
}
