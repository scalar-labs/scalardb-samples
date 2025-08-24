using MicroserviceTransactionsSample.Common.CustomerService;
using MicroserviceTransactionsSample.Common.OrderService;
using Microsoft.Extensions.Logging;
using ScalarDB.Client;
using ScalarDB.Client.Extensions;

namespace MicroserviceTransactionsSample.DataLoader;

public class SchemaCreator
{
    private readonly IDistributedTransactionAdmin _admin;
    private readonly ILogger<SchemaCreator> _logger;

    public SchemaCreator(IDistributedTransactionAdmin admin,
                         ILoggerFactory loggerFactory)
    {
        _admin = admin;
        _logger = loggerFactory.CreateLogger<SchemaCreator>();
    }

    public async Task Create(bool resetIfNeeded)
    {
        try
        {
            // create tables etc.
            await _admin.CreateCoordinatorTablesAsync(true);

            await _admin.CreateNamespaceAsync<Customer>(true);
            await _admin.CreateNamespaceAsync<Order>(true);

            if (resetIfNeeded)
            {
                await _admin.DropTableAsync<Customer>(true);
                await _admin.DropTableAsync<Order>(true);
                await _admin.DropTableAsync<Statement>(true);
                await _admin.DropTableAsync<Item>(true);
            }

            await _admin.CreateTableAsync<Customer>(true);
            await _admin.CreateTableAsync<Order>(true);
            await _admin.CreateTableAsync<Statement>(true);
            await _admin.CreateTableAsync<Item>(true);

            _logger.LogInformation("Database schema initialized");
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Initializing database schema failed");
            throw;
        }
    }
}
