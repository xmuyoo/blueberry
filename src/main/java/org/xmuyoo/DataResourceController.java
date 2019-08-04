package org.xmuyoo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping()
public class DataResourceController {

    @Autowired
    private DataResourceRepo dataResourceRepo;

    @Autowired
    private SchemaRepo schemaRepo;

    @RequestMapping(value = "/data-resource/add", method = RequestMethod.POST)
    public DataResource addDataResource(@RequestBody DataResource reqDataResource) {
        if (!ParameterValidator.notBlank(reqDataResource.sourceName(), reqDataResource.sourceUrl(),
                                         reqDataResource.sourceType())) {
            throw new IllegalArgumentException("Missing required parameters");
        }

        DataResource dataResource = DataResource.builder()
                .sourceName(reqDataResource.sourceName())
                .sourceUrl(reqDataResource.sourceUrl())
                .sourceType(reqDataResource.sourceType())
                .created(LocalDateTime.now())
                .build();

        dataResource = dataResourceRepo.save(dataResource);

        return dataResource;
    }

    @RequestMapping(value = "/schema/add", method = RequestMethod.POST)
    public void addSchema(@RequestBody Schema reqSchema) {
        if (!ParameterValidator.notBlank(reqSchema.name()))
            throw new IllegalArgumentException("Schema name is required");

        schemaRepo.save(reqSchema);
    }

    @RequestMapping(value = "/schema/list", method = RequestMethod.GET)
    public List<Schema> listSchema() {
        List<Schema> schemaList = new ArrayList<>();
        schemaRepo.findAll().forEach(schemaList::add);

        return schemaList;
    }
}