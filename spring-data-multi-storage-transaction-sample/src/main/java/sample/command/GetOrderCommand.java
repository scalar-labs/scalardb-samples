package sample.command;

import java.util.concurrent.Callable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import sample.SampleService;

@Component
@Command(name = "GetOrder", description = "Get order information by order ID")
public class GetOrderCommand implements Callable<Integer> {
  @Autowired private SampleService sampleService;

  @Parameters(index = "0", paramLabel = "ORDER_ID", description = "order ID")
  private String orderId;

  @Override
  public Integer call() {
    System.out.println(sampleService.getOrderByOrderId(orderId));
    return 0;
  }
}
