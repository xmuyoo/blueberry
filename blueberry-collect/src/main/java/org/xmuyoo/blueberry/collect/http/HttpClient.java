package org.xmuyoo.blueberry.collect.http;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.typesafe.config.Config;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.xmuyoo.blueberry.collect.Configs;
import org.xmuyoo.blueberry.collect.Lifecycle;
import org.xmuyoo.blueberry.collect.ResponseHandler;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.function.Function;

@Slf4j
public class HttpClient implements Lifecycle {

    private static final String CLIENT_THREADS_RATIO = "client.threads.ratio";
    private static final int RING_BUFFER_SIZE = 4096;

    private final EventTranslatorTwoArg<RequestWrapper, Request, ResponseHandler> requestTranslator;
    private Disruptor<RequestWrapper> disruptor;
    private RingBuffer<RequestWrapper> requestsQueueBuffer;

    private final OkHttpClient httpClient;

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

    }

    public void async(Request request, ResponseHandler handler) {
        requestsQueueBuffer.publishEvent(requestTranslator, request, handler);
    }

    public <R> R sync(Request req, Function<okhttp3.Response, R> responseRFunction) throws Exception {
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder();
        if (StringUtils.isBlank(req.url()))
            builder.url(req.fullUrl());
        else
            builder.url(req.url());

        req.headers().forEach(builder::header);

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

        okhttp3.Response response = httpClient.newCall(request).execute();

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
        if (null != disruptor)
            disruptor.shutdown();
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
                            if (null != responseBody)
                                data = responseBody.bytes();

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
                if (null != syncResponse)
                    syncResponse.close();
            }
        }
    }
}