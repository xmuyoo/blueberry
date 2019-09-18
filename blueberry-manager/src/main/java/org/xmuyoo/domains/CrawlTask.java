package org.xmuyoo.domains;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CrawlTask {

    public enum SourceType {
        PureUrl,
        Html
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    private Long id;

    @JsonProperty
    @Column
    private Long userId;

    @JsonProperty
    @Column(nullable = false)
    private String sourceName;

    @JsonProperty
    @Column(columnDefinition = "text", nullable = false)
    private String sourceUrl;

    @JsonProperty
    @Column(columnDefinition = "text", nullable = false)
    private SourceType sourceType;

    @JsonProperty
    @Column(columnDefinition = "text", nullable = false)
    private String httpMethod;

    @JsonProperty
    @ElementCollection
    private Map<String, String> queryParameters;

    @JsonProperty
    @ElementCollection
    private Map<String, String> bodyParameters;

    @JsonProperty
    @Column
    private String crawlerName;

    @JsonProperty
    @Column
    private String timeRanges;

    @JsonProperty
    @Column
    private String period;

    @JsonProperty
    @Column
    private boolean active;

    @Column(columnDefinition = "timestamp with time zone", nullable = false)
    private LocalDateTime created;

    @Column(columnDefinition = "text")
    private String description;
}
