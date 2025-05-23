using System.CommandLine;
using System.Diagnostics;
using ScalarDB.Client.Exceptions;

namespace ScalarDbClusterSample.Commands;

public static class LoadInitialDataCommand
{
    private const string Name = "LoadInitialData";
    private const string Description = "Load initial data";

    public static Command Create()
    {
        var loadInitialDataCommand = new Command(Name, Description);
        loadInitialDataCommand.SetHandler(async () =>
        {
            using var sample = new Sample();
            await sample.CreateTables();

            IllegalArgumentException? lastException = null;
            var attempts = 10;
            while (attempts-- > 0)
            {
                try
                {
                    await sample.LoadInitialData();
                    return;
                }
                catch (IllegalArgumentException ex)
                {
                    // there's can be a lag until ScalarDB Cluster recognize namespaces and tables created
                    // in some databases like Cassandra, so if this command was called for the first time
                    // the first attempts can fail with 'The namespace does not exist' error
                    lastException = ex;
                    await Task.Delay(TimeSpan.FromSeconds(1));
                }
            }

            Debug.Assert(lastException != null);
            throw lastException;
        });

        return loadInitialDataCommand;
    }
}
