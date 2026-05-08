using MicroserviceTransactionsSample.Common.CustomerService;
using ScalarDB.Client;

namespace MicroserviceTransactionsSample.CustomerService;

public class CustomerDbContext : ScalarDbContext
{
#nullable disable
    public ScalarDbSet<Customer> Customers { get; set; }
#nullable restore
}
