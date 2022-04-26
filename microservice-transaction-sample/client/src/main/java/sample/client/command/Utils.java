package sample.client.command;

import com.google.protobuf.Message;
import com.googlecode.protobuf.format.JsonFormat;
import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import java.util.concurrent.TimeUnit;

public final class Utils {

  private static final JsonFormat JSON_FORMAT = new JsonFormat();

  private Utils() {}

  public static ManagedChannel getCustomerServiceChannel() {
    return NettyChannelBuilder.forAddress("localhost", 10010).usePlaintext().build();
  }

  public static ManagedChannel getOrderServiceChannel() {
    return NettyChannelBuilder.forAddress("localhost", 10020).usePlaintext().build();
  }

  public static void shutdownChannel(ManagedChannel channel) {
    try {
      channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      System.err.println("failed to shutdown the channel");
    }
  }

  public static void printJsonString(Message message) {
    System.out.println(JSON_FORMAT.printToString(message));
  }
}
