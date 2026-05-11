using ScalarDB.Client.Extensions;
using static MicroserviceTransactionsSample.Rpc.CustomerService;

namespace MicroserviceTransactionsSample.OrderService;

public class Program
{
    public static async Task Main(string[] args)
    {
        var customerServiceUrl = Environment.GetEnvironmentVariable("CUSTOMER_SERVICE_URL")!;

        var builder = WebApplication.CreateBuilder(args);

        builder.Services.AddLogging(o =>
        {
            o.AddSimpleConsole(options =>
            {
                options.TimestampFormat = "HH:mm:ss ";
            });
        });

        // Add services to the container.
        builder.Services.AddGrpc();
        builder.Services.AddGrpcClient<CustomerServiceClient>(o =>
        {
            o.Address = new Uri(customerServiceUrl);
        });

        // Add OrderDbContext to the container.
        builder.Services.AddScalarDbContext<OrderDbContext>();

        var app = builder.Build();

        // Configure the HTTP request pipeline.
        app.MapGrpcService<OrderService>();
        app.MapGet("/", () => "Communication with gRPC endpoints must be made through a gRPC client. To learn how to create a client, visit: https://go.microsoft.com/fwlink/?linkid=2086909");

        await app.RunAsync();
    }
}
