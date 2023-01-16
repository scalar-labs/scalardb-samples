# ScalarDB Server Sample
This is a sample application that uses ScalarDB Server, a gRPC server that implements ScalarDB interface, as a backend.
For using the native ScalarDB library, please refer to [Getting Started](https://github.com/scalar-labs/scalardb/blob/master/docs/getting-started.md).
More information about ScalarDB Server can be found [here](https://github.com/scalar-labs/scalardb/tree/master/docs/scalardb-server.md).

## Sample application
The sample application is a simple electronic money application that has the following features:
- Charge an `amount` to a `user_id`
- Pay an `amount` from a `user_id` to another `user_id`
- Get a `balace` of a `user_id`

## Prerequisites
- Java (OpenJDK 8 or higher)
- Gradle
- Docker, Docker Compose

## Set up
### ScalarDB Server
In this sample, we will use Cassandra as storage for ScalarDB Server.
The configuration of ScalarDB Server is shown below. (It is also stored in [database.properties](./database.properties))
```properties
# Comma separated contact points
scalar.db.contact_points=cassandra

# Port number for all the contact points. Default port number for each database is used if empty.
scalar.db.contact_port=9042

# Credential information to access the database
scalar.db.username=cassandra
scalar.db.password=cassandra

# Storage implementation
scalar.db.storage=cassandra
```

To start Cassandra and ScalarDB Server, we use the following command.
Please note that we should wait around a bit more than one minute because ScalarDB container has to wait for Cassandra container to be fully started.
```shell
$ docker-compose -f docker-compose-cassandra.yml up -d
```
*For using other databases as the backend for ScalarDB Server, we can change the configuration of [database.properties](database.properties) according to [Getting Started](https://github.com/scalar-labs/scalardb/blob/master/docs/getting-started.md). After that we can start ScalarDB Server using [docker-compose.yml](docker-compose.yml) instead.*

### ScalarDB client
The sample application uses a client that implements ScalarDB interface.
Thus, you can configure the client in the same way as the server-side.
But, in this case, you need to specify the server as a contact point and `grpc` for the storage and transaction_manager configuration as follows. (it is stored in [scalardb-client.properties](scalardb-client.properties)).
```properties
# Comma separated contact points
scalar.db.contact_points=localhost

# Port number for all the contact points. Default port number for each database is used if empty.
scalar.db.contact_port=60051

# Storage implementation
scalar.db.storage=grpc

# The type of the transaction manager
scalar.db.transaction_manager=grpc
```

### Set up database schema
Now you apply the database schema of the sample application as shown below. (It is stored in [emoney.json](emoney.json)).
```json
{
  "emoney.account": {
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

You then apply the schema with the following command.
Please download the schema tool `scalardb-schema-loader-<version>.jar` that can be found in [releases](https://github.com/scalar-labs/scalardb/releases) of ScalarDB.
```shell
$ java -jar scalardb-schema-loader-<version>.jar --config scalardb-client.properties --schema-file emoney.json --coordinator
```

Note that `--coordinator` is specified to create the coordinator table needed for transactions.

## Run the sample
- Charge `1000` to `user1`:
```shell
$ ./gradlew run --args="-action charge -amount 1000 -to user1"
```

- Charge `0` to `merchant1` (Just create an account for `merchant1`):
```shell
$ ./gradlew run --args="-action charge -amount 0 -to merchant1"
```

- Pay `100` from `user1` to `merchant1`:
```shell
$ ./gradlew run --args="-action pay -amount 100 -from user1 -to merchant1"
```

- Get the balance of `user1`:
```shell
$ ./gradlew run --args="-action getBalance -id user1"
```

- Get the balance of `merchant1`:
```shell
$ ./gradlew run --args="-action getBalance -id merchant1"
```

## Storage abstraction
ScalarDB Server also supports [Storage API](https://github.com/scalar-labs/scalardb/blob/master/docs/storage-abstraction.md).
The following describes a sample of Storage API.

### Set up database schema
If you have created the tables for transactions, you can delete them with the `-D` option as follows.
```shell
$ java -jar scalardb-schema-loader-<version>.jar --config scalardb-client.properties --schema-file emoney.json --coordinator -D
```

You can create a schema by setting `transaction` to false. (The updated schema is stored in [emoney-storage.json](emoney-storage.json))
```json
{
  "emoney.account": {
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

You then apply the schema with the following command.
```shell
$ java -jar scalardb-schema-loader-<version>.jar --config scalardb-client.properties --schema-file emoney-storage.json
```

### Run the sample with Storage API
- Charge `1000` to `user1`:
```shell
$ ./gradlew -Pstorage run --args="-action charge -amount 1000 -to user1"
```

- Charge `0` to `merchant1` (Just create an account for `merchant1`):
```shell
$ ./gradlew -Pstorage run --args="-action charge -amount 0 -to merchant1"
```

- Pay `100` from `user1` to `merchant1`:
```shell
$ ./gradlew -Pstorage run --args="-action pay -amount 100 -from user1 -to merchant1"
```

- Get the balance of `user1`:
```shell
$ ./gradlew -Pstorage run --args="-action getBalance -id user1"
```

- Get the balance of `merchant1`:
```shell
$ ./gradlew -Pstorage run --args="-action getBalance -id merchant1"
```

## Clean up
To stop Cassandra and ScalarDB Server, run the following command:
```shell
$ docker-compose -f docker-compose-cassandra.yml down
```