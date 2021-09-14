package example.client;

import example.client.command.GetCustomerInfoCommand;
import example.client.command.GetOrderCommand;
import example.client.command.GetOrdersCommand;
import example.client.command.PlaceOrderCommand;
import example.client.command.RepaymentCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;

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

  @Override
  public void run() {}

  public static void main(String[] args) {
    new CommandLine(new Client()).execute(args);
  }
}
