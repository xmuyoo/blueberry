-- Initialize collectors
insert into "collector"(id, name, driver, alias, description) values
    ('1', 'StockRealtimePrice', 'org.xmuyoo.blueberry.collect.collectors.StockRealtimePriceCollector',
        '实时股价采集器', '股票实时交易数据爬虫，只在开市时间段内爬取每只股票的实时数据'),
    ('2', 'StockCode', 'org.xmuyoo.blueberry.collect.collectors.StockCodeCollector',
        '股票代码采集器', '爬取上交所和深交所的所有股票代码'),
    ('3', 'FinancialReport', 'org.xmuyoo.blueberry.collect.collectors.FinancialReportCollector',
        '中国上市公司财报采集器', '爬取中国股市上市公司定期发布的财报数据')
on conflict (id) do nothing;

-- Initialize system collector tasks
insert into "collect_task"(id, active, body_pattern, collector_id, created, description,
                        http_method, period, source_name, source_type, source_url, time_ranges)
values
    ('1', true, '', 1, now(), '交易日采集实时股价任务', 'get', '1 minute',
         '新浪实时股价数据', 'Api', 'http://hq.sinajs.cn/list=${exchange}${code}.html',
         '09:30-18:30,13:00-15:00'),
    ('2', true, '', 2, now(), '定期采集中国股票代码列表的任务', 'get', '1 week',
         '东方财经上证深证股票列表', 'Html', 'http://quote.eastmoney.com/stock_list.html', ''),
    ('3', true, '', 3, now(), '定期采集中国股市上市公司现金流量表数据', 'get', '1 day',
         '网易财经数据', 'Api', 'http://quotes.money.163.com/service/xjllb_${code}.html', ''),
    ('4', true, '', 3, now(), '定期采集中国股市上市公司利润表数据', 'get', '1 day',
         '网易财经数据', 'Api', 'http://quotes.money.163.com/service/lrb_${code}.html', '')
on conflict(id) do nothing;
