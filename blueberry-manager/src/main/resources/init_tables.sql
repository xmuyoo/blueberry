-- Initialize system user
insert into "users"(id, name) values
(1, 'system')
on conflict(id) do nothing;

-- Initialize collectors
insert into "collectors"(id, name, driver, alias, description) values
    (1, 'StockRealtimePrice', 'org.xmuyoo.blueberry.collect.collectors.StockRealtimePriceCollector',
        '实时股价采集器', '股票实时交易数据爬虫，只在开市时间段内爬取每只股票的实时数据'),
    (2, 'StockCode', 'org.xmuyoo.blueberry.collect.collectors.StockCodeCollector',
        '股票代码采集器', '爬取上交所和深交所的所有股票代码')
on conflict (id) do nothing;

-- Initialize data schemas
insert into "data_schema"(id, namespace, name, user_id, type, description)
values
    (1, 'stock_code', 'code', 1, 'text', '上交所和深交所的股票代码'),
    (2, 'stock_code', 'name', 1, 'text', '上交所和深交所股票的名称'),
    (3, 'stock_code', 'type', 1, 'text', '中国股票所属单位，上交所或深交所')
on conflict(namespace, name, user_id) do nothing;

-- Initialize system collector tasks
insert into "collect_task"(id, active, body_pattern, collect_id, created, description,
                        http_method, period, source_name, source_type, source_url, time_ranges, user_id)
values
    (1, true, null, 1, now(), '交易日采集实时股价任务', 'get', '1 minute',
         '新浪实时股价数据', 'Api', 'http://hq.sinajs.cn/list=${stock_code.type}${stock_code.code}.html',
         '09:30-18:30,13:00-15:00', 1),
    (2, true, null, 2, now(), '定期采集中国股票代码列表的任务', 'get', '1 week',
         '新浪上交所深交所股票列表', 'Html', 'http://quote.eastmoney.com/stock_list.html', null, 1)
on conflict(id) do nothing;
insert into "collect_task_data_schema_set"(collect_task_id, data_schema_set_id)
values (1, 3), (1, 1);

-- Create Views
create or replace view "task_definition" as
    select t.id, t.description, t.source_name, t.source_url, t.source_type, t.time_ranges, t.period,
            t.user_id, t.body_pattern, t.active, t.http_method,
            c.name as "collector_name", c.alias as "collector_alias", c.driver as "collector_driver"
    from collector c, collect_task t
    where c.id = t.collector_id
;

create or replace view "task_results" as
    select r.id as "record_id", r.collected_datetime, r.success as "result",
            t.description as "task_description", t.id as "collect_task_id",
            t.source_name, t.source_url, t.source_type, t.time_ranges, t.period, t.user_id,
            t.body_pattern, t.http_method
    from collect_task t, collect_record r
    where r.collect_task_id = t.id
;

create or replace view "task_schema" as
    select t.id as "collect_task_id", t.description as "carwl_task_description",
		t.user_id as "user_id",
		s.id as "schema_id", s.namespace as "schema_namespace", s.name as "schema_name",
		s.type as "schema_type", s.description as "schema_description"
	from collect_task t, data_schema s, collect_task_data_schema_set m
	where t.id = m.collect_task_id and m.data_schema_set_id = s.id
;