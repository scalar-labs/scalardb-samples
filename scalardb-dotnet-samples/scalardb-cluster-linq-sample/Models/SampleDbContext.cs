using ScalarDB.Client;

namespace ScalarDbClusterLinqSample.Models;

public class SampleDbContext : ScalarDbContext
{
#nullable disable
    public ScalarDbSet<Customer> Customers { get; set; }
    public ScalarDbSet<Order> Orders { get; set; }
    public ScalarDbSet<Statement> Statements { get; set; }
    public ScalarDbSet<Item> Items { get; set; }
#nullable restore
}
