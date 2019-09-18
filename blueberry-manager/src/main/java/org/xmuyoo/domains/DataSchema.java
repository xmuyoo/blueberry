package org.xmuyoo.domains;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.persistence.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"namespace", "name", "userId"})})
public class DataSchema {

    public static final String VALUE_TYPE_NUMBER = "number";
    public static final String VALUE_TYPE_TEXT = "text";
    public static final String VALUE_TYPE_JSON = "json";

    @JsonProperty
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @JsonProperty
    @Column
    private String namespace;

    @JsonProperty
    @Column
    private String name;

    @JsonProperty
    @Column
    private Long userId;

    @JsonProperty
    @Column
    private String type;

    @JsonProperty
    @Column(columnDefinition = "text")
    private String description;


    public static boolean validateValueType(String valueType) {
        return VALUE_TYPE_NUMBER.equalsIgnoreCase(valueType)
                || VALUE_TYPE_TEXT.equalsIgnoreCase(valueType)
                || VALUE_TYPE_JSON.equalsIgnoreCase(valueType);
    }
}
