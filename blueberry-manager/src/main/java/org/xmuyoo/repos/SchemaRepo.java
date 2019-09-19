package org.xmuyoo.repos;

import org.springframework.data.repository.CrudRepository;
import org.xmuyoo.domains.DataSchema;

import java.util.List;

public interface SchemaRepo extends CrudRepository<DataSchema, Long> {

    DataSchema findByNamespaceAndName(String namespace, String name);

    DataSchema findAllByNamespaceAndNameAndUserId(String namespace, String name, Long userId);
}
