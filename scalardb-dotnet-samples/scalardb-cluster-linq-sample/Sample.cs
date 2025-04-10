using System.Diagnostics;
using System.Text.Json.Nodes;
using Microsoft.Extensions.Logging;
using ScalarDB.Client;
using ScalarDB.Client.Exceptions;
using ScalarDB.Client.Extensions;
using ScalarDbClusterLinqSample.Models;

namespace ScalarDbClusterLinqSample;

public class Sample: IDisposable
{
    private const string ConfigFileName = "scalardb-options.json";
    
    private readonly SampleDbContext _db =
        ScalarDbContext.Create<SampleDbContext>(ConfigFileName, getLoggerFactory());

    public static async Task CreateTables()
    {
        using var admin = TransactionFactory.Create(ConfigFileName, getLoggerFactory())
                                            .GetTransactionAdmin();
        
        await admin.CreateCoordinatorTablesAsync(true);
        await admin.CreateNamespaceAsync<Customer>(true);
        
        await admin.CreateTableAsync<Customer>(true);
        await admin.CreateTableAsync<Order>(true);
        await admin.CreateTableAsync<Statement>(true);
        await admin.CreateTableAsync<Item>(true);
    }

    public async Task LoadInitialData()
    {
        Customer[] customers =
        [
            new() { Id = 1, Name = "Yamada Taro", CreditLimit = 10000, CreditTotal = 0 },
            new() { Id = 2, Name = "Yamada Hanako", CreditLimit = 10000, CreditTotal = 0 },
            new() { Id = 3, Name = "Suzuki Ichiro", CreditLimit = 10000, CreditTotal = 0 }
        ];

        Item[] items =
        [
            new() { Id = 1, Name = "Apple", Price = 1000 },
            new() { Id = 2, Name = "Orange", Price = 2000 },
            new() { Id = 3, Name = "Grape", Price = 2500 },
            new() { Id = 4, Name = "Mango", Price = 5000 },
            new() { Id = 5, Name = "Melon", Price = 3000 }
        ];

        await _db.BeginTransactionAsync();
        try
        {
            foreach (var customer in customers)
            {
                if (_db.Customers.FirstOrDefault(c => c.Id == customer.Id) is not null)
                    continue;

                await _db.Customers.AddAsync(customer);
            }

            foreach (var item in items)
            {
                if (_db.Items.FirstOrDefault(i => i.Id == item.Id) is not null)
                    continue;

                await _db.Items.AddAsync(item);
            }

            await _db.CommitTransactionAsync();
        }
        catch (TransactionException)
        {
            // If an error occurs, rollback the transaction
            await _db.RollbackTransactionAsync();
            throw;
        }
    }

    public JsonObject GetCustomerInfo(int customerId)
    {
        // Retrieve the customer info for the specified customer ID from the customers table
        var customer = _db.Customers.FirstOrDefault(c => c.Id == customerId);
        if (customer is null)
        {
            // If the customer info the specified customer ID doesn't exist, throw an exception
            throw new Exception($"Customer not found (id: {customerId})");
        }

        return new JsonObject
               {
                   { "id", customer.Id },
                   { "name", customer.Name },
                   { "credit_limit", customer.CreditLimit },
                   { "credit_total", customer.CreditTotal }
               };
    }

    public async Task<JsonObject> PlaceOrder(int customerId,
                                             IReadOnlyDictionary<int, int> itemCounts)
    {
        await _db.BeginTransactionAsync();
        try
        {
            var orderId = Guid.NewGuid().ToString();

            // Insert the order info into the orders table
            await _db.Orders.AddAsync(
                new()
                {
                    Id = orderId,
                    CustomerId = customerId,
                    Timestamp = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds()
                }
            );

            var amount = 0;
            foreach (var (itemId, count) in itemCounts)
            {
                // Insert the order statement into the statements table
                await _db.Statements.AddAsync(
                    new()
                    {
                        OrderId = orderId,
                        ItemId = itemId,
                        Count = count
                    }
                );
                
                // Retrieve the item info from the items table
                var item = _db.Items.FirstOrDefault(i => i.Id == itemId);
                if (item is null)
                    throw new Exception($"Item not found (id: {itemId})");

                // Calculate the total amount
                amount += item.Price * count;
            }

            // Check if the credit total exceeds the credit limit after payment
            var customer = _db.Customers.FirstOrDefault(c => c.Id == customerId);
            if (customer is null)
                throw new Exception($"Customer not found (id: {customerId})");
            
            customer.CreditTotal += amount;
            if (customer.CreditTotal > customer.CreditLimit)
                throw new Exception($"Credit limit exceeded ({customer.CreditTotal} > {customer.CreditLimit})");

            // Update credit_total for the customer
            await _db.UpdateAsync(customer);

            // Commit the transaction
            await _db.CommitTransactionAsync();

            return new JsonObject
                   {
                       { "order_id", orderId }
                   };
        }
        catch (TransactionException)
        {
            // If an error occurs, rollback the transaction
            await _db.RollbackTransactionAsync();
            throw;
        }
    }

    public async Task<JsonObject> GetOrderByOrderId(string orderId)
    {
        await _db.BeginTransactionAsync();
        try
        {
            // Get an order JSON for the specified order ID
            var orderJson = getOrder(orderId);

            // Commit the transaction (even when the transaction is read-only, we need to commit)
            await _db.CommitTransactionAsync();

            return new JsonObject
                   {
                       { "order", orderJson }
                   };
        }
        catch (TransactionException)
        {
            // If an error occurs, rollback the transaction
            await _db.RollbackTransactionAsync();
            throw;
        }
    }

    public async Task<JsonObject> GetOrdersByCustomerId(int customerId)
    {
        await _db.BeginTransactionAsync();
        try
        {
            // Retrieve the order info for the customer ID from the orders table
            var orderIds = from order in _db.Orders
                           where order.CustomerId == customerId
                           select order.Id;

            // Make order JSONs for the orders of the customer
            var orderJsons = new JsonArray();
            foreach (var orderId in orderIds)
                orderJsons.Add(getOrder(orderId));
            
            await _db.CommitTransactionAsync();

            return new JsonObject
                   {
                       { "orders", orderJsons }
                   };
        }
        catch (TransactionException)
        {
            // If an error occurs, rollback the transaction
            await _db.RollbackTransactionAsync();
            throw;
        }
    }

    public async Task Repayment(int customerId, int amount)
    {
        await _db.BeginTransactionAsync();
        try
        {
            // Retrieve the customer info for the specified customer ID from the customers table
            var customer = _db.Customers.FirstOrDefault(c => c.Id == customerId);
            if (customer is null)
                throw new Exception($"Customer not found (id: {customerId})");
            
            customer.CreditTotal -= amount;

            // Check if over repayment or not
            if (customer.CreditTotal < 0)
                throw new Exception($"Over-repayment ({customer.CreditTotal})");

            // Reduce credit_total for the customer
            await _db.UpdateAsync(customer);

            // Commit the transaction
            await _db.CommitTransactionAsync();
        }
        catch (TransactionException)
        {
            // If an error occurs, rollback the transaction
            await _db.RollbackTransactionAsync();
            throw;
        }
    }

    private JsonObject getOrder(string orderId)
    {
        // Retrieve the order info for the order ID from the orders table
        var order = _db.Orders.FirstOrDefault(o => o.Id == orderId);
        if (order is null)
            throw new Exception($"Order not found (id: {orderId})");

        // Retrieve the customer info for the specified customer ID from the customers table
        var customer = _db.Customers.FirstOrDefault(c => c.Id == order.CustomerId);
        Debug.Assert(customer is not null);

        // Retrieve the order statements for the order ID
        var statements = from statement in _db.Statements
                         join item in _db.Items on statement.ItemId equals item.Id
                         where statement.OrderId == orderId
                         select new
                                {
                                    ItemId = item.Id,
                                    ItemName = item.Name,
                                    Price = item.Price,
                                    Count = statement.Count,
                                    Total = item.Price * statement.Count
                                };

        // Make the statements JSONs
        var total = 0;
        var statementJsons = new JsonArray();

        foreach (var statement in statements)
        {
            statementJsons.Add(
                new JsonObject
                {
                    { "item_id", statement.ItemId },
                    { "item_name", statement.ItemName },
                    { "price", statement.Price },
                    { "count", statement.Count },
                    { "total", statement.Total }
                }
            );

            total += statement.Total;
        }
        
        return new JsonObject
               {
                   { "order_id", order.Id },
                   { "timestamp", order.Timestamp },
                   { "customer_id", customer.Id },
                   { "customer_name", customer.Name },
                   { "statements", statementJsons },
                   { "total", total }
               };
    }

    private static ILoggerFactory getLoggerFactory()
        => LoggerFactory.Create(builder =>
        {
            builder.SetMinimumLevel(LogLevel.Warning);
            builder.AddSimpleConsole(options => { options.TimestampFormat = "HH:mm:ss "; });
        });

    public void Dispose()
        => _db.Dispose();
}
