package nao;

import java.io.IOException;

import zephyr.plugin.core.api.Zephyr;
import zephyr.plugin.core.api.monitoring.annotations.Monitor;
import zephyr.plugin.core.api.synchronization.Clock;

@Monitor
public class NaoTestZephyr implements Runnable {
  private final NaoRobot environment = new NaoRobot();// CritterbotEnvironments.createRobotEnvironment();
  @Monitor
  NaoTest runner;
  private final Clock clock = new Clock();
  @Monitor
  int watchable = 1;

  // rlpark.plugin.video ImageProvider
  public NaoTestZephyr() {
    System.out.println("Initializing the Runnable class...");
    try {
      runner = new NaoTest(environment, clock);
    } catch (IOException e) {
      e.printStackTrace();
      runner = null;
    }
    Zephyr.advertise(clock, this); // zephyr is told when clock.tick() is
                                   // invoked
  }

  @Override
  public void run() {
    System.out.println("Calling the run() method of NaoTestZephyr class");
    runner.run(clock);
    // while (clock.tick()) {
    // double[] obs = environment.waitNewObs();
    // NaoAction act = runner.getAtp1(obs);
    // environment.sendAction(act);
    // }
    // environment.run(runner);
    // while (clock.tick())
    // watchable++;
  }

  public static void main(String[] args) {
    new NaoTestZephyr().run();
  }


}