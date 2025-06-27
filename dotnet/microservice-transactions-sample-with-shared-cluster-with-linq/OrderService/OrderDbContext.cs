using MicroserviceTransactionsSample.Common.OrderService;
using ScalarDB.Client;

namespace MicroserviceTransactionsSample.OrderService;

public class OrderDbContext : ScalarDbContext
{
#nullable disable
    public ScalarDbSet<Order> Orders { get; set; }
    public ScalarDbSet<Statement> Statements { get; set; }
    public ScalarDbSet<Item> Items { get; set; }
#nullable restore
}
