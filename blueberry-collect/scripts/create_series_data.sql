-- Create Functions
CREATE OR REPLACE FUNCTION blueberry.tag_metric(JSONB)
  RETURNS TEXT
AS $$
SELECT $1 ->> 'metric'
$$
LANGUAGE SQL IMMUTABLE PARALLEL SAFE;

CREATE OR REPLACE FUNCTION blueberry.tag_stock_code(JSONB)
  RETURNS TEXT
AS $$
SELECT $1 ->> 'stockCode'
$$
LANGUAGE SQL IMMUTABLE PARALLEL SAFE;

CREATE OR REPLACE FUNCTION blueberry.tag_stock_name(JSONB)
  RETURNS TEXT
AS $$
SELECT $1 ->> 'stockName'
$$
LANGUAGE SQL IMMUTABLE PARALLEL SAFE;

-- Create Tables
CREATE TABLE blueberry.values_stock_realtime_price (
	record_time timestamp with time zone not null,
	value double precision not null,
	tag_id bigint not null
);
CREATE UNIQUE INDEX ON values_stock_realtime_price(record_time, tag_id);
CREATE INDEX ON values_stock_realtime_price USING BRIN(record_time) WITH(autosummarize = ON);
SELECT create_hypertable(
  'values_stock_realtime_price',
  'record_time',
  chunk_time_interval => INTERVAL '1 day',
  create_default_indexes => false
);

CREATE TABLE blueberry.tags_stock_realtime_price (
  record_time timestamp with time zone NOT NULL,
  tag_id bigint NOT NULL,
  tags jsonb NOT NULL DEFAULT '{}'::jsonb
);
CREATE UNIQUE INDEX ON tags_stock_realtime_price(tag_id, record_time);
CREATE INDEX ON tags_stock_realtime_price USING BRIN (record_time) WITH (autosummarize = ON);
CREATE INDEX ON tags_stock_realtime_price USING GIN(tags jsonb_ops);
CREATE INDEX ON tags_stock_realtime_price (tag_stock_code(tags));
CREATE INDEX ON tags_stock_realtime_price (tag_stock_name(tags));

-- Create Views
CREATE VIEW blueberry.stock_realtime_price AS
  SELECT
    v.record_time,
    v.value,
    tag_metric(tags) AS metric,
    t.tags
  FROM
  (
    SELECT DISTINCT tag_id, tags FROM tags_stock_realtime_price
  ) AS t,
  values_stock_realtime_price v
  WHERE t.tag_id = v.tag_id;
