package sample.command;

import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import sample.Sample;

@Command(name = "LoadInitialData", description = "Load initial data")
public class LoadInitialDataCommand implements Callable<Integer> {

  @Override
  public Integer call() throws Exception {
    try (Sample sample = new Sample()) {
      sample.loadInitialData();
    }
    return 0;
  }
}
