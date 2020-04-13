package org.xmuyoo.blueberry.collect.domains;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(of = {"createdTime", "tagId"})
@ToString
public class SeriesData {

    @JsonProperty
    private long createdTime;

    @JsonProperty
    private double value;

    @JsonProperty
    private long tagId;

    @JsonProperty
    private Map<String, String> tags;
}
