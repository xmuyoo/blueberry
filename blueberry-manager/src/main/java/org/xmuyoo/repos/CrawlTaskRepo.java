package org.xmuyoo.repos;

import org.springframework.data.repository.CrudRepository;
import org.xmuyoo.domains.CrawlTask;

public interface CrawlTaskRepo extends CrudRepository<CrawlTask, Long> {
}
