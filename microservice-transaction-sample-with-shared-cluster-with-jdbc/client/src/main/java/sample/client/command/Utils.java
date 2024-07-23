package sample.client.command;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;
import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import java.util.concurrent.TimeUnit;

public final class Utils {

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
      System.err.println("failed to shut down the channel");
    }
  }

  public static void printJsonString(MessageOrBuilder messageOrBuilder)
      throws InvalidProtocolBufferException {
    System.out.println(JsonFormat.printer().print(messageOrBuilder));
  }
}
