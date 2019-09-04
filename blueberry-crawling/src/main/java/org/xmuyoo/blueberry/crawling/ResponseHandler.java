package org.xmuyoo.blueberry.crawling;

import org.xmuyoo.blueberry.crawling.http.Response;

public interface ResponseHandler {

    void handle(Response response);
}
