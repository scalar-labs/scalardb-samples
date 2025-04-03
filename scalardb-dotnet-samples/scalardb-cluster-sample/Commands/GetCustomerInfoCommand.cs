using System.CommandLine;

namespace ScalarDbClusterSample.Commands;

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

        getCustomerInfoCommand.SetHandler(async customerId =>
        {
            using var sample = new Sample();
            var customerInfo = await sample.GetCustomerInfo(customerId);
            
            Console.WriteLine(customerInfo);
        }, customerIdArg);
        
        return getCustomerInfoCommand;
    }
}
