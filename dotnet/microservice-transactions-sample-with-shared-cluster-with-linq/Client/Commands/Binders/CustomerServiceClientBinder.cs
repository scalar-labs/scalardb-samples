using System.CommandLine.Binding;
using Grpc.Net.Client;
using static MicroserviceTransactionsSample.Rpc.CustomerService;

namespace MicroserviceTransactionsSample.Client.Commands.Binders;

public class CustomerServiceClientBinder : BinderBase<CustomerServiceClient>
{
    private const string CustomerServiceUrl = "http://localhost:10010";

    protected override CustomerServiceClient GetBoundValue(BindingContext bindingContext)
    {
        var customerServiceUrl = Environment.GetEnvironmentVariable("CUSTOMER_SERVICE_URL");
        if (String.IsNullOrEmpty(customerServiceUrl))
            customerServiceUrl = CustomerServiceUrl;

        var grpcOptions = new GrpcChannelOptions { LoggerFactory = Logging.GetLoggerFactory() };
        var grpcChannel = GrpcChannel.ForAddress(customerServiceUrl, grpcOptions);

        return new CustomerServiceClient(grpcChannel);
    }
}
