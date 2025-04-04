using System.ComponentModel.DataAnnotations.Schema;
using ScalarDB.Client.DataAnnotations;

namespace ScalarDbClusterLinqSample.Models;

[Table("sample.items")]
public class Item
{
    [PartitionKey]
    [Column("item_id", Order = 0)]
    public int Id { get; set; }

    [Column("name", Order = 1)]
    public string Name { get; set; } = "";

    [Column("price", Order = 2)]
    public int Price { get; set; }
}
