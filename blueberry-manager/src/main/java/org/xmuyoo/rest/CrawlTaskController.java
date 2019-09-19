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
import org.xmuyoo.domains.CrawlTask;
import org.xmuyoo.domains.Crawler;
import org.xmuyoo.domains.DataSchema;
import org.xmuyoo.repos.CrawlTaskRepo;
import org.xmuyoo.repos.CrawlerRepo;
import org.xmuyoo.repos.SchemaRepo;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RestController
@RequestMapping("/crawl-task")
public class CrawlTaskController {

    private static final Pattern DATA_SCHEMA_PATTERN =
            Pattern.compile("\\$\\{([a-zA-Z_\\.0-9]+)\\}");

    private static final Set<String> AVAILABLE_METHODS =
            ImmutableSet.of(HttpMethod.GET.name().toLowerCase(),
                            HttpMethod.POST.name().toLowerCase(),
                            HttpMethod.PUT.name().toLowerCase(),
                            HttpMethod.DELETE.name().toLowerCase());

    @Autowired
    private CrawlTaskRepo crawlTaskRepo;

    @Autowired
    private SchemaRepo schemaRepo;

    @Autowired
    private CrawlerRepo crawlerRepo;


    /**
     * Add a new data resource.
     *
     * @param reqCrawlTask The new data resource details. Note that the schemaIds in the parameters
     *                     should be available ids of existing {@link DataSchema}.
     * @return Created data resource
     */
    @RequestMapping(value = "add", method = RequestMethod.POST)
    public CrawlTask addCrawlTask(@RequestBody CrawlTaskRequest reqCrawlTask) {
        if (!ParameterValidator.notBlank(reqCrawlTask.sourceName(), reqCrawlTask.sourceUrl(),
                                         reqCrawlTask.sourceType(), reqCrawlTask.userId())) {
            throw new IllegalArgumentException("Missing required parameters");
        }

        if (!AVAILABLE_METHODS.contains(reqCrawlTask.httpMethod().toLowerCase()))
            throw new IllegalArgumentException(reqCrawlTask.httpMethod() + " is not supported!");

        Optional<Crawler> opt = crawlerRepo.findById(reqCrawlTask.crawlerId());
        if (!opt.isPresent()) {
            throw new IllegalArgumentException(
                    "Can not find crawler for " + reqCrawlTask.crawlerId());
        }
        Crawler crawler = opt.get();

        Long userId = reqCrawlTask.userId();
        Set<DataSchema> extractedDataSchema = extractDataSchema(userId, reqCrawlTask.sourceUrl());
        if (StringUtils.isNotEmpty(reqCrawlTask.bodyPattern()))
            extractedDataSchema.addAll(extractDataSchema(userId, reqCrawlTask.bodyPattern()));

        CrawlTask crawlTask = CrawlTask.builder()
                .userId(reqCrawlTask.userId())
                .sourceName(reqCrawlTask.sourceName())
                .sourceUrl(reqCrawlTask.sourceUrl())
                .sourceType(CrawlTask.SourceType.valueOf(reqCrawlTask.sourceType()))
                .httpMethod(reqCrawlTask.httpMethod())
                .bodyPattern(reqCrawlTask.bodyPattern())
                .crawler(crawler)
                .dataSchemaSet(extractedDataSchema)
                .timeRanges(Utils.commaJoin(reqCrawlTask.timeRanges()))
                .period(reqCrawlTask.period())
                .active(true)
                .created(LocalDateTime.now())
                .description(reqCrawlTask.description())
                .build();

        crawlTask = crawlTaskRepo.save(crawlTask);

        return crawlTask;
    }

    @RequestMapping(value = "list", method = RequestMethod.GET)
    public List<CrawlTask> listTasks() {
        List<CrawlTask> crawlTasksList = new ArrayList<>();
        crawlTaskRepo.findAll().forEach(crawlTasksList::add);

        return crawlTasksList;
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
    public static class CrawlTaskRequest {
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
        private Long crawlerId;
    }
}