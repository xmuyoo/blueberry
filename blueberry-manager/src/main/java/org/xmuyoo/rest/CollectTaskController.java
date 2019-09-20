package org.xmuyoo.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.xmuyoo.Utils;
import org.xmuyoo.domains.CollectTask;
import org.xmuyoo.domains.Collector;
import org.xmuyoo.domains.DataSchema;
import org.xmuyoo.repos.CollectTaskRepo;
import org.xmuyoo.repos.CollectorRepo;
import org.xmuyoo.repos.SchemaRepo;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RestController
@RequestMapping("/collect-task")
public class CollectTaskController {

    private static final Pattern DATA_SCHEMA_PATTERN =
            Pattern.compile("\\$\\{([a-zA-Z_\\.0-9]+)\\}");

    private static final Set<String> AVAILABLE_METHODS =
            ImmutableSet.of(HttpMethod.GET.name().toLowerCase(),
                            HttpMethod.POST.name().toLowerCase(),
                            HttpMethod.PUT.name().toLowerCase(),
                            HttpMethod.DELETE.name().toLowerCase());

    @Autowired
    private CollectTaskRepo collectTaskRepo;

    @Autowired
    private SchemaRepo schemaRepo;

    @Autowired
    private CollectorRepo collectorRepo;


    /**
     * Add a new data resource.
     *
     * @param reqCollectTask The new data resource details. Note that the schemaIds in the parameters
     *                     should be available ids of existing {@link DataSchema}.
     * @return Created data resource
     */
    @RequestMapping(value = "add", method = RequestMethod.POST)
    public CollectTask addCollectTask(@RequestBody CollectTaskRequest reqCollectTask) {
        if (!ParameterValidator.notBlank(reqCollectTask.sourceName(), reqCollectTask.sourceUrl(),
                                         reqCollectTask.sourceType(), reqCollectTask.userId())) {
            throw new IllegalArgumentException("Missing required parameters");
        }

        if (!AVAILABLE_METHODS.contains(reqCollectTask.httpMethod().toLowerCase()))
            throw new IllegalArgumentException(reqCollectTask.httpMethod() + " is not supported!");

        Optional<Collector> opt = collectorRepo.findById(reqCollectTask.collectorId());
        if (!opt.isPresent()) {
            throw new IllegalArgumentException(
                    "Can not find collector for " + reqCollectTask.collectorId());
        }
        Collector collector = opt.get();

        Long userId = reqCollectTask.userId();
        Set<DataSchema> extractedDataSchema = extractDataSchema(userId, reqCollectTask.sourceUrl());
        if (StringUtils.isNotEmpty(reqCollectTask.bodyPattern()))
            extractedDataSchema.addAll(extractDataSchema(userId, reqCollectTask.bodyPattern()));

        CollectTask collectTask = CollectTask.builder()
                .userId(reqCollectTask.userId())
                .sourceName(reqCollectTask.sourceName())
                .sourceUrl(reqCollectTask.sourceUrl())
                .sourceType(CollectTask.SourceType.valueOf(reqCollectTask.sourceType()))
                .httpMethod(reqCollectTask.httpMethod())
                .bodyPattern(reqCollectTask.bodyPattern())
                .collector(collector)
                .dataSchemaSet(extractedDataSchema)
                .timeRanges(Utils.commaJoin(reqCollectTask.timeRanges()))
                .period(reqCollectTask.period())
                .active(true)
                .created(LocalDateTime.now())
                .description(reqCollectTask.description())
                .build();

        collectTask = collectTaskRepo.save(collectTask);

        return collectTask;
    }

    @RequestMapping(value = "list", method = RequestMethod.GET)
    public List<CollectTask> listTasks() {
        List<CollectTask> collectTasksList = new ArrayList<>();
        collectTaskRepo.findAll().forEach(collectTasksList::add);

        return collectTasksList;
    }

    private Set<DataSchema> extractDataSchema(Long userId, String patternString) {
        Set<DataSchema> dataSchemaSet = new HashSet<>();
        Set<DataSchema.BasicInfo> basicInfoSet = new HashSet<>();
        Matcher matcher = DATA_SCHEMA_PATTERN.matcher(patternString);
        while (matcher.find()) {
            String dataSchemaStr = matcher.group(1);
            DataSchema.BasicInfo basicInfo = DataSchema.getSchemaBasicInfo(dataSchemaStr);
            basicInfoSet.add(basicInfo);
        }

        for (DataSchema.BasicInfo basicInfo : basicInfoSet) {
            DataSchema dataSchema = schemaRepo.findAllByNamespaceAndNameAndUserId(
                    basicInfo.namespace(), basicInfo.name(), userId);
            if (null != dataSchema)
                dataSchemaSet.add(dataSchema);
        }

        return dataSchemaSet;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CollectTaskRequest {
        @JsonProperty
        private Long userId;

        @JsonProperty
        private String sourceName;

        @JsonProperty
        private String sourceUrl;

        @JsonProperty
        private String sourceType;

        @JsonProperty
        private String httpMethod;

        @JsonProperty
        private String bodyPattern;

        @JsonProperty
        private List<String> timeRanges;

        @JsonProperty
        private String period;

        @JsonProperty
        private String description;

        @JsonProperty
        private Long collectorId;
    }
}