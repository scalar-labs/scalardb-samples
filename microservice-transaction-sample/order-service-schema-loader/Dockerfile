FROM ghcr.io/scalar-labs/scalardb-schema-loader:3.4.1
COPY order-service-schema.json schema.json
ENTRYPOINT ["java", "-jar", "app.jar", "-f", "schema.json"]
