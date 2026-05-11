using Microsoft.Extensions.Logging;
using ScalarDB.Client;
using ScalarDB.Client.Core.Admin;
using ScalarDB.Client.Exceptions;

namespace MicroserviceTransactionsSample.DataLoader;

public class UsersCreator
{
    private const string CustomerServiceUsername = "customer-service";
    private const string CustomerServicePassword = "customer-service";
    private const string CustomerServiceNamespace = "customer_service";
    private const string OrderServiceUsername = "order-service";
    private const string OrderServicePassword = "order-service";
    private const string OrderServiceNamespace = "order_service";

    private readonly Privilege[] _privileges = [Privilege.Read, Privilege.Create, Privilege.Write, Privilege.Delete];

    private readonly IDistributedTransactionAdmin _admin;
    private readonly ILogger<UsersCreator> _logger;

    public UsersCreator(IDistributedTransactionAdmin admin,
                        ILoggerFactory loggerFactory)
    {
        _admin = admin;
        _logger = loggerFactory.CreateLogger<UsersCreator>();
    }

    public async Task Create()
    {
        var attempts = Program.RetryCount;

        while (true)
        {
            try
            {
                if (await _admin.GetUserAsync(CustomerServiceUsername) == null)
                    await _admin.CreateUserAsync(CustomerServiceUsername, CustomerServicePassword);

                if (await _admin.GetUserAsync(OrderServiceUsername) == null)
                    await _admin.CreateUserAsync(OrderServiceUsername, OrderServicePassword);

                await _admin.GrantAsync(CustomerServiceUsername, CustomerServiceNamespace, null, _privileges);
                await _admin.GrantAsync(OrderServiceUsername, OrderServiceNamespace, null, _privileges);

                _logger.LogInformation("Privileges initialized");
                return;
            }
            catch (IllegalArgumentException) when (--attempts > 0)
            {
                // there's can be a lag until ScalarDB Cluster recognize namespaces and tables
                // created in Cassandra, so if this method is called after `SchemaCreator.Create()`
                // the first attempts can fail with 'The namespace does not exist' error

                _logger.LogWarning("Initializing privileges failed. "
                                   + "Retrying after {interval}...", Program.RetryInternal);

                await Task.Delay(Program.RetryInternal);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Initializing privileges failed");
                throw;
            }
        }
    }
}
