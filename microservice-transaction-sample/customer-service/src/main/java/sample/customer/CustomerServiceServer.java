package sample.customer;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "customer-service-server", description = "Starts Customer Service server.")
public class CustomerServiceServer implements Callable<Integer> {
  private static final Logger logger = LoggerFactory.getLogger(CustomerServiceServer.class);

  private static final int PORT = 10010;

  @CommandLine.Option(
      names = {"--config"},
      required = true,
      paramLabel = "PROPERTIES_FILE",
      description = "A configuration file in properties format.")
  private String configFile;

  private CustomerService service;
  private Server server;

  @Override
  public Integer call() throws Exception {
    addShutdownHook();
    start();
    blockUntilShutdown();
    return 0;
  }

  public void start() throws Exception {
    service = new CustomerService(configFile);
    server = ServerBuilder.forPort(PORT).addService(service).build().start();
    logger.info("Customer Service server started, listening on " + PORT);
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
                  logger.info("The server shut down");
                }));
  }

  private void blockUntilShutdown() {
    if (server != null) {
      try {
        server.awaitTermination();
      } catch (InterruptedException ignored) {
        // don't need to handle InterruptedException
      }
    }
  }

  private void shutdown() {
    if (server != null) {
      server.shutdown();
    }
  }

  public static void main(String[] args) {
    int exitCode = new CommandLine(new CustomerServiceServer()).execute(args);
    System.exit(exitCode);
  }
}
