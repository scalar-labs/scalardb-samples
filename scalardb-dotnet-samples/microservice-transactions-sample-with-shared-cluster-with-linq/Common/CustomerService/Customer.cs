using System.ComponentModel.DataAnnotations.Schema;
using ScalarDB.Client.DataAnnotations;

namespace MicroserviceTransactionsSample.Common.CustomerService;

[Table("customer_service.customers")]
public class Customer
{
    [PartitionKey]
    [Column("customer_id", Order = 0)]
    public int Id { get; set; }

    [Column("name", Order = 1)]
    public string Name { get; set; } = "";

    [Column("credit_limit", Order = 2)]
    public int CreditLimit { get; set; }

    [Column("credit_total", Order = 3)]
    public int CreditTotal { get; set; }
}
