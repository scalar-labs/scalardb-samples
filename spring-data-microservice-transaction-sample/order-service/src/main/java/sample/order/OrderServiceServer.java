package sample.order;

import com.scalar.db.sql.springdata.EnableScalarDbRepositories;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@SpringBootApplication
@EnableScalarDbRepositories(transactionManagerRef = "scalarDbSuspendableTransactionManager")
@EnableRetry
@Command(name = "order-service-server", description = "Starts Order Service server.")
public class OrderServiceServer implements Callable<Integer>, CommandLineRunner, ExitCodeGenerator {

  private static final Logger logger = LoggerFactory.getLogger(OrderServiceServer.class);

  private static final int PORT = 10020;

  @Autowired
  private OrderService service;

  private volatile Server server;

  private int exitCode;

  @Autowired
  private CommandLine.IFactory factory;

  public static void main(String[] args) {
    // Invoke this application via org.springframework.boot.CommandLineRunner.run
    int exitCode = SpringApplication.exit(SpringApplication.run(OrderServiceServer.class, args));
    System.exit(exitCode);
  }

  @Override
  public Integer call() throws Exception {
    addShutdownHook();
    start();
    blockUntilShutdown();
    return 0;
  }

  public void start() throws Exception {
    service.init();
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
                  logger.info("The server shut down");
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
        logger.warn("Shutdown() failed", e);
      }
    }
  }

  @Override
  public void run(String... args) {
    exitCode = new CommandLine(this, factory).execute(args);
  }

  @Override
  public int getExitCode() {
    return exitCode;
  }
}
