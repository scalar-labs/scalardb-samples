using System.CommandLine;
using MicroserviceTransactionsSample.Client.Commands;

namespace MicroserviceTransactionsSample.Client;

class Program
{
    static async Task Main(string[] args)
    {
        var rootCommand = new RootCommand("MicroserviceTransactionsSample.Client")
        {
            GetCustomerInfoCommand.Create(),
            GetOrderCommand.Create(),
            GetOrdersCommand.Create(),
            PlaceOrderCommand.Create(),
            RepaymentCommand.Create()
        };

        await rootCommand.InvokeAsync(args);
    }
}
