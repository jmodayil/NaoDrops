package nao;

import rltoys.algorithms.representations.actions.Action;
import rltoys.environments.envio.actions.ActionArray;
import rltoys.environments.envio.observations.Legend;
import rltoys.environments.envio.observations.TRStep;
import rltoys.environments.envio.problems.ProblemBounded;
import rltoys.environments.envio.problems.ProblemDiscreteAction;
import rltoys.math.ranges.Range;
import zephyr.plugin.core.api.monitoring.annotations.Monitor;

@Monitor
public class ObamaMerkelProblem implements ProblemBounded, ProblemDiscreteAction {
  protected static final ActionArray LEFT = new ActionArray(-0.05);
  protected static final ActionArray RIGHT = new ActionArray(0.05);
  protected static final ActionArray REST = new ActionArray(0.0);
  protected static final Action[] Actions = { LEFT, RIGHT, REST };


  private double[] leds = new double[83];
  private final NaoRobot robot;
  private final NaoAction naoAct = new NaoAction();


  double[] obsArray;

  private double reward = 0.0;

  double motion = 0.0;
  double oldmotion = 0.0;
  private double secondLastHeadPosition = 0.0;
  private double lastHeadPosition = 0.0;
  private double newHeadPosition = 0.0;
  private final double motionBorder;
  private final double[] joints = new double[14];
  private final double[] stiffness = new double[14];
  private Legend legend;

  // private final PVector soundMagnitudes0 = new PVector(1024);
  // private final PVector soundMagnitudes1 = new PVector(1024);


  public ObamaMerkelProblem(NaoRobot R, double motionBorder) {
    this.robot = R;
    naoAct.set(joints, 0.1, stiffness, null, null);
    robot.sendAction(naoAct);
    this.motionBorder = motionBorder;
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
    motion = robot.getMotion();

    // get new Head Position:
    newHeadPosition = obsArray[12];

    // Calculate Reward:
    if (Math.abs(newHeadPosition - lastHeadPosition) < 0.001
        && Math.abs(newHeadPosition - secondLastHeadPosition) < 0.001) {
      System.out.println("Motion is zero!");
      if (motion > motionBorder) {
        reward = 1.0;
      }
    } else {
      reward = 0.0;
    }
    secondLastHeadPosition = lastHeadPosition;
    lastHeadPosition = newHeadPosition;


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

  @Override
  public Action[] actions() {
    return Actions;
  }

  @Override
  public Range[] getObservationRanges() {
    return new Range[] {};
  }

  @Override
  public Legend legend() {
    return legend;
  }

  @Override
  public TRStep initialize() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TRStep step(Action action) {
    // TODO Auto-generated method stub
    return null;
  }
}
