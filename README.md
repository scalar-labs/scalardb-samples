# Scalar DB Samples
- This repository contains the sample application that will connect to `ScalarDB Server` as backend. For using native `ScalarDB` library to connect to `ScalarDB` directly please refer to this document [`Getting Started`](https://github.com/scalar-labs/scalardb/blob/master/docs/getting-started.md).
- `ScalarDB Server` is `gRPC` server that implements ScalarDB interface. More information about `ScalarDB Server` can be found [`here`](https://github.com/scalar-labs/scalardb/tree/master/docs/scalardb-server.md)

## Our sample application
A simple electronic money cli app which has features:
- Charge an `amount` to a `user_id`
- Pay `amount` from a `user_id` to another `user_id`

## Prerequisites
- Java (OpenJDK 8 or higher)
- Gradle
- Docker, Docker Compose

## Setting up
### ScalarDB Server
- In this sample we will use Cassandra as storage for our ScalarDB server. The Cassandra will also run along with ScalarDB Server in containers. The setting for our ScalarDB Server is [`database.properties`](./database.properties)
```
# Comma separated contact points
scalar.db.contact_points=cassandra

# Port number for all the contact points. Default port number for each database is used if empty.
scalar.db.contact_port=9042

# Credential information to access the database
scalar.db.username=cassandra
scalar.db.password=cassandra

# Storage implementation. Either cassandra or cosmos or dynamo or jdbc can be set. Default storage is cassandra.
scalar.db.storage=cassandra
```
- To start our ScalarDB Server. We use below command (noted that we should wait around a bit more one minute to make sure every container is already up, because ScalarDB container have to wait for Cassandra already up)
```
docker-compose -f docker-compose-cassandra.yml up -d
```
- *For using other databases as backend for ScalarDB Server we can change the configuration in [`database.properties`](database.properties) according to [`Getting Started`](https://github.com/scalar-labs/scalardb/blob/master/docs/getting-started.md). After that we can start our `ScalarDB Server` using this [`docker-compose.yml`](docker-compose.yml) instead.*
### ScalarDB Client
- Our sample application will connect to `ScalarDB Server` via gRPC, the configuration basically is same format with our server configuration, the different is we have to change storage to gRPC and transaction manager to gRPC as well. The configuration is [`scalardb-client.properties`](scalardb-client.properties).
```
# Comma separated contact points
scalar.db.contact_points=127.0.0.1

# Port number for all the contact points. Default port number for each database is used if empty.
scalar.db.contact_port=60051

# Credential information to access the database
scalar.db.username=
scalar.db.password=

# Storage implementation
scalar.db.storage=grpc

# The type of the transaction manager
scalar.db.transaction_manager=grpc
```
- Setting up ScalarDB schema: As mentioned in [`Getting started with ScalarDB`](https://github.com/scalar-labs/scalardb/blob/master/docs/getting-started-with-scalardb.md) storing and retrieving data in `ScalarDB` can be `storage` or `transaction`.
    - Our storage schema is  [`emoney-storage-schema.json`](./src/main/resources/emoney-storage-schema.json)
    ```
    {
      "emoney_storage.account": {
        "transaction": false,
        "partition-key": [
          "id"
        ],
        "clustering-key": [],
        "columns": {
          "id": "TEXT",
          "balance": "INT"
        }
      }
    }
    ```
    to register schema to our Cassandra instance we run following command:
    ```
    java -jar tools/scalar-schema-standalone-3.0.0.jar --cassandra -h localhost -u cassandra -p cassandra -f src/main/resources/emoney-storage-schema.json
    ```
    noted that `scalar-schema-standalone-3.0.0.jar` is schema registration tool that can be found in [`release`](https://github.com/scalar-labs/scalardb/releases) of `ScalarDB`

    - In case using transaction, our schema looks like [`emoney-transaction-schema.json`](src/main/resources/emoney-transaction-schema.json)
    ```
    {
      "emoney_transaction.account": {
        "transaction": true,
        "partition-key": [
          "id"
        ],
        "clustering-key": [],
        "columns": {
          "id": "TEXT",
          "balance": "INT"
        }
      }
    }
    ```
    and the registering command is
    ```
    java -jar tools/scalar-schema-standalone-3.0.0.jar --cassandra -h localhost -u cassandra -p cassandra -f src/main/resources/emoney-transaction-schema.json
    ```

## Run our sample
- Scenario:
    - Charge a user `user1` amount 10.
    - Charge a user `user2` amount 20.
    - `user1` pay amount of 5 to `user2`
  
- Run
    - Storage mode
     ``` 
     # charge user1 with amount 10
     ./gradlew run --args="cmd storage charge -a 10 -u user1"
     
     # charge user2 with amount 20
     ./gradlew run --args="cmd storage charge -a 20 -u user2"
     
     # pay 5 from user1 to user2
     ./gradlew run --args="cmd storage pay -a 5 -f user1 -t user2"
     ```

    - Transaction mode
     ``` 
     # charge user1 with amount 10
     ./gradlew run --args="cmd transaction charge -a 10 -u user1"
     
     # charge user2 with amount 20
     ./gradlew run --args="cmd transaction charge -a 20 -u user2"
     
     # pay 5 from user1 to user2
     ./gradlew run --args="cmd transaction pay -a 5 -f user1 -t user2"
     ```
