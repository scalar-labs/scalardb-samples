# Sample application of Spring Data JDBC for ScalarDB with Multi-storage Transactions

This tutorial describes how to create a sample Spring Boot application by using Spring Data JDBC for ScalarDB with Multi-storage Transactions.

## Prerequisites

- Java (OpenJDK 8 or higher)
- Gradle
- Docker, Docker Compose

## Sample application

### Overview

This tutorial describes how to create a sample Spring Boot application for the same use case as [ScalarDB Sample](https://github.com/scalar-labs/scalardb-samples/tree/main/scalardb-sample) but by using Spring Data JDBC for ScalarDB with Multi-storage Transaction.
Please note that application-specific error handling, authentication processing, etc. are omitted in the sample application since this tutorial focuses on explaining how to use Spring Data JDBC for ScalarDB with Multi-storage Transaction.
For details, please see [Guide of Spring Data JDBC for ScalarDB](https://scalar-labs.github.io/scalardb-sql/spring-data-guide.html).

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

All the tables are created in the `customer` and `order` namespaces.

- `customer.customers`: a table that manages customers' information
  - `credit_limit`: the maximum amount of money a lender will allow each customer to spend when using a credit card
  - `credit_total`: the amount of money that each customer has already spent by using the credit card
- `order.orders`: a table that manages order information
- `order.statements`: a table that manages order statement information
- `order.items`: a table that manages information of items to be ordered

The Entity Relationship Diagram for the schema is as follows:

![ERD](images/ERD.png)

### Transactions

The following five transactions are implemented in this sample application:

1. Getting customer information
2. Placing an order by credit card (checks if the cost of the order is below the credit limit, then records order history and updates the `credit_total` if the check passes)
3. Getting order information by order ID
4. Getting order information by customer ID
5. Repayment (reduces the amount in the `credit_total`)

### Configuration

Configurations for the sample Spring Boot application are as follows:

```application.properties
spring.datasource.driver-class-name=com.scalar.db.sql.jdbc.SqlJdbcDriver
spring.datasource.url=jdbc:scalardb:\
?scalar.db.storage=multi-storage\
&scalar.db.multi_storage.storages=cassandra,mysql\
&scalar.db.multi_storage.storages.cassandra.storage=cassandra\
&scalar.db.multi_storage.storages.cassandra.contact_points=localhost\
&scalar.db.multi_storage.storages.cassandra.username=cassandra\
&scalar.db.multi_storage.storages.cassandra.password=cassandra\
&scalar.db.multi_storage.storages.mysql.storage=jdbc\
&scalar.db.multi_storage.storages.mysql.contact_points=jdbc:mysql://localhost:3306/\
&scalar.db.multi_storage.storages.mysql.username=root\
&scalar.db.multi_storage.storages.mysql.password=mysql\
&scalar.db.multi_storage.namespace_mapping=customer:mysql,order:cassandra,coordinator:cassandra\
&scalar.db.multi_storage.default_storage=cassandra
```

- `scalar.db.storage`: Specifying `multi-storage` is necessary to use Multi-storage Transactions in ScalarDB.
- `scalar.db.multi_storage.storages`: Your storage names must be defined here.
- `scalar.db.multi_storage.storages.cassandra.*`: These configurations are for the `cassandra` storage, which is one of the storage names defined in `scalar.db.multi_storage.storages`. You can configure all the `scalar.db.*` properties for the `cassandra` storage here.
- `scalar.db.multi_storage.storages.mysql.*`: These configurations are for the `mysql` storage, which is one of the storage names defined in `scalar.db.multi_storage.storages`. You can configure all the `scalar.db.*` properties for the `mysql` storage here.
- `scalar.db.multi_storage.namespace_mapping`: This configuration maps the namespaces to the storage. In this sample application, operations for `customer` namespace tables are mapped to the `mysql` storage and operations for `order` namespace tables are mapped to the `cassandra` storage. You can also define which storage is mapped for the `coordinator` namespace that is used in Consensus Commit transactions.
- `scalar.db.multi_storage.default_storage`: This configuration sets the default storage that is used for operations on unmapped namespace tables.

For details, please see [Configuration - Multi-storage Transactions](https://github.com/scalar-labs/scalardb/blob/master/docs/multi-storage-transactions.md#configuration).

## Setup

To start Cassandra and MySQL, and load the schema, you need to run the following `docker-compose` command:

```shell
$ docker-compose up -d
```

Please note that starting the containers may take more than one minute.

### Initial data

After the containers have started, you need to load the initial data by running the following command:

```shell
$ ./gradlew run --args="LoadInitialData"
```

After the initial data has loaded, the following records should be stored in the tables:

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

Let's start with getting information about the customer whose ID is `1`:

```shell
$ ./gradlew run --args="GetCustomerInfo 1"
...
{"customer_id":1,"name":"Yamada Taro","credit_limit":10000,"credit_total":0}
...
```

Then, place an order for three apples and two oranges by using customer ID `1`. Note that the order format is `<Item ID>:<Count>,<Item ID>:<Count>,...`:

```shell
$ ./gradlew run --args="PlaceOrder 1 1:3,2:2"
...
{"order_id":"5d49eb62-fcb9-4dd2-9ae5-e714d989937f","customer_id":1,"timestamp":1677564659810}
...
```

You can see that running this command shows the order ID.

Let's check the details of the order by using the order ID:

```shell
$ ./gradlew run --args="GetOrder 5d49eb62-fcb9-4dd2-9ae5-e714d989937f"
...
{"order_id":"5d49eb62-fcb9-4dd2-9ae5-e714d989937f","timestamp":1677564659810,"customer_id":1,"customer_name":"Yamada Taro","statements":[{"item_id":1,"item_name":"Apple","price":1000,"count":3,"total":3000},{"item_id":2,"item_name":"Orange","price":2000,"count":2,"total":4000}],"total":7000}
...
```

Then, let's place another order and get the order history of customer ID `1`:

```shell
$ ./gradlew run --args="PlaceOrder 1 5:1"
...
{"order_id":"ccd97d75-ee57-4393-a0bb-5230c4a8c68a","customer_id":1,"timestamp":1677564776069}
...
$ ./gradlew run --args="GetOrders 1"
...
[{"order_id":"ccd97d75-ee57-4393-a0bb-5230c4a8c68a","timestamp":1677564776069,"customer_id":1,"customer_name":"Yamada Taro","statements":[{"item_id":5,"item_name":"Melon","price":3000,"count":1,"total":3000}],"total":3000},{"order_id":"5d49eb62-fcb9-4dd2-9ae5-e714d989937f","timestamp":1677564659810,"customer_id":1,"customer_name":"Yamada Taro","statements":[{"item_id":1,"item_name":"Apple","price":1000,"count":3,"total":3000},{"item_id":2,"item_name":"Orange","price":2000,"count":2,"total":4000}],"total":7000}]
...
```

This order history is shown in descending order by timestamp.

The customer's current `credit_total` is `10000`. Since the customer has now reached their `credit_limit`, which was shown when retrieving their information, they cannot place anymore orders.

```shell
$ ./gradlew run --args="GetCustomerInfo 1"
...
{"customer_id":1,"name":"Yamada Taro","credit_limit":10000,"credit_total":10000}
...
$ ./gradlew run --args="PlaceOrder 1 3:1,4:1"
...
java.lang.RuntimeException: Credit limit exceeded. limit:10000, total:17500
        at sample.SampleService.placeOrder(SampleService.java:102)
        at sample.SampleService$$FastClassBySpringCGLIB$$1123c447.invoke(<generated>)
        at org.springframework.cglib.proxy.MethodProxy.invoke(MethodProxy.java:218)
        at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.invokeJoinpoint(CglibAopProxy.java:793)
        at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163)
        at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:763)
        at org.springframework.transaction.interceptor.TransactionInterceptor$1.proceedWithInvocation(TransactionInterceptor.java:123)
        at org.springframework.transaction.interceptor.TransactionAspectSupport.invokeWithinTransaction(TransactionAspectSupport.java:388)
        at org.springframework.transaction.interceptor.TransactionInterceptor.invoke(TransactionInterceptor.java:119)
        at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186)
        at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:763)
        at org.springframework.aop.framework.CglibAopProxy$DynamicAdvisedInterceptor.intercept(CglibAopProxy.java:708)
        at sample.SampleService$$EnhancerBySpringCGLIB$$1cb0cc8c.placeOrder(<generated>)
        at sample.command.PlaceOrderCommand.call(PlaceOrderCommand.java:37)
        at sample.command.PlaceOrderCommand.call(PlaceOrderCommand.java:13)
        at picocli.CommandLine.executeUserObject(CommandLine.java:2041)
        at picocli.CommandLine.access$1500(CommandLine.java:148)
        at picocli.CommandLine$RunLast.executeUserObjectOfLastSubcommandWithSameParent(CommandLine.java:2461)
        at picocli.CommandLine$RunLast.handle(CommandLine.java:2453)
        at picocli.CommandLine$RunLast.handle(CommandLine.java:2415)
        at picocli.CommandLine$AbstractParseResultHandler.execute(CommandLine.java:2273)
        at picocli.CommandLine$RunLast.execute(CommandLine.java:2417)
        at picocli.CommandLine.execute(CommandLine.java:2170)
        at sample.SampleApp.run(SampleApp.java:26)
        at org.springframework.boot.SpringApplication.callRunner(SpringApplication.java:768)
        at org.springframework.boot.SpringApplication.callRunners(SpringApplication.java:752)
        at org.springframework.boot.SpringApplication.run(SpringApplication.java:314)
        at org.springframework.boot.SpringApplication.run(SpringApplication.java:1303)
        at org.springframework.boot.SpringApplication.run(SpringApplication.java:1292)
        at sample.SampleApp.main(SampleApp.java:35)
...
```

After making a payment, the customer will be able to place orders again.

```shell
$ ./gradlew run --args="Repayment 1 8000"
...
$ ./gradlew run --args="GetCustomerInfo 1"
...
{"customer_id":1,"name":"Yamada Taro","credit_limit":10000,"credit_total":2000}
...
$ ./gradlew run --args="PlaceOrder 1 3:1,4:1"
...
{"order_id":"3ac4a1bf-a724-4f26-b948-9f03281a971e","customer_id":1,"timestamp":1677565028204}
...
```

## Cleanup

To stop Cassandra and MySQL, run the following command:

```shell
$ docker-compose down
```

