# ScalarDB Analytics Spark Sample

## Setup

### 1. Start services

```bash
docker compose up -d
```

### 2. Load sample data

```bash
docker compose run --rm sample-data-loader
```

### 3. Create catalog

```bash
docker compose run --rm scalardb-analytics-cli catalog create --catalog sample_catalog
```

### 4. Register data sources

```bash
# Register ScalarDB data source
docker compose run --rm scalardb-analytics-cli data-source register --data-source-json /config/data-sources/scalardb.json

# Register PostgreSQL data source
docker compose run --rm scalardb-analytics-cli data-source register --data-source-json /config/data-sources/postgres.json
```

### 5. Run Spark SQL

```bash
docker compose run --rm spark-sql
```

## Query examples

```sql
-- List catalogs
SHOW CATALOGS;

-- Use ScalarDB catalog
USE sample_catalog;

-- Query ScalarDB tables
SELECT * FROM scalardb.mysqlns.orders LIMIT 10;
SELECT * FROM scalardb.cassandrans.lineitem LIMIT 10;

-- Query PostgreSQL tables
SELECT * FROM postgres.public.customer LIMIT 10;
```

## Stop services

```bash
docker compose down
```
