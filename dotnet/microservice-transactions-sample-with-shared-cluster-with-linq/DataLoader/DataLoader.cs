using ScalarDB.Client;
using ScalarDB.Client.Extensions;
using MicroserviceTransactionsSample.Common.CustomerService;
using MicroserviceTransactionsSample.Common.OrderService;
using Microsoft.Extensions.Logging;
using ScalarDB.Client.Exceptions;

namespace MicroserviceTransactionsSample.DataLoader;

public class DataLoader
{
    private readonly IDistributedTransactionManager _manager;
    private readonly ILogger<DataLoader> _logger;

    public DataLoader(IDistributedTransactionManager manager,
                      ILoggerFactory loggerFactory)
    {
        _manager = manager;
        _logger = loggerFactory.CreateLogger<DataLoader>();
    }

    public async Task Load()
    {
        var attempts = Program.RetryCount;

        while (true)
        {
            IDistributedTransaction? transaction = null;
            try
            {
                // fill the data
                transaction = await _manager.BeginAsync();

                // Customers
                var customers = new[]
                                {
                                    new Customer { Id = 1, Name = "Yamada Taro", CreditLimit = 10000, CreditTotal = 0 },
                                    new Customer { Id = 2, Name = "Yamada Hanako", CreditLimit = 10000, CreditTotal = 0 },
                                    new Customer { Id = 3, Name = "Suzuki Ichiro", CreditLimit = 10000, CreditTotal = 0 }
                                };

                foreach (var customer in customers)
                {
                    var key = new Dictionary<string, object> { { nameof(Customer.Id), customer.Id } };
                    if (await transaction.GetAsync<Customer>(key) == null)
                        await transaction.InsertAsync(customer);
                }

                // Items
                var items = new[]
                            {
                                new Item { Id = 1, Name = "Apple", Price = 1000 },
                                new Item { Id = 2, Name = "Orange", Price = 2000 },
                                new Item { Id = 3, Name = "Grape", Price = 2500 },
                                new Item { Id = 4, Name = "Mango", Price = 5000 },
                                new Item { Id = 5, Name = "Melon", Price = 3000 }
                            };

                foreach (var item in items)
                {
                    var key = new Dictionary<string, object> { { nameof(Item.Id), item.Id } };
                    if (await transaction.GetAsync<Item>(key) == null)
                        await transaction.InsertAsync(item);
                }

                await transaction.CommitAsync();

                _logger.LogInformation("Initial data loaded");
                return;
            }
            catch (IllegalArgumentException) when (--attempts > 0)
            {
                // there's can be a lag until ScalarDB Cluster recognize namespaces and tables
                // created in Cassandra, so if this method is called after `SchemaCreator.Create()`
                // the first attempts can fail with 'The namespace does not exist' error

                _logger.LogWarning("Loading initial data failed. "
                                   + "Retrying after {interval}...", Program.RetryInternal);

                transaction?.RollbackAsync();

                await Task.Delay(Program.RetryInternal);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Loading initial data failed");

                transaction?.RollbackAsync();

                throw;
            }
        }
    }
}
