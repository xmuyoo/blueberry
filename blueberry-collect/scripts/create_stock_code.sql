-- StockCode collector
create table if not exists stock_code (
    code text not null primary key,
    name text not null default '',
    exchange text not null default ''
);
