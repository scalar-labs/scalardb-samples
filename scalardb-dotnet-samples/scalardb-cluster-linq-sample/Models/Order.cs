using System.ComponentModel.DataAnnotations.Schema;
using ScalarDB.Client.DataAnnotations;

namespace ScalarDbClusterLinqSample.Models;

[Table("sample.orders")]
public class Order
{
    [SecondaryIndex]
    [Column("order_id", Order = 0)]
    public string Id { get; set; } = "";
    
    [PartitionKey]
    [Column("customer_id", Order = 1)]
    public int CustomerId { get; set; }

    [ClusteringKey]
    [Column("timestamp", Order = 2)]
    public long Timestamp { get; set; }
}
