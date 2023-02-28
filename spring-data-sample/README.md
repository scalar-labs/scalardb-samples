# Spring Data integration Sample

This is a sample Spring Boot application for Spring Data integration with ScalarDB.

## Prerequisites
- Java (OpenJDK 8 or higher)
- Gradle
- Docker, Docker Compose

## Sample application

### Overview

This article describes how to create a Spring Boot applicaiton for the same use case as [ScalarDB Sample](https://github.com/scalar-labs/scalardb-samples/tree/main/scalardb-sample) using Spring Data integration with ScalarDB.
Please note that application-specific error handling, authentication processing, etc., are omitted in the sample application since it focuses on explaining how to use Spring Data integration.
Please see [this document](https://scalar-labs.github.io/scalardb-sql/spring-data-guide.html) for the details.

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

The configurations for the sample Spring Boot application is as follows:

```application.properties
spring.datasource.driver-class-name=com.scalar.db.sql.jdbc.SqlJdbcDriver
spring.datasource.url=jdbc:scalardb:
?scalar.db.storage=cassandra\
&scalar.db.contact_points=localhost\
&scalar.db.username=cassandra\
&scalar.db.password=cassandra
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
{"customer_id":1,"name":"Yamada Taro","credit_limit":10000,"credit_total":0}
...
```

Then, place an order for three apples and two oranges with customer ID `1`. Note that the format of order is `<Item ID>:<Count>,<Item ID>:<Count>,...`:
```shell
$ ./gradlew run --args="PlaceOrder 1 1:3,2:2"
...
{"order_id":"2358ab35-5819-4f8f-acb1-12e73d97d34e","customer_id":1,"timestamp":1677478005400}
...
```

The command shows the order ID of the order.

Let's check the details of the order with the order ID:
```shell
$ ./gradlew run --args="GetOrder 2358ab35-5819-4f8f-acb1-12e73d97d34e"
...
{"order_id":"2358ab35-5819-4f8f-acb1-12e73d97d34e","timestamp":1677478005400,"customer_id":1,"customer_name":"Yamada Taro","statements":[{"item_id":1,"item_name":"Apple","price":1000,"count":3,"total":3000},{"item_id":2,"item_name":"Orange","price":2000,"count":2,"total":4000}],"total":7000}
...
```

So, let's place an order again and get the order histories by customer ID `1`:
```shell
$ ./gradlew run --args="PlaceOrder 1 5:1"
...
{"order_id":"46062b16-b71b-46f9-a9ff-dc6b0991259b","customer_id":1,"timestamp":1677478201428}
...
$ ./gradlew run --args="GetOrders 1"
...
[{"order_id":"2358ab35-5819-4f8f-acb1-12e73d97d34e","timestamp":1677478005400,"customer_id":1,"customer_name":"Yamada Taro","statements":[{"item_id":1,"item_name":"Apple","price":1000,"count":3,"total":3000},{"item_id":2,"item_name":"Orange","price":2000,"count":2,"total":4000}],"total":7000},{"order_id":"46062b16-b71b-46f9-a9ff-dc6b0991259b","timestamp":1677478201428,"customer_id":1,"customer_name":"Yamada Taro","statements":[{"item_id":5,"item_name":"Melon","price":3000,"count":1,"total":3000}],"total":3000}]
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
        at sample.SampleService$$EnhancerBySpringCGLIB$$a94e1d9.placeOrder(<generated>)
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

After repayment, the customer will be able to place an order again!

```shell
$ ./gradlew run --args="Repayment 1 8000"
...
$ ./gradlew run --args="GetCustomerInfo 1"
...
{"customer_id":1,"name":"Yamada Taro","credit_limit":10000,"credit_total":2000}
...
$ ./gradlew run --args="PlaceOrder 1 3:1,4:1"
...
{"order_id":"0350947a-9003-46f2-870e-6aa4b2df0f1f","customer_id":1,"timestamp":1677478728134}
...
```

## Clean up

To stop Cassandra, run the following command:
```shell
$ docker-compose down
```

