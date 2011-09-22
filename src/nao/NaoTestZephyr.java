package nao;

import java.io.IOException;

import zephyr.plugin.core.api.Zephyr;
import zephyr.plugin.core.api.monitoring.annotations.Monitor;
import zephyr.plugin.core.api.synchronization.Clock;

@Monitor
public class NaoTestZephyr implements Runnable {
  private final NaoRobot environment = new NaoRobot();// CritterbotEnvironments.createRobotEnvironment();
  NaoTest runner;
  private final Clock clock = new Clock();
  @Monitor
  int watchable = 1;

  // rlpark.plugin.video ImageProvider
  public NaoTestZephyr() {

    try {
      runner = new NaoTest(environment, clock);
    } catch (IOException e) {
      e.printStackTrace();
      runner = null;
    }
    Zephyr.advertise(clock, this);
  }

  @Override
  public void run() {
    while (clock.tick()) {
      double[] obs = environment.waitNewObs();
      NaoAction act = runner.getAtp1(obs);
      environment.sendAction(act);
    }
    // environment.run(runner);
    // while (clock.tick())
    // watchable++;
  }

  public static void main(String[] args) {
    new NaoTestZephyr().run();
  }


}