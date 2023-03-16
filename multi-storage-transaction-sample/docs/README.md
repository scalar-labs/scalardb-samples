# Multi-storage Transaction Sample

This is a sample application for Multi-storage Transaction in ScalarDB.

## Prerequisites
- Java (OpenJDK 8 or higher)
- Gradle
- Docker, Docker Compose

## Sample application

### Overview

This is a simple EC application where you can order items and pay with a credit card using ScalarDB.

In this article, you create the sample application on Cassandra and MySQL.
With Multi-storage Transaction in ScalarDB, you can execute a transaction that spans Cassandra and MySQL.
Please note that application-specific error handling, authentication processing, etc., are omitted in the sample application since it focuses on explaining how to use ScalarDB.
Please see [this document](https://github.com/scalar-labs/scalardb/blob/master/docs/api-guide.md#handle-exceptions) for the details of how to handle exceptions in ScalarDB.

![Overview](images/overview.png)

### Schema

[The schema](schema.json) is as follows:

```json
{
  "customer.customers": {
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
  "order.orders": {
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
  "order.statements": {
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
  "order.items": {
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

The `customers` table is created in the `customer` namespace. And the `orders`, `statements`, and `items` tables are created in the `order` namespace.

The `customer.customers` table manages customers' information.
The `credit_limit` is the maximum amount of money a lender will allow each customer to spend using a credit card, and the `credit_total` is the amount of money that each customer has already spent by using the credit card.

The `order.orders` table manages orders' information, and the `order.statements` table manages the statements' information of the orders. Finally, the `order.items` table manages items' information to be ordered.

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
scalar.db.storage=multi-storage
scalar.db.multi_storage.storages=cassandra,mysql
scalar.db.multi_storage.storages.cassandra.storage=cassandra
scalar.db.multi_storage.storages.cassandra.contact_points=localhost
scalar.db.multi_storage.storages.cassandra.username=cassandra
scalar.db.multi_storage.storages.cassandra.password=cassandra
scalar.db.multi_storage.storages.mysql.storage=jdbc
scalar.db.multi_storage.storages.mysql.contact_points=jdbc:mysql://localhost:3306/
scalar.db.multi_storage.storages.mysql.username=root
scalar.db.multi_storage.storages.mysql.password=mysql
scalar.db.multi_storage.namespace_mapping=customer:mysql,order:cassandra,coordinator:cassandra
scalar.db.multi_storage.default_storage=cassandra
```

This configuration defines two storages, `cassandra` and `mysql`, in the `scalar.db.multi_storage.storages` property.
And the storage settings of each of them is configured in the `scalar.db.multi_storage.storages.cassandra.*` properties and the `scalar.db.multi_storage.storages.mysql.*` properties respectively.
The `scalar.db.multi_storage.namespace_mapping` property defines the mapping between namespaces and storages, in this case, operations for the tables in the `customer` namespace are mapped to the `mysql` storage, and operations for the tables in the `order` namespace are mapped to the `cassandra` storage.
Note that it also defines that operations for the tables in the `coordinator` namespace are mapped to the `cassandra` storage.
The tables in the `coordinator` namespace are created automatically and used in the ScalarDB's transaction protocol called Consensus Commit. 
And the `scalar.db.multi_storage.default_storage` property defines the default storage that’s used if a specified table doesn't have any table mapping.
In this case, if a specified table doesn't have any table mapping, operations for the table are mapped to the `cassandra` storage.

Please see below for the details of Multi-storage Transaction configurations:
https://github.com/scalar-labs/scalardb/blob/master/docs/multi-storage-transactions.md

## Set up

You need to run the following `docker-compose` command:
```shell
$ docker-compose up -d
```

This command starts Cassandra and MySQL, and loads the schema.
Please note that you need to wait around more than one minute for the containers to be fully started.

### Initial data

You first need to load initial data with the following command:
```shell
$ ./gradlew run --args="LoadInitialData"
```

And the following data will be loaded:

- For the `customer.customers` table:

| customer_id | name          | credit_limit | credit_total |
|-------------|---------------|--------------|--------------|
| 1           | Yamada Taro   | 10000        | 0            |
| 2           | Yamada Hanako | 10000        | 0            |
| 3           | Suzuki Ichiro | 10000        | 0            |

- For the `order.items` table:

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
{"order_id": "9099eca6-98b8-4ef5-a803-3166dfe635ad"}
...
```

The command shows the order ID of the order.

Let's check the details of the order with the order ID:
```shell
$ ./gradlew run --args="GetOrder 9099eca6-98b8-4ef5-a803-3166dfe635ad"
...
{"order": {"order_id": "9099eca6-98b8-4ef5-a803-3166dfe635ad","timestamp": 1652668907742,"customer_id": 1,"customer_name": "Yamada Taro","statement": [{"item_id": 1,"item_name": "Apple","price": 1000,"count": 3,"total": 3000},{"item_id": 2,"item_name": "Orange","price": 2000,"count": 2,"total": 4000}],"total": 7000}}
...
```

So, let's place an order again and get the order histories by customer ID `1`:
```shell
$ ./gradlew run --args="PlaceOrder 1 5:1"
...
{"order_id": "cff41250-01aa-4a4e-ae1c-651b74bb1cff"}
...
$ ./gradlew run --args="GetOrders 1"
...
{"order": [{"order_id": "9099eca6-98b8-4ef5-a803-3166dfe635ad","timestamp": 1652668907742,"customer_id": 1,"customer_name": "Yamada Taro","statement": [{"item_id": 1,"item_name": "Apple","price": 1000,"count": 3,"total": 3000},{"item_id": 2,"item_name": "Orange","price": 2000,"count": 2,"total": 4000}],"total": 7000},{"order_id": "cff41250-01aa-4a4e-ae1c-651b74bb1cff","timestamp": 1652668943114,"customer_id": 1,"customer_name": "Yamada Taro","statement": [{"item_id": 5,"item_name": "Melon","price": 3000,"count": 1,"total": 3000}],"total": 3000}]}
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
{"order_id": "4b1c5f53-e2fb-4489-be1f-cfc2aef29123"}
...
```

## Clean up

To stop Cassandra and MySQL, run the following command:
```shell
$ docker-compose down
```
