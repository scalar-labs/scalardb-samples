# ScalarDB Sample

This is a sample application for ScalarDB.

## Prerequisites
- Java (OpenJDK 8 or higher)
- Gradle
- Docker, Docker Compose

## Sample application

### Overview

This is a simple EC application where you can order items and pay with a credit card using ScalarDB.
In this article, you create the application on Cassandra.
Even though Cassandra does not provide ACID transaction capability, you can achieve ACID transactions on Cassandra by accessing Cassandra through ScalarDB.
Please note that application-specific error handling, authentication processing, etc., are omitted in the sample application since it focuses on explaining how to use ScalarDB.
Please see [this document](https://github.com/scalar-labs/scalardb/blob/master/docs/api-guide.md#handle-exceptions) for the details of how to handle exceptions in ScalarDB.

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

The `sample.customers` table manages customers' information.
The `credit_limit` is the maximum amount of money a lender will allow each customer to spend using a credit card, and the `credit_total` is the amount of money that each customer has already spent by using the credit card.

The `sample.orders` table manages orders' information, and the `sample.statements` table manages the statements' information of the orders. Finally, the `sample.items` table manages items' information to be ordered.


The ER diagram for the schema is as follows:

![ERD](images/ERD.png)

### Transactions

The following five transactions are implemented in this sample application:

1. Getting customer information
3. Placing an order. An order is paid by a credit card. It first checks if the amount of the money of the order exceeds the credit limit. If the check passes, it records order histories and updates the `credit_total`
4. Getting order information by order ID
5. Getting order information by customer ID
6. Repayment. It reduces the amount of `credit_total`.

### Configuration

The configurations for the sample application is as follows:

```properties
scalar.db.storage=cassandra
scalar.db.contact_points=localhost
scalar.db.username=cassandra
scalar.db.password=cassandra
```

Since you use Cassandra in this sample application as mentioned above, you need to configure Cassandra settings in the configuration.

## Set up

You need to run the following `docker-compose` command:
```shell
$ docker-compose up -d
```

This command starts Cassandra and loads the schema.
Please note that you need to wait around more than one minute for the containers to be fully started.

### Initial data

You first need to load initial data with the following command:
```shell
$ ./gradlew run --args="LoadInitialData"
```

And the following data will be loaded:

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

Let's start with getting the customer information whose ID is `1`:
```shell
$ ./gradlew run --args="GetCustomerInfo 1"
...
{"id": 1, "name": "Yamada Taro", "credit_limit": 10000, "credit_total": 0}
...
```

Then, place an order for three apples and two oranges with customer ID `1`. Note that the format of order is `<Item ID>:<Count>,<Item ID>:<Count>,...`:
```shell
$ ./gradlew run --args="PlaceOrder 1 1:3,2:2"
...
{"order_id": "dea4964a-ff50-4ecf-9201-027981a1566e"}
...
```

The command shows the order ID of the order.

Let's check the details of the order with the order ID:
```shell
$ ./gradlew run --args="GetOrder dea4964a-ff50-4ecf-9201-027981a1566e"
...
{"order": {"order_id": "dea4964a-ff50-4ecf-9201-027981a1566e","timestamp": 1650948340914,"customer_id": 1,"customer_name": "Yamada Taro","statement": [{"item_id": 1,"item_name": "Apple","price": 1000,"count": 3,"total": 3000},{"item_id": 2,"item_name": "Orange","price": 2000,"count": 2,"total": 4000}],"total": 7000}}
...
```

So, let's place an order again and get the order histories by customer ID `1`:
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

These histories are ordered by timestamp in a descending manner.

The current `credit_total` is `10000`, so it has reached the `credit_limit`.
So, the customer can't place an order anymore due to the limit.

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

After repayment, the customer will be able to place an order again!

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
