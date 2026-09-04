package sample.client.command;

import io.grpc.ManagedChannel;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import sample.rpc.ItemOrder;
import sample.rpc.OrderServiceGrpc;
import sample.rpc.PlaceOrderRequest;
import sample.rpc.PlaceOrderResponse;

@Command(name = "PlaceOrder", description = "Place an order")
public class PlaceOrderCommand implements Callable<Integer> {

  @Parameters(index = "0", paramLabel = "CUSTOMER_ID", description = "customer ID")
  private int customerId;

  @Parameters(
      index = "1",
      paramLabel = "ORDERS",
      description = "orders. The format is \"<Item ID>:<Count>,<Item ID>:<Count>,...\"")
  private String orders;

  @Override
  public Integer call() {
    ManagedChannel channel = Utils.getOrderServiceChannel();
    try {
      OrderServiceGrpc.OrderServiceBlockingStub stub = OrderServiceGrpc.newBlockingStub(channel);

      PlaceOrderRequest.Builder builder = PlaceOrderRequest.newBuilder().setCustomerId(customerId);
      for (String order : orders.split(",", -1)) {
        String[] s = order.split(":", -1);
        int itemId = Integer.parseInt(s[0]);
        int count = Integer.parseInt(s[1]);
        builder.addItemOrder(ItemOrder.newBuilder().setItemId(itemId).setCount(count).build());
      }

      PlaceOrderResponse response = stub.placeOrder(builder.build());

      Utils.printJsonString(response);
      return 0;
    } catch (Exception e) {
      e.printStackTrace();
      return 1;
    } finally {
      Utils.shutdownChannel(channel);
    }
  }
}
