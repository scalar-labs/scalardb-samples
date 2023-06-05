CREATE COORDINATOR TABLES IF NOT EXIST;

CREATE NAMESPACE IF NOT EXISTS customer;

CREATE TABLE IF NOT EXISTS customer.customers (
  customer_id INT PRIMARY KEY,
  name TEXT,
  credit_limit INT,
  credit_total INT
);

CREATE NAMESPACE IF NOT EXISTS "order";

CREATE TABLE IF NOT EXISTS "order".orders (
  customer_id INT,
  timestamp BIGINT,
  order_id TEXT,
  PRIMARY KEY (customer_id, timestamp)
);

CREATE INDEX IF NOT EXISTS ON "order".orders (order_id);

CREATE TABLE IF NOT EXISTS "order".statements (
  order_id TEXT,
  item_id INT,
  count INT,
  PRIMARY KEY (order_id, item_id)
);

CREATE TABLE IF NOT EXISTS "order".items (
  item_id INT PRIMARY KEY,
  name TEXT,
  price INT
);
