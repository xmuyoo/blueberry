create database if not exists `blueberry`;

create table if not exists data_schema (
    id varchar(128) not null primary key,
    name varchar(128) not null default '',
    namespace varchar(64) not null default '',
    type varchar(64) not null default '',
    description text default '',
    collect_task_id varchar(128) not null
);
create unique index data_schema_idx on data_schema(namespace, name);

create table if not exists collector (
    id varchar(128) not null primary key,
    alias varchar(64) not null default '',
    driver text not null,
    name text not null default '',
    description text default ''
);
create unique index collector_idx on collector(name, driver);
create unique index collector_driver_idx on collector(driver);

create table if not exists collect_task (
    id varchar(128) not null primary key,
    collector_id varchar(128) not null,
    active boolean not null default true,
    body_pattern text not null default '',
    http_method text not null default '',
    period text not null default '',
    source_name text not null default '',
    source_type text not null default '',
    source_url text not null default '',
    time_ranges text not null default '',
    created timestamp with time zone default current_timestamp,
    description text default ''
);

create table if not exists collect_record (
    id varchar(128) not null primary key,
    collect_task_id varchar(128) not null,
    collected_datetime timestamp with time zone not null,
    status varchar(16) default ''
);

create table if not exists stock_code (
    code text not null primary key,
    name text not null default '',
    exchange text not null default ''
);

-- Create Views
create or replace view "task_definition" as
    select t.id, t.description, t.source_name, t.source_url, t.source_type, t.time_ranges, t.period,
            t.body_pattern, t.active, t.http_method,
            c.name as "collector_name", c.alias as "collector_alias", c.driver as "collector_driver"
    from collector c, collect_task t
    where c.id = t.collector_id
;

create or replace view "task_results" as
    select r.id as "record_id", r.collected_datetime, r.success as "result",
            t.description as "task_description", t.id as "collect_task_id",
            t.source_name, t.source_url, t.source_type, t.time_ranges, t.period,
            t.body_pattern, t.http_method
    from collect_task t, collect_record r
    where r.collect_task_id = t.id
;

create or replace view "task_schema" as
    select t.id as "collect_task_id", t.description as "carwl_task_description",
		s.id as "schema_id", s.namespace as "schema_namespace", s.name as "schema_name",
		s.type as "schema_type", s.description as "schema_description"
	from collect_task t, data_schema s
	where t.id = s.collect_task_id
;
