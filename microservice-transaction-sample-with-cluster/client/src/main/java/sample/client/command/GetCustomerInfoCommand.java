package sample.client.command;

import io.grpc.ManagedChannel;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import sample.rpc.CustomerServiceGrpc;
import sample.rpc.GetCustomerInfoRequest;
import sample.rpc.GetCustomerInfoResponse;

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
