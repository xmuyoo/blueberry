package org.xmuyoo.blueberry.collect.collectors.data.source;

import java.util.Map;

public interface RemoteDataSource {

    Map<String, String> getRemoteHTTPRequestHeaders();

    String getRemoteHTTPRequestCookie();

    int getCollectIntervalMilliseconds();
}
