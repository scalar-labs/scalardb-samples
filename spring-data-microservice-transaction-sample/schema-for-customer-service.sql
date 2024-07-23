CREATE NAMESPACE IF NOT EXISTS customer_service;

CREATE TABLE IF NOT EXISTS customer_service.customers (
  customer_id INT PRIMARY KEY,
  name TEXT,
  credit_limit INT,
  credit_total INT
);
