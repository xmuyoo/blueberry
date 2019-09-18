package org.xmuyoo.repos;

import org.springframework.data.repository.CrudRepository;
import org.xmuyoo.domains.DataSchema;

public interface SchemaRepo extends CrudRepository<DataSchema, Long> {

    DataSchema findByNamespaceAndName(String namespace, String name);
}
