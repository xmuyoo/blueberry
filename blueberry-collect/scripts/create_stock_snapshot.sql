-- Stock snapshot data
create table if not exists stock_snapshot (
    code text not null primary key,
    name text not null,
    pe_lyr numeric(12, 3), -- 静态市盈率
    pe_ttm numeric(12, 3), -- 市盈率 TTM
    pb numeric(12, 3), -- 市净率
    dividend_yield numeric(12, 3), -- 股息率
    market_capital decimal(16, 2), -- 总市值
    total_shares decimal(16, 2), -- 总股本
    navps numeric(12, 2), -- 每股净资产
    last_close numeric(12, 2), -- 昨日收盘价
    amount decimal(16, 2), -- 成交额
    volume decimal(16, 2), --成交额
    volume_ratio numeric(12, 3), -- 量比
    float_market_capital decimal(16, 2), -- 流通市值
    high52w numeric(12, 3), -- 52 周最高
    low52w numeric(12, 3), -- 52周最低
    turnover_rate numeric(12, 3) -- 换手率
);

create view stock_snapshot_details as
select ss.*, sc.category
from stock_code sc, stock_snapshot ss
where sc.code = ss.code;