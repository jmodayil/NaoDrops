package AudioPrediction;

import nao.MathUtilsCD;
import nao.NaoAction;
import nao.NaoRobot;
import rltoys.algorithms.representations.actions.Action;
import rltoys.environments.envio.actions.ActionArray;
import rltoys.math.ranges.Range;
import rltoys.math.vector.implementations.PVector;
import zephyr.plugin.core.api.monitoring.annotations.Monitor;
import zephyr.plugin.core.api.synchronization.Clock;

@Monitor
public class ObamaMerkelProblem {
  // Actions
  protected static final ActionArray RIGHT = new ActionArray(0.4);
  protected static final ActionArray LEFT = new ActionArray(0.6);
  protected static final ActionArray CENTER = new ActionArray(0.5);
  protected static final Action[] Actions = { RIGHT, LEFT, CENTER };

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

  int nbOfObs = 1 + 1 + 1 + 1; // cam motion, head position, meanFrequency,
  // Variance

  private double reward = 0.0;

  private double cameraMotion = 0.0;

  // observation OUTPUT to agent:
  private final PVector obsVector = new PVector(nbOfObs);

  // Ranges
  private Range cameraMotionRange;
  private Range headPositionRange;
  // private Range soundEnergyRange;
  private Range soundFrequencyRange;
  private Range soundVarianceRange;

  // Sound Data
  double[] oldFFTvalues = new double[3];
  private final PVector meanSoundMagnitudes = new PVector(1024);
  private double mean;
  private double variance;
  private double soundEnergy;
  private double oldAction;


  public ObamaMerkelProblem(NaoRobot R, Clock clock) {
    this.robot = R;
    joints[0] = 0.5;
    stiffness[0] = 0.4; // 0.075
    naoAct.set(joints, maxVel, stiffness, null, null);
    robot.sendAction(naoAct);

    this.clock = clock;
  }

  public void update(ActionArray action) {
    // Send the desired action to the robot:
    joints[0] = action.actions[0];
    naoAct.set(joints, maxVel, stiffness, null, null);
    robot.sendAction(naoAct);

    // Reset sound vector:
    meanSoundMagnitudes.mapMultiplyToSelf(0.0);

    // Get new observations from robot and force him to wait for some waitNewObs
    // calls:
    for (int n = 0; n < 18; n++) {
      if (!clock.isTerminated()) {
        obsArray = robot.waitNewObs();
        // Check whether sound Data is the same...
        if (!(obsArray[100] == oldFFTvalues[0] && obsArray[200] == oldFFTvalues[1] && obsArray[1000] == oldFFTvalues[2])) {
          // Sound data is different!
          oldFFTvalues[0] = obsArray[100];
          oldFFTvalues[1] = obsArray[200];
          oldFFTvalues[2] = obsArray[1000];

          // Add Sound Magnitudes to the soundObsVector:
          for (int k = 0; k < 1024; k++) {
            double d = obsArray[67 + k] + obsArray[67 + k + 1024];
            double oldValue = meanSoundMagnitudes.getEntry(k);
            meanSoundMagnitudes.setEntry(k, d + oldValue);
          }
        }
        robot.sendAction(naoAct);
        clock.tick();
      }
    }


    // Calculate 1st and 2nd order Moments of meanSoundMagnitudes:
    mean = MathUtilsCD.expectation(meanSoundMagnitudes);
    variance = MathUtilsCD.variance(meanSoundMagnitudes, mean);


    // get new cam motion value:
    cameraMotion = robot.getMotion();

    reward = cameraMotion;

    // calculate sound energy for all channels:
    soundEnergy = 0.0;
    for (int n1 = 0; n1 < 3072; n1++) {
      soundEnergy += obsArray[67 + n1] * obsArray[67 + n1];
    }
    soundEnergy /= (3072 * 10000000000.0);


    if (cameraMotion < 3.0 && soundEnergy > 16) {
      reward = -1.0;
    } else {
      reward = 1.0;
      if (oldAction != action.actions[0])
        reward = 0.0;
    }

    oldAction = action.actions[0];

    // Set the observations to the observation vector:
    obsVector.setEntry(0, headPositionRange.bound(action.actions[0]));
    obsVector.setEntry(1, cameraMotionRange.bound(cameraMotion) - 0.00001);
    obsVector.setEntry(2, soundFrequencyRange.bound(mean) - 0.00001);
    obsVector.setEntry(3, soundVarianceRange.bound(variance) - 0.00001);

    System.out.print("bounded mean freq + variance: " + (soundFrequencyRange.bound(mean) - 0.00001) + "  "
        + (soundVarianceRange.bound(variance) - 0.00001) + "\n");
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

  // private void updateHeadMotion(double alpha) {
  // oldHeadPosition = currentHeadPosition;
  // currentHeadPosition = obsArray[12];
  // headDifference = currentHeadPosition - oldHeadPosition;
  //
  // headMotion = headMotion + alpha * (headDifference - headMotion);
  // }

  public PVector getObs() {
    return obsVector.copy();
  }

  public double getReward() {
    return reward;
  }

  public Range[] getObservationRanges() {
    // soundEnergyRange = new Range(0.0, 30.0);
    cameraMotionRange = new Range(0.0, 6.0);
    headPositionRange = new Range(0.39, 0.61);
    soundFrequencyRange = new Range(130.0, 205.0);
    soundVarianceRange = new Range(59000.0, 140000.0);
    // Range headMotionRange = new Range(-0.06, 0.06);

    Range[] outRanges = new Range[nbOfObs];

    outRanges[0] = headPositionRange;
    outRanges[1] = cameraMotionRange;
    outRanges[2] = soundFrequencyRange;
    outRanges[3] = soundVarianceRange;
    return outRanges;
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
