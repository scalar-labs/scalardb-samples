CREATE COORDINATOR TABLES IF NOT EXIST;

CREATE NAMESPACE IF NOT EXISTS sample;

CREATE TABLE IF NOT EXISTS sample.customers (
  customer_id INT PRIMARY KEY,
  name TEXT,
  credit_limit INT,
  credit_total INT
);

CREATE TABLE IF NOT EXISTS sample.orders (
  customer_id INT,
  timestamp BIGINT,
  order_id TEXT,
  PRIMARY KEY (customer_id, timestamp)
);

CREATE INDEX IF NOT EXISTS ON sample.orders (order_id);

CREATE TABLE IF NOT EXISTS sample.statements (
  order_id TEXT,
  item_id INT,
  count INT,
  PRIMARY KEY (order_id, item_id)
);

CREATE TABLE IF NOT EXISTS sample.items (
  item_id INT PRIMARY KEY,
  name TEXT,
  price INT
);
