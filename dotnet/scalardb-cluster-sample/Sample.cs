using System.Diagnostics;
using System.Text.Json.Nodes;
using Microsoft.Extensions.Logging;
using ScalarDB.Client;
using ScalarDB.Client.Builders;
using ScalarDB.Client.Builders.Admin;
using ScalarDB.Client.Core;
using ScalarDB.Client.Exceptions;

namespace ScalarDbClusterSample;

public class Sample: IDisposable
{
    private readonly TransactionFactory _factory;
    private readonly IDistributedTransactionManager _manager;

    public Sample()
    {
        var loggerFactory = LoggerFactory.Create(builder =>
        {
            builder.SetMinimumLevel(LogLevel.Warning);
            builder.AddSimpleConsole(options => { options.TimestampFormat = "HH:mm:ss "; });
        });
        
        _factory = TransactionFactory.Create("scalardb-options.json", loggerFactory);
        _manager = _factory.GetTransactionManager();
    }

    public async Task CreateTables()
    {
        using var admin = _factory.GetTransactionAdmin();
        
        var customersTableMetadata
            = new TableMetadataBuilder()
              .AddPartitionKey("customer_id", DataType.Int)
              .AddColumn("name", DataType.Text)
              .AddColumn("credit_limit", DataType.Int)
              .AddColumn("credit_total", DataType.Int)
              .Build();

        var ordersTableMetadata
            = new TableMetadataBuilder()
              .AddSecondaryIndex("order_id", DataType.Text)
              .AddPartitionKey("customer_id", DataType.Int)
              .AddClusteringKey("timestamp", DataType.Bigint)
              .Build();
        
        var statementsTableMetadata
            = new TableMetadataBuilder()
                .AddPartitionKey("order_id", DataType.Text)
                .AddClusteringKey("item_id", DataType.Int)
                .AddColumn("count", DataType.Int)
                .Build();
        
        var itemsTableMetadata
            = new TableMetadataBuilder()
                .AddPartitionKey("item_id", DataType.Int)
                .AddColumn("name", DataType.Text)
                .AddColumn("price", DataType.Int)
                .Build();
        
        await admin.CreateCoordinatorTablesAsync(true);
        await admin.CreateNamespaceAsync("sample", true);
        
        await admin.CreateTableAsync("sample", "customers", customersTableMetadata, true);
        await admin.CreateTableAsync("sample", "orders", ordersTableMetadata, true);
        await admin.CreateTableAsync("sample", "statements", statementsTableMetadata, true);
        await admin.CreateTableAsync("sample", "items", itemsTableMetadata, true);
    }

    public async Task LoadInitialData()
    {
        var transaction = await _manager.BeginAsync();
        try
        {
            await LoadCustomerIfNotExists(1, "Yamada Taro", 10000, 0);
            await LoadCustomerIfNotExists(2, "Yamada Hanako", 10000, 0);
            await LoadCustomerIfNotExists(3, "Suzuki Ichiro", 10000, 0);
            await LoadItemIfNotExists(1, "Apple", 1000);
            await LoadItemIfNotExists(2, "Orange", 2000);
            await LoadItemIfNotExists(3, "Grape", 2500);
            await LoadItemIfNotExists(4, "Mango", 5000);
            await LoadItemIfNotExists(5, "Melon", 3000);
            
            await transaction.CommitAsync();
        }
        catch (TransactionException)
        {
            // If an error occurs, rollback the transaction
            await transaction.RollbackAsync();
            throw;
        }
        
        return;

        async Task LoadCustomerIfNotExists(int customerId, string name,
                                           int creditLimit, int creditTotal)
        {
            var customer =
                await transaction.GetAsync(
                    new GetBuilder()
                        .SetNamespaceName("sample")
                        .SetTableName("customers")
                        .AddPartitionKey("customer_id", customerId)
                        .Build()
                );

            if (customer is not null)
                return;

            await transaction.InsertAsync(
                new InsertBuilder()
                    .SetNamespaceName("sample")
                    .SetTableName("customers")
                    .AddPartitionKey("customer_id", customerId)
                    .AddColumn("name", name)
                    .AddColumn("credit_limit", creditLimit)
                    .AddColumn("credit_total", creditTotal)
                    .Build()
            );
        }

        async Task LoadItemIfNotExists(int itemId, string name, int price)
        {
            var item =
                await transaction.GetAsync(
                    new GetBuilder()
                        .SetNamespaceName("sample")
                        .SetTableName("items")
                        .AddPartitionKey("item_id", itemId)
                        .Build()
                );

            if (item is not null)
                return;

            await transaction.InsertAsync(
                new InsertBuilder()
                    .SetNamespaceName("sample")
                    .SetTableName("items")
                    .AddPartitionKey("item_id", itemId)
                    .AddColumn("name", name)
                    .AddColumn("price", price)
                    .Build()
            );
        }
    }

    public async Task<JsonObject> GetCustomerInfo(int customerId)
    {
        var transaction = await _manager.BeginAsync();
        try
        {
            // Retrieve the customer info for the specified customer ID from the customers table
            var customer =
                await transaction.GetAsync(
                    new GetBuilder()
                        .SetNamespaceName("sample")
                        .SetTableName("customers")
                        .AddPartitionKey("customer_id", customerId)
                        .Build()
                );

            if (customer is null)
            {
                // If the customer info the specified customer ID doesn't exist, throw an exception
                throw new Exception($"Customer not found (id: {customerId})");
            }

            // Commit the transaction (even when the transaction is read-only, we need to commit)
            await transaction.CommitAsync();

            return new JsonObject
                   {
                       { "id", customerId },
                       { "name", customer.GetValue<string>("name") },
                       { "credit_limit", customer.GetValue<int>("credit_limit") },
                       { "credit_total", customer.GetValue<int>("credit_total") }
                   };
        }
        catch (TransactionException)
        {
            // If an error occurs, rollback the transaction
            await transaction.RollbackAsync();
            throw;
        }
    }

    public async Task<JsonObject> PlaceOrder(int customerId,
                                             IReadOnlyDictionary<int, int> itemCounts)
    {
        var transaction = await _manager.BeginAsync();
        try
        {
            var orderId = Guid.NewGuid().ToString();

            // Insert the order info into the orders table
            await transaction.InsertAsync(
                new InsertBuilder()
                    .SetNamespaceName("sample")
                    .SetTableName("orders")
                    .AddPartitionKey("customer_id", customerId)
                    .AddClusteringKey("timestamp", DateTimeOffset.UtcNow.ToUnixTimeMilliseconds())
                    .AddColumn("order_id", orderId)
                    .Build()
            );

            var amount = 0;
            foreach (var (itemId, count) in itemCounts)
            {
                // Insert the order statement into the statements table
                await transaction.InsertAsync(
                    new InsertBuilder()
                        .SetNamespaceName("sample")
                        .SetTableName("statements")
                        .AddPartitionKey("order_id", orderId)
                        .AddClusteringKey("item_id", itemId)
                        .AddColumn("count", count)
                        .Build()
                );

                // Retrieve the item info from the items table
                var item =
                    await transaction.GetAsync(
                        new GetBuilder()
                            .SetNamespaceName("sample")
                            .SetTableName("items")
                            .AddPartitionKey("item_id", itemId)
                            .Build()
                    );
                if (item is null)
                    throw new Exception($"Item not found (id: {itemId})");

                // Calculate the total amount
                amount += item.GetValue<int>("price") * count;
            }

            // Check if the credit total exceeds the credit limit after payment
            var customer =
                await transaction.GetAsync(
                    new GetBuilder()
                        .SetNamespaceName("sample")
                        .SetTableName("customers")
                        .AddPartitionKey("customer_id", customerId)
                        .Build()
                );
            if (customer is null)
                throw new Exception($"Customer not found (id: {customerId})");

            var creditLimit = customer.GetValue<int>("credit_limit");
            var creditTotal = customer.GetValue<int>("credit_total");
            var newCreditTotal = creditTotal + amount;
            if (newCreditTotal > creditLimit)
                throw new Exception($"Credit limit exceeded ({newCreditTotal} > {creditLimit})");

            // Update credit_total for the customer
            await transaction.UpdateAsync(
                new UpdateBuilder()
                    .SetNamespaceName("sample")
                    .SetTableName("customers")
                    .AddPartitionKey("customer_id", customerId)
                    .AddColumn("credit_total", newCreditTotal)
                    .Build()
            );

            // Commit the transaction
            await transaction.CommitAsync();

            return new JsonObject
                   {
                       { "order_id", orderId }
                   };
        }
        catch (TransactionException)
        {
            // If an error occurs, rollback the transaction
            await transaction.RollbackAsync();
            throw;
        }
    }

    public async Task<JsonObject> GetOrderByOrderId(string orderId)
    {
        var transaction = await _manager.BeginAsync();
        try
        {
            // Get an order JSON for the specified order ID
            var orderJson = await getOrder(transaction, orderId);

            // Commit the transaction (even when the transaction is read-only, we need to commit)
            await transaction.CommitAsync();

            return new JsonObject
                   {
                       { "order", orderJson }
                   };
        }
        catch (TransactionException)
        {
            // If an error occurs, rollback the transaction
            await transaction.RollbackAsync();
            throw;
        }
    }

    public async Task<JsonObject> GetOrdersByCustomerId(int customerId)
    {
        var transaction = await _manager.BeginAsync();
        try
        {
            // Retrieve the order info for the customer ID from the orders table
            var orders =
                await transaction.ScanAsync(
                    new ScanBuilder()
                        .SetNamespaceName("sample")
                        .SetTableName("orders")
                        .AddPartitionKey("customer_id", customerId)
                        .Build()
                );

            // Make order JSONs for the orders of the customer
            var orderJsons = new JsonArray();
            foreach (var order in orders)
                orderJsons.Add(await getOrder(transaction, order.GetValue<string>("order_id")));

            // Commit the transaction (even when the transaction is read-only, we need to commit)
            await transaction.CommitAsync();

            return new JsonObject
                   {
                       { "orders", orderJsons }
                   };
        }
        catch (TransactionException)
        {
            // If an error occurs, rollback the transaction
            await transaction.RollbackAsync();
            throw;
        }
    }

    public async Task Repayment(int customerId, int amount)
    {
        var transaction = await _manager.BeginAsync();
        try
        {
            // Retrieve the customer info for the specified customer ID from the customers table
            var customer =
                await transaction.GetAsync(
                    new GetBuilder()
                        .SetNamespaceName("sample")
                        .SetTableName("customers")
                        .AddPartitionKey("customer_id", customerId)
                        .Build()
                );
            if (customer is null)
                throw new Exception($"Customer not found (id: {customerId})");

            var updatedCreditTotal = customer.GetValue<int>("credit_total") - amount;

            // Check if over repayment or not
            if (updatedCreditTotal < 0)
                throw new Exception($"Over-repayment ({updatedCreditTotal})");

            // Reduce credit_total for the customer
            await transaction.UpdateAsync(
                new UpdateBuilder()
                    .SetNamespaceName("sample")
                    .SetTableName("customers")
                    .AddPartitionKey("customer_id", customerId)
                    .AddColumn("credit_total", updatedCreditTotal)
                    .Build()
            );

            // Commit the transaction
            await transaction.CommitAsync();
        }
        catch (TransactionException)
        {
            // If an error occurs, rollback the transaction
            await transaction.RollbackAsync();
            throw;
        }
    }

    private static async Task<JsonObject> getOrder(IDistributedTransaction transaction, string orderId)
    {
        // Retrieve the order info for the order ID from the orders table
        var order =
            await transaction.GetAsync(
                new GetBuilder()
                    .SetNamespaceName("sample")
                    .SetTableName("orders")
                    .SetGetType(GetOperationType.GetWithIndex)
                    .AddPartitionKey("order_id", orderId)
                    .Build()
            );
        if (order is null)
            throw new Exception($"Order not found (id: {orderId})");

        var customerId = order.GetValue<int>("customer_id");

        // Retrieve the customer info for the specified customer ID from the customers table
        var customer =
            await transaction.GetAsync(
                new GetBuilder()
                    .SetNamespaceName("sample")
                    .SetTableName("customers")
                    .AddPartitionKey("customer_id", customerId)
                    .Build()
            );
        Debug.Assert(customer is not null);

        // Retrieve the order statements for the order ID from the statements table
        var statements =
            await transaction.ScanAsync(
                new ScanBuilder()
                    .SetNamespaceName("sample")
                    .SetTableName("statements")
                    .AddPartitionKey("order_id", orderId)
                    .Build()
            );

        // Make the statements JSONs
        var total = 0;
        var statementJsons = new JsonArray();

        foreach (var statement in statements)
        {
            var itemId = statement.GetValue<int>("item_id");

            // Retrieve the item data from the items table
            var item =
                await transaction.GetAsync(
                    new GetBuilder()
                        .SetNamespaceName("sample")
                        .SetTableName("items")
                        .AddPartitionKey("item_id", itemId)
                        .Build()
                );
            if (item is null)
                throw new Exception($"Item not found (id: {itemId})");

            var price = item.GetValue<int>("price");
            var count = statement.GetValue<int>("count");
            var totalForStatement = price * count;

            statementJsons.Add(
                new JsonObject
                {
                    { "item_id", itemId },
                    { "item_name", item.GetValue<string>("name") },
                    { "price", price },
                    { "count", count },
                    { "total", totalForStatement }
                }
            );

            total += totalForStatement;
        }
        
        return new JsonObject
               {
                   { "order_id", orderId },
                   { "timestamp", order.GetValue<long>("timestamp") },
                   { "customer_id", customerId },
                   { "customer_name", customer.GetValue<string>("name") },
                   { "statements", statementJsons },
                   { "total", total }
               };
    }

    public void Dispose()
        => _manager.Dispose();
}
