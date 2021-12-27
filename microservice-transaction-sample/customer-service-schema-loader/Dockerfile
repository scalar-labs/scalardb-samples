FROM ghcr.io/scalar-labs/scalardb-schema-loader:3.4.1
COPY customer-service-schema.json schema.json
ENTRYPOINT ["java", "-jar", "app.jar", "-f", "schema.json"]

