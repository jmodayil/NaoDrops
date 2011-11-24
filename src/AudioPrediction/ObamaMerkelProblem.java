package AudioPrediction;

import java.util.Random;

import nao.NaoAction;
import nao.NaoRobot;
import rltoys.algorithms.learning.control.acting.EpsilonGreedy;
import rltoys.algorithms.learning.control.qlearning.QLearning;
import rltoys.algorithms.learning.control.qlearning.QLearningControl;
import rltoys.algorithms.representations.acting.Policy;
import rltoys.algorithms.representations.actions.Action;
import rltoys.algorithms.representations.actions.TabularAction;
import rltoys.algorithms.representations.tilescoding.TileCodersNoHashing;
import rltoys.algorithms.representations.traces.ATraces;
import rltoys.environments.envio.actions.ActionArray;
import rltoys.math.ranges.Range;
import rltoys.math.vector.BinaryVector;
import rltoys.math.vector.RealVector;
import rltoys.math.vector.implementations.PVector;
import zephyr.plugin.core.api.monitoring.annotations.Monitor;
import zephyr.plugin.core.api.synchronization.Clock;

@Monitor
public class ObamaMerkelProblem {
  QLearning learning;
  QLearningControl control;
  TileCodersNoHashing tileCoders;
  protected static final ActionArray LEFT = new ActionArray(0.4);
  protected static final ActionArray RIGHT = new ActionArray(0.6);
  // protected static final ActionArray REST = new ActionArray(0.0);
  protected static final Action[] Actions = { LEFT, RIGHT };

  int nbOfObs = 1 + 1; //

  private double[] leds = new double[83];
  private final NaoRobot robot;
  private final NaoAction naoAct = new NaoAction();

  double expectation;

  double[] obsArray;
  double[] oldFFTvalues = new double[3];

  double energy;
  private double reward = 0.0;

  double cameraMotion = 0.0;
  double luminance = 0.0;

  private double headMotion = 0.0;
  private double oldHeadPosition;
  private double currentHeadPosition;
  private double headDifference;
  double headJoint = 0;
  double headMotionThreshold = 0.00075;

  private final double[] joints = new double[14];
  private final double[] stiffness = new double[14];
  private final double maxVel = 0.5;
  private final Clock clock;

  // head pos + cam motion + head motion + FFT magnitudes
  @Monitor
  private final PVector obsVector = new PVector(nbOfObs);
  double oldAction = 0;
  private final TabularAction toStateAction;
  private Range cameraMotionRange;
  private Range headPositionRange;
  private Range luminanceRange;


  public ObamaMerkelProblem(NaoRobot R, Clock clock) {
    this.robot = R;
    joints[0] = 0.5;
    stiffness[0] = 0.4; // 0.075
    naoAct.set(joints, maxVel, stiffness, null, null);
    robot.sendAction(naoAct);

    this.clock = clock;

    // Initialise tilecoder
    tileCoders = new TileCodersNoHashing(this.getObservationRanges());
    tileCoders.addFullTilings(3, 1);

    // Associate actions and states...
    toStateAction = new TabularAction(this.actions(), tileCoders.vectorSize());
    // Set parameters for Sarsa
    double alpha = .2 / tileCoders.vectorNorm();
    double gamma = 0.0;
    double lambda = 0.0;
    // Initialize Sarsa Algorithm:
    // learning = new Sarsa(alpha, gamma, lambda, toStateAction.vectorSize());
    learning = new QLearning(this.actions(), alpha, gamma, lambda, toStateAction, toStateAction.vectorSize(),
                             new ATraces());
    double epsilon = 0.1;
    // Use epsilon-greedy policy:
    Policy acting = new EpsilonGreedy(new Random(0), this.actions(), toStateAction, learning, epsilon);
    // Initialize the sarsa control algorithm:
    // control = new SarsaControl(acting, toStateAction, learning);
    control = new QLearningControl(acting, learning);

    System.out.println("tostateaction vectorsize:  " + toStateAction.vectorSize());

  }

  public void update(ActionArray action) {
    // Send the desired action to the robot:
    joints[0] = action.actions[0];
    naoAct.set(joints, maxVel, stiffness, null, null);
    robot.sendAction(naoAct);


    // Get new observations from robot and force him to wait for 50 waitnewobs
    // calls:
    for (int n = 0; n < 15; n++) {
      obsArray = robot.waitNewObs();
      robot.sendAction(naoAct);
      clock.tick();
    }


    // get new cam motion value:
    cameraMotion = robot.getMotion();

    reward = cameraMotion;

    // Set the observations to the observation vector:

    oldAction = action.actions[0];
    obsVector.setEntry(0, headPositionRange.bound(action.actions[0]));
    obsVector.setEntry(1, cameraMotionRange.bound(cameraMotion));
  }

  private void lightLEDsReward() {
    // Light LEDs of Nao according to reward:
    if (reward < 1.0) {
      leds = NaoAction.setFaceLeds(2);
    } else if (reward < 10) {
      leds = NaoAction.setFaceLeds(0);
    } else {
      leds = NaoAction.setFaceLeds(1);
    }
    naoAct.set(null, maxVel, null, leds, null);
    robot.sendAction(naoAct);
  }

  private void waitNewSound() {
    while (obsArray[100] == oldFFTvalues[0] || obsArray[200] == oldFFTvalues[1] || obsArray[1000] == oldFFTvalues[2]) {
      obsArray = robot.waitNewObs();
      clock.tick();
    }
    oldFFTvalues[0] = obsArray[100];
    oldFFTvalues[1] = obsArray[200];
    oldFFTvalues[2] = obsArray[1000];
    return;
  }

  private void updateHeadMotion(double alpha) {
    oldHeadPosition = currentHeadPosition;
    currentHeadPosition = obsArray[12];
    headDifference = currentHeadPosition - oldHeadPosition;

    headMotion = headMotion + alpha * (headDifference - headMotion);
  }

  public PVector getObs() {
    return obsVector.copy();
  }

  public double getReward() {
    return reward;
  }

  public Range[] getObservationRanges() {
    // private final Range soundMagnitudeRange = new Range(0.0, 200.0);
    // luminanceRange = new Range(0.0, 255.0);
    cameraMotionRange = new Range(1.0, 10.0);
    headPositionRange = new Range(0.39, 0.61);
    // Range headMotionRange = new Range(-0.06, 0.06);

    Range[] outRanges = new Range[nbOfObs];

    outRanges[0] = headPositionRange;
    outRanges[1] = cameraMotionRange;
    return outRanges;
  }

  public void run() {
    System.out.println("Entering the run function...");

    // run the problem:
    ActionArray a_tp1 = (ActionArray) this.actions()[1];
    ActionArray a_t = a_tp1;
    this.update(a_tp1);

    PVector o_tp1 = this.getObs();
    double r_tp1 = this.getReward();
    RealVector x_t = tileCoders.project(o_tp1.accessData());

    while (!this.robot.isClosed() && !clock.isTerminated()) {
      clock.tick(); // CD: observes all variables for zephyr plot function

      this.update(a_tp1);
      a_t = a_tp1;
      r_tp1 = this.getReward();
      o_tp1 = this.getObs();

      BinaryVector x_tp1 = tileCoders.project(o_tp1.accessData());
      a_tp1 = (ActionArray) control.step(x_t, a_t, x_tp1, r_tp1);
      inspect(x_tp1);
      x_t = x_tp1;


    }
    // Release the robot's stiffness:
    System.out.println("Release the robot's stiffness:");
    this.releaseRobot();
  }

  private void inspect(BinaryVector x_tp1) {
    // TODO Auto-generated method stub
    int nbOfFeatures = toStateAction.vectorSize();
    PVector probe = new PVector(nbOfFeatures);

    for (int n = 0; n < nbOfFeatures; n++) {
      probe.setEntry(n, 1);

      String s = "";
      if (x_tp1.getEntry(n) != 0) {
        s = "*";
      }
      System.out.println(learning.predict(probe) + "   " + n + s);
      probe.setEntry(n, 0);
    }


  }

  public void releaseRobot() {
    // Release the robot's stiffness...
    System.out.println("releasing the robot...");
    stiffness[0] = 0.0;
    joints[0] = 0.5;
    naoAct.set(joints, 0.3, stiffness, null, null);
    robot.sendAction(naoAct);

    System.out.println("Robot is released...");

  }

  public Action[] actions() {
    return Actions;
  }
}
