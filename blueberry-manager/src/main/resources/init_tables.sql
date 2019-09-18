-- Initialize system user
insert into "users"(id, name) values
(1, 'system')
on conflict(id) do nothing;

-- Initialize crawlers
insert into "crawler"(id, name, driver, description)
values(1, 'StockRealtimePrice',
	   'org.xmuyoo.blueberry.crawling.crawlers.StockRealtimePriceCrawler',
	   '股票实时交易数据爬虫，只在开市时间段内爬取每只股票的实时数据')
on conflict (id) do nothing;

-- Initialize data schemas
insert into "data_schema"(id, namespace, name, user_id, type, description)
values
    (1, 'stock_code', 'code', 1, 'text', '上交所和深交所的股票代码'),
    (2, 'stock_code', 'name', 1, 'text', '上交所和深交所股票的名称')
on conflict(namespace, name, user_id) do nothing;

-- Initialize system crawler tasks