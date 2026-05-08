using System.ComponentModel.DataAnnotations.Schema;
using ScalarDB.Client.DataAnnotations;

namespace MicroserviceTransactionsSample.Common.OrderService;

[Table("order_service.statements")]
public class Statement
{
    [PartitionKey]
    [Column("order_id", Order = 0)]
    public string OrderId { get; set; } = "";

    [ClusteringKey]
    [Column("item_id", Order = 1)]
    public int ItemId { get; set; }

    [Column("count", Order = 2)]
    public int Count { get; set; }
}
