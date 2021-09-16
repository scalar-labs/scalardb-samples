package example.client.command;

import example.rpc.CustomerServiceGrpc;
import example.rpc.GetCustomerInfoRequest;
import example.rpc.GetCustomerInfoResponse;
import io.grpc.ManagedChannel;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "GetCustomerInfo", description = "Get customer information")
public class GetCustomerInfoCommand implements Callable<Integer> {

  @Parameters(index = "0", paramLabel = "CUSTOMER_ID", description = "customer ID")
  private int customerId;

  @Override
  public Integer call() {
    ManagedChannel channel = Utils.getCustomerServiceChannel();
    try {
      CustomerServiceGrpc.CustomerServiceBlockingStub stub =
          CustomerServiceGrpc.newBlockingStub(channel);
      GetCustomerInfoResponse response =
          stub.getCustomerInfo(
              GetCustomerInfoRequest.newBuilder().setCustomerId(customerId).build());
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
