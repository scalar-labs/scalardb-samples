using System.CommandLine.Binding;
using Grpc.Net.Client;
using static MicroserviceTransactionsSample.Rpc.OrderService;

namespace MicroserviceTransactionsSample.Client.Commands.Binders;

public class OrderServiceClientBinder : BinderBase<OrderServiceClient>
{
    private const string OrderServiceUrl = "http://localhost:10020";

    protected override OrderServiceClient GetBoundValue(BindingContext bindingContext)
    {
        var orderServiceUrl = Environment.GetEnvironmentVariable("ORDER_SERVICE_URL");
        if (String.IsNullOrEmpty(orderServiceUrl))
            orderServiceUrl = OrderServiceUrl;

        var grpcOptions = new GrpcChannelOptions { LoggerFactory = Logging.GetLoggerFactory() };
        var grpcChannel = GrpcChannel.ForAddress(orderServiceUrl, grpcOptions);

        return new OrderServiceClient(grpcChannel);
    }
}
