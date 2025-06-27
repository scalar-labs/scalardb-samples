using Microsoft.Extensions.Logging;

namespace MicroserviceTransactionsSample.Client;

public static class Logging
{
    public static ILoggerFactory GetLoggerFactory()
        => LoggerFactory.Create(builder =>
        {
            builder.SetMinimumLevel(LogLevel.Warning);
            builder.AddSimpleConsole(options => { options.TimestampFormat = "HH:mm:ss "; });
        });
}
