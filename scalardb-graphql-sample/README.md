# Create a Sample Application That Uses ScalarDB GraphQL

{% capture notice--info %}
**Note**

ScalarDB GraphQL Server is now deprecated, and consequently, this sample code is also deprecated. To use the ScalarDB GraphQL interface, you need to use ScalarDB Cluster, which is available only in the Enterprise edition. For more information, see [ScalarDB Cluster](https://scalardb.scalar-labs.com/docs/latest/scalardb-cluster/).
{% endcapture %}

<div class="notice--info">{{ notice--info | markdownify }}</div>

This tutorial describes how to create a sample electronic money application that uses the ScalarDB GraphQL interface.

## Overview

In the sample, you will use Cassandra as the database, and the Cassandra server and the ScalarDB GraphQL Server will run in Docker. Then, you will build a Node.js client application that will communicate with the ScalarDB GraphQL Server.

{% capture notice--info %}
**Note**

The sample client application is just one example of what you can create. Since GraphQL is a communication pattern, many tools in various programming languages exist for building applications.
{% endcapture %}

<div class="notice--info">{{ notice--info | markdownify }}</div>

### What you can do in this sample application

The sample application supports the following types of transactions:

- Credit an amount to a user.
- Send an amount from one user to another user.
- Show a user's balance.

## Prerequisites

- [Docker](https://www.docker.com/get-started/) 20.10 or later with [Docker Compose](https://docs.docker.com/compose/install/) V2 or later
- [Node.js](https://nodejs.org/en/download/current/) 16+

In addition, you need access to the Docker image for ScalarDB GraphQL in the GitHub Container registry, which is private. The registry is available only those who are using ScalarDB Enterprise. If you need a license for ScalarDB Enterprise, please [contact us](https://scalar-labs.com/contact_us/).

After confirming that you have access to the ScalarDB GraphQL repository and its packages in the GitHub Container registry, you will need to set your GitHub username and your personal access token. To specify these properties as environment variables, run the following commands, replacing `<YOUR_GITHUB_USERNAME>` with your GitHub username and `<YOUR_PERSONAL_ACCESS_TOKEN>` with your personal access token:

```console
$ export USERNAME=<YOUR_GITHUB_USERNAME>
$ export TOKEN=<YOUR_PERSONAL_ACCESS_TOKEN>
```

## Set up ScalarDB GraphQL

The following sections describe how to set up the sample electronic money application.

### Clone the ScalarDB samples repository

Open **Terminal**, then clone the ScalarDB samples repository by running the following command:

```console
$ git clone https://github.com/scalar-labs/scalardb-samples
```

Then, go to the directory that contains the sample application by running the following command:

```console
$ cd scalardb-samples/scalardb-graphql-sample
```

### Start Cassandra

To start the Cassandra server, which is included in the Docker container for the sample application, make sure Docker is running and then run the following command:

```console
$ docker-compose up -d cassandra
```

{% capture notice--info %}
**Note**

Starting the Docker container may take more than one minute depending on your development environment.

To check the logs, run the following command:

```console
$ docker-compose logs -f
```
{% endcapture %}

<div class="notice--info">{{ notice--info | markdownify }}</div>

### Load the schema

The database schema (the method in which the data will be organized) for the sample application has already been defined in [`emoney-schema.json`](emoney-schema.json).

To apply the schema, go to the [ScalarDB Releases](https://github.com/scalar-labs/scalardb/releases) page and download the ScalarDB Schema Loader that matches the version of ScalarDB that you want to use to the `scalardb-samples/scalardb-graphql-sample` folder.

Then, run the following command, replacing `<VERSION>` with the version of the ScalarDB Schema Loader that you downloaded:

```console
$ java -jar scalardb-schema-loader-<VERSION>.jar --config database.properties --schema-file emoney-schema.json --coordinator
```

### Start the ScalarDB GraphQL Server

Before starting the ScalarDB GraphQL Server, log in to the GitHub Container registry by running the following command and entering your GitHub credentials as instructed:

```console
$ docker login ghcr.io
```

To start the ScalarDB GraphQL Server, which is included in the Docker container for the sample application, make sure Docker is running and then run the following command:

```console
$ docker-compose up -d
```

{% capture notice--info %}
**Note**

- Starting the Docker container may take more than one minute depending on your development environment.
- Running this command will start all services except for `schema-loader`, which has a different profile. In addition, since the `cassandra` service is already running, the command will start only the `scalardb-graphql` service.
- The next time you start these services, you will not need to load the schema. Instead, you can start the service by running only the command above.
{% endcapture %}

<div class="notice--info">{{ notice--info | markdownify }}</div>

After the Docker container has started, open the GraphQL endpoint, http://localhost:8080/graphql, in your web browser. The GraphiQL IDE lets you use and browse the GraphQL schema for the `account` table.

## Build the client application

With the ScalarDB GraphQL Server running, you can now build the client application that will communicate with the GraphQL endpoint.

To download and install the dependencies for the client application, run the following command:

```console
$ npm install
```

The application source code is located in the `src` directory. To compile the source files, run the following command:

```console
$ npx tsc
```

After running this command, the source files should be in a new directory titled `dist` in the `src` directory.

## Execute transactions and retrieve data in the sample application

The following sections describe how to execute transactions and retrieve data in the sample electronic money application.

### Create accounts with a balance

You need an account with a balance so that you can send funds between accounts.

To create an account for `user1` that has a balance of `10`, run the following command:

```console
$ node dist/emoney.js charge user1 10
```

To create an account for `user2` that has a balance of `20`, run the following command:

```console
$ node dist/emoney.js charge user2 20
```

### Send electronic money between two accounts

Now that you have created two accounts, you can send funds from one account to the other account.

To have `user1` pay `5` to `user2`, run the following command:

```console
$ node dist/emoney.js pay user1 user2 5
```

### Get an account balance

After sending funds from one account to the other, you can check the balance of each account.

To get the balance of `user1`, run the following command:

```console
$ node dist/emoney.js show user1
```

To get the balance of `user2`, run the following command:

```console
$ node dist/emoney.js show user2
```

### Stop the sample application

To stop the sample application, stop the Docker container by running the following command:

```console
$ docker-compose down
```

## Reference - Application structure

This section describes how this client application is configured in detail.

### Files and directories

The following list is a description of the key files and directories in this application:

- `graphql`. The GraphQL schemas (queries and mutations) for the application functions are located in this directory. The GraphQL Code Generator (`graphql-codegen`) reads the schema to generate the client TypeScript code. For more details, see [GraphQL Code Generator](#graphql-code-generator).
- `src`. All TypeScript code is located in this directory.
  - `src/generated/graphql.ts`. This is generated by using `graphql-codegen` and is based on the GraphQL endpoint and the local schemas in the `graphql` directory.
  - `src/emoney.ts`. This is the main file of the client application.
- `codegen.yml`. This is the configuration file for `graphql-codegen`.

### GraphQL Code Generator

The code for this client application was generated by using [GraphQL Code Generator](https://www.graphql-code-generator.com/), and the application was configured by using the `codegen.yml` file, which enables the following three modules in the `node_modules` directory to generate `src/generated/graphql.ts`:

- `typescript`. As an essential module, generates the TypeScript types based on the GraphQL schema.
- `typescript-operations`. Generates TypeScript types based on the schemas in the `graphql` directory.
- `typescript-graphql-request`. Generates the client code as an SDK. With the functions that the SDK provides, you can call the GraphQL operations that are defined in the schema.

{% capture notice--info %}
**Note**

- The `rawRequest: true` configuration specified in `codegen.yml` should match the same configuration in the `typescript-graphql-request` module. This configuration is necessary because you need to access the `extensions` key in the GraphQL response when the server returns the transaction ID.
- To re-generate the code after changing a configuration, run the following command when the sample application is running in Docker:

```console
$ npm run generate
```
{% endcapture %}

<div class="notice--info">{{ notice--info | markdownify }}</div>
