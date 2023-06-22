package sample.client;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import sample.client.command.GetCustomerInfoCommand;
import sample.client.command.GetOrderCommand;
import sample.client.command.GetOrdersCommand;
import sample.client.command.PlaceOrderCommand;
import sample.client.command.RepaymentCommand;

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

  public static void main(String[] args) {
    new CommandLine(new Client()).execute(args);
  }

  @Override
  public void run() {
    if (showHelp) {
      CommandLine.usage(this, System.out);
    }
  }
}
