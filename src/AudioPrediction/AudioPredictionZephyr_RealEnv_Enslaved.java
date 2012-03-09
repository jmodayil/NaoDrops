package AudioPrediction;

import java.io.IOException;

import nao.NaoRobot;
import rltoys.algorithms.learning.predictions.LearningAlgorithm;
import rltoys.environments.envio.actions.ActionArray;
import rltoys.math.vector.RealVector;
import rltoys.math.vector.implementations.PVector;
import web.ExternalAction;
import zephyr.plugin.core.api.Zephyr;
import zephyr.plugin.core.api.monitoring.annotations.Monitor;
import zephyr.plugin.core.api.synchronization.Clock;

@Monitor
public class AudioPredictionZephyr_RealEnv_Enslaved implements Runnable {
  @Monitor
  private final NaoRobot robot;
  @Monitor
  private TableProblem problem;
  
  private ExternalAction actionGenerator;

  private final Clock clock = new Clock();


  // Variables for the main loop:
  ActionArray a_tp1;
  
  int totalSteps;

  public AudioPredictionZephyr_RealEnv_Enslaved() throws IllegalArgumentException, IOException, ClassNotFoundException {
    System.out.println("Initializing the Runnable class...");

    robot = new NaoRobot();
    totalSteps = 2823;
    problem = new TableProblem(robot, clock, totalSteps, 0.9);
    Zephyr.advertise(clock, this);
    
    this.actionGenerator = new ExternalAction();
    robot.waitNewObs();

    // Initialize the main loop variables:
    a_tp1 = (ActionArray) problem.getPossibleActions()[1];
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
      
      a_tp1 = (ActionArray) problem.getPossibleActions()[actionGenerator.getAction()];
      
      step++;
      System.out.println("PercentageCorrect: " + problem.getPercentageReward1());
    }
//	  System.out.println("PercentageCorrect: " + problem.getPercentageReward1());
	  double[] returns = problem.getReturns();
	  for (int n = 0; n < totalSteps; n++) {
		  System.out.println(returns[n]);
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