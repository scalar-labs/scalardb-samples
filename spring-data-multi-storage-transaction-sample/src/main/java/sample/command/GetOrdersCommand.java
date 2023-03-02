package sample.command;

import java.util.concurrent.Callable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import sample.SampleService;

@Component
@Command(name = "GetOrders", description = "Get order information by customer ID")
public class GetOrdersCommand implements Callable<Integer> {
  @Autowired private SampleService sampleService;

  @Parameters(index = "0", paramLabel = "CUSTOMER_ID", description = "customer ID")
  private int customerId;

  @Override
  public Integer call() {
    System.out.println(sampleService.getOrdersByCustomerId(customerId));
    return 0;
  }
}
