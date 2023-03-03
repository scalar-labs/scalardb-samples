package sample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import picocli.CommandLine;
import picocli.CommandLine.IFactory;
import sample.command.SampleCommand;

@Configuration
@ComponentScan
@EnableAutoConfiguration
public class SampleApp implements CommandLineRunner, ExitCodeGenerator {
  private int exitCode;

  @Autowired private IFactory factory;

  @Autowired private SampleCommand sampleCommand;

  @Override
  public void run(String... args) {
    exitCode = new CommandLine(sampleCommand, factory).execute(args);
  }

  @Override
  public int getExitCode() {
    return exitCode;
  }

  public static void main(String[] args) {
    System.exit(SpringApplication.exit(SpringApplication.run(SampleApp.class, args)));
  }
}
