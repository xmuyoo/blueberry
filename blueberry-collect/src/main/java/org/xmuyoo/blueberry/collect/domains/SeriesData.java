package org.xmuyoo.blueberry.collect.domains;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSortedMap;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
    private ImmutableSortedMap<String, String> tags;
}
