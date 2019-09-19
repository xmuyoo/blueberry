package org.xmuyoo.domains;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.persistence.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private static final Pattern SCHEMA_PATTERN = Pattern.compile("([\\w]+)\\.([\\w]+)");

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

    public static BasicInfo getSchemaBasicInfo(String patternString) {
        BasicInfo basicInfo = null;
        Matcher matcher = SCHEMA_PATTERN.matcher(patternString);
        if (matcher.find()) {
            String namespace = matcher.group(1);
            String name = matcher.group(2);

            basicInfo = new BasicInfo(namespace, name);
        }

        return basicInfo;
    }

    @Getter
    @AllArgsConstructor
    public static class BasicInfo {
        private String namespace;
        private String name;
    }
}
