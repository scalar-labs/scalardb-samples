package sample.command;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import sample.SampleService;
import sample.domain.model.ItemOrder;

@Component
@Command(name = "PlaceOrder", description = "Place an order")
public class PlaceOrderCommand implements Callable<Integer> {
  @Autowired private SampleService sampleService;

  @Parameters(index = "0", paramLabel = "CUSTOMER_ID", description = "customer ID")
  private int customerId;

  @Parameters(
      index = "1",
      paramLabel = "ORDERS",
      description = "orders. The format is \"<Item ID>:<Count>,<Item ID>:<Count>,...\"")
  private String orders;

  @Override
  public Integer call() {
    String[] split = orders.split(",", -1);
    List<ItemOrder> itemOrders = new ArrayList<>();

    for (String value : split) {
      String[] s = value.split(":", -1);
      itemOrders.add(new ItemOrder(Integer.parseInt(s[0]), Integer.parseInt(s[1])));
    }

    System.out.println(sampleService.placeOrder(customerId, itemOrders));
    return 0;
  }
}
