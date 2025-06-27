using System.CommandLine;
using MicroserviceTransactionsSample.Client.Commands.Binders;
using MicroserviceTransactionsSample.Rpc;
using static MicroserviceTransactionsSample.Rpc.CustomerService;

namespace MicroserviceTransactionsSample.Client.Commands;

public static class RepaymentCommand
{
    private const string Name = "Repayment";
    private const string Description = "Make a repayment";

    private const string ArgName1 = "customer_id";
    private const string ArgDescription1 = "customer ID";
    private const string ArgName2 = "amount";
    private const string ArgDescription2 = "repayment amount";

    public static Command Create()
    {
        var customerIdArg = new Argument<int>(ArgName1, ArgDescription1);
        var amountArg = new Argument<int>(ArgName2, ArgDescription2);
        var repaymentCommand = new Command(Name, Description)
        {
            customerIdArg,
            amountArg
        };

        repaymentCommand.SetHandler(repay,
                                    customerIdArg,
                                    amountArg,
                                    new CustomerServiceClientBinder());
        return repaymentCommand;
    }

    private static async Task repay(int customerId, int amount, CustomerServiceClient client)
    {
        var request = new RepaymentRequest { CustomerId = customerId, Amount = amount };
        var response = await client.RepaymentAsync(request);

        Console.WriteLine(response);
    }
}
