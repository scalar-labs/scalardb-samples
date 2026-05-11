using ScalarDB.Client.Extensions;

namespace MicroserviceTransactionsSample.CustomerService;

public class Program
{
    public static async Task Main(string[] args)
    {
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

        // Add CustomerDbContext to the container.
        builder.Services.AddScalarDbContext<CustomerDbContext>();

        var app = builder.Build();

        // Configure the HTTP request pipeline.
        app.MapGrpcService<CustomerService>();
        app.MapGet("/", () => "Communication with gRPC endpoints must be made through a gRPC client. To learn how to create a client, visit: https://go.microsoft.com/fwlink/?linkid=2086909");

        await app.RunAsync();
    }
}
