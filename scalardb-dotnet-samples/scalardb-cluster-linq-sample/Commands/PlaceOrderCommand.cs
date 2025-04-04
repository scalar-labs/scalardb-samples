using System.CommandLine;

namespace ScalarDbClusterLinqSample.Commands;

public static class PlaceOrderCommand
{
    private const string Name = "PlaceOrder";
    private const string Description = "Place an order";

    private const string ArgName1 = "customer_id";
    private const string ArgDescription1 = "customer ID";
    private const string ArgName2 = "orders";
    private const string ArgDescription2 = "orders. The format is \"<Item ID>:<Count>,<Item ID>:<Count>,...\"";

    public static Command Create()
    {
        var customerIdArg = new Argument<int>(ArgName1, ArgDescription1);
        var ordersArg = new Argument<Dictionary<int, int>>(
            name: ArgName2,
            parse: arg =>
            {
                var argStr = arg.Tokens.First().Value;
                var orders = argStr
                             .Split(',')
                             .Select(s => s.Split(':'))
                             .ToDictionary(
                                 s => Int32.Parse(s[0]),
                                 s => Int32.Parse(s[1])
                             );

                return orders;
            },
            description: ArgDescription2)
        {
            Arity = ArgumentArity.ExactlyOne
        };

        var placeOrderCommand = new Command(Name, Description)
        {
            customerIdArg,
            ordersArg
        };

        placeOrderCommand.SetHandler(async (customerId, orders) =>
        {
            using var sample = new Sample();
            var order = await sample.PlaceOrder(customerId, orders);
            
            Console.WriteLine(order);
        }, customerIdArg, ordersArg);

        return placeOrderCommand;
    }
}
