-- Create the coordinator tables.
CREATE COORDINATOR TABLES IF NOT EXIST;

-- Create the schema for customer service.
CREATE NAMESPACE IF NOT EXISTS customer_service;

CREATE TABLE IF NOT EXISTS customer_service.customers (
  customer_id INT PRIMARY KEY,
  name TEXT,
  credit_limit INT,
  credit_total INT
);

-- Create the schema for order service.
CREATE NAMESPACE IF NOT EXISTS order_service;

CREATE TABLE IF NOT EXISTS order_service.orders (
  customer_id INT,
  timestamp BIGINT,
  order_id TEXT,
  PRIMARY KEY (customer_id, timestamp)
);

CREATE INDEX IF NOT EXISTS ON order_service.orders (order_id);

CREATE TABLE IF NOT EXISTS order_service.statements (
  order_id TEXT,
  item_id INT,
  count INT,
  PRIMARY KEY (order_id, item_id)
);

CREATE TABLE IF NOT EXISTS order_service.items (
  item_id INT PRIMARY KEY,
  name TEXT,
  price INT
);
