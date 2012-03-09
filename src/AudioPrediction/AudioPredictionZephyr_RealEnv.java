package AudioPrediction;

import java.io.FileWriter;
import java.io.IOException;

import nao.NaoRobot;
import rltoys.algorithms.learning.predictions.LearningAlgorithm;
import rltoys.environments.envio.actions.ActionArray;
import rltoys.math.vector.RealVector;
import rltoys.math.vector.implementations.PVector;
import zephyr.plugin.core.api.Zephyr;
import zephyr.plugin.core.api.monitoring.annotations.Monitor;
import zephyr.plugin.core.api.synchronization.Clock;

@Monitor
public class AudioPredictionZephyr_RealEnv implements Runnable {
  @Monitor
  private final NaoRobot robot;
  @Monitor
  private TableProblem problem;
  @Monitor
  private final TableAgent agent;

  private final Clock clock = new Clock();


  // Variables for the main loop:
  ActionArray a_tp1;
  ActionArray a_t;
  PVector o_tp1;
  PVector o_art;
  double r_tp1;
  RealVector x_t;
  int totalSteps;
  
  private PVector suppressAudioVector;
  
  FileWriter soundFeaturesFile = new FileWriter("observedHeadphoneFeatures.txt");

  public AudioPredictionZephyr_RealEnv() throws IllegalArgumentException, IOException, ClassNotFoundException {
    System.out.println("Initializing the Runnable class...");

    robot = new NaoRobot();
    totalSteps = 2823;
    problem = new TableProblem(robot, clock, totalSteps, 0.9);
    agent = new TableAgent(problem.getObservationRanges(), problem.getPossibleActions(),getInitializedThetaVector());
    Zephyr.advertise(clock, this);
    
    robot.waitNewObs();

    // Initialize the main loop variables:
    a_tp1 = (ActionArray) problem.getPossibleActions()[1];
    a_t = a_tp1;
    
    suppressAudioVector=getSuppressAudioVector();
    o_art = new PVector(17);
  }

  @Override
  public void run() {
    System.out.println("Calling the run() method of AudioPredictionZephyr class");
    int step = 0;
    // run the problem:
//    agent.greedify();
    try {
		Thread.sleep(5000);
	} catch (InterruptedException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
    while (!this.robot.isClosed() && !clock.isTerminated() && step < totalSteps) {
      clock.tick(); // CD: observes all variables for zephyr plot function
      
      try {
		problem.step(a_tp1);
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
      
      //Display the soundFeatures for checking:
      for (int n = 0; n < 14; n++) {
    	  try {
			soundFeaturesFile.append(o_tp1.getEntry(n+3) + ",");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      }
      try {
		soundFeaturesFile.append("\n");
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
      System.out.println();
      
      RealVector x_tp1 = agent.project(o_tp1.accessData());
      
      //Suppress AUdio Data for the moment...
      if (false) {
    	  x_tp1 = (RealVector) x_tp1.ebeMultiply(suppressAudioVector);
      }
//	  agent.inspect(x_tp1);
      a_tp1 = (ActionArray) agent.step(x_t, a_t, x_tp1, r_tp1,o_tp1.getEntry(0));
//      robot.updateShowCurrentImage(a_tp1.actions[0]);
      if (r_tp1 > 0.5) {
    	  //Update other states as well. Construct artificial observation vector:
    	  for (int n = 0; n < problem.getPossibleActions().length; n++) {
    		  o_art.set(o_tp1);
    		  if ((int) o_tp1.getEntry(1) != n) {
//    			  System.out.println(n + " is eligible for update, cause pastAction is " + o_tp1.getEntry(1));
    			  o_art.setEntry(0, 0.0);
    			  o_art.setEntry(1, n);
    			  o_art.setEntry(2, 0.0);
    			  RealVector x = agent.project(o_art.accessData());
    			  agent.updateSingleState(x, 4.0, a_t);
//    			  System.out.println("For State " + n + " I am upvoting the action to move to " + a_t.actions[0]);
    		  }
    	  }
      }
      
      x_t = x_tp1;
      step++;
      System.out.println("Average Reward:   " + agent.getAverageReward());
      System.out.println("PercentageCorrect: " + problem.getPercentageReward1());
    }
//	  System.out.println("PercentageCorrect: " + problem.getPercentageReward1());
	  double[] returns = problem.getReturns();
	  for (int n = 0; n < totalSteps; n++) {
		  System.out.println(returns[n]);
	  }
	  try {
		agent.saveSarsa("SarsaTrainedDoro.sarsa");
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	  try {
		soundFeaturesFile.close();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    System.out.println("Release the robot's stiffness:");
    problem.releaseRobot();
  }
  
  public static PVector getInitializedThetaVector() {
	  PVector theta = new PVector(753);
	  for (int n = 0; n < 753; n++) {
		  theta.setEntry(0, 0.0);
	  }
	  int mot = 1;
	  int pst = 3*1;
	  int rew = 3*3*1;
	  int act = 251;
	  
	  for (int action = 0; action < 3; action++) {
		  for (int reward = 0; reward < 3; reward++) {
			  int entry = action*act + reward*rew + action*pst + 2*mot;
			  theta.setEntry(entry, 1.0);
		  }
	  }
	  
	  for (int action = 0; action < 3; action++) {
		  int entry = action*act + 2*rew + action*pst + 0*mot;
		  theta.setEntry(entry, 1.0);
	  }
	  
	  for (int action = 0; action < 3; action++) {
		  for (int pastAction = 0; pastAction < 3; pastAction++) {
			  if (action != pastAction) {
				  int entry = action*act + 0*rew + pastAction*pst + 0*mot;
				  theta.setEntry(entry, 1.0);
			  }
		  }
	  }
	  return theta;
  }


	public static PVector getSuppressAudioVector() {
		  PVector theta = new PVector(753);
		  for (int n = 0; n < 753; n++) {
			  theta.setEntry(0, 0.0);
		  }
		  for (int n = 0; n < 3; n++) {
			  for (int m = 0; m < 27; m++) {
				  theta.setEntry(n*251+m, 1.0);
			  }
		  }
		  return theta;
	}
}