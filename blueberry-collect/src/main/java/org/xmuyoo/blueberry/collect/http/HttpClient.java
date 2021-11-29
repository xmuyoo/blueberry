package org.xmuyoo.blueberry.collect.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventTranslatorTwoArg;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.typesafe.config.Config;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;
import org.xmuyoo.blueberry.collect.Configs;
import org.xmuyoo.blueberry.collect.Lifecycle;
import org.xmuyoo.blueberry.collect.ResponseHandler;
import org.xmuyoo.blueberry.collect.utils.Utils;

@Slf4j
public class HttpClient implements Lifecycle {

    private static final String CLIENT_THREADS_RATIO = "client.threads.ratio";
    private static final int RING_BUFFER_SIZE = 4096;
    private static final TypeReference<byte[]> TYPE_BYTES = new TypeReference<>() {
    };

    private final EventTranslatorTwoArg<RequestWrapper, Request, ResponseHandler> requestTranslator;
    private Disruptor<RequestWrapper> disruptor;
    private RingBuffer<RequestWrapper> requestsQueueBuffer;

    private final OkHttpClient httpClient;

    private Retryer<okhttp3.Response> retryer;

    public HttpClient() {
        httpClient = new OkHttpClient();

        Config config = Configs.networkConfig();
        int cores = Runtime.getRuntime().availableProcessors();
        int clientThreads = config.hasPath(CLIENT_THREADS_RATIO) ?
                cores * config.getInt(CLIENT_THREADS_RATIO) : cores;
        clientThreads =
                clientThreads <= 0 ? Runtime.getRuntime().availableProcessors() : clientThreads;
        RequestHandler[] requestHandlers = new RequestHandler[clientThreads];
        for (int i = 0; i < clientThreads; i++) {
            requestHandlers[i] = new RequestHandler(httpClient);
        }

        ThreadFactory factory = new ThreadFactoryBuilder().setNameFormat("http-client-%s").build();
        disruptor =
                new Disruptor<>(RequestWrapper::new, RING_BUFFER_SIZE, factory, ProducerType.MULTI,
                        new BlockingWaitStrategy());
        disruptor.handleEventsWithWorkerPool(requestHandlers);
        disruptor.setDefaultExceptionHandler(new ExceptionHandler<RequestWrapper>() {
            @Override
            public void handleEventException(Throwable ex, long sequence,
                                             RequestWrapper requestWrapper) {
                log.error(String.format("Request failed: %s", requestWrapper.toString()), ex);
            }

            @Override
            public void handleOnStartException(Throwable ex) {
                // do nothing
                log.error("Error on start", ex);
            }

            @Override
            public void handleOnShutdownException(Throwable ex) {
                // do nothing
                log.error("Error on shutdown", ex);
            }
        });
        requestsQueueBuffer = disruptor.getRingBuffer();
        requestTranslator = (wrapper, sequence, request, handler) -> {
            wrapper.request(request);
            wrapper.responseHandler(handler);
        };

        retryer = RetryerBuilder.<okhttp3.Response>newBuilder()
                .withWaitStrategy(WaitStrategies.fibonacciWait(30, TimeUnit.SECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(6))
                .retryIfException(t -> t instanceof SocketTimeoutException)
                .build();
    }

    public static byte[] getResponseData(okhttp3.Response response) {
        try {
            if (response.isSuccessful()) {
                return response.body().bytes();
            } else {
                log.error("Failed to get response data. Code: {}", response.code());
                return null;
            }
        } catch (Exception e) {
            log.error("Failed to get response data", e);
            return null;
        }
    }

    public static <T> T getResponseData(okhttp3.Response response, Class<T> clz) {
        String contentEncoding = response.headers().get("Content-Encoding");
        byte[] data = getResponseData(response);
        if (null == data) {
            return null;
        } else if ("gzip".equals(contentEncoding)) {
            return Utils.deserialize(decompressGZIPBytes(data), clz);
        } else {
            return Utils.deserialize(data, clz);
        }
    }

    public static <T> T getResponseData(okhttp3.Response response, TypeReference<T> type) {
        String contentEncoding = response.headers().get("Content-Encoding");
        byte[] data = getResponseData(response);
        if (null == data) {
            return null;
        } else if ("gzip".equals(contentEncoding)) {
            return Utils.deserialize(decompressGZIPBytes(data), type);
        } else {
            return Utils.deserialize(data, type);
        }
    }

    public static byte[] decompressGZIPBytes(byte[] zipBytes) {
        try (ByteArrayInputStream in = new ByteArrayInputStream(zipBytes);
             GZIPInputStream gzipInputStream = new GZIPInputStream(in);
             ByteArrayOutputStream out = new ByteArrayOutputStream()
        ) {
            int res = 0;
            byte[] buf = new byte[1024];
            while (res >= 0) {
                res = gzipInputStream.read(buf, 0, buf.length);
                if (res > 0) {
                    out.write(buf, 0, res);
                }
            }

            return out.toByteArray();
        } catch (IOException e) {
            log.error("Failed to decompress gzip bytes", e);
            return null;
        }
    }

    public void async(Request request, ResponseHandler handler) {
        requestsQueueBuffer.publishEvent(requestTranslator, request, handler);
    }

    public <I, O> O sync(Request req, Function<I, O> applier, Class<I> clz) throws Exception {
        I data = sync(req, resp -> getResponseData(resp, clz));
        if (null == data) {
            return null;
        }

        return applier.apply(data);
    }

    public <R> R sync(Request req, Function<okhttp3.Response, R> responseRFunction) throws Exception {
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder();
        builder.url(req.fullUrl());

        req.headers().forEach(builder::header);

        final okhttp3.Request request;
        switch (req.method()) {
            case GET:
                request = builder.build();
                break;
            case PUT:
                builder.put(RequestBody.create(req.body()));
                request = builder.build();
                break;
            case POST:
                if (null != req.body()) {
                    builder.post(RequestBody.create(req.body()));
                }
                request = builder.build();
                break;
            default:
                // do as GET
                log.warn("No http method specified and use GET by default: {}", req.toString());
                request = builder.build();
                break;
        }

        okhttp3.Response response = retryer.call(() -> httpClient.newCall(request).execute());
        R r = responseRFunction.apply(response);
        response.close();

        return r;
    }

    @Override
    public void start() {
        disruptor.start();
    }

    @Override
    public void shutdown() {
        if (null != disruptor) {
            disruptor.shutdown();
        }
    }

    @NoArgsConstructor
    @Getter
    @Setter
    @ToString
    private static class RequestWrapper {
        private Request request;
        private ResponseHandler responseHandler;
        private boolean sync = false;
    }

    private class RequestHandler implements WorkHandler<RequestWrapper> {

        private final OkHttpClient httpClient;

        private RequestHandler(OkHttpClient httpClient) {
            this.httpClient = httpClient;
        }

        @Override
        public void onEvent(RequestWrapper requestWrapper) {
            ResponseHandler handler = requestWrapper.responseHandler();
            final Request req = requestWrapper.request();

            okhttp3.Request.Builder builder = new okhttp3.Request.Builder();
            builder.url(req.fullUrl());
            req.headers().forEach(builder::header);

            okhttp3.Response syncResponse = null;
            okhttp3.Request request;
            switch (req.method()) {
                case GET:
                    request = builder.build();
                    break;
                case PUT:
                    builder.put(RequestBody.create(req.body()));
                    request = builder.build();
                case POST:
                    builder.post(RequestBody.create(req.body()));
                    request = builder.build();
                    break;
                default:
                    // do as GET
                    log.warn("No http method specified and use GET by default: {}", req.toString());
                    request = builder.build();
                    break;
            }

            try {
                if (!requestWrapper.sync()) {
                    httpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            okhttp3.Request request = call.request();
                            log.error(String.format("Failed to request: %s", request.toString()),
                                    e);
                        }

                        @Override
                        public void onResponse(@NotNull Call call,
                                               @NotNull okhttp3.Response response)
                                throws IOException {
                            int code = response.code();

                            byte[] data = null;
                            ResponseBody responseBody = response.body();
                            if (null != responseBody) {
                                data = responseBody.bytes();
                            }

                            Map<String, String> headers = new HashMap<>();
                            Iterator<kotlin.Pair<String, String>> iterator =
                                    response.headers().iterator();
                            while (iterator.hasNext()) {
                                kotlin.Pair<String, String> entry = iterator.next();
                                headers.put(entry.getFirst(), entry.getSecond());
                            }
                            handler.handle(req, new Response(headers, code, data));
                            response.close();
                        }
                    });
                } else {
                    syncResponse = httpClient.newCall(request).execute();
                }
            } catch (IOException e) {
                log.error(String.format("Failed to request %s", req.toString()), e);
            } finally {
                if (null != syncResponse) {
                    syncResponse.close();
                }
            }
        }
    }
}