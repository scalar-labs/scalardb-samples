using System.CommandLine;
using MicroserviceTransactionsSample.Client.Commands.Binders;
using MicroserviceTransactionsSample.Rpc;
using static MicroserviceTransactionsSample.Rpc.OrderService;

namespace MicroserviceTransactionsSample.Client.Commands;

public static class GetOrdersCommand
{
    private const string Name = "GetOrders";
    private const string Description = "Get orders information by customer ID";

    private const string ArgName = "customer_id";
    private const string ArgDescription = "customer ID";

    public static Command Create()
    {
        var customerIdArg = new Argument<int>(ArgName, ArgDescription);
        var getOrdersCommand = new Command(Name, Description)
        {
            customerIdArg
        };

        getOrdersCommand.SetHandler(getOrders,
                                    customerIdArg,
                                    new OrderServiceClientBinder());
        return getOrdersCommand;
    }

    private static async Task getOrders(int customerId, OrderServiceClient client)
    {
        var request = new GetOrdersRequest { CustomerId = customerId };
        var response = await client.GetOrdersAsync(request);

        Console.WriteLine(response);
    }
}
