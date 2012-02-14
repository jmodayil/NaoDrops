package AudioPrediction;

import java.io.IOException;

import nao.NaoAction;
import nao.NaoRobot;

import org.apache.commons.lang3.ArrayUtils;

import rltoys.algorithms.representations.actions.Action;
import rltoys.environments.envio.actions.ActionArray;
import rltoys.math.ranges.Range;
import rltoys.math.vector.implementations.PVector;
import zephyr.plugin.core.api.monitoring.annotations.Monitor;
import zephyr.plugin.core.api.synchronization.Clock;

@Monitor
public class ObamaMerkelProblem {
  // Actions
  protected static final ActionArray UL = new ActionArray(0.0);
  protected static final ActionArray UR = new ActionArray(1.0);
  protected static final ActionArray LL = new ActionArray(2.0);
  protected static final ActionArray LR = new ActionArray(3.0);


  protected static final Action[] Actions = { UL, UR, LL, LR };

  int nbOfObs = 1 + 14 + 1; //

  // Robot
  private double[] leds = new double[83];
  private final NaoRobot robot;
  private final NaoAction naoAct = new NaoAction();
  private final double[] joints = new double[14];
  private final double[] stiffness = new double[14];
  private final double maxVel = 0.5;

  // Zephyr Clock
  private final Clock clock;

  // Raw Observations
  double[] obsArray;

  private double reward = 0.0;

  private double[] cameraMotion = new double[4];

  // observation OUTPUT to agent:
  private final PVector obsVector = new PVector(nbOfObs);

  // Ranges
  private Range cameraMotionRange;
  // private Range headPositionRange;
  private Range[] soundFeatureRange;
  private Range actionRange;

  // Sound Data
  private double[] mfccs;
  private double[] frame;

  // MFCC Processor:
  private final MFCCProvider mfccProc = new MFCCProvider();

  // Choice of the subImage:
  private double subImageSelection = 0.0;

  // nextAction and currentAction:
  double a_t = 0.0;
  double a_tp1 = 0.0;


  public ObamaMerkelProblem(NaoRobot R, Clock clock) {
    this.robot = R;
    // joints[0] = 0.5;
    // stiffness[0] = 0.4; // 0.075
    // naoAct.set(null, 0.0, null, null, null);
    // robot.sendAction(naoAct);

    this.clock = clock;
  }

  public void update(ActionArray action) throws IllegalArgumentException, IOException {
    if (clock.isTerminated()) {
      // Check whether the clock is still active...
      return;
    }
    // Send the desired action to the robot:
    a_tp1 = action.actions[0];
    subImageSelection = a_tp1;
    naoAct.set(null, 0.0, null, leds(), null);
    robot.sendAction(naoAct);

    // Receive new observations:

    obsArray = robot.waitNewObs();

    // get new cam motion value:
    cameraMotion = robot.getCameraMotion();

    calculateReward();


    a_t = a_tp1;

    generateOutputObsVector();

  }

  private void generateOutputObsVector() throws IOException {
    // Output the new observations:
    // obsVector.setEntry(0, headPositionRange.bound(action.actions[0]));
    obsVector.setEntry(0, cameraMotionRange.bound(cameraMotion[(int) a_tp1]) - 0.00001);


    // Calculate the MFCCs:
    frame = ArrayUtils.subarray(obsArray, 83, obsArray.length);
    // System.out.println("Length of Frame: " + frame.length);
    mfccs = mfccProc.getMeanMfccVector(frame);
    // MFCC Calculation Done.
    for (int n = 1; n < 15; n++) {
      obsVector.setEntry(n, soundFeatureRange[n - 1].bound(mfccs[n - 1]) - 0.00001);
    }
    obsVector.setEntry(15, a_t);
  }

  private void calculateReward() {
    if (cameraMotion[(int) a_tp1] > 2.50) {
      reward = 1.0;
    } else {
      reward = 0.0;
    }
    if (a_t != a_tp1) {
      reward = -0.5;
    }
  }

  private double[] leds() {
    // Light LEDs of Nao according to reward:
    if (reward == 1.0) {
      leds = NaoAction.setFaceLeds(2);
    } else if (reward == 0.0) {
      leds = NaoAction.setFaceLeds(1);
    } else {
      leds = NaoAction.setFaceLeds(0);
    }
    return leds;
  }

  public PVector getObs() {
    return obsVector.copy();
  }

  public double getReward() {
    return reward;
  }

  public Range[] getObservationRanges() {
    soundFeatureRange = new Range[14];
    cameraMotionRange = new Range(0.0, 6.0);
    // headPositionRange = new Range(0.39, 0.61);
    actionRange = new Range(-0.01, 3.01);

    soundFeatureRange[0] = new Range(21.06, 24.16);
    soundFeatureRange[1] = new Range(531.4, 616.19);
    soundFeatureRange[2] = new Range(24.6, 87.18);
    soundFeatureRange[3] = new Range(-16.32, 27.28);
    soundFeatureRange[4] = new Range(-10.45, 32.0);
    soundFeatureRange[5] = new Range(-20.18, 8.27);
    soundFeatureRange[6] = new Range(-25.89, 8.3);
    soundFeatureRange[7] = new Range(-19.31, 13.36);
    soundFeatureRange[8] = new Range(-11.76, 11.65);
    soundFeatureRange[9] = new Range(-8.54, 13.78);
    soundFeatureRange[10] = new Range(-8.44, 14.79);
    soundFeatureRange[11] = new Range(-11.54, 10.56);
    soundFeatureRange[12] = new Range(-8.29, 5.78);
    soundFeatureRange[13] = new Range(-14.24, 6.42);

    Range[] outRanges = new Range[nbOfObs];

    // outRanges[0] = headPositionRange;
    outRanges[0] = cameraMotionRange;
    for (int n = 1; n < 15; n++) {
      outRanges[n] = soundFeatureRange[n - 1];
    }
    System.out.println("Length of outRanges: " + outRanges.length);
    outRanges[15] = actionRange;
    return outRanges;
  }

  public void releaseRobot() {
    // Release the robot's stiffness...
    System.out.println("releasing the robot...");
    stiffness[0] = 0.0;
    joints[0] = 0.5;
    naoAct.set(joints, 0.0, stiffness, null, null);
    robot.sendAction(naoAct);

    System.out.println("Robot is released...");
  }

  public Action[] actions() {
    return Actions;
  }
}
