using System.ComponentModel.DataAnnotations.Schema;
using ScalarDB.Client.DataAnnotations;

namespace MicroserviceTransactionsSample.Common.OrderService;

[Table("order_service.items")]
public class Item
{
    public static readonly Item EmptyItem = new() { Id = -1, Name = "", Price = 0 };

    [PartitionKey]
    [Column("item_id", Order = 0)]
    public int Id { get; set; }

    [Column("name", Order = 1)]
    public string Name { get; set; } = "";

    [Column("price", Order = 2)]
    public int Price { get; set; }
}
