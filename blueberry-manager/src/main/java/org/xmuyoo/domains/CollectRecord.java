package org.xmuyoo.domains;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CollectRecord {

    @JsonProperty
    private Long id;

    @JsonProperty
    private LocalDateTime collectedDatetime;

    @JsonProperty
    private boolean success;

    @JsonProperty
    private Long collectTaskId;
}
