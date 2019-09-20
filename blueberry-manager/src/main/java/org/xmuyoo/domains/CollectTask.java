package org.xmuyoo.domains;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CollectTask {

    public enum SourceType {
        Api,
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

    /**
     * SourceUrl is a url pattern which may contain some data schemas.
     * Each data schema in the pattern should be the id of a existing {@link DataSchema}.
     */
    @JsonProperty
    @Column(columnDefinition = "text", nullable = false)
    private String sourceUrl;

    @JsonProperty
    @Column(columnDefinition = "text", nullable = false)
    private SourceType sourceType;

    @JsonProperty
    @Column(columnDefinition = "text", nullable = false)
    private String httpMethod;

    /**
     * BodyPattern is a body parameters pattern which may contain some data schemas.
     * Each data schema in the pattern should be the id of a existing {@link DataSchema}.
     */
    @JsonProperty
    @Column(columnDefinition = "text")
    private String bodyPattern;

    @JsonProperty
    @ManyToOne(cascade = CascadeType.ALL)
    private Collector collector;

    @JsonProperty
    @ElementCollection
    private Set<DataSchema> dataSchemaSet;

    @JsonProperty
    @Column
    private String timeRanges;

    @JsonProperty
    @Column(columnDefinition = "text not null")
    private String period;

    @JsonProperty
    @Column
    private boolean active;

    @Column(columnDefinition = "timestamp with time zone", nullable = false)
    private LocalDateTime created;

    @Column(columnDefinition = "text")
    private String description;
}