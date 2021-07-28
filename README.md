# Scalar DB Server Samples
This repository contains a sample application that uses Scalar DB server, a gRPC server that implements Scalar DB interface, as a backend. For using the native `Scalar DB` library, please refer to [Getting Started](https://github.com/scalar-labs/scalardb/blob/master/docs/getting-started.md).
More information about Scalar DB server can be found [here](https://github.com/scalar-labs/scalardb/tree/master/docs/scalardb-server.md)

## Sample application
The sample application is a simple electronic money application that has the following features:
- Charge an `amount` to a `user_id`
- Pay an `amount` from a `user_id` to another `user_id`

## Prerequisites
- Java (OpenJDK 8 or higher)
- Gradle
- Docker, Docker Compose

## Set up
### Scalar DB server
- In this sample, we will use Cassandra as storage for Scalar DB server. The configuration of Scalar DB server is shown below. (It is also stored in [database.properties](./database.properties))
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
- To start Scalar DB server, we use the following command. Please note that we should wait around a bit more than one minute because Scalar DB container has to wait for Cassandra container to be fully started.
```
docker-compose -f docker-compose-cassandra.yml up -d
```
*For using other databases as the backend for Scalar DB server, we can change the configuration of [database.properties](database.properties) according to [Getting Started](https://github.com/scalar-labs/scalardb/blob/master/docs/getting-started.md). After that we can start Scalar DB server using [docker-compose.yml](docker-compose.yml) instead.*

### Scalar DB client
- The sample application uses a client that implements Scalar DB interface. Thus, you can configure the client in the same way as the server-side. But, in this case, you need to specify the server as a contact point and `grpc` for the storage and transaction_manager configuration as follows. (it is stored in [scalardb-client.properties](scalardb-client.properties)).
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
- Setting up Scalar DB schema: As mentioned in [Getting started with Scalar DB](https://github.com/scalar-labs/scalardb/blob/master/docs/getting-started-with-scalardb.md) storing and retrieving data in `Scalar DB` can be done using either the `storage` or the `transaction` mode.
    - Our storage schema is  [emoney-storage-schema.json](./src/main/resources/emoney-storage-schema.json)
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
  to register schema to our Cassandra instance we run the following command:
    ```
    java -jar tools/scalar-schema-standalone-3.1.0.jar --cassandra -h localhost -u cassandra -p cassandra -f src/main/resources/emoney-storage-schema.json
    ```
  Please note that `scalar-schema-standalone-3.1.0.jar` is the schema registration tool that can be found in [release](https://github.com/scalar-labs/scalardb/releases) of `Scalar DB`

    - In case using transaction, our schema looks like [emoney-transaction-schema.json](src/main/resources/emoney-transaction-schema.json)
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
    java -jar tools/scalar-schema-standalone-3.1.0.jar --cassandra -h localhost -u cassandra -p cassandra -f src/main/resources/emoney-transaction-schema.json
    ```

## Run the sample
- Scenario:
    - Charge the user `user1` with an amount of 10.
    - Charge the user `user2` with an amount of 20.
    - `user1` pays an amount of 5 to `user2`

- Run
    - Storage mode
     ``` 
     # charge user1 with an amount of 10
     ./gradlew run --args="cmd storage charge -a 10 -u user1"
     
     # charge user2 with an amount of 20
     ./gradlew run --args="cmd storage charge -a 20 -u user2"
     
     # pay 5 from user1 to user2
     ./gradlew run --args="cmd storage pay -a 5 -f user1 -t user2"
     ```

    - Transaction mode
     ``` 
     # charge user1 with an amount of 10
     ./gradlew run --args="cmd transaction charge -a 10 -u user1"
     
     # charge user2 with an amount of 20
     ./gradlew run --args="cmd transaction charge -a 20 -u user2"
     
     # pay 5 from user1 to user2
     ./gradlew run --args="cmd transaction pay -a 5 -f user1 -t user2"
     ```
