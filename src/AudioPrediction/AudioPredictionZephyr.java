package AudioPrediction;

import nao.NaoRobot;
import zephyr.plugin.core.api.Zephyr;
import zephyr.plugin.core.api.monitoring.annotations.Monitor;
import zephyr.plugin.core.api.synchronization.Clock;

@Monitor
public class AudioPredictionZephyr implements Runnable {
  @Monitor
  private final NaoRobot robot;
  @Monitor
  ObamaMerkelProblem runner;
  private final Clock clock = new Clock();

  public AudioPredictionZephyr() {
    System.out.println("Initializing the Runnable class...");
    robot = new NaoRobot();
    runner = new ObamaMerkelProblem(robot, clock);
    Zephyr.advertise(clock, this); // zephyr is told when clock.tick() is
                                   // invoked
  }

  @Override
  public void run() {
    System.out.println("Calling the run() method of NaoTestZephyr class");
    runner.run();
  }


}