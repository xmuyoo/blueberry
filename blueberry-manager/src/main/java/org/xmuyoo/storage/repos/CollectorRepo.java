package org.xmuyoo.storage.repos;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;
import org.xmuyoo.domains.Collector;

public interface CollectorRepo {

    String COLUMNS = "id AS id, alias AS alias, driver AS driver, " +
            "name AS name, description as description";

    @Select("SELECT " + COLUMNS + " FROM collector WHERE id = #{collectorId}")
    @ResultType(Collector.class)
    Collector findOneById(@Param("collectorId") String collectorId);
}