package AudioPrediction;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.apache.commons.lang3.ArrayUtils;

import nao.CDArrays;
import nao.NaoAction;
import nao.NaoRobot;
import nao.RewardFromCardGenerator;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

import rltoys.algorithms.representations.actions.Action;
import rltoys.environments.envio.actions.ActionArray;
import rltoys.math.ranges.Range;
import rltoys.math.vector.implementations.PVector;
import zephyr.plugin.core.api.monitoring.annotations.Monitor;
import zephyr.plugin.core.api.synchronization.Clock;

@Monitor
public class TableProblem {
  // Actions
  protected static final ActionArray A4 = new ActionArray(0);
  protected static final ActionArray A3 = new ActionArray(1);
  protected static final ActionArray A2 = new ActionArray(2);
  protected static final ActionArray A1 = new ActionArray(3);

  protected static final Action[] possibleActions = {A4,A3,A2,A1}; //,A3,A2, A1,
//  double[] truePositions = {-0.8284019827842712,0.004560038447380066,0.8298520445823669}; // For .3 .5 .7
//  double[] truePositions = {-0.4126879572868347,0.00762803852558136,0.628898024559021, 0.0}; -0.6182439923286438
  double[] truePositions = {-0.40808597207069397,-0.13043196499347687,0.17176604270935059, 0.45402204990386963};
  double[] rawActions = {.40, .47, .54, .61};
  BufferVector pastAction = new BufferVector(1,1);

  int nbOfObs = 1 + 1 + 1 + 14; //HeadMotion, LastAction, Reward

  // Robot
  private double[] leds = new double[83];
  private final NaoRobot robot;
  private final NaoAction naoAct = new NaoAction();
  private final double[] joints = new double[14];
  double formerJoint = 0.0;
  private final double[] stiffness = new double[14];
  private final double maxVel = 0.4;

  // Zephyr Clock
  private final Clock clock;

  // Raw Observations
  double[] obsArray;
  BufferedImage currentImage;
  // observation OUTPUT to agent:
  private final PVector obsVector = new PVector(nbOfObs);
  
  double headMotion = 0;
  
  
  RewardFromCardGenerator rewardGen = new RewardFromCardGenerator();
  private double reward = 0.0;
  
  
  //Ranges
  Range headMotionRange = new Range(-0.01,1.01);
  Range pastActionRange = new Range(-0.01,3.01);
  Range rewardRange = new Range(-0.01,1.01);
  Range[] soundFeatureRange = new Range[14];
  
  
  //Performance Measure:
  private int step = 0;
  int totalSteps = 0;
  double[] returnT = null;
  double[] rewardArray = null;
  double gamma;
  
  //MFCC Provider:
  private MFCCProvider mfccProc;

  public TableProblem(NaoRobot R, Clock clock, int totalSteps, double gamma) {
    this.robot = R;
    this.clock = clock;
    this.totalSteps = totalSteps;
    this.returnT = new double[totalSteps];
    this.rewardArray = new double[totalSteps];
    this.gamma = gamma;
    
    //Initialize the MFCC Provider:
    this.mfccProc = new MFCCProvider();
    
    joints[0] = .5;
    stiffness[0] = 0.4;
    naoAct.set(joints, maxVel, stiffness, null, null);
    robot.sendAction(naoAct);
    
    //Initialize all Variables etc.
    obsArray = robot.waitNewObs();
    
//    soundFeatureRange[0] = new Range(21.06, 24.16);
//    soundFeatureRange[1] = new Range(531.4, 616.19);
//    soundFeatureRange[2] = new Range(24.6, 87.18);
//    soundFeatureRange[3] = new Range(-16.32, 27.28);
//    soundFeatureRange[4] = new Range(-10.45, 32.0);
//    soundFeatureRange[5] = new Range(-20.18, 8.27);
//    soundFeatureRange[6] = new Range(-25.89, 8.3);
//    soundFeatureRange[7] = new Range(-19.31, 13.36);
//    soundFeatureRange[8] = new Range(-11.76, 11.65);
//    soundFeatureRange[9] = new Range(-8.54, 13.78);
//    soundFeatureRange[10] = new Range(-8.44, 14.79);
//    soundFeatureRange[11] = new Range(-11.54, 10.56);
//    soundFeatureRange[12] = new Range(-8.29, 5.78);
//    soundFeatureRange[13] = new Range(-14.24, 6.42);
    soundFeatureRange[0] = new Range(22.9547,25.9159);
    soundFeatureRange[1] = new Range(581.3397,637.9171);
    soundFeatureRange[2] = new Range(63.2995,85.4238);
    soundFeatureRange[3] = new Range(-24.9805,6.2582);
    soundFeatureRange[4] = new Range(-14.6902,4.8118);
    soundFeatureRange[5] = new Range(-11.2709, 2.2149);
    soundFeatureRange[6] = new Range(-8.9603,4.8100);
    soundFeatureRange[7] = new Range(-20.3622, -8.2914);
    soundFeatureRange[8] = new Range(1.7313,13.0658);
    soundFeatureRange[9] = new Range(-3.7998,7.7916);
    soundFeatureRange[10] = new Range(-5.5061,4.9647);
    soundFeatureRange[11] = new Range(-3.0026,5.9516);
    soundFeatureRange[12] = new Range(-5.1040,3.0828);
    soundFeatureRange[13] = new Range(-8.2047,0.0168);
  }

  public void step(ActionArray action) throws IllegalArgumentException, IOException {
    if (clock.isTerminated()) {
      System.out.println("CLOCK IS TERMINATED!");
      // Check whether the clock is still active...
      return;
    }
    
    step++;
    
    // Send the desired action to the robot:
    formerJoint = joints[0];
    joints[0] = rawActions[(int) action.actions[0]];
    naoAct.set(joints, maxVel, stiffness, leds, null);
    robot.sendAction(naoAct);
    obsArray = robot.waitNewObs();
    
	if(Math.abs(obsArray[12]-truePositions[(int) action.actions[0]]) < 0.03) {
		headMotion = 0;
	}
	else {
		headMotion = 1;
	}
	pastAction.push((int) action.actions[0]);
    currentImage = robot.getImage();
	

    calculateReward();
    leds = leds();

    setOutputObsVector();
    
//    System.out.println("Motion: " + obsVector.getEntry(0) + "  At-1: " + obsVector.getEntry(1) + "  At-2: " + obsVector.getEntry(2) + "  Reward t:" + obsVector.getEntry(3));
  }

  public double getPercentageReward1() {
	// TODO Auto-generated method stub
	  return (double) CDArrays.sum(rewardArray) / (double) step;
  }
  
  public double[] getReturns() {
	  returnT[totalSteps-1] = 0.0;
	  for (int n = totalSteps-2; n >-1; n--) {
		  returnT[n] = rewardArray[n+1] + gamma*returnT[n+1];
	  }
	  return returnT;
  }
  

  private void setOutputObsVector() throws IOException {
	  obsVector.setEntry(0, headMotionRange.bound(headMotion));
	  obsVector.setEntry(1, pastActionRange.bound(pastAction.get(0)));
	  obsVector.setEntry(2, rewardRange.bound(reward));
	  
	  double[] frame;
	  double[] mfccs;
	  double value;
    // Calculate the MFCCs:
    frame = ArrayUtils.subarray(obsArray, 83, obsArray.length);
    // System.out.println("Length of Frame: " + frame.length);
    mfccs = mfccProc.getMeanMfccVector(frame);
    // MFCC Calculation Done.

	  for (int n = 0; n < 14; n++) {
		  value = soundFeatureRange[n].bound(mfccs[n]);
//		  value = mfccs[n];
//		  if (value != mfccs[n]){ System.out.println("BOUNDING # " + n); }
		  if (mfccs[n] >= value ) {
			  obsVector.setEntry(n+3, value-0.00001);
		  }
		  else{
			  obsVector.setEntry(n+3, value);
		  }
	  }
  }

  private void calculateReward() {
	  reward = rewardGen.getReward(currentImage) > 0.99 ? 1:0;
	  if (headMotion > 0.99) {
//		  System.out.println("Motion: " + headMotion);
		  reward = 0.0;
	  }
	  else {
//		  System.out.println("Motion: " + headMotion);
	  }
	  rewardArray[step-1] = reward;
	  
  }

  private double[] leds() {
    // Light LEDs of Nao according to reward:
    if (reward == 1.0) {
      leds = NaoAction.setFaceLeds("green");
    } else if (reward < 1.0 && reward > -1.0) {
      leds = NaoAction.setFaceLeds("blue");
    } else if (reward == -1.0){
      leds = NaoAction.setFaceLeds("red");
    }
    else {
    	System.out.println("ERROR IN LEDS() FUNCTION!");
    	return null;
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
	Range[] outRanges = new Range[nbOfObs];
    
	outRanges[0] = headMotionRange;
	outRanges[1] = pastActionRange;
	outRanges[2] = rewardRange;
    
    for (int n = 0; n < 14; n++) {
    	outRanges[n+3] = soundFeatureRange[n];
    }
    return outRanges;
  }

  public void releaseRobot() {
    // Release the robot's stiffness...
    System.out.println("releasing the robot...");
    stiffness[0] = 0.0;
    joints[0] = 0.5;
    naoAct.set(joints, 0.0, stiffness, null, null);
    robot.sendAction(naoAct);
    robot.waitNewObs();
    naoAct.set(joints, 0.0, stiffness, null, null);
    robot.sendAction(naoAct);
    robot.waitNewObs();
    naoAct.set(joints, 0.0, stiffness, null, null);
    robot.sendAction(naoAct);
    robot.waitNewObs();

    System.out.println("Robot is released...");
  }

  public Action[] getPossibleActions() {
    return possibleActions;
  }
}
