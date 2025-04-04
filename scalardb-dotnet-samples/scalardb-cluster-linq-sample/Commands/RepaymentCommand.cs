using System.CommandLine;

namespace ScalarDbClusterLinqSample.Commands;

public static class RepaymentCommand
{
    private const string Name = "Repayment";
    private const string Description = "Repayment";

    private const string ArgName1 = "customer_id";
    private const string ArgDescription1 = "customer ID";
    private const string ArgName2 = "amount";
    private const string ArgDescription2 = "amount of the money for repayment";

    public static Command Create()
    {
        var customerIdArg = new Argument<int>(ArgName1, ArgDescription1);
        var amountArg = new Argument<int>(ArgName2, ArgDescription2);
        var repaymentCommand = new Command(Name, Description)
        {
            customerIdArg,
            amountArg
        };

        repaymentCommand.SetHandler(async (customerId, amount) =>
        {
            using var sample = new Sample();
            await sample.Repayment(customerId, amount);
        }, customerIdArg, amountArg);

        return repaymentCommand;
    }
}
