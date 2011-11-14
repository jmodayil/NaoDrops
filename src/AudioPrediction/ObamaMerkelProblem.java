package AudioPrediction;

<<<<<<< HEAD
import java.util.Random;

import rltoys.algorithms.learning.control.acting.EpsilonGreedy;
import rltoys.algorithms.learning.control.sarsa.Sarsa;
import rltoys.algorithms.learning.control.sarsa.SarsaControl;
import rltoys.algorithms.representations.acting.Policy;
=======
import nao.NaoAction;
import nao.NaoRobot;
>>>>>>> 7f8f29780bbb7055bcfbb48adbb89288b338552e
import rltoys.algorithms.representations.actions.Action;
import rltoys.algorithms.representations.actions.TabularAction;
import rltoys.algorithms.representations.tilescoding.TileCodersNoHashing;
import rltoys.environments.envio.actions.ActionArray;
import rltoys.math.ranges.Range;
import rltoys.math.vector.BinaryVector;
import rltoys.math.vector.implementations.PVector;
import zephyr.plugin.core.api.monitoring.annotations.Monitor;
import zephyr.plugin.core.api.synchronization.Clock;

@Monitor
public class ObamaMerkelProblem {
  protected static final ActionArray LEFT = new ActionArray(-0.03);
  protected static final ActionArray RIGHT = new ActionArray(0.03);
  protected static final ActionArray REST = new ActionArray(0.0);
  protected static final Action[] Actions = { LEFT, RIGHT, REST };


  private double[] leds = new double[83];
  private final NaoRobot robot;
  private final NaoAction naoAct = new NaoAction();


  double[] obsArray;
  double energy;
  private double reward = 0.0;

  double cameraMotion = 0.0;
  private final double cameraMotionThreshold = 13.5;


  private final double headMotionThreshold = 0.001;
  private final double secondLastHeadPosition = 0.0;
  private double headMotion = 0.0;
  private double oldHeadPosition;
  private double currentHeadPosition;
  private double headDifference;
  double headJoint = 0;

  private final double[] joints = new double[14];
  private final double[] stiffness = new double[14];
  private final Clock clock;

  private final Range soundMagnitudeRange = new Range(7.0, 400.0);
  private final Range cameraMotionRange = new Range(4.0, 20.0);
  private final Range headPositionRange = new Range(0.2, 0.8);
  private final Range headMotionRange = new Range(-0.06, 0.06);

  // head pos + cam motion + head motion + FFT magnitudes
  private final PVector obsRewardVector = new PVector(1 + 1 + 1 + 1024);


  public ObamaMerkelProblem(NaoRobot R, Clock clock) {
    this.robot = R;
    joints[0] = 0.5;
    stiffness[0] = 0.1;
    naoAct.set(joints, 0.3, stiffness, null, null);
    robot.sendAction(naoAct);

    this.clock = clock;
  }

  public void update(ActionArray action) {
    // Get new observations from robot:
    obsArray = robot.waitNewObs();

    // Copy the sound observations to the soundMagnitudes vectors:
    for (int n = 0; n < 1024; n++) {
      obsRewardVector.setEntry(n + 3, obsArray[67 + n]);
    }
    // Scale the values...
    obsRewardVector.mapMultiplyToSelf(0.00001);

    energy = obsRewardVector.dotProduct(obsRewardVector);

    // get new cam motion value:
    cameraMotion = robot.getMotion();

    // get new Head Position and motion:
    oldHeadPosition = currentHeadPosition;
    currentHeadPosition = obsArray[12];
    headDifference = currentHeadPosition - oldHeadPosition;
    headMotion = headMotion + .5 * (headDifference - headMotion);

    // set headMotion, head Position and cam motion in obsRewardVector:
    obsRewardVector.setEntry(0, currentHeadPosition);
    obsRewardVector.setEntry(1, cameraMotion);
    obsRewardVector.setEntry(2, headMotion);


    // Calculate Reward:
    if (Math.abs(headMotion) < headMotionThreshold
        && Math.abs(headMotion - secondLastHeadPosition) < headMotionThreshold) {
      if (cameraMotion > cameraMotionThreshold) {
        reward = 1.0;
        // System.out.println("Headmotion is ZERO, cameramotion is high");
      } else {
        reward = 0.0;
        // System.out.println("Headmotion is zero, cameramotion is low...");
      }
    } else {
      // System.out.println("Motion is NOT zero!");
      reward = 0.0;
    }

    // Set the reward:
    obsRewardVector.setEntry(0, reward);

    // Light LEDs of Nao according to reward:
    if (reward < 1.0) {
      leds = NaoAction.setFaceLeds(1);
    } else {
      leds = NaoAction.setFaceLeds(2);
    }

    // Set the desired agent action:
    joints[0] = joints[0] + action.actions[0];
    if (joints[0] > 0.8) {
      joints[0] = 0.8;
    } else if (joints[0] < 0.2) {
      joints[0] = 0.2;
    }


    // Put these LEDs and joint values to the nao...
    naoAct.set(joints, 0.3, stiffness, leds, null);
    robot.sendAction(naoAct);
  }

  public PVector getObs() {
    return obsRewardVector.copy();
  }

  public double getReward() {
    return reward;
  }

  public Range[] getObservationRagnes() {
    Range[] outRanges = new Range[1 + 1 + 1 + 1024];

    outRanges[0] = headPositionRange;
    outRanges[1] = cameraMotionRange;
    outRanges[2] = headMotionRange;

    for (int n = 0; n < 1024; n++) {
      outRanges[n + 3] = soundMagnitudeRange;
    }
    return outRanges;
  }

  public void run() {
    System.out.println("Entering the run function...");
    Action[] act = this.actions();

    // Initialise tilecoder
    TileCodersNoHashing tileCoders = new TileCodersNoHashing(this.getObservationRagnes());
    tileCoders.addFullTilings(20, 4);

    // Associate actions and states...
    TabularAction toStateAction = new TabularAction(this.actions(), tileCoders.vectorSize());
    // Set parameters for Sarsa
    double alpha = .2 / tileCoders.nbActive();
    double gamma = 0.6;
    double lambda = .3;
    // Initialize Sarsa Algorithm:
    Sarsa sarsa = new Sarsa(alpha, gamma, lambda, toStateAction.vectorSize()); // Hope
                                                                               // vectorsize
                                                                               // is
                                                                               // correct!
    double epsilon = 0.01;
    // Use epsilon-greedy policy:
    Policy acting = new EpsilonGreedy(new Random(0), this.actions(), toStateAction, sarsa, epsilon);
    // Initialize the sarsa control algorithm:
    SarsaControl control = new SarsaControl(acting, toStateAction, sarsa);


    // run the problem:
    PVector o_t = null;
    PVector o_tp1 = null;
    BinaryVector x_t = null;
    ActionArray a_tp1 = (ActionArray) this.actions()[2];
    ActionArray a_t = null;
    double r_tp1;

    while (!this.robot.isClosed() && !clock.isTerminated()) {
      clock.tick(); // CD: observes all variables for zephyr plot function

      this.update(a_tp1);
      a_t = a_tp1;
      r_tp1 = this.getReward();
      o_tp1 = this.getObs();

      BinaryVector x_tp1 = tileCoders.project(o_tp1.accessData());
      a_tp1 = (ActionArray) control.step(x_t, a_t, x_tp1, r_tp1);
      x_t = x_tp1;
    }


    // int stepss = 0;
    // Random gen = new Random();
    //
    // while (!clock.isTerminated()) {
    // stepss++;
    // clock.tick();
    // if (stepss % 25 == 0) {
    // // if (test > .5) {
    // // this.update((ActionArray) act[1]);
    // // System.out.println("LEFT");
    // // } else {
    // // this.update((ActionArray) act[0]);
    // // System.out.println("RIGHT");
    // // }
    // this.update((ActionArray) act[0]);
    // } else {
    // this.update((ActionArray) act[2]);
    // }
    // }
  }

  public Action[] actions() {
    return Actions;
  }
}
