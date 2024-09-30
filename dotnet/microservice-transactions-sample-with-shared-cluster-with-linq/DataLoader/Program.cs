using System.CommandLine;
using Microsoft.Extensions.Logging;
using ScalarDB.Client;

namespace MicroserviceTransactionsSample.DataLoader;

class Program
{
    internal const int RetryCount = 10;
    internal static readonly TimeSpan RetryInternal = TimeSpan.FromSeconds(1);

    static async Task Main(string[] args)
    {
        var configOption = new Option<string>("--config", "Path to the config file") { IsRequired = true };
        var resetDataOptions = new Option<bool>("--reset-data", "Recreate tables and other data");
        var logLevelOption = new Option<LogLevel>(name: "--log-level",
                                                  description: "Minimum LogLevel",
                                                  getDefaultValue: () => LogLevel.Information);

        var rootCommand = new RootCommand("MicroserviceTransactionsSample.DataLoader")
        {
            configOption,
            resetDataOptions,
            logLevelOption
        };

        rootCommand.SetHandler(loadData, configOption, resetDataOptions, logLevelOption);
        await rootCommand.InvokeAsync(args);
    }

    private static async Task loadData(string configFilePath, bool resetData, LogLevel logLevel)
    {
        var loggerFactory = getDefaultLoggerFactory(logLevel);
        var factory = TransactionFactory.Create(configFilePath, loggerFactory);

        using var admin = factory.GetTransactionAdmin();
        await (new SchemaCreator(admin, loggerFactory)).Create(resetData);
        await (new UsersCreator(admin, loggerFactory)).Create();

        using var manager = factory.GetTransactionManager();
        await (new DataLoader(manager, loggerFactory)).Load();
    }

    private static ILoggerFactory getDefaultLoggerFactory(LogLevel logLevel)
        => LoggerFactory.Create(builder =>
        {
            builder.SetMinimumLevel(logLevel);
            builder.AddSimpleConsole(options => { options.TimestampFormat = "HH:mm:ss.fff "; });
        });
}
