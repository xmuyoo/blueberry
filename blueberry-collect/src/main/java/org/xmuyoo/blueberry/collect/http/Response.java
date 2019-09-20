package org.xmuyoo.blueberry.collect.http;

import lombok.*;

import java.util.Map;

@Getter
@Setter(AccessLevel.PACKAGE)
@NoArgsConstructor
@AllArgsConstructor
public class Response {

    private Map<String, String> headers;
    private int status;
    private byte[] data;

    @Override
    public String toString() {
        return "Response{headers=[" + (null == headers ? null : headers.toString()) + "]," +
                "status=[" + status + "]," +
                "data=[" + new String(data) + "]" +
                "}";
    }
}
