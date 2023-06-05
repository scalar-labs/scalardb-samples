# ScalarDB Sample

This tutorial describes how to create a sample application by using ScalarDB.

## Prerequisites

- Java (OpenJDK 8 or higher)
- Gradle
- Docker, Docker Compose

## Sample application

### Overview

This tutorial illustrates the process of creating a sample e-commerce application, where items can be ordered and paid for with a credit card using ScalarDB.
In this tutorial, you will build the application on Cassandra.
Although Cassandra does not provide ACID transaction capability, you can run ACID transactions on Cassandra if you interact with it through ScalarDB.
Please note that application-specific error handling, authentication processing, and similar functions are not included in the sample application, as the focus is on demonstrating the use of ScalarDB.
For detailed information on exception handling in ScalarDB, see [Handle SQLException](https://github.com/scalar-labs/scalardb/blob/master/docs/api-guide.md#handle-exceptions).

### Schema

[The schema](schema.json) is as follows:

```json
{
  "sample.customers": {
    "transaction": true,
    "partition-key": [
      "customer_id"
    ],
    "columns": {
      "customer_id": "INT",
      "name": "TEXT",
      "credit_limit": "INT",
      "credit_total": "INT"
    }
  },
  "sample.orders": {
    "transaction": true,
    "partition-key": [
      "customer_id"
    ],
    "clustering-key": [
      "timestamp"
    ],
    "secondary-index": [
      "order_id"
    ],
    "columns": {
      "order_id": "TEXT",
      "customer_id": "INT",
      "timestamp": "BIGINT"
    }
  },
  "sample.statements": {
    "transaction": true,
    "partition-key": [
      "order_id"
    ],
    "clustering-key": [
      "item_id"
    ],
    "columns": {
      "order_id": "TEXT",
      "item_id": "INT",
      "count": "INT"
    }
  },
  "sample.items": {
    "transaction": true,
    "partition-key": [
      "item_id"
    ],
    "columns": {
      "item_id": "INT",
      "name": "TEXT",
      "price": "INT"
    }
  }
}
```

All the tables are created in the `sample` namespace.

- `sample.customers`: a table that manages customers' information
    - `credit_limit`: the maximum amount of money a lender will allow each customer to spend when using a credit card
    - `credit_total`: the amount of money that each customer has already spent by using the credit card
- `sample.orders`: a table that manages order information
- `sample.statements`: a table that manages order statement information
- `sample.items`: a table that manages information of items to be ordered

The Entity Relationship Diagram for the schema is as follows:

![ERD](images/ERD.png)

### Transactions

The following five transactions are implemented in this sample application:

1. Getting customer information
2. Placing an order by credit card (checks if the cost of the order is below the credit limit, then records order history and updates the `credit_total` if the check passes)
3. Getting order information by order ID
4. Getting order information by customer ID
5. Repayment (reduces the amount in the `credit_total`)

## Configuration

Configurations for the sample application are as follows:

```properties
scalar.db.storage=cassandra
scalar.db.contact_points=localhost
scalar.db.username=cassandra
scalar.db.password=cassandra
```

Since this sample application uses Cassandra, as shown above, you need to configure your settings for Cassandra in this configuration.

## Setup

### Start Cassandra

To start Cassandra, you need to run the following `docker-compose` command:

```shell
$ docker-compose up -d
```

Please note that starting the containers may take more than one minute.

### Load schema

You then need to apply the schema with the following command.
To download the schema loader tool, `scalardb-schema-loader-<VERSION>.jar`, see the [Releases](https://github.com/scalar-labs/scalardb/releases) of ScalarDB and download the version that you want to use.

```shell
$ java -jar scalardb-schema-loader-<VERSION>.jar --config database.properties --schema-file schema.json --coordinator
```

### Load initial data

After the containers have started, you need to load the initial data by running the following command:

```shell
$ ./gradlew run --args="LoadInitialData"
```

After the initial data has loaded, the following records should be stored in the tables:

- For the `sample.customers` table:

| customer_id | name          | credit_limit | credit_total |
|-------------|---------------|--------------|--------------|
| 1           | Yamada Taro   | 10000        | 0            |
| 2           | Yamada Hanako | 10000        | 0            |
| 3           | Suzuki Ichiro | 10000        | 0            |

- For the `sample.items` table:

| item_id | name   | price |
|---------|--------|-------|
| 1       | Apple  | 1000  |
| 2       | Orange | 2000  |
| 3       | Grape  | 2500  |
| 4       | Mango  | 5000  |
| 5       | Melon  | 3000  |

## Run the sample application

Let's start with getting information about the customer whose ID is `1`:

```shell
$ ./gradlew run --args="GetCustomerInfo 1"
...
{"id": 1, "name": "Yamada Taro", "credit_limit": 10000, "credit_total": 0}
...
```

Then, place an order for three apples and two oranges by using customer ID `1`.
Note that the order format is `<Item ID>:<Count>,<Item ID>:<Count>,...`:

```shell
$ ./gradlew run --args="PlaceOrder 1 1:3,2:2"
...
{"order_id": "dea4964a-ff50-4ecf-9201-027981a1566e"}
...
```

You can see that running this command shows the order ID.

Let's check the details of the order by using the order ID:

```shell
$ ./gradlew run --args="GetOrder dea4964a-ff50-4ecf-9201-027981a1566e"
...
{"order": {"order_id": "dea4964a-ff50-4ecf-9201-027981a1566e","timestamp": 1650948340914,"customer_id": 1,"customer_name": "Yamada Taro","statement": [{"item_id": 1,"item_name": "Apple","price": 1000,"count": 3,"total": 3000},{"item_id": 2,"item_name": "Orange","price": 2000,"count": 2,"total": 4000}],"total": 7000}}
...
```

Then, let's place another order and get the order history of customer ID `1`:

```shell
$ ./gradlew run --args="PlaceOrder 1 5:1"
...
{"order_id": "bcc34150-91fa-4bea-83db-d2dbe6f0f30d"}
...
$ ./gradlew run --args="GetOrders 1"
...
{"order": [{"order_id": "dea4964a-ff50-4ecf-9201-027981a1566e","timestamp": 1650948340914,"customer_id": 1,"customer_name": "Yamada Taro","statement": [{"item_id": 1,"item_name": "Apple","price": 1000,"count": 3,"total": 3000},{"item_id": 2,"item_name": "Orange","price": 2000,"count": 2,"total": 4000}],"total": 7000},{"order_id": "bcc34150-91fa-4bea-83db-d2dbe6f0f30d","timestamp": 1650948412766,"customer_id": 1,"customer_name": "Yamada Taro","statement": [{"item_id": 5,"item_name": "Melon","price": 3000,"count": 1,"total": 3000}],"total": 3000}]}
...
```

This order history is shown in descending order by timestamp.

The customer's current `credit_total` is `10000`.
Since the customer has now reached their `credit_limit`, which was shown when retrieving their information, they cannot place anymore orders.

```shell
$ ./gradlew run --args="GetCustomerInfo 1"
...
{"id": 1, "name": "Yamada Taro", "credit_limit": 10000, "credit_total": 10000}
...
$ ./gradlew run --args="PlaceOrder 1 3:1,4:1"
...
java.lang.RuntimeException: Credit limit exceeded
        at sample.Sample.placeOrder(Sample.java:205)
        at sample.command.PlaceOrderCommand.call(PlaceOrderCommand.java:33)
        at sample.command.PlaceOrderCommand.call(PlaceOrderCommand.java:8)
        at picocli.CommandLine.executeUserObject(CommandLine.java:1783)
        at picocli.CommandLine.access$900(CommandLine.java:145)
        at picocli.CommandLine$RunLast.handle(CommandLine.java:2141)
        at picocli.CommandLine$RunLast.handle(CommandLine.java:2108)
        at picocli.CommandLine$AbstractParseResultHandler.execute(CommandLine.java:1975)
        at picocli.CommandLine.execute(CommandLine.java:1904)
        at sample.command.SampleCommand.main(SampleCommand.java:35)
...
```

After making a payment, the customer will be able to place orders again.

```shell
$ ./gradlew run --args="Repayment 1 8000"
...
$ ./gradlew run --args="GetCustomerInfo 1"
...
{"id": 1, "name": "Yamada Taro", "credit_limit": 10000, "credit_total": 2000}
...
$ ./gradlew run --args="PlaceOrder 1 3:1,4:1"
...
{"order_id": "8911cab3-1c2b-4322-9386-adb1c024e078"}
...
```

## Clean up

To stop Cassandra, run the following command:

```shell
$ docker-compose down
```
