-- Stock snapshot data
create table if not exists stock_snapshot (
    code text not null primary key,
    name text not null default '',
    pe_lyr numeric(12, 3) not null default 0.0, -- 静态市盈率
    pb numeric(12, 3) not null default 0.0, -- 市净率
    dividend_yield numeric(12, 3) not null default 0.0, -- 股息率
    market_capital bigint not null default 0, -- 总市值
    total_shares bigint not null default 0, -- 总股本
    navps numeric(12, 2) not null default 0.0, -- 每股净资产
    last_close numeric(12, 2) not null default 0.0 -- 昨日收盘价
);

create view stock_snapshot_details as
select ss.*, sc.category
from stock_code sc, stock_snapshot ss
where sc.code = ss.code;