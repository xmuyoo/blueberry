package org.xmuyoo.blueberry.collect.http;

import com.google.common.base.Joiner;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.http.HttpMethod;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ToString
@Getter
@Setter
@NoArgsConstructor
public class Request {

    private static final Joiner QUERY_JOINER = Joiner.on("&");
    private static final Joiner PARAMETER_JOINER = Joiner.on("=");

    public enum HttpProtocol {
        V1_1("http"),
        TLS("https"),
        V2("https");

        private final String protocolValue;

        HttpProtocol(String protocolValue) {
            this.protocolValue = protocolValue;
        }

        public String protocolValue() {
            return this.protocolValue;
        }
    }

    private HttpProtocol protocol = HttpProtocol.V1_1;
    private String host;
    private int port;
    private HttpMethod method = HttpMethod.GET;
    private Map<String, String> headers = new HashMap<>();
    private String path;
    private Map<String, String> parameters = new HashMap<>();
    private byte[] body;

    // Sent timestamp
    private long sentTime = System.currentTimeMillis();

    public String fullUrl() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(protocol.protocolValue()).append("://");

        String location;
        if (0 == port)
            location = host;
        else
            location = host + ":" + port;
        stringBuilder.append(location);
        if (null != parameters && !parameters.isEmpty()) {
            String queryStr = QUERY_JOINER.join(
                    parameters.entrySet()
                            .stream()
                            .map(entry -> PARAMETER_JOINER.join(entry.getKey(), entry.getValue()))
                            .collect(Collectors.toList()));
            stringBuilder.append("/").append("?").append(queryStr);
        }

        return stringBuilder.toString();
    }
}
