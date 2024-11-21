using System.ComponentModel.DataAnnotations.Schema;
using ScalarDB.Client.DataAnnotations;

namespace MicroserviceTransactionsSample.Common.OrderService;

[Table("order_service.orders")]
public class Order
{
    [PartitionKey]
    [Column("customer_id", Order = 0)]
    public int CustomerId { get; set; }

    [ClusteringKey]
    [Column("timestamp", Order = 1)]
    public long Timestamp { get; set; }

    [SecondaryIndex]
    [Column("order_id", Order = 2)]
    public string Id { get; set; } = "";
}
