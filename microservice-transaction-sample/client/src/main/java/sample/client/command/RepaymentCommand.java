package sample.client.command;

import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import sample.rpc.CustomerServiceGrpc;
import sample.rpc.RepaymentRequest;

@Command(name = "Repayment", description = "Repayment")
public class RepaymentCommand implements Callable<Integer> {

  @Parameters(index = "0", paramLabel = "CUSTOMER_ID", description = "customer ID")
  private int customerId;

  @Parameters(index = "1", paramLabel = "AMOUNT", description = "amount of the money for repayment")
  private int amount;

  @Override
  public Integer call() {
    ManagedChannel channel = Utils.getCustomerServiceChannel();
    try {
      CustomerServiceGrpc.CustomerServiceBlockingStub stub =
          CustomerServiceGrpc.newBlockingStub(channel);
      Empty response =
          stub.repayment(
              RepaymentRequest.newBuilder().setCustomerId(customerId).setAmount(amount).build());
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
