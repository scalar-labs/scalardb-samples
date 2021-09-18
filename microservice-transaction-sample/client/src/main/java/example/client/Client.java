package example.client;

import example.client.command.GetCustomerInfoCommand;
import example.client.command.GetOrderCommand;
import example.client.command.GetOrdersCommand;
import example.client.command.PlaceOrderCommand;
import example.client.command.RepaymentCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
    name = "bin/client",
    description = "Sample application for Microservice Transaction",
    subcommands = {
      PlaceOrderCommand.class,
      GetOrderCommand.class,
      GetOrdersCommand.class,
      GetCustomerInfoCommand.class,
      RepaymentCommand.class
    })
public class Client implements Runnable {

  @Option(
      names = {"-h", "--help"},
      usageHelp = true,
      description = "Displays this help message and quits.",
      defaultValue = "true")
  private Boolean showHelp;

  @Override
  public void run() {
    if (showHelp) {
      CommandLine.usage(this, System.out);
    }
  }

  public static void main(String[] args) {
    new CommandLine(new Client()).execute(args);
  }
}
