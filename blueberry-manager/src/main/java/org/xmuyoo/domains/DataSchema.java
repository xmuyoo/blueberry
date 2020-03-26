package org.xmuyoo.domains;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataSchema {

    public static final String VALUE_TYPE_NUMBER = "number";
    public static final String VALUE_TYPE_TEXT = "text";
    public static final String VALUE_TYPE_JSON = "json";

    private static final Pattern SCHEMA_PATTERN = Pattern.compile("([\\w]+)\\.([\\w]+)");

    @JsonProperty
    private String id;

    @JsonProperty
    private String namespace;

    @JsonProperty
    private String name;

    @JsonProperty
    private String type;

    @JsonProperty
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
