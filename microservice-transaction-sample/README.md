# Microservice Transaction Sample

This is a sample application for Microservice Transaction that uses Two-phase Commit Transactions in Scalar DB.
You can find more information about Two-phase Commit Transactions in Scalar DB [here](https://github.com/scalar-labs/scalardb/tree/master/docs/two-phase-commit-transactions.md).

## Prerequisites
- Java (OpenJDK 8 or higher)
- Gradle
- Docker, Docker Compose

## Sample application

### Overview

There are two microservices called *Customer Service* and *Order Service* based on the [*Database-per-service* pattern](https://microservices.io/patterns/data/database-per-service.html) in this sample application.

Customer Service manages customers' information including credit card information like a credit limit and a credit total.
Order Service is responsible for order operations like placing an order and getting order histories.
Each service has gRPC endpoints. Clients call the endpoints, and the services call the endpoints each other as well.
Customer Service and Order Service uses MySQL and Cassandra through Scalar DB, respectively.

![Overview](images/overview.png)

Note that both services access a small coordinator database used for the Consensus Commit protocol.
The database is service-independent and exists for managing transaction metadata for Consensus Commit in a highly available manner, so we don't think it is violating the database-per-service pattern. We also plan to create a microservice container for the coordinator database to truly achieve the database-per-service pattern.


In this sample application, for ease of setup and explanation, we colocate the coordinator database in the same Cassandra instance of the Order Service, but of course, the coordinator database can be managed as a separate database.


### Schemas

[The schema for Customer Service](customer-service-schema-loader/customer-service-schema.json) is as follows:

```json
{
  "customer_service.customers": {
    "transaction": true,
    "partition-key": [
      "id"
    ],
    "clustering-key": [],
    "columns": {
      "id": "INT",
      "name": "TEXT",
      "credit_limit": "INT",
      "credit_total": "INT"
    }
  }
}
```

The `customer_service.customers` table manages customers' information.
The `credit_limit` for a customer is the maximum amount of money a lender will allow the customer to spend using a credit card,
and the `credit_total` is the amount of money that the customer has already spent by using the credit card.

[The schema for Order Service](order-service-schema-loader/order-service-schema.json) is as follows:

```json
{
  "order_service.orders": {
    "transaction": true,
    "partition-key": [
      "customer_id"
    ],
    "clustering-key": [
      "timestamp"
    ],
    "secondary-index": [
      "id"
    ],
    "columns": {
      "id": "TEXT",
      "customer_id": "INT",
      "timestamp": "BIGINT"
    }
  },
  "order_service.statements": {
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
  "order_service.items": {
    "transaction": true,
    "partition-key": [
      "id"
    ],
    "clustering-key": [],
    "columns": {
      "id": "INT",
      "name": "TEXT",
      "price": "INT"
    }
  }
}
```

The `order_service.orders` table manages orders' information, and the `order_service.statements` table manages the states' information of the orders.
The `order_service.items` table manages items' information to be ordered.

### Transactions

There are the following five transactions implemented in this sample application:

1. Getting customer information. It is a transaction in Customer Service
2. Placing an order. It is a transaction that spans Order Service and Customer Service. It first checks if the amount of the money of the order exceeds the credit limit. And when the check is passed, it records order histories and updates the `credit_total`
3. Getting order information by order ID. It is a transaction in Order Service
4. Getting order information by customer ID. It is also a transaction in Order Service
5. Repayment. It is a transaction in Customer Service. It reduces the amount of `credit_total`.

## Set up

First, you need to build the docker images of the sample application with the following command:
```
./gradlew docker
```

After that, you need to run the following `docker-compose` command:
```
docker-compose up -d
```

This command starts Cassandra and MySQL and loads the schemas, and starts the microservices.
Please note that you need to wait around more than one minute for the containers to be fully started.

### Initial data

When the microservices start, the initial data is loaded automatically as follows:

- For the `customer_service.customers` table:

| id | name | credit_limit | credit_total |
| ---- | ---- | ---- | ---- |
| 1 | Yamada Taro | 10000 | 0 |
| 2 | Yamada Hanako | 10000 | 0 |
| 3 | Suzuki Ichiro | 10000 | 0 |

- For the `order_service.items` table:

| id | name | price | 
| ---- | ---- | ---- | 
| 1 | Apple | 1000 |
| 2 | Orange | 2000 |
| 3 | Grape | 2500 |
| 4 | Mango | 5000 |
| 5 | Melon | 3000 |

## Run the sample application

Let's start with getting the customer information whose ID is `1`:
```
# ./gradlew :client:run --args="GetCustomerInfo 1"
...
{"id": 1,"name": "Yamada Taro","credit_limit": 10000}
...
```

At this time, `credit_total` isn't shown, which means the current value of `credit_total` is `0`.

Then, place an order for three apples and two oranges with customer ID `1`. Note that the format of order is `<Item ID>:<Count>,<Item ID>:<Count>,...`:
```
# ./gradlew :client:run --args="PlaceOrder 1 1:3,2:2"
...
{"order_id": "4ccdb21c-ac03-4b48-bcb7-cad57eac1e79"}
...
```

The command shows the order ID of the order. 

Let's check the details of the order with the order ID:
```
# ./gradlew :client:run --args="GetOrder 4ccdb21c-ac03-4b48-bcb7-cad57eac1e79"
...
{"order": {"order_id": "4ccdb21c-ac03-4b48-bcb7-cad57eac1e79","timestamp": 1631605253126,"customer_id": 1,"customer_name": "Yamada Taro","statement": [{"item_id": 1,"item_name": "Apple","price": 1000,"count": 3,"total": 3000},{"item_id": 2,"item_name": "Orange","price": 2000,"count": 2,"total": 4000}],"total": 7000}}
...
```

So, let's place an order again and get the order histories by customer ID `1`:
```
# ./gradlew :client:run --args="PlaceOrder 1 5:1"
...
{"order_id": "0b10db66-faa6-4323-8a7a-474e8534a7ee"}
...
# ./gradlew :client:run --args="GetOrders 1"
...
{"order": [{"order_id": "0b10db66-faa6-4323-8a7a-474e8534a7ee","timestamp": 1631605501485,"customer_id": 1,"customer_name": "Yamada Taro","statement": [{"item_id": 5,"item_name": "Melon","price": 3000,"count": 1,"total": 3000}],"total": 3000},{"order_id": "4ccdb21c-ac03-4b48-bcb7-cad57eac1e79","timestamp": 1631605253126,"customer_id": 1,"customer_name": "Yamada Taro","statement": [{"item_id": 1,"item_name": "Apple","price": 1000,"count": 3,"total": 3000},{"item_id": 2,"item_name": "Orange","price": 2000,"count": 2,"total": 4000}],"total": 7000}]}
...
```

These histories are ordered by timestamp in a descending manner.

The current `credit_total` is `10000`, so it has reached the `credit_limit`.
So, the customer can't place an order anymore due to the limit.

```
# ./gradlew :client:run --args="GetCustomerInfo 1"
...
{"id": 1,"name": "Yamada Taro","credit_limit": 10000,"credit_total": 10000}
...
# ./gradlew :client:run --args="PlaceOrder 1 3:1,4:1"
...
io.grpc.StatusRuntimeException: FAILED_PRECONDITION: Credit limit exceeded
        at io.grpc.stub.ClientCalls.toStatusRuntimeException(ClientCalls.java:262)
        at io.grpc.stub.ClientCalls.getUnchecked(ClientCalls.java:243)
        at io.grpc.stub.ClientCalls.blockingUnaryCall(ClientCalls.java:156)
        at example.rpc.OrderServiceGrpc$OrderServiceBlockingStub.placeOrder(OrderServiceGrpc.java:295)
        at example.client.command.PlaceOrderCommand.call(PlaceOrderCommand.java:35)
        at example.client.command.PlaceOrderCommand.call(PlaceOrderCommand.java:12)
        at picocli.CommandLine.executeUserObject(CommandLine.java:1783)
        at picocli.CommandLine.access$900(CommandLine.java:145)
        at picocli.CommandLine$RunLast.handle(CommandLine.java:2141)
        at picocli.CommandLine$RunLast.handle(CommandLine.java:2108)
        at picocli.CommandLine$AbstractParseResultHandler.execute(CommandLine.java:1975)
        at picocli.CommandLine.execute(CommandLine.java:1904)
        at example.client.Client.main(Client.java:27)
...
```

After repayment, the customer will be able to place an order again!

```
# ./gradlew :client:run --args="Repayment 1 8000"
...
# ./gradlew :client:run --args="GetCustomerInfo 1"
...
{"id": 1,"name": "Yamada Taro","credit_limit": 10000,"credit_total": 2000}
...
# ./gradlew :client:run --args="PlaceOrder 1 3:1,4:1"
...
{"order_id": "dd53dd9d-aaf4-41db-84b2-56951fed6425"}
...
```
