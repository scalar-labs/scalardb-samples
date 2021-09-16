package example.client.command;

import example.rpc.GetOrderRequest;
import example.rpc.GetOrderResponse;
import example.rpc.OrderServiceGrpc;
import io.grpc.ManagedChannel;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "GetOrder", description = "Get order information by order ID")
public class GetOrderCommand implements Callable<Integer> {

  @Parameters(index = "0", paramLabel = "ORDER_ID", description = "order ID")
  private String orderId;

  @Override
  public Integer call() {
    ManagedChannel channel = Utils.getOrderServiceChannel();
    try {
      OrderServiceGrpc.OrderServiceBlockingStub stub = OrderServiceGrpc.newBlockingStub(channel);
      GetOrderResponse response =
          stub.getOrder(GetOrderRequest.newBuilder().setOrderId(orderId).build());
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
