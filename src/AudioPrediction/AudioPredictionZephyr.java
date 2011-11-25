package AudioPrediction;

import nao.NaoRobot;
import rltoys.environments.envio.actions.ActionArray;
import rltoys.math.vector.BinaryVector;
import rltoys.math.vector.RealVector;
import rltoys.math.vector.implementations.PVector;
import zephyr.plugin.core.api.Zephyr;
import zephyr.plugin.core.api.monitoring.annotations.Monitor;
import zephyr.plugin.core.api.synchronization.Clock;

@Monitor
public class AudioPredictionZephyr implements Runnable {
  @Monitor
  private final NaoRobot robot;
  @Monitor
  private final ObamaMerkelProblem problem;
  @Monitor
  private final ObamaMerkelAgent agent;
  private final Clock clock = new Clock();

  public AudioPredictionZephyr() {
    System.out.println("Initializing the Runnable class...");
    robot = new NaoRobot();
    problem = new ObamaMerkelProblem(robot, clock);
    agent = new ObamaMerkelAgent(problem.getObservationRanges(), problem.actions());
    Zephyr.advertise(clock, this); // zephyr is told when clock.tick() is
                                   // invoked
  }

  @Override
  public void run() {
    System.out.println("Calling the run() method of NaoTestZephyr class");
    // // // //

    // run the problem:
    ActionArray a_tp1 = (ActionArray) problem.actions()[0];
    ActionArray a_t = a_tp1;
    problem.update(a_tp1);

    PVector o_tp1 = problem.getObs();
    double r_tp1 = problem.getReward();
    RealVector x_t = agent.project(o_tp1.accessData());

    while (!this.robot.isClosed() && !clock.isTerminated()) {
      clock.tick(); // CD: observes all variables for zephyr plot function

      problem.update(a_tp1);
      a_t = a_tp1;
      r_tp1 = problem.getReward();
      o_tp1 = problem.getObs();

      BinaryVector x_tp1 = agent.project(o_tp1.accessData());
      a_tp1 = (ActionArray) agent.step(x_t, a_t, x_tp1, r_tp1);
      agent.inspect(x_tp1);
      x_t = x_tp1;

      System.out.print("BinObsVector: " + x_t.getEntry(0) + " " + x_t.getEntry(1) + " " + x_t.getEntry(2) + " "
          + x_t.getEntry(3) + "\n\n");
    }
    // Release the robot's stiffness:
    System.out.println("Release the robot's stiffness:");
    problem.releaseRobot();
  }


}