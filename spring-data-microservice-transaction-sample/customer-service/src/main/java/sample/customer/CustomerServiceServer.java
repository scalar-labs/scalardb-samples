package sample.customer;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IFactory;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@Command(name = "customer-service-server", description = "Starts Customer Service server.")
public class CustomerServiceServer implements Callable<Integer>, CommandLineRunner, ExitCodeGenerator {
  private static final Logger logger = LoggerFactory.getLogger(CustomerServiceServer.class);

  private static final int PORT = 10010;

  @Autowired
  private CustomerService service;

  private Server server;

  private int exitCode;

  @Autowired
  private IFactory factory;

  @Override
  public Integer call() throws Exception {
    addShutdownHook();
    start();
    blockUntilShutdown();
    return 0;
  }

  public void start() throws Exception {
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

  @Override
  public void run(String... args) {
    service.init();
    exitCode = new CommandLine(this, factory).execute(args);
  }

  @Override
  public int getExitCode() {
    return exitCode;
  }

  public static void main(String[] args) {
    // Invoke this application via org.springframework.boot.CommandLineRunner.run
    int exitCode = SpringApplication.exit(SpringApplication.run(CustomerServiceServer.class, args));
    System.exit(exitCode);
  }
}
