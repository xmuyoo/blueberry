package org.xmuyoo.domains;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(
        of = {"id", "sourceName", "sourceUrl", "sourceType", "httpMethod", "bodyPattern",
                "collectorId", "timeRanges", "period"})
public class CollectTask {

    public enum SourceType {
        API,
        HTML
    }

    @JsonProperty
    private String id;

    @JsonProperty
    private String sourceName;

    /**
     * SourceUrl is a url pattern which may contain some data schemas.
     * Each data schema in the pattern should be the id of a existing {@link DataSchema}.
     */
    @JsonProperty
    private String sourceUrl;

    @JsonProperty
    private SourceType sourceType;

    @JsonProperty
    private String httpMethod;

    /**
     * BodyPattern is a body parameters pattern which may contain some data schemas.
     * Each data schema in the pattern should be the id of a existing {@link DataSchema}.
     */
    @JsonProperty
    private String bodyPattern;

    @JsonProperty
    private String collectorId;

    @JsonProperty
    private String timeRanges;

    @JsonProperty
    private String period;

    @JsonProperty
    private boolean active;

    @JsonProperty
    private Timestamp created;

    @JsonProperty
    private String description;

    public void setSourceType(String sourceType) {
        this.sourceType = SourceType.valueOf(sourceType.toUpperCase());
    }
}