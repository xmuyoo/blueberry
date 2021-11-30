-- Convertible Bond Code collector
create table if not exists convertible_bond_code (
    code                 text not null primary key, -- 可转债代码
    name                 text not null default '', -- 名称
    market_cd            text, -- 交易所代码
    stock_code           text not null default '', -- 正股代码
    stock_name           text not null default '', -- 正股名称
    active               boolean not null, -- 是否上市
    bond_rating          text, -- 可转债评级
    conversion_price     numeric(12, 3), -- 转股价格
    conversion_value     numeric(12, 3), -- 转股价值
    ytm_rt               numeric(12, 3), -- 到期税前收益
    convert_amt_ratio    numeric(12, 3), -- 转债占比
    adj_cnt              numeric(12, 3), -- 下修转股价次数
    resale_trigger_price numeric(12, 3), -- 回售触发价
    frt_price            numeric(12, 3), -- Strong Redemption Trigger Price，强赎触发价
    expire_date          text, -- 到期时间
    delisted             boolean not null, -- 是否退市
    last_price           numeric(12, 3), -- 最后交易价格，针对已退市转债
    lasting_years        numeric(12, 3), -- 存续年限
    delist_reason        text, -- 退市原因
    delist_date          text -- 退市日期
);
