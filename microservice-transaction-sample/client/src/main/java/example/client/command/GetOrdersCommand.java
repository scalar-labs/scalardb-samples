package example.client.command;

import example.rpc.GetOrdersRequest;
import example.rpc.GetOrdersResponse;
import example.rpc.OrderServiceGrpc;
import io.grpc.ManagedChannel;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "GetOrders", description = "Get order information by customer ID")
public class GetOrdersCommand implements Callable<Integer> {

  @Parameters(index = "0", paramLabel = "CUSTOMER_ID", description = "customer ID")
  private int customerId;

  @Override
  public Integer call() {
    ManagedChannel channel = Utils.getOrderServiceChannel();
    try {
      OrderServiceGrpc.OrderServiceBlockingStub stub = OrderServiceGrpc.newBlockingStub(channel);
      GetOrdersResponse response =
          stub.getOrders(GetOrdersRequest.newBuilder().setCustomerId(customerId).build());
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
