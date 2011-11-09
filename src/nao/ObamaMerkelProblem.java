package nao;

import rltoys.algorithms.representations.actions.Action;
import rltoys.environments.envio.actions.ActionArray;
import rltoys.environments.envio.observations.Legend;
import zephyr.plugin.core.api.monitoring.annotations.Monitor;
import zephyr.plugin.core.api.synchronization.Clock;

@Monitor
public class ObamaMerkelProblem {
  protected static final ActionArray LEFT = new ActionArray(-0.05);
  protected static final ActionArray RIGHT = new ActionArray(0.05);
  protected static final ActionArray REST = new ActionArray(0.0);
  protected static final Action[] Actions = { LEFT, RIGHT, REST };


  private double[] leds = new double[83];
  private final NaoRobot robot;
  private final NaoAction naoAct = new NaoAction();


  double[] obsArray;

  private double reward = 0.0;
  private double motionValue;

  double currentMotion = 0.0;
  double lastMotion = 0.0;
  double secondLastMotion = 0.0;
  private final double headMotionThreshold = 0.001;
  private final double cameraMotionThreshold = 0.01;
  private double secondLastHeadPosition = 0.0;
  private double lastHeadPosition = 0.0;
  private double currentHeadPosition = 0.0;

  private final double[] joints = new double[14];
  private final double[] stiffness = new double[14];
  private Legend legend;
  private final Clock clock;

  // private final PVector soundMagnitudes0 = new PVector(1024);
  // private final PVector soundMagnitudes1 = new PVector(1024);


  public ObamaMerkelProblem(NaoRobot R, Clock clock) {
    this.robot = R;
    naoAct.set(joints, 0.1, stiffness, null, null);
    robot.sendAction(naoAct);
    this.clock = clock;
  }

  protected void update(ActionArray action) {
    // Get new observations from robot:
    obsArray = robot.waitNewObs();
    // // Copy the sound observations to the soundMagnitudes vectors:
    // for (int n = 0; n < 1024; n++) {
    // soundMagnitudes0.setEntry(n, obsArray[67 + n]);
    // soundMagnitudes1.setEntry(n, obsArray[67 + n + 1024]);
    // }
    // soundMagnitudes0.mapMultiplyToSelf(0.00001);
    // soundMagnitudes1.mapMultiplyToSelf(0.00001);

    // get new motion value:
    secondLastMotion = lastMotion;
    lastMotion = currentMotion;
    currentMotion = robot.getMotion();

    // get new Head Position:
    secondLastHeadPosition = lastHeadPosition;
    lastHeadPosition = currentHeadPosition;
    currentHeadPosition = obsArray[12];

    motionValue = Math.abs(currentMotion - lastMotion) > Math.abs(currentMotion - secondLastMotion) ? Math
        .abs(currentMotion - lastMotion) : Math.abs(currentMotion - secondLastMotion);

    // Calculate Reward:
    if (Math.abs(currentHeadPosition - lastHeadPosition) < 0.001
        && Math.abs(currentHeadPosition - secondLastHeadPosition) < 0.001) {
      System.out.println("Motion is zero!");
      if ((Math.abs(currentMotion - lastMotion) > cameraMotionThreshold)
          || (Math.abs(currentMotion - secondLastMotion) > cameraMotionThreshold)) {
        reward = 1.0;
      }
    } else {
      reward = 0.0;
    }

    // Light LEDs of Nao according to reward:
    if (reward < 1.0) {
      leds = NaoAction.setFaceLeds(2);
    } else {
      leds = NaoAction.setFaceLeds(1);
    }

    // Set the desired agent action:
    joints[0] = action.actions[0];
    if (joints[0] > 1.0) {
      joints[0] = 1.0;
    } else if (joints[0] < -1.0) {
      joints[0] = -1.0;
    }
    // Put these LEDs to the nao...
    naoAct.set(joints, 0.1, stiffness, leds, null);
    robot.sendAction(naoAct);
  }

  public void run() {
    System.out.println("Entering the run function...");
    Action[] act = this.actions();

    while (!clock.isTerminated()) {
      clock.tick();
      this.update((ActionArray) act[2]);
    }
  }

  public Action[] actions() {
    return Actions;
  }
}
