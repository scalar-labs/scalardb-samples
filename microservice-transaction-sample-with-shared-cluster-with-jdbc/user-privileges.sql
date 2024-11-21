-- Create a user and grant privileges for order-service.
CREATE USER "order-service" WITH PASSWORD 'order-service';
GRANT SELECT, INSERT, UPDATE, DELETE ON NAMESPACE order_service TO "order-service";

-- Create a user and grant privileges for customer-service.
CREATE USER "customer-service" WITH PASSWORD 'customer-service';
GRANT SELECT, INSERT, UPDATE, DELETE ON NAMESPACE customer_service TO "customer-service";
