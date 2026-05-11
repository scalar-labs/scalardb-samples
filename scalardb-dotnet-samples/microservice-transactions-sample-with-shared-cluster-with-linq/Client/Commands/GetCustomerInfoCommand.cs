using System.CommandLine;
using MicroserviceTransactionsSample.Client.Commands.Binders;
using MicroserviceTransactionsSample.Rpc;
using static MicroserviceTransactionsSample.Rpc.CustomerService;

namespace MicroserviceTransactionsSample.Client.Commands;

public static class GetCustomerInfoCommand
{
    private const string Name = "GetCustomerInfo";
    private const string Description = "Get customer information";

    private const string ArgName = "id";
    private const string ArgDescription = "customer ID";

    public static Command Create()
    {
        var customerIdArg = new Argument<int>(ArgName, ArgDescription);
        var getCustomerInfoCommand = new Command(Name, Description)
        {
            customerIdArg
        };

        getCustomerInfoCommand.SetHandler(getCustomerInfo,
                                          customerIdArg,
                                          new CustomerServiceClientBinder());
        return getCustomerInfoCommand;
    }

    private static async Task getCustomerInfo(int customerId, CustomerServiceClient client)
    {
        var request = new GetCustomerInfoRequest { CustomerId = customerId };
        var response = await client.GetCustomerInfoAsync(request);

        Console.WriteLine(response);
    }
}
