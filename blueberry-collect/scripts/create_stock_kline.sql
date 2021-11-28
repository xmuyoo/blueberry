-- History data of stocks
create table if not exists stock_k_line (
    code              text not null,  -- 股票代码
    name              text not null,  -- 股票名称
    record_time       bigint,         -- 日期时间戳
    volume            bigint,         -- 成交量
    open              decimal(16, 2), -- 开盘价
    close             decimal(16, 2), -- 收盘价
    high              decimal(16, 2), -- 最高
    low               decimal(16, 2), -- 最低
    chg               decimal(16, 2), -- 涨跌额
    percent           decimal(16, 2), -- 涨跌幅
    turn_overrate     decimal(16, 2), -- 换手率
    amount            decimal(16, 2), -- 成交额
    volume_post       bigint,         -- 盘后成交量（股）
    amount_post       decimal(16, 2), -- 盘后成交额
    pe                decimal(16, 2), -- 市盈率(TTM)
    pb                decimal(16, 2), -- 市净率
    ps                decimal(16, 2), --
    pcf               decimal(16, 2), --
    market_capital    bigint,         -- 总市值
    balance           decimal(16, 2), --
    hold_volume_cn    decimal(16, 2), --
    hold_ratio_cn     decimal(16, 2), --
    net_volume_cn     decimal(16, 2), --
    hold_volume_hk    decimal(16, 2), --
    hold_ratio_hk     decimal(16, 2), --
    net_volume_hk     decimal(16, 2)  --
);

create unique index skl_code_time_idx on stock_k_line(code, record_time);