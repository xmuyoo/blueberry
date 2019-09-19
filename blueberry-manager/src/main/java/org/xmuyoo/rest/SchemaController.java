package org.xmuyoo.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.xmuyoo.domains.DataSchema;
import org.xmuyoo.repos.SchemaRepo;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequestMapping("data-schema")
@RestController
public class SchemaController {

    @Autowired
    private SchemaRepo schemaRepo;

    @RequestMapping(value = "add", method = RequestMethod.POST)
    public List<DataSchema> addSchema(@RequestBody List<DataSchema> reqDataSchemas) {
        List<DataSchema> dataSchemaList = new ArrayList<>();
        List<DataSchema> savedDataSchemas = new ArrayList<>();
        for (DataSchema reqDataSchema : reqDataSchemas) {
            if (!ParameterValidator.notBlank(reqDataSchema.name(), reqDataSchema.namespace(),
                                             reqDataSchema.userId(), reqDataSchema.type())) {
                throw new IllegalArgumentException(
                        "DataSchema namespace, name, user_id and type are required");
            }

            if (!DataSchema.validateValueType(reqDataSchema.type()))
                throw new IllegalArgumentException("Unknown value type of " + reqDataSchema.type());

            DataSchema dataSchema = schemaRepo.findAllByNamespaceAndNameAndUserId(
                    reqDataSchema.namespace(), reqDataSchema.name(), reqDataSchema.userId());
            if (null != dataSchema) {
                savedDataSchemas.add(dataSchema);
                continue;
            }

            DataSchema newDataSchema = DataSchema.builder()
                    .namespace(reqDataSchema.namespace())
                    .name(reqDataSchema.name())
                    .userId(reqDataSchema.userId())
                    .type(reqDataSchema.type())
                    .description(reqDataSchema.description())
                    .build();

            dataSchemaList.add(newDataSchema);
        }

        savedDataSchemas.addAll((List<DataSchema>) schemaRepo.saveAll(dataSchemaList));
        return savedDataSchemas;
    }

    @RequestMapping(value = "list", method = RequestMethod.GET)
    public List<DataSchema> listSchema() {
        List<DataSchema> dataSchemaList = new ArrayList<>();
        schemaRepo.findAll().forEach(dataSchemaList::add);

        return dataSchemaList;
    }
}
