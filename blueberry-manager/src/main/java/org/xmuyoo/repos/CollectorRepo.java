package org.xmuyoo.repos;

import org.springframework.data.repository.CrudRepository;
import org.xmuyoo.domains.Collector;

public interface CollectorRepo extends CrudRepository<Collector, Long> {
}
