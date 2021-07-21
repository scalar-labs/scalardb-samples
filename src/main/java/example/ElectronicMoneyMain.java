package example;

import example.command.ElectronicMoneyCommand;
import picocli.CommandLine;

@CommandLine.Command(name = "em", description = "Electronic Money CLI App example using ScalarDB", version = "1.0",
  subcommands = {
      ElectronicMoneyCommand.class
  })
public class ElectronicMoneyMain implements Runnable{
  public static void main(String... args) {
    new CommandLine(new ElectronicMoneyMain()).execute(args);
  }

  @Override
  public void run() {}
}
