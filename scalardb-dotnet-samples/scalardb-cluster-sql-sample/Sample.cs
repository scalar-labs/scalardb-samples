using System.Diagnostics;
using System.Text.Json.Nodes;
using Microsoft.Extensions.Logging;
using ScalarDB.Client;
using ScalarDB.Client.Builders.Sql;
using ScalarDB.Client.Exceptions;

namespace ScalarDbClusterSqlSample;

public class Sample: IDisposable
{
    private readonly ISqlTransactionManager _manager;

    public Sample()
    {
        var loggerFactory = LoggerFactory.Create(builder =>
        {
            builder.SetMinimumLevel(LogLevel.Warning);
            builder.AddSimpleConsole(options => { options.TimestampFormat = "HH:mm:ss "; });
        });
        
        var factory = TransactionFactory.Create("scalardb-options.json", loggerFactory);
        _manager = factory.GetSqlTransactionManager();
    }

    public async Task CreateTables()
    {
        await _manager.ExecuteAsync("CREATE COORDINATOR TABLES IF NOT EXIST");
        await _manager.ExecuteAsync("CREATE NAMESPACE IF NOT EXISTS sample");

        await _manager.ExecuteAsync("CREATE TABLE IF NOT EXISTS sample.customers (" +
                                    "customer_id INT PRIMARY KEY, " +
                                    "name TEXT, " +
                                    "credit_limit INT, " +
                                    "credit_total INT" +
                                    ")");
        await _manager.ExecuteAsync("CREATE TABLE IF NOT EXISTS sample.orders (" +
                                    "customer_id INT, " +
                                    "\"timestamp\" BIGINT, " +
                                    "order_id TEXT, " +
                                    "PRIMARY KEY (customer_id, \"timestamp\")" +
                                    ")");
        await _manager.ExecuteAsync("CREATE TABLE IF NOT EXISTS sample.statements (" +
                                    "order_id TEXT, " +
                                    "item_id INT, " +
                                    "count INT, " +
                                    "PRIMARY KEY (order_id, item_id)" +
                                    ")");
        await _manager.ExecuteAsync("CREATE TABLE IF NOT EXISTS sample.items (" +
                                    "item_id INT PRIMARY KEY, " +
                                    "name TEXT, " +
                                    "price INT" +
                                    ")");
        
        await _manager.ExecuteAsync("CREATE INDEX IF NOT EXISTS ON sample.orders (order_id)");
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
            var resultSet =
                await transaction.ExecuteAsync(
                    new SqlStatementBuilder()
                        .SetSql("SELECT * FROM sample.customers WHERE customer_id = ?")
                        .AddParam(customerId)
                        .Build()
                );

            if (resultSet.Records.Count > 0)
                return;

            await transaction.ExecuteAsync(
                new SqlStatementBuilder()
                    .SetSql("INSERT INTO sample.customers (customer_id, name, credit_limit, credit_total) VALUES (?, ?, ?, ?)")
                    .AddParam(customerId)
                    .AddParam(name)
                    .AddParam(creditLimit)
                    .AddParam(creditTotal)
                    .Build()
            );
        }

        async Task LoadItemIfNotExists(int itemId, string name, int price)
        {
            var resultSet =
                await transaction.ExecuteAsync(
                    new SqlStatementBuilder()
                        .SetSql("SELECT * FROM sample.items WHERE item_id = ?")
                        .AddParam(itemId)
                        .Build()
                );
            
            if (resultSet.Records.Count > 0)
                return;

            await transaction.ExecuteAsync(
                new SqlStatementBuilder()
                    .SetSql("INSERT INTO sample.items (item_id, name, price) VALUES (?, ?, ?)")
                    .AddParam(itemId)
                    .AddParam(name)
                    .AddParam(price)
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
            var resultSet =
                await transaction.ExecuteAsync(
                    new SqlStatementBuilder()
                        .SetSql("SELECT * FROM sample.customers WHERE customer_id = ?")
                        .AddParam(customerId)
                        .Build()
                );

            var customer = resultSet.Records.FirstOrDefault();
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
            await transaction.ExecuteAsync(
                new SqlStatementBuilder()
                    .SetSql("INSERT INTO sample.orders (customer_id, order_id, \"timestamp\") VALUES (?, ?, ?)")
                    .AddParam(customerId)
                    .AddParam(orderId)
                    .AddParam(DateTimeOffset.UtcNow.ToUnixTimeMilliseconds())
                    .Build()
            );

            var amount = 0;
            foreach (var (itemId, count) in itemCounts)
            {
                // Insert the order statement into the statements table
                await transaction.ExecuteAsync(
                    new SqlStatementBuilder()
                        .SetSql("INSERT INTO sample.statements (order_id, item_id, count) VALUES (?, ?, ?)")
                        .AddParam(orderId)
                        .AddParam(itemId)
                        .AddParam(count)
                        .Build()
                );

                // Retrieve the item info from the items table
                var result =
                    await transaction.ExecuteAsync(
                        new SqlStatementBuilder()
                            .SetSql("SELECT * FROM sample.items WHERE item_id = ?")
                            .AddParam(itemId)
                            .Build()
                    );
 
                var item = result.Records.FirstOrDefault();
                if (item is null)
                    throw new Exception($"Item not found (id: {itemId})");

                // Calculate the total amount
                amount += item.GetValue<int>("price") * count;
            }

            // Check if the credit total exceeds the credit limit after payment
            var resultSet =
                await transaction.ExecuteAsync(
                    new SqlStatementBuilder()
                        .SetSql("SELECT * FROM sample.customers WHERE customer_id = ?")
                        .AddParam(customerId)
                        .Build()
                );

            var customer = resultSet.Records.FirstOrDefault();
            if (customer is null)
                throw new Exception($"Customer not found (id: {customerId})");

            var creditLimit = customer.GetValue<int>("credit_limit");
            var creditTotal = customer.GetValue<int>("credit_total");
            var newCreditTotal = creditTotal + amount;
            if (newCreditTotal > creditLimit)
                throw new Exception($"Credit limit exceeded ({newCreditTotal} > {creditLimit})");

            // Update credit_total for the customer
            await transaction.ExecuteAsync(
                new SqlStatementBuilder()
                    .SetSql("UPDATE sample.customers SET credit_total = ? WHERE customer_id = ?")
                    .AddParam(newCreditTotal)
                    .AddParam(customerId)
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
            var resultSet =
                await transaction.ExecuteAsync(
                    new SqlStatementBuilder()
                        .SetSql("SELECT * FROM sample.orders WHERE customer_id = ?")
                        .AddParam(customerId)
                        .Build()
                );

            // Make order JSONs for the orders of the customer
            var orderJsons = new JsonArray();
            foreach (var order in resultSet.Records)
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
            var resultSet =
                await transaction.ExecuteAsync(
                    new SqlStatementBuilder()
                        .SetSql("SELECT * FROM sample.customers WHERE customer_id = ?")
                        .AddParam(customerId)
                        .Build()
                );

            var customer = resultSet.Records.FirstOrDefault();
            if (customer is null)
                throw new Exception($"Customer not found (id: {customerId})");

            var updatedCreditTotal = customer.GetValue<int>("credit_total") - amount;

            // Check if over repayment or not
            if (updatedCreditTotal < 0)
                throw new Exception($"Over-repayment ({updatedCreditTotal})");

            // Reduce credit_total for the customer
            await transaction.ExecuteAsync(
                new SqlStatementBuilder()
                    .SetSql("UPDATE sample.customers SET credit_total = ? WHERE customer_id = ?")
                    .AddParam(updatedCreditTotal)
                    .AddParam(customerId)
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

    private static async Task<JsonObject> getOrder(ISqlTransaction transaction, string orderId)
    {
        // Retrieve the order info for the order ID from the orders table
        var resultSet =
            await transaction.ExecuteAsync(
                new SqlStatementBuilder()
                    .SetSql("SELECT * FROM sample.orders WHERE order_id = ?")
                    .AddParam(orderId)
                    .Build()
            );
  
        var order = resultSet.Records.FirstOrDefault();
        if (order is null)
            throw new Exception($"Order not found (id: {orderId})");

        var customerId = order.GetValue<int>("customer_id");

        // Retrieve the customer info for the specified customer ID from the customers table
        resultSet =
            await transaction.ExecuteAsync(
                new SqlStatementBuilder()
                    .SetSql("SELECT * FROM sample.customers WHERE customer_id = ?")
                    .AddParam(customerId)
                    .Build()
            );

        var customer = resultSet.Records.FirstOrDefault();
        Debug.Assert(customer is not null);

        // Retrieve the order statements for the order ID from the statements table
        resultSet =
            await transaction.ExecuteAsync(
                new SqlStatementBuilder()
                    .SetSql("SELECT * FROM sample.statements WHERE order_id = ?")
                    .AddParam(orderId)
                    .Build()
            );
        var statements = resultSet.Records;

        // Make the statements JSONs
        var total = 0;
        var statementJsons = new JsonArray();

        foreach (var statement in statements)
        {
            var itemId = statement.GetValue<int>("item_id");

            // Retrieve the item data from the items table
            resultSet =
                await transaction.ExecuteAsync(
                    new SqlStatementBuilder()
                        .SetSql("SELECT * FROM sample.items WHERE item_id = ?")
                        .AddParam(itemId)
                        .Build()
                );

            var item = resultSet.Records.FirstOrDefault();
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
