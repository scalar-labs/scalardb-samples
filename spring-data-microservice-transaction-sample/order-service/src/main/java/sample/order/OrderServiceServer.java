package sample.order;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "order-service-server", description = "Starts Order Service server.")
public class OrderServiceServer implements Callable<Integer> {
  private static final Logger logger = LoggerFactory.getLogger(OrderServiceServer.class);

  private static final int PORT = 10020;

  private OrderService service;
  private Server server;

  @Override
  public Integer call() throws Exception {
    addShutdownHook();
    start();
    blockUntilShutdown();
    return 0;
  }

  public void start() throws Exception {
    service = new OrderService();
    server = ServerBuilder.forPort(PORT).addService(service).build().start();
    logger.info("Order Service server started, listening on " + PORT);
  }

  public void addShutdownHook() {
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  logger.info("Signal received. Shutting down the server ...");
                  shutdown();
                  blockUntilShutdown();
                  service.close();
                  logger.info("The server shut down.");
                }));
  }

  private void blockUntilShutdown() {
    if (server != null) {
      try {
        server.awaitTermination();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        logger.warn("Unexpectedly received an interruption");
      }
    }
  }

  private void shutdown() {
    if (server != null) {
      try {
        server.shutdown();
      } catch (Exception e) {
        logger.warn("shutdown() failed", e);
      }
    }
  }

  public static void main(String[] args) {
    int exitCode = new CommandLine(new OrderServiceServer()).execute(args);
    System.exit(exitCode);
  }
}
