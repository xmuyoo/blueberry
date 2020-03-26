package org.xmuyoo.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.xmuyoo.Utils;
import org.xmuyoo.domains.DataSchema;
import org.xmuyoo.storage.repos.DataSchemaRepo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequestMapping("data-schema")
@RestController
public class SchemaController {

    @Autowired
    private DataSchemaRepo schemaRepo;

    @RequestMapping(value = "add", method = RequestMethod.POST)
    public List<DataSchema> addSchema(@RequestBody List<DataSchema> reqDataSchemas) {
        List<DataSchema> savedDataSchemas = new ArrayList<>();
        for (DataSchema reqDataSchema : reqDataSchemas) {
            if (!ParameterValidator.notBlank(reqDataSchema.name(), reqDataSchema.namespace(),
                    reqDataSchema.type())) {
                throw new IllegalArgumentException(
                        "DataSchema namespace, name, user_id and type are required");
            }

            if (!DataSchema.validateValueType(reqDataSchema.type()))
                throw new IllegalArgumentException("Unknown value type of " + reqDataSchema.type());

            DataSchema dataSchema = schemaRepo.queryByNamespaceAndName(
                    reqDataSchema.namespace(), reqDataSchema.name());
            if (null != dataSchema) {
                savedDataSchemas.add(dataSchema);
                continue;
            }

            String namespace = reqDataSchema.namespace();
            String name = reqDataSchema.name();
            String id = Utils.MURMUR3.hashBytes(
                    String.format("%s:%s", namespace, name).getBytes()).toString();
            DataSchema newDataSchema = DataSchema
                    .builder()
                    .id(id)
                    .namespace(reqDataSchema.namespace())
                    .name(reqDataSchema.name())
                    .type(reqDataSchema.type())
                    .description(reqDataSchema.description())
                    .build();

            schemaRepo.insert(newDataSchema);
            savedDataSchemas.add(newDataSchema);
        }

        return savedDataSchemas;
    }

    @RequestMapping(value = "all", method = RequestMethod.GET)
    public List<DataSchema> queryAllSchemas() {
        return schemaRepo.queryAll();
    }
}
