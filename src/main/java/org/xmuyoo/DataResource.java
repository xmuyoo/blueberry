package org.xmuyoo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataResource {

    public enum SourceType {
        PureUrl,
        Html
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @JsonProperty("source_name")
    @Column(nullable = false)
    private String sourceName;

    @JsonProperty("source_url")
    @Column(columnDefinition = "text", nullable = false)
    private String sourceUrl;

    @JsonProperty("source_type")
    @Column(columnDefinition = "text", nullable = false)
    private SourceType sourceType;

    @Column
    private boolean active;

    @ElementCollection
    private List<CrawlRecord> crawlRecords;

    @Column(columnDefinition = "timestamp with time zone", nullable = false)
    private LocalDateTime created;
}
