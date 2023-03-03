package sample.command;

import java.util.concurrent.Callable;
import org.springframework.beans.factory.annotation.Autowired;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import sample.SampleService;

@Command(name = "Repayment", description = "Repayment")
public class RepaymentCommand implements Callable<Integer> {
  @Autowired private SampleService sampleService;

  @Parameters(index = "0", paramLabel = "CUSTOMER_ID", description = "customer ID")
  private int customerId;

  @Parameters(index = "1", paramLabel = "AMOUNT", description = "amount of the money for repayment")
  private int amount;

  @Override
  public Integer call() {
    sampleService.repayment(customerId, amount);
    return 0;
  }
}
