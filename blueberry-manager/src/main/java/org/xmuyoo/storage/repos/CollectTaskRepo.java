package org.xmuyoo.storage.repos;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;
import org.mybatis.spring.annotation.MapperScan;
import org.xmuyoo.domains.CollectTask;

import java.util.List;

@MapperScan
public interface CollectTaskRepo {

    String INSERT_COLUMNS = " id" + "," +
            "collector_id" + "," +
            "active" + "," +
            "body_pattern" + "," +
            "http_method" + "," +
            "period" + "," +
            "source_name" + "," +
            "source_type" + "," +
            "source_url" + "," +
            "time_ranges" + "," +
            "created" + "," +
            "description";
    String QUERY_COLUMNS = "id as id" + "," +
            "collector_id as collectorId" + "," +
            "active as active" + "," +
            "body_pattern as bodyPattern" + "," +
            "http_method as httpMethod" + "," +
            "period as period" + "," +
            "source_name as sourceName" + "," +
            "source_type as sourceType" + "," +
            "source_url as sourceUrl" + "," +
            "time_ranges as timeRanges" + "," +
            "created as created" + "," +
            "description as description";

    @Insert("INSERT INTO collect_task(" + INSERT_COLUMNS + ") VALUES " +
            "(" +
            "  #{id}, #{collectorId}, #{active}, #{bodyPattern}, #{httpMethod}, #{period}, #{sourceName}, " +
            "  #{sourceType}, #{sourceUrl}, ${timeRanges}, #{created}, #{description}" +
            ")"
    )
    void insert(CollectTask collectTask);

    @Select("SELECT " + QUERY_COLUMNS + " FROM collect_task")
    @ResultType(CollectTask.class)
    List<CollectTask> queryAll();
}