package sample.command;

import java.util.concurrent.Callable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import sample.SampleService;

@Component
@Command(name = "LoadInitialData", description = "Load initial data")
public class LoadInitialDataCommand implements Callable<Integer> {
  @Autowired private SampleService sampleService;

  @Override
  public Integer call() {
    sampleService.loadInitialData();
    return 0;
  }
}
