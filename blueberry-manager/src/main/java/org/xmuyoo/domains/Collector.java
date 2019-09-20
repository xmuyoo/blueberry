package org.xmuyoo.domains;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"name", "driver"})})
@ToString
@Getter
public class Collector {

    @Id
    private Long id;

    @JsonProperty
    @Column(columnDefinition = "text not null")
    private String name;

    @JsonProperty
    @Column(columnDefinition = "text not null")
    private String driver;

    @JsonProperty
    @Column(columnDefinition = "text")
    private String alias;

    @JsonProperty
    @Column(columnDefinition = "text")
    private String description;
}
