using System.CommandLine;
using MicroserviceTransactionsSample.Client.Commands.Binders;
using MicroserviceTransactionsSample.Rpc;
using static MicroserviceTransactionsSample.Rpc.OrderService;

namespace MicroserviceTransactionsSample.Client.Commands;

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

        getOrderCommand.SetHandler(getOrder,
                                   orderIdArg,
                                   new OrderServiceClientBinder());
        return getOrderCommand;
    }

    private static async Task getOrder(string orderId, OrderServiceClient client)
    {
        var request = new GetOrderRequest { OrderId = orderId };
        var response = await client.GetOrderAsync(request);

        Console.WriteLine(response);
    }
}
