using System.CommandLine;
using ScalarDbClusterSqlSample.Commands;

var rootCommand = new RootCommand("Sample application for ScalarDB Cluster .NET Client SDK")
                  {
                      LoadInitialDataCommand.Create(),
                      GetCustomerInfoCommand.Create(),
                      GetOrderCommand.Create(),
                      GetOrdersCommand.Create(),
                      PlaceOrderCommand.Create(),
                      RepaymentCommand.Create()
                  };

await rootCommand.InvokeAsync(args);
