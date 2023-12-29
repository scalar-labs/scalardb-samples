# Create a Sample Application That Supports Microservice Transactions

This tutorial describes how to create a sample application that supports microservice transactions in ScalarDB.

## Overview

The sample e-commerce application shows how users can order and pay for items by using a line of credit. The use case described in this tutorial is the same as the basic [ScalarDB sample](../scalardb-sample/README.md) but takes advantage of [transactions with a two-phase commit interface](https://github.com/scalar-labs/scalardb/tree/master/docs/two-phase-commit-transactions.md) when using ScalarDB.

The sample application has two microservices called the *Customer Service* and the *Order Service* based on the [database-per-service pattern](https://microservices.io/patterns/data/database-per-service.html):

- The **Customer Service** manages customer information, including line-of-credit information, credit limit, and credit total.
- The **Order Service** is responsible for order operations like placing an order and getting order histories.

Each service has gRPC endpoints. Clients call the endpoints, and the services call each endpoint as well.

The databases that you will be using in the sample application are Cassandra and MySQL. The Customer Service and the Order Service use Cassandra and MySQL, respectively, through ScalarDB.

![Overview](images/overview.png)

As shown in the diagram, both services access a small Coordinator database used for the Consensus Commit protocol. The database is service-independent and exists for managing transaction metadata for Consensus Commit in a highly available manner.

In the sample application, for ease of setup and explanation, we co-locate the Coordinator database in the same Cassandra instance of the Order Service. Alternatively, you can manage the Coordinator database as a separate database.

{% capture notice--info %}
**Note**

Since the focus of the sample application is to demonstrate using ScalarDB, application-specific error handling, authentication processing, and similar functions are not included in the sample application. For details about exception handling in ScalarDB, see [How to handle exceptions](https://github.com/scalar-labs/scalardb/blob/master/docs/api-guide.md#how-to-handle-exceptions).

Additionally, for the purpose of the sample application, each service has one container so that you can avoid using request routing between the services. However, for production use, because each service typically has multiple servers or hosts for scalability and availability, you should consider request routing between the services in transactions with a two-phase commit interface. For details about request routing, see [Request routing in transactions with a two-phase commit interface](https://github.com/scalar-labs/scalardb/blob/master/docs/two-phase-commit-transactions.md#request-routing-in-transactions-with-a-two-phase-commit-interface).
{% endcapture %}

<div class="notice--info">{{ notice--info | markdownify }}</div>

### Service endpoints

The endpoints defined in the services are as follows:

- Customer Service
  - `getCustomerInfo`
  - `payment`
  - `prepare`
  - `validate`
  - `commit`
  - `rollback`
  - `repayment`

- Order Service
  - `placeOrder`
  - `getOrder`
  - `getOrders`

### What you can do in this sample application

The sample application supports the following types of transactions:

- Get customer information through the `getCustomerInfo` endpoint of the Customer Service.
- Place an order by using a line of credit through the `placeOrder` endpoint of the Order Service and the `payment`, `prepare`, `validate`, `commit`, and `rollback` endpoints of the Customer Service.
  - Checks if the cost of the order is below the customer's credit limit.
  - If the check passes, records the order history and updates the amount the customer has spent.
- Get order information by order ID through the `getOrder` of the Order Service.
- Get order information by customer ID through the `getOrders` of the Order Service.
- Make a payment through the `repayment` endpoint of the Customer Service.
  - Reduces the amount the customer has spent.

## Prerequisites

- One of the following Java Development Kits (JDKs):
  - [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) LTS version (8, 11, or 17)
  - [OpenJDK](https://openjdk.org/install/) LTS version (8, 11, or 17)
- [Docker](https://www.docker.com/get-started/) 20.10 or later with [Docker Compose](https://docs.docker.com/compose/install/) V2 or later

{% capture notice--info %}
**Note**

We recommend using the LTS versions mentioned above, but other non-LTS versions may work.

In addition, other JDKs should work with ScalarDB, but we haven't tested them.
{% endcapture %}

<div class="notice--info">{{ notice--info | markdownify }}</div>

## Set up ScalarDB

The following sections describe how to set up the sample application that supports microservices transactions in ScalarDB.

### Clone the ScalarDB samples repository

Open **Terminal**, then clone the ScalarDB samples repository by running the following command:

```console
$ git clone https://github.com/scalar-labs/scalardb-samples
```

Then, go to the directory that contains the sample application by running the following command:

```console
$ cd scalardb-samples/microservice-transaction-sample
```

### Start Cassandra and MySQL

Cassandra and MySQL are already configured for the sample application, as shown in [`database.properties`](database.properties). For details about configuring the multi-storage transactions feature in ScalarDB, see [How to configure ScalarDB to support multi-storage transactions](https://github.com/scalar-labs/scalardb/blob/master/docs/multi-storage-transactions.md#how-to-configure-scalardb-to-support-multi-storage-transactions).

To start Cassandra and MySQL, which are included in the Docker container for the sample application, run the following command:

```console
$ docker-compose up -d mysql cassandra
```

{% capture notice--info %}
**Note**

Starting the Docker container may take more than one minute depending on your development environment.
{% endcapture %}

<div class="notice--info">{{ notice--info | markdownify }}</div>

### Load the schema

The database schema (the method in which the data will be organized) for the sample application has already been defined in [`customer-service-schema.json`](customer-service-schema.json) for the Customer Service and [`order-service-schema.json`](order-service-schema.json) for the Order Service.

To apply the schema, go to the [ScalarDB Releases](https://github.com/scalar-labs/scalardb/releases) page and download the ScalarDB Schema Loader that matches the version of ScalarDB that you want to use to the `scalardb-samples/microservice-transaction-sample` folder.

#### MySQL

To load the schema for [`customer-service-schema.json`](customer-service-schema.json) into MySQL, run the following command, replacing `<VERSION>` with the version of the ScalarDB Schema Loader that you downloaded:

```console
$ java -jar scalardb-schema-loader-<VERSION>.jar --config database-mysql.properties --schema-file customer-service-schema.json
```

#### Cassandra

To load the schema for [`order-service-schema.json`](order-service-schema.json) into Cassandra, run the following command, replacing `<VERSION>` with the version of the ScalarDB Schema Loader that you downloaded:

```console
$ java -jar scalardb-schema-loader-<VERSION>.jar --config database-cassandra.properties --schema-file order-service-schema.json --coordinator
```

#### Schema details

As shown in [`customer-service-schema.json`](customer-service-schema.json) for the sample application, all the tables for the Customer Service are created in the `customer_service` namespace.

- `customer_service.customers`: a table that manages customers' information
  - `credit_limit`: the maximum amount of money a lender will allow each customer to spend when using a line of credit
  - `credit_total`: the amount of money that each customer has already spent by using their line of credit

As shown in [`order-service-schema.json`](order-service-schema.json) for the sample application, all the tables for the Order Service are created in the `order_service` namespace.

- `order_service.orders`: a table that manages order information
- `order_service.statements`: a table that manages order statement information
- `order_service.items`: a table that manages information of items to be ordered

The Entity Relationship Diagram for the schema is as follows:

![ERD](images/ERD.png)

### Load the initial data by starting the microservices

Before starting the microservices, build the Docker images of the sample application by running the following command:

```console
$ ./gradlew docker
```

Then, start the microservices by running the following command:

```console
$ docker-compose up -d customer-service order-service
```

After starting the microservices and the initial data has loaded, the following records should be stored in the `customer_service.customers` table:

| customer_id | name          | credit_limit | credit_total |
|-------------|---------------|--------------|--------------|
| 1           | Yamada Taro   | 10000        | 0            |
| 2           | Yamada Hanako | 10000        | 0            |
| 3           | Suzuki Ichiro | 10000        | 0            |

And the following records should be stored in the `order_service.items` table:

| item_id | name   | price |
|---------|--------|-------|
| 1       | Apple  | 1000  |
| 2       | Orange | 2000  |
| 3       | Grape  | 2500  |
| 4       | Mango  | 5000  |
| 5       | Melon  | 3000  |

## Execute transactions and retrieve data in the sample application

The following sections describe how to execute transactions and retrieve data in the sample e-commerce application.

### Get customer information

Start with getting information about the customer whose ID is `1` by running the following command:

```console
$ ./gradlew :client:run --args="GetCustomerInfo 1"
```

You should see the following output:

```console
...
{"id": 1,"name": "Yamada Taro","credit_limit": 10000}
...
```

At this time, `credit_total` isn't shown, which means the current value of `credit_total` is `0`.

### Place an order

Then, have customer ID `1` place an order for three apples and two oranges by running the following command:

{% capture notice--info %}
**Note**

The order format in this command is `./gradlew run --args="PlaceOrder <CUSTOMER_ID> <ITEM_ID>:<COUNT>,<ITEM_ID>:<COUNT>,..."`.
{% endcapture %}

<div class="notice--info">{{ notice--info | markdownify }}</div>

```console
$ ./gradlew :client:run --args="PlaceOrder 1 1:3,2:2"
```

You should see a similar output as below, with a different UUID for `order_id`, which confirms that the order was successful:

```console
...
{"order_id": "4ccdb21c-ac03-4b48-bcb7-cad57eac1e79"}
...
```

### Check order details

Check details about the order by running the following command, replacing `<ORDER_ID_UUID>` with the UUID for the `order_id` that was shown after running the previous command:

```console
$ ./gradlew :client:run --args="GetOrder <ORDER_ID_UUID>"
```

You should see a similar output as below, with different UUIDs for `order_id` and `timestamp`:

```console
...
{"order": {"order_id": "4ccdb21c-ac03-4b48-bcb7-cad57eac1e79","timestamp": 1631605253126,"customer_id": 1,"customer_name": "Yamada Taro","statement": [{"item_id": 1,"item_name": "Apple","price": 1000,"count": 3,"total": 3000},{"item_id": 2,"item_name": "Orange","price": 2000,"count": 2,"total": 4000}],"total": 7000}}
...
```

### Place another order

Place an order for one melon that uses the remaining amount in `credit_total` for customer ID `1` by running the following command:

```console
$ ./gradlew :client:run --args="PlaceOrder 1 5:1"
```

You should see a similar output as below, with a different UUID for `order_id`, which confirms that the order was successful:

```console
...
{"order_id": "0b10db66-faa6-4323-8a7a-474e8534a7ee"}
...
```

### Check order history

Get the history of all orders for customer ID `1` by running the following command:

```console
$ ./gradlew :client:run --args="GetOrders 1"
```

You should see a similar output as below, with different UUIDs for `order_id` and `timestamp`, which shows the history of all orders for customer ID `1` in descending order by timestamp:

```console
...
{"order": [{"order_id": "0b10db66-faa6-4323-8a7a-474e8534a7ee","timestamp": 1631605501485,"customer_id": 1,"customer_name": "Yamada Taro","statement": [{"item_id": 5,"item_name": "Melon","price": 3000,"count": 1,"total": 3000}],"total": 3000},{"order_id": "4ccdb21c-ac03-4b48-bcb7-cad57eac1e79","timestamp": 1631605253126,"customer_id": 1,"customer_name": "Yamada Taro","statement": [{"item_id": 1,"item_name": "Apple","price": 1000,"count": 3,"total": 3000},{"item_id": 2,"item_name": "Orange","price": 2000,"count": 2,"total": 4000}],"total": 7000}]}
...
```

### Check credit total

Get the credit total for customer ID `1` by running the following command:

```console
$ ./gradlew :client:run --args="GetCustomerInfo 1"
```

You should see the following output, which shows that customer ID `1` has reached their `credit_limit` in `credit_total` and cannot place anymore orders:

```console
...
{"id": 1,"name": "Yamada Taro","credit_limit": 10000,"credit_total": 10000}
...
```

Try to place an order for one grape and one mango by running the following command:

```console
$ ./gradlew :client:run --args="PlaceOrder 1 3:1,4:1"
```

You should see the following output, which shows that the order failed because the `credit_total` amount would exceed the `credit_limit` amount:

```console
...
io.grpc.StatusRuntimeException: FAILED_PRECONDITION: Credit limit exceeded
        at io.grpc.stub.ClientCalls.toStatusRuntimeException(ClientCalls.java:271)
        at io.grpc.stub.ClientCalls.getUnchecked(ClientCalls.java:252)
        at io.grpc.stub.ClientCalls.blockingUnaryCall(ClientCalls.java:165)
        at sample.rpc.OrderServiceGrpc$OrderServiceBlockingStub.placeOrder(OrderServiceGrpc.java:296)
        at sample.client.command.PlaceOrderCommand.call(PlaceOrderCommand.java:38)
        at sample.client.command.PlaceOrderCommand.call(PlaceOrderCommand.java:12)
        at picocli.CommandLine.executeUserObject(CommandLine.java:2041)
        at picocli.CommandLine.access$1500(CommandLine.java:148)
        at picocli.CommandLine$RunLast.executeUserObjectOfLastSubcommandWithSameParent(CommandLine.java:2461)
        at picocli.CommandLine$RunLast.handle(CommandLine.java:2453)
        at picocli.CommandLine$RunLast.handle(CommandLine.java:2415)
        at picocli.CommandLine$AbstractParseResultHandler.execute(CommandLine.java:2273)
        at picocli.CommandLine$RunLast.execute(CommandLine.java:2417)
        at picocli.CommandLine.execute(CommandLine.java:2170)
        at sample.client.Client.main(Client.java:39)
...
```

### Make a payment

To continue making orders, customer ID `1` must make a payment to reduce the `credit_total` amount.

Make a payment by running the following command:

```console
$ ./gradlew :client:run --args="Repayment 1 8000"
```

Then, check the `credit_total` amount for customer ID `1` by running the following command:

```console
$ ./gradlew :client:run --args="GetCustomerInfo 1"
```

You should see the following output, which shows that a payment was applied to customer ID `1`, reducing the `credit_total` amount:

```console
...
{"id": 1,"name": "Yamada Taro","credit_limit": 10000,"credit_total": 2000}
...
```

Now that customer ID `1` has made a payment, place an order for one grape and one melon by running the following command:

```console
$ ./gradlew :client:run --args="PlaceOrder 1 3:1,4:1"
```

You should see a similar output as below, with a different UUID for `order_id`, which confirms that the order was successful:

```console
...
{"order_id": "dd53dd9d-aaf4-41db-84b2-56951fed6425"}
...
```

## Stop the sample application

To stop the sample application, you need to stop the Docker containers that are running Cassandra, MySQL, and the microservices. To stop the Docker containers, run the following command:

```console
$ docker-compose down
```

## Reference - How the microservice transaction is achieved

The transaction for placing an order achieves the microservice transaction, so this section focuses on how the transaction that spans the Customer Service and the Order Service is implemented.

The following sequence diagram shows the transaction for placing an order:

![Microservice transaction sequence diagram](images/sequence_diagram.png)

### 1. Transaction with a two-phase commit interface is started

When a client sends a request to place an order to the Order Service, `OrderService.placeOrder()` is called, and the microservice transaction starts.

At first, the Order Service starts a transaction with a two-phase commit interface with `start()` as follows. For reference, see [`OrderService.java`](order-service/src/main/java/sample/order/OrderService.java).

```java
transaction = twoPhaseCommitTransactionManager.start();
```

### 2. CRUD operations are executed

After the transaction with a two-phase commit interface starts, CRUD operations are executed. The Order Service puts the order information in the `order_service.orders` table and the detailed information in the `order_service.statements` table as follows. For reference, see [`OrderService.java`](order-service/src/main/java/sample/order/OrderService.java):

```java
// Put the order info into the `orders` table.
Order.put(transaction, orderId, request.getCustomerId(), System.currentTimeMillis());

int amount = 0;
for (ItemOrder itemOrder : request.getItemOrderList()) {
  // Put the order statement into the `statements` table.
  Statement.put(transaction, orderId, itemOrder.getItemId(), itemOrder.getCount());

  // Retrieve the item info from the `items` table.
  Optional<Item> item = Item.get(transaction, itemOrder.getItemId());
  if (!item.isPresent()) {
    responseObserver.onError(
        Status.NOT_FOUND.withDescription("Item not found").asRuntimeException());
    return;
  }

  // Calculate the total amount.
  amount += item.get().price * itemOrder.getCount();
}
```

Then, the Order Service calls the `payment` gRPC endpoint of the Customer Service along with the transaction ID. For reference, see [`OrderService.java`](order-service/src/main/java/sample/order/OrderService.java).

This endpoint first joins the transaction with `join()` as follows. For reference, see [`CustomerService.java`](customer-service/src/main/java/sample/customer/CustomerService.java).

```java
twoPhaseCommitTransactionManager.join(request.getTransactionId());
```

The endpoint then gets the customer information and checks if the customer's credit total exceeds the credit limit after the payment. If the credit total does not exceed the credit limit, the endpoint updates the customer's credit total. For reference, see [`CustomerService.java`](customer-service/src/main/java/sample/customer/CustomerService.java).

```java
// Join the transaction that the Order Service started.
transaction = twoPhaseCommitTransactionManager.join(request.getTransactionId());

// Retrieve the customer info for the customer ID.
Optional<Customer> result = Customer.get(transaction, request.getCustomerId());
if (!result.isPresent()) {
  responseObserver.onError(
      Status.NOT_FOUND.withDescription("Customer not found").asRuntimeException());
  return;
}

int updatedCreditTotal = result.get().creditTotal + request.getAmount();

// Check if the credit total exceeds the credit limit after payment.
if (updatedCreditTotal > result.get().creditLimit) {
  responseObserver.onError(
      Status.FAILED_PRECONDITION
          .withDescription("Credit limit exceeded")
          .asRuntimeException());
  return;
}

// Update `credit_total` for the customer.
Customer.updateCreditTotal(transaction, request.getCustomerId(), updatedCreditTotal);
```

### 3. Transaction is committed by using the two-phase commit protocol

After the Order Service receives the update that the payment succeeded, the Order Service tries to commit the transaction.

To commit the transaction, the Order Service starts with preparing the transaction. The Order Service calls `prepare()` from its transaction object and calls the `prepare` gRPC endpoint of the Customer Service. For reference, see [`OrderService.java`](order-service/src/main/java/sample/order/OrderService.java):

```java
transaction.prepare();
callPrepareEndpoint(transaction.getId());
```

In this endpoint, the Customer Service resumes the transaction and calls `prepare()` from its transaction object, as well. For reference, see [`CustomerService.java`](customer-service/src/main/java/sample/customer/CustomerService.java):

```java
// Resume the transaction.
transaction = twoPhaseCommitTransactionManager.resume(request.getTransactionId());

// Prepare the transaction.
transaction.prepare();
```

Similarly, the Order Service and the Customer Service call `validate()` from their transaction objects. For reference, see [`OrderService.java`](order-service/src/main/java/sample/order/OrderService.java) and [`CustomerService.java`](customer-service/src/main/java/sample/customer/CustomerService.java). For details about `validate()`, see [Validate the transaction](https://github.com/scalar-labs/scalardb/blob/master/docs/two-phase-commit-transactions.md#validate-the-transaction).

After preparing and validating the transaction succeeds in both the Order Service and the Customer Service, the transaction can be committed. In this case, the Order Service calls `commit()` from its transaction object and then calls the `commit` gRPC endpoint of the Customer Service. For reference, see [`OrderService.java`](order-service/src/main/java/sample/order/OrderService.java).

```java
transaction.commit();
callCommitEndpoint(transaction.getId());
```

In this endpoint, the Customer Service resumes the transaction and calls `commit()` from its transaction object, as well. For reference, see [`CustomerService.java`](customer-service/src/main/java/sample/customer/CustomerService.java).

```java
// Resume the transaction.
transaction = twoPhaseCommitTransactionManager.resume(request.getTransactionId());

// Commit the transaction.
transaction.commit();
```

### Error handling

If an error happens while executing a transaction, you will need to roll back the transaction. In this case, the Order Service calls `rollback()` from its transaction object and then calls the `rollback` gRPC endpoint of the Customer Service. For reference, see [`OrderService.java`](order-service/src/main/java/sample/order/OrderService.java).

```java
transaction.rollback();
callRollbackEndpoint(transaction.getId());
```

In this endpoint, the Customer Service resumes the transaction and calls `rollback()` from its transaction object, as well. For reference, see [`CustomerService.java`](customer-service/src/main/java/sample/customer/CustomerService.java).

```java
// Resume the transaction.
TwoPhaseCommitTransaction transaction =
    twoPhaseCommitTransactionManager.resume(request.getTransactionId());

// Roll back the transaction.
transaction.rollback();
```

For details about how to handle exceptions in ScalarDB, see [How to handle exceptions](https://github.com/scalar-labs/scalardb/blob/master/docs/api-guide.md#how-to-handle-exceptions).
