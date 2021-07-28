package example.command;

import example.service.ElectronicMoney;
import example.service.ElectronicMoneyWithStorage;
import example.service.ElectronicMoneyWithTransaction;
import picocli.CommandLine;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "cmd", description = "Commands for Electronic Money CLI App")
public class ElectronicMoneyCommand implements Callable<Integer> {
  private static final String STORAGE_MODE = "storage";
  private static final String TRANSACTION_MODE = "transaction";
  private static final String CHARGE_ACTION = "charge";
  private static final String PAY_ACTION = "pay";

  public ElectronicMoneyCommand() {}

  @CommandLine.Parameters(
      index = "0",
      paramLabel = "mode",
      description = "Mode of execution, either storage or transaction")
  String mode;

  @CommandLine.Parameters(
      index = "1",
      paramLabel = "action",
      description = "Action for an account, either charge or pay")
  String action;

  @CommandLine.Option(
      names = {"-a", "--amount"},
      description = "Amount of money")
  Integer amount;

  @CommandLine.Option(
      names = {"-u", "--user_account"},
      description = "User account")
  String userAcc;

  @CommandLine.Option(
      names = {"-f", "--from"},
      description = "From account")
  String from;

  @CommandLine.Option(
      names = {"-t", "--to"},
      description = "To account")
  String to;

  @Override
  public Integer call() throws Exception {
    ElectronicMoney eMoney;
    if (mode.equals(STORAGE_MODE)) {
      eMoney = new ElectronicMoneyWithStorage();
    } else if (mode.equals(TRANSACTION_MODE)) {
      eMoney = new ElectronicMoneyWithTransaction();
    } else {
      throw new Exception("Wrong in mode, mode must be either storage or transaction.");
    }

    if (action.equals(CHARGE_ACTION)) {
      if (userAcc == null || amount == null) {
        throw new Exception("--user_account and --amount are required");
      }
      eMoney.charge(userAcc, amount);
      System.out.println("Charge " + amount + " to " + userAcc + " successfully.");
    } else if (action.equals(PAY_ACTION)) {
      if (from == null || to == null || amount == null) {
        throw new Exception("--from, --to and --amount are required");
      }
      eMoney.pay(from, to, amount);
      System.out.println("Pay " + amount + " from " + from + " to " + to + " successfully.");
    } else {
      throw new Exception("Wrong in action, action must be either charge or pay.");
    }

    eMoney.close();
    return 0;
  }
}
