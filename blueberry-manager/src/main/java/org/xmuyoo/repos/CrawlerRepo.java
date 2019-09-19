package org.xmuyoo.repos;

import org.springframework.data.repository.CrudRepository;
import org.xmuyoo.domains.Crawler;

public interface CrawlerRepo extends CrudRepository<Crawler, Long> {
}
