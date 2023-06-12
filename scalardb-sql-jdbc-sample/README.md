# ScalarDB SQL (JDBC) Sample

This tutorial describes how to create a sample application by using ScalarDB SQL (JDBC).

## Prerequisites

- Java (OpenJDK 8 or higher)
- Gradle
- Docker, Docker Compose

In addition, you need access to the [ScalarDB SQL GitHub repository](https://github.com/scalar-labs/scalardb-sql) and [Packages in ScalarDB SQL repository](https://github.com/orgs/scalar-labs/packages?repo_name=scalardb-sql).
These repositories are available only to users with a commercial license and permission.
To get a license and permission, please [contact us](https://scalar-labs.com/contact_us/).

You also need the `gpr.user` property for your GitHub username and the `gpr.key` property for your personal access token. 
You must either add these properties in `~/.gradle/gradle.properties` or specify the properties by using the `-P` option when running the `./gradlew` command as follows:

```shell
$ ./gradlew run ... -Pgpr.user=<YOUR_GITHUB_USERNAME> -Pgpr.key=<YOUR_PERSONAL_ACCESS_TOKEN>
```

Or you can also use environment variables, `USERNAME` for your GitHub username and `TOKEN` for your personal access token.

```shell
$ export USERNAME=<YOUR_GITHUB_USERNAME>
$ export TOKEN=<YOUR_PERSONAL_ACCESS_TOKEN>
```

For more details, see [Install - ScalarDB SQL](https://github.com/scalar-labs/scalardb-sql#install).

## Sample application

### Overview

This tutorial describes how to create a sample application for the same use case as [ScalarDB Sample](https://github.com/scalar-labs/scalardb-samples/tree/main/scalardb-sample) but by using ScalarDB SQL (JDBC).
In this tutorial, you will build the application on Cassandra.
Although Cassandra does not provide ACID transaction capabilities, Cassandra can support ACID transactions by interfacing through ScalarDB SQL (JDBC).
Please note that application-specific error handling, authentication processing, and similar functions are not included in the sample application, as the focus is on demonstrating the use of ScalarDB SQL (JDBC).
For detailed information on exception handling in ScalarDB SQL (JDBC), see [Handle SQLException](https://github.com/scalar-labs/scalardb-sql/blob/main/docs/jdbc-guide.md#handle-sqlexception).

### Schema

[The schema](schema.sql) is as follows:

```sql
CREATE NAMESPACE IF NOT EXISTS sample;

CREATE TABLE IF NOT EXISTS sample.customers (
  customer_id INT PRIMARY KEY,
  name TEXT,
  credit_limit INT,
  credit_total INT
);

CREATE TABLE IF NOT EXISTS sample.orders (
  customer_id INT,
  timestamp BIGINT,
  order_id TEXT,
  PRIMARY KEY (customer_id, timestamp)
);

CREATE INDEX IF NOT EXISTS ON sample.orders (order_id);

CREATE TABLE IF NOT EXISTS sample.statements (
  order_id TEXT,
  item_id INT,
  count INT,
  PRIMARY KEY (order_id, item_id)
);

CREATE TABLE IF NOT EXISTS sample.items (
  item_id INT PRIMARY KEY,
  name TEXT,
  price INT
);
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
scalar.db.sql.connection_mode=direct

scalar.db.storage=cassandra
scalar.db.contact_points=localhost
scalar.db.username=cassandra
scalar.db.password=cassandra
```

Since this sample application uses Cassandra, as shown above, you need to configure your settings for Cassandra in this configuration.

## Setup Cassandra

To start Cassandra, you need to run the following `docker-compose` command:

```shell
$ docker-compose up -d
```

Please note that starting the containers may take more than one minute.

## Load schema

You then need to apply the schema with the following command.
To download the CLI tool, `scalardb-sql-cli-<VERSION>-all.jar`, see the [Releases](https://github.com/scalar-labs/scalardb-sql/releases) of ScalarDB SQL and download the version that you want to use.

```shell
$ java -jar scalardb-sql-cli-<VERSION>-all.jar --config scalardb-sql.properties --file schema.sql
```

## Load initial data

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
{"order_id": "454f9c97-f456-44fd-96da-f527187fe39b"}
...
```

You can see that running this command shows the order ID.

Let's check the details of the order by using the order ID:

```shell
$ ./gradlew run --args="GetOrder 454f9c97-f456-44fd-96da-f527187fe39b"
...
{"order": {"order_id": "454f9c97-f456-44fd-96da-f527187fe39b","timestamp": 1685602722821,"customer_id": 1,"customer_name": "Yamada Taro","statement": [{"item_id": 1, "name": "Apple", "price": 1000, "count": 3},{"item_id": 2, "name": "Orange", "price": 2000, "count": 2}],"total": 7000}}
...
```

Then, let's place another order and get the order history of customer ID `1`:

```shell
$ ./gradlew run --args="PlaceOrder 1 5:1"
...
{"order_id": "3f40c718-59ec-48aa-a6fe-2fdaf12ad094"}
...
$ ./gradlew run --args="GetOrders 1"
...
{"order": [{"order_id": "454f9c97-f456-44fd-96da-f527187fe39b","timestamp": 1685602722821,"customer_id": 1,"customer_name": "Yamada Taro","statement": [{"item_id": 1, "name": "Apple", "price": 1000, "count": 3},{"item_id": 2, "name": "Orange", "price": 2000, "count": 2}],"total": 7000},{"order_id": "3f40c718-59ec-48aa-a6fe-2fdaf12ad094","timestamp": 1685602811718,"customer_id": 1,"customer_name": "Yamada Taro","statement": [{"item_id": 5, "name": "Melon", "price": 3000, "count": 1}],"total": 3000}]}
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
        at sample.Sample.placeOrder(Sample.java:184)
        at sample.command.PlaceOrderCommand.call(PlaceOrderCommand.java:32)
        at sample.command.PlaceOrderCommand.call(PlaceOrderCommand.java:8)
        at picocli.CommandLine.executeUserObject(CommandLine.java:2041)
        at picocli.CommandLine.access$1500(CommandLine.java:148)
        at picocli.CommandLine$RunLast.executeUserObjectOfLastSubcommandWithSameParent(CommandLine.java:2461)
        at picocli.CommandLine$RunLast.handle(CommandLine.java:2453)
        at picocli.CommandLine$RunLast.handle(CommandLine.java:2415)
        at picocli.CommandLine$AbstractParseResultHandler.execute(CommandLine.java:2273)
        at picocli.CommandLine$RunLast.execute(CommandLine.java:2417)
        at picocli.CommandLine.execute(CommandLine.java:2170)
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
{"order_id": "fb71279d-88ea-4974-a102-0ec4e7d65e25"}
...
```

## Clean up

To stop Cassandra, run the following command:

```shell
$ docker-compose down
```
