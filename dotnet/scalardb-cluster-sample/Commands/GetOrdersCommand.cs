using System.CommandLine;

namespace ScalarDbClusterSample.Commands;

public static class GetOrdersCommand
{
    private const string Name = "GetOrders";
    private const string Description = "Get information about orders by customer ID";

    private const string ArgName = "customer_id";
    private const string ArgDescription = "customer ID";

    public static Command Create()
    {
        var customerIdArg = new Argument<int>(ArgName, ArgDescription);
        var getOrdersCommand = new Command(Name, Description)
        {
            customerIdArg
        };

        getOrdersCommand.SetHandler(async customerId =>
        {
            using var sample = new Sample();
            var orders = await sample.GetOrdersByCustomerId(customerId);
            
            Console.WriteLine(orders);
        }, customerIdArg);
        
        return getOrdersCommand;
    }
}
