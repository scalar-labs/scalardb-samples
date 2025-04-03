using System.CommandLine;

namespace ScalarDbClusterSqlSample.Commands;

public static class GetOrderCommand
{
    private const string Name = "GetOrder";
    private const string Description = "Get order information by order ID";

    private const string ArgName = "id";
    private const string ArgDescription = "order ID";

    public static Command Create()
    {
        var orderIdArg = new Argument<string>(ArgName, ArgDescription);
        var getOrderCommand = new Command(Name, Description)
        {
            orderIdArg
        };

        getOrderCommand.SetHandler(async orderId =>
        {
            using var sample = new Sample();
            var order = await sample.GetOrderByOrderId(orderId);
            
            Console.WriteLine(order);
        }, orderIdArg);
    
        return getOrderCommand;
    }
}
