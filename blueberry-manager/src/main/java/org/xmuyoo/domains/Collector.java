package org.xmuyoo.domains;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class Collector {

    @JsonProperty
    private String id;

    @JsonProperty
    private String name;

    @JsonProperty
    private String driver;

    @JsonProperty
    private String alias;

    @JsonProperty
    private String description;
}
