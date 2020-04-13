package org.xmuyoo.blueberry.collect;

import org.xmuyoo.blueberry.collect.http.Request;
import org.xmuyoo.blueberry.collect.http.Response;

public interface ResponseHandler {

    void handle(Request request, Response response);
}
