# ScalarDB GraphQL Sample

This sample application is a simple CLI electronic money application that has the following features:

- Charge an `amount` to a `user`
- Pay an `amount` from a `user` to another `user`
- Show the balance of a `user`

In this sample, we will use Cassandra as a storage for ScalarDB. The Cassandra server and the ScalarDB GraphQL server will run with Docker.

Apart from them, we will build a client Node.js application that will communicate with the GraphQL server. Please note that this client app is just one example of the clients; since GraphQL is a communication pattern, there are many tools in various programming languages for building applications.

## Prerequisites

- Docker Engine 20.10+
- Docker Compose 1.28+
- Node.js 16+

You need a Personal Access Token (PAT) to access the Docker image of ScalarDB GraphQL in GitHub Container registry since the image is private. Ask a person in charge to get your account ready. Please read [the official document](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry) for more detail.

## Set up

This section describes the steps to start the database and the GraphQL server and build the client application.

### Start Cassandra

First, let's start the Cassandra server with `docker-compose`:

```console
docker-compose up -d cassandra
```

You need to wait for the Cassandra to start up. Checking the logs with the `docker-compose logs -f` command would be helpful.

### Load schema

In this step, you will load the database schema for the sample app (`emoney-schema.json`) to the Cassandra database.
To download the schema loader tool, `scalardb-schema-loader-<VERSION>.jar`, see the [Releases](https://github.com/scalar-labs/scalardb/releases) of ScalarDB and download the version that you want to use.
You can load the schema to the database by the following command:

```shell
$ java -jar scalardb-schema-loader-<VERSION>.jar --config database.properties --schema-file emoney-schema.json --coordinator
```

### Start GraphQL server

Since the schema is ready, you can start the GraphQL server container. The `docker-compose up` command will start all services except for `schema-loader` (because the `schema-loader` has a different profile ). Since the `cassandra` service is already running, the command will start the `scalardb-graphql` service additionally:

```console
docker-compose up -d
```

The next time you start these services, you will not need to load the schema, so you can start them with this command alone.

At this point, you can open the GraphQL endpoint http://localhost:8080/graphql with your web browser. The GraphiQL IDE allows you to play with the GraphQL schema for the `account` table and browse the schema document.

### Build the client app

In this step, you will build the client app that will communicate with the GraphQL endpoint.

The `npm install` command will download and install the dependencies:

```console
npm install
```

The application source code is located in the `src` directory. The `tsc` command will compile the source files into the `dist` directory:

```console
npx tsc
```

## Run the sample app

Scenario:

- Charge the amount 10 to the account for `user1`.
- Charge the amount 20 to the account for `user2`.
- `user1` pays an amount of 5 to `user2`.
- Show the account balances for `user1` and `user2`.

The following commands will do the tasks:

```console
node dist/emoney.js charge user1 10
node dist/emoney.js charge user2 20
node dist/emoney.js pay user1 user2 5
node dist/emoney.js show user1
node dist/emoney.js show user2
```

## Application structure

In this section, we will look at the application configuration in detail.

### Files and directories

The following list is a description of the key files and directories in this application.

- `graphql/` - The GraphQL documents (queries and mutations) for the app functions are located here. The GraphQL Code Generator (`graphql-codegen`) reads them to generate the client TypeScript code. Read the next section about the generator.
- `src/` - All TypeScript code are located here.
  - `src/generated/graphql.ts` - The generated file with `graphql-codegen` based on the GraphQL endpoint and the local documents in the `graphql/` directory.
  - `emoney.ts` - The main file of the client app
- `codegen.yml` - The configuration file for `graphql-codegen`

### GraphQL Code Generator

The client code for this app was generated with [GraphQL Code Generator](https://www.graphql-code-generator.com/).

It is configured with the `codegen.yml` file. This file enables three plugins to generate `src/generated/graphql.ts`.

- `typescript` - An essential plugin to generate the TypeScript types based on the GraphQL schema.
- `typescript-operations` - Generates TypeScript types based on the schema and the documents in the `graphql` directory.
- `typescript-graphql-request `- Generates the client code that is called "SDK". With the functions provided by the SDK, we can call GraphQL operations defined in the document easily.

Note that the `rawRequest: true` configuration should be set for the `typescript-graphql-request ` plugin as in `codegen.yml`. This is because we need to access the `extensions` key in the GraphQL response where the the transaction ID is returned from the server.

To generate the code again, you can run:

````console
npm run generate
````

## Clean up

To stop the services and remove the volume for the Cassandra data, run the following commands:

```console
docker-compose down
```
