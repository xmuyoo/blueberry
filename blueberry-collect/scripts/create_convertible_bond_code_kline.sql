-- History of convertible bonds
create table if not exists convertible_bond_history (
    code              text not null,  -- 可转债代码
    record_time       bigint,         -- 日期时间戳
    ytm_rt            decimal(16, 2), -- 到期税前收益率
    premium_rt        decimal(16, 2), -- 转股溢价率
    convert_value     decimal(16, 2), -- 转股价值
    price             decimal(16, 2), -- 价格
    volume            decimal(16, 2), -- 成交额（万元）
    stock_volume      bigint,         -- 成交量
    curr_iss_amt      decimal(16, 2), -- 剩余规模（亿元）
    turnover_rt       decimal(16, 2)  -- 换手率
);

create unique index cb_code_time_idx on convertible_bond_history(code, record_time);
