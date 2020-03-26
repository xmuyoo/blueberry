package org.xmuyoo.storage.repos;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;
import org.mybatis.spring.annotation.MapperScan;
import org.xmuyoo.domains.DataSchema;

import java.util.List;

@MapperScan
public interface DataSchemaRepo {
    String TABLE = "data_schema";
    String INSERT_COLUMNS = "id" + "," +
            "name" + "," +
            "namespace" + "," +
            "type" + "," +
            "description" + "," +
            "collect_task_id";
    String QUERY_COLUMNS = "id as id" + "," +
            "name as name" + "," +
            "namespace as namespace" + "," +
            "type as type" + "," +
            "description as description" + "," +
            "collect_task_id as collectTaskId";

    @Insert("INSERT INTO " + TABLE + "(" + INSERT_COLUMNS + ") VALUES (" +
            "#{id}, #{name}, #{namespace}, #{type}, #{description}, #{collectTaskId}" +
            ")"
    )
    void insert(DataSchema dataSchema);

    @Select("SELECT " + QUERY_COLUMNS + " FROM " + TABLE)
    @ResultType(DataSchema.class)
    List<DataSchema> queryAll();

    @Select("SELECT " + QUERY_COLUMNS + " FROM data_schema WHERE namespace = #{namespace} AND name = #{name}")
    @ResultType(DataSchema.class)
    DataSchema queryByNamespaceAndName(@Param("namespace") String namespace,
                                       @Param("name") String name);

}
