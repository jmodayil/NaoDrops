package AudioPrediction;

import java.io.IOException;

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


  // Variables for the main loop:
  ActionArray a_tp1;
  ActionArray a_t;
  PVector o_tp1;
  double r_tp1;
  RealVector x_t;

  public AudioPredictionZephyr() {
    System.out.println("Initializing the Runnable class...");

    robot = new NaoRobot();
    problem = new ObamaMerkelProblem(robot, clock);
    agent = new ObamaMerkelAgent(problem.getObservationRanges(), problem.actions());
    Zephyr.advertise(clock, this);

    // Initialize the main loop variables:
    a_tp1 = (ActionArray) problem.actions()[0];
    a_t = a_tp1;

    try {
      problem.update(a_tp1);
    } catch (IllegalArgumentException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    } catch (IOException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }

    o_tp1 = problem.getObs();
    r_tp1 = problem.getReward();
    x_t = agent.project(o_tp1.accessData());
  }

  @Override
  public void run() {
    System.out.println("Calling the run() method of AudioPredictionZephyr class");

    // run the problem:
    while (!this.robot.isClosed() && !clock.isTerminated()) {
      clock.tick(); // CD: observes all variables for zephyr plot function
      try {
        problem.update(a_tp1);
      } catch (IllegalArgumentException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      a_t = a_tp1;
      r_tp1 = problem.getReward();
      o_tp1 = problem.getObs();

      BinaryVector x_tp1 = agent.project(o_tp1.accessData());
      a_tp1 = (ActionArray) agent.step(x_t, a_t, x_tp1, r_tp1);
      robot.updateShowCurrentImage(a_tp1.actions[0]);
      x_t = x_tp1;
    }
    // Release the robot's stiffness:
    System.out.println("Release the robot's stiffness:");
    problem.releaseRobot();
  }
}