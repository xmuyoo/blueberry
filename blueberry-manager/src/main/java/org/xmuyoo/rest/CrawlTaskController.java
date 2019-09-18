package org.xmuyoo.rest;

import com.google.common.collect.ImmutableSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.xmuyoo.domains.CrawlTask;
import org.xmuyoo.domains.DataSchema;
import org.xmuyoo.repos.CrawlTaskRepo;

import java.time.LocalDateTime;
import java.util.Set;

@Slf4j
@RestController("/crawl-task")
@RequestMapping()
public class CrawlTaskController {

    private static final Set<String> AVAILABLE_METHODS =
            ImmutableSet.of(HttpMethod.GET.name().toLowerCase(),
                            HttpMethod.POST.name().toLowerCase(),
                            HttpMethod.PUT.name().toLowerCase(),
                            HttpMethod.DELETE.name().toLowerCase());

    @Autowired
    private CrawlTaskRepo crawlTaskRepo;


    /**
     * Add a new data resource.
     *
     * @param reqCrawlTask The new data resource details. Note that the schemaIds in the parameters
     *                     should be available ids of existing {@link DataSchema}.
     * @return Created data resource
     */
    @RequestMapping(value = "add", method = RequestMethod.POST)
    public CrawlTask addCrawlTask(@RequestBody CrawlTask reqCrawlTask) {
        if (!ParameterValidator.notBlank(reqCrawlTask.sourceName(), reqCrawlTask.sourceUrl(),
                                         reqCrawlTask.sourceType(), reqCrawlTask.userId())) {
            throw new IllegalArgumentException("Missing required parameters");
        }

        if (!AVAILABLE_METHODS.contains(reqCrawlTask.httpMethod().toLowerCase()))
            throw new IllegalArgumentException(reqCrawlTask.httpMethod() + " is not supported!");

        CrawlTask crawlTask = CrawlTask.builder()
                .userId(reqCrawlTask.userId())
                .sourceName(reqCrawlTask.sourceName())
                .sourceUrl(reqCrawlTask.sourceUrl())
                .sourceType(reqCrawlTask.sourceType())
                .httpMethod(reqCrawlTask.httpMethod().toLowerCase())
                .queryParameters(reqCrawlTask.queryParameters())
                .bodyParameters(reqCrawlTask.bodyParameters())
                .crawlerName(reqCrawlTask.crawlerName())
                .active(true)
                .created(LocalDateTime.now())
                .build();

        crawlTask = crawlTaskRepo.save(crawlTask);

        return crawlTask;
    }
}