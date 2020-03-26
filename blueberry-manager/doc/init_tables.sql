-- Initialize collectors
insert into "collector"(id, name, driver, alias, description) values
    (1, 'StockRealtimePrice', 'org.xmuyoo.blueberry.collect.collectors.StockRealtimePriceCollector',
        '实时股价采集器', '股票实时交易数据爬虫，只在开市时间段内爬取每只股票的实时数据'),
    (2, 'StockCode', 'org.xmuyoo.blueberry.collect.collectors.StockCodeCollector',
        '股票代码采集器', '爬取上交所和深交所的所有股票代码')
on conflict (id) do nothing;

-- Initialize data schemas
insert into "data_schema"(id, namespace, name, type, description)
values
    (1, 'stock_code', 'code', 'text', '上交所和深交所的股票代码'),
    (2, 'stock_code', 'name', 'text', '上交所和深交所股票的名称'),
    (3, 'stock_code', 'type', 'text', '中国股票所属单位，上交所或深交所')
on conflict(namespace, name) do nothing;

-- Initialize system collector tasks
insert into "collect_task"(id, active, body_pattern, collector_id, created, description,
                        http_method, period, source_name, source_type, source_url, time_ranges)
values
    (1, true, '', 1, now(), '交易日采集实时股价任务', 'get', '1 minute',
         '新浪实时股价数据', 'Api', 'http://hq.sinajs.cn/list=${stock_code.type}${stock_code.code}.html',
         '09:30-18:30,13:00-15:00'),
    (2, true, '', 2, now(), '定期采集中国股票代码列表的任务', 'get', '1 week',
         '新浪上交所深交所股票列表', 'Html', 'http://quote.eastmoney.com/stock_list.html', '')
on conflict(id) do nothing;
