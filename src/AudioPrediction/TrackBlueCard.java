package AudioPrediction;

import java.io.IOException;
import java.util.Random;

import nao.NaoAction;
import nao.NaoRobot;
import nao.RewardFromCardGenerator;

import org.apache.commons.lang3.ArrayUtils;

import rltoys.algorithms.learning.control.acting.EpsilonGreedy;
import rltoys.algorithms.learning.control.sarsa.Sarsa;
import rltoys.algorithms.learning.control.sarsa.SarsaControl;
import rltoys.algorithms.representations.acting.Policy;
import rltoys.algorithms.representations.actions.Action;
import rltoys.algorithms.representations.actions.TabularAction;
import rltoys.algorithms.representations.tilescoding.TileCodersNoHashing;
import rltoys.environments.envio.actions.ActionArray;
import rltoys.math.ranges.Range;
import rltoys.math.vector.BinaryVector;
import rltoys.math.vector.RealVector;
import rltoys.math.vector.implementations.PVector;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class TrackBlueCard {

  /**
   * @param args
   * @throws IOException
   * @throws IllegalArgumentException
 * @throws InterruptedException 
   */
  public static void main(String[] args) throws IllegalArgumentException, IOException, InterruptedException {
    // TODO Auto-generated method stub
	  //Actions: 
    final ActionArray LEFT = new ActionArray(0.01,0.0);
    final ActionArray RIGHT = new ActionArray(-0.01,0.0);
    final ActionArray UP = new ActionArray(0.0,0.01);
    final ActionArray DOWN = new ActionArray(0.0,-0.01);
    final ActionArray REST = new ActionArray(0.0,0.0);
    final Action[] possibleActions = { LEFT, RIGHT, UP, DOWN, REST};
	  
    // AGENT STUFF:
	TileCodersNoHashing tileCoders;
	TabularAction toStateAction;
	Sarsa learning;
	SarsaControl control;
	double epsilon = 0.1;
	Policy acting;
	double alpha, gamma, lambda;
	  
	Range[] ranges = new Range[2];
	ranges[0] = new Range(58.0,245.0);
	ranges[1] = new Range(90.0,170.0);
	tileCoders = new TileCodersNoHashing(ranges);
	tileCoders.addIndependentTilings(20, 2);
	tileCoders.includeActiveFeature();
	
	toStateAction = new TabularAction(possibleActions, tileCoders.vectorNorm(), tileCoders.vectorSize());
	System.out.println("VectorNorm of TileCoder (is supposed to be the number of active Features): "
	        + tileCoders.vectorNorm());
	    System.out.println("VectorSize of Tilecoder: " + tileCoders.vectorSize());
	
    alpha = .1 / tileCoders.vectorNorm();
    gamma = 0.95;
    lambda = 0.4;
	
    learning = new Sarsa(alpha, gamma, lambda, toStateAction.vectorSize());
    
    acting = new EpsilonGreedy(new Random(0), possibleActions, toStateAction, learning, epsilon);
    
    control = new SarsaControl(acting, toStateAction, learning);
    
    ActionArray a_tp1 = REST;
    ActionArray a_t = REST;
    PVector o_tp1;

    RealVector x_t;
	  
	  //ROBOT STUFF:
	NaoRobot robot = new NaoRobot();
//    double[] obsArray;
    
    // REWARD STUFF:
    RewardFromCardGenerator rewardGen = new RewardFromCardGenerator();
    double r_tp1;
    
    double[] stiffness = new double[14];
    stiffness[0] = 0.8;
    stiffness[1] = 0.8;
    double[] joints = new double[14];
    joints[0] = .5;
    joints[1] = .45;
    
    NaoAction action = new NaoAction();
    action.set(joints, 0.6, stiffness, null, null);
    robot.sendAction(action);
    Thread.sleep(5000);
    
    
    o_tp1 = new PVector(160.0,120.0);
    x_t = tileCoders.project(o_tp1.accessData());
    double[] xy;
    // Wait for the desired number of Steps and record the sound Data:
    while (true) {
      // Wait for new sound Data:

      robot.waitNewObs();
      

      r_tp1 = rewardGen.getRewardForBlueTracking(IplImage.createFrom(robot.getImage()));
      xy = rewardGen.getXYcoordinates(IplImage.createFrom(robot.getImage()));
      o_tp1.setEntry(0, ranges[0].bound(xy[0]) - 0.001);
      o_tp1.setEntry(1, ranges[1].bound(xy[1]) - 0.001);

      BinaryVector x_tp1 = tileCoders.project(o_tp1.accessData());
      
      a_tp1 = (ActionArray) control.step(x_t, a_t, x_tp1, r_tp1);
      x_t = x_tp1;
      a_t = a_tp1;
      
      //Perform Action:
      joints[0] += a_tp1.actions[0];
      joints[1] += a_tp1.actions[1];
      
      if (joints[0] < 0.45) {
    	  joints[0] = 0.45;
      }
      if (joints[0] > 0.55) {
    	  joints[0] = 0.55;
      }
      if (joints[1] < 0.4) {
    	  joints[1] = 0.4;
      }
      if (joints[1] > 0.52) {
    	  joints[1] = 0.52;
      }
      
      action.set(joints, 0.6, stiffness, null, null);
      robot.sendAction(action);
      
      System.out.println("Reward: " + r_tp1 + " CurrentAction: " + a_tp1 + " Observation: " + o_tp1.getEntry(0) + " " + o_tp1.getEntry(1) + " Joints: " + joints[0] + " " + joints[1]);
      
      
      
      }
  }
}