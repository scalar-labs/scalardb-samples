package sample.command;

import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Component
@Command(
    name = "bin/sample",
    description = "Sample application for Microservice Transaction",
    subcommands = {
      LoadInitialDataCommand.class,
      PlaceOrderCommand.class,
      GetOrderCommand.class,
      GetOrdersCommand.class,
      GetCustomerInfoCommand.class,
      RepaymentCommand.class
    })
public class SampleCommand implements Runnable {
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
}
