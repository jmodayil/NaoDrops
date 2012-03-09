package AudioPrediction;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;

import rltoys.algorithms.learning.control.AverageReward;
import rltoys.algorithms.learning.control.acting.EpsilonGreedy;
import rltoys.algorithms.learning.control.acting.Greedy;
import rltoys.algorithms.learning.control.sarsa.SarsaControl;
import rltoys.algorithms.representations.acting.Policy;
import rltoys.algorithms.representations.actions.Action;
import rltoys.algorithms.representations.actions.TabularAction;
import rltoys.algorithms.representations.tilescoding.TileCodersNoHashing;
import rltoys.math.ranges.Range;
import rltoys.math.vector.BinaryVector;
import rltoys.math.vector.RealVector;
import rltoys.math.vector.implementations.PVector;
import zephyr.plugin.core.api.monitoring.annotations.Monitor;

@Monitor
public class TableAgent {
  private TileCodersNoHashing tileCoders;
  public TabularAction toStateAction;
  public final SarsaInitialized learning;
  private SarsaControl control;
  private Policy acting;
  private Action[] possibleActions;
  double alpha, gamma, lambda, epsilon;
  RealVector xa_t;
  
  private AverageRewardInitialized rAverage;
  
  
  private boolean agentIsGreedy = false;


  public TableAgent(Range[] obsRanges, Action[] possibleActions) {
	  this.initialize(obsRanges, possibleActions);
    // Initialize Sarsa Algorithm:
    // learning = new Sarsa(alpha, gamma, lambda, toStateAction.vectorSize());
    learning = new SarsaInitialized(alpha, gamma, lambda, toStateAction.vectorSize());

    // Use epsilon-greedy policy:
    acting = new EpsilonGreedy(new Random(0), possibleActions, toStateAction, learning, epsilon);
  }
  
  public TableAgent(Range[] obsRanges, Action[] possibleActions, PVector initialTheta) {
	// Initialize tilecoder
	this.initialize(obsRanges, possibleActions);
	
	// Initialize Sarsa Algorithm:
	// learning = new Sarsa(alpha, gamma, lambda, toStateAction.vectorSize());
	learning = new SarsaInitialized(alpha, gamma, lambda, toStateAction.vectorSize(),initialTheta);
	
	// Use epsilon-greedy policy:
	acting = new EpsilonGreedy(new Random(0), possibleActions, toStateAction, learning, epsilon);
	
	  }
  
  public TableAgent(Range[] obsRanges, Action[] possibleActions, String sarsaLearning) throws IOException, ClassNotFoundException {
	  this.initialize(obsRanges, possibleActions);
	  
	    // Copy the Sarsa Learning from existing file:
	    FileInputStream fis = new FileInputStream(sarsaLearning);
	    ObjectInputStream in = new ObjectInputStream(fis);
	    learning = (SarsaInitialized) in.readObject();
	    in.close();
	    System.out.println("Sucessfully loaded sarsa learner from file...");
	    // Use epsilon-greedy policy:
	    acting = new EpsilonGreedy(new Random(0), possibleActions, toStateAction, learning, epsilon);
}
  
  private void initialize(Range[] obsRanges, Action[] possibleActions) {
    this.possibleActions = possibleActions;
    this.rAverage = new AverageRewardInitialized(.004, .7);
    
    
    tileCoders = new TileCodersNoHashing(obsRanges);
    int[] jointlyIndexes = {0, 1 , 2};
    tileCoders.addTileCoder(jointlyIndexes, 3, 1);
    System.out.println("before: " + tileCoders.vectorSize());
    int[] index = new int[1];
    for (int n = 3; n < 17; n++) {
    	index[0] = n;
    	tileCoders.addTileCoder(index, 4, 4);
    }
    
    // Associate actions and states...
    toStateAction = new TabularAction(possibleActions, tileCoders.vectorNorm(), tileCoders.vectorSize());
    // Set parameters for Sarsa
    System.out.println("VectorNorm of TileCoder (is supposed to be the number of active Features): "
        + tileCoders.vectorNorm());
    System.out.println("VectorSize of Tilecoder: " + tileCoders.vectorSize());
    
    alpha = .1 / tileCoders.vectorNorm();
//    alpha = 0.0;
//    alpha = .1;
    gamma = 1.0;
    lambda = 0.95;
    epsilon = 0.3;
}

  public Action step(RealVector x_t, Action a_t, RealVector x_tp1, double r_tp1, double motion) {
	  Action a_tp1;
	  if (x_t == null)
        xa_t = null;
	  if (r_tp1 < 0.5 && motion < 0.5) {
		  a_tp1 = acting.decide(x_tp1);
		  if (a_tp1 != ((EpsilonGreedy) acting).computeBestAction(x_tp1)) {
//			  System.out.println("ACTUALLY EPSILONGREEDY!");
		  }
	  }
	  else {
	      a_tp1 = ((EpsilonGreedy) acting).computeBestAction(x_tp1);
//	      System.out.println("Greedy");
	  }
	  double curAv = rAverage.average(r_tp1);
      RealVector xa_tp1 = toStateAction.stateAction(x_tp1, a_tp1);
      learning.update(xa_t, xa_tp1, curAv);
      System.out.println("Rdiff:        " + curAv);
      xa_t = xa_tp1;
      return a_tp1;
  }
  
  public void updateSingleState(RealVector x, double z, Action a) {
	  RealVector xa = toStateAction.stateAction(x, a);
	  learning.updateSingleState(xa, z);
  }

  public BinaryVector project(double[] inputs) {
    return this.tileCoders.project(inputs);
  }

  public void inspect(RealVector x_tp1){
    // TODO Auto-generated method stub
//    int nbOfFeatures = toStateAction.vectorSize();
//    PVector probe = new PVector(nbOfFeatures);
//
//    for (int n = 0; n < nbOfFeatures; n++) {
//      probe.setEntry(n, 1);
//
//      String s = "";
//      if (x_tp1.getEntry(n) != 0) {
//        s = "*";
//      }
//      System.out.println(learning.predict(probe) + "   " + n + s);
//      probe.setEntry(n, 0);
//	  PVector testObs = new PVector(17);
//	  testObs.setEntry(0, 0.0);
//	  testObs.setEntry(1, 1);
//	  testObs.setEntry(2, 0);
//	  System.out.println("Values for the three actions: " + learning.predict(toStateAction.stateAction(this.project(testObs.accessData()), possibleActions[0])) + "  " + learning.predict(toStateAction.stateAction(this.project(testObs.accessData()), possibleActions[1])) + "  " + learning.predict(toStateAction.stateAction(this.project(testObs.accessData()), possibleActions[2])));
	  PrintActionValueTable.printTable(learning, toStateAction, tileCoders, possibleActions, x_tp1);
  }


  

//  public void greedify() {
//	  System.out.println("GREEDIFY!");
//    acting = new Greedy(learning, possibleActions, toStateAction);
//    control = new SarsaControl(acting, toStateAction, learning);
//    this.agentIsGreedy = true;
//    // TODO Auto-generated method stub
//  }
//  
//  public void unGreedify() {
//	  System.out.println("UNGREEDIFY!");
//	  acting = new EpsilonGreedy(new Random(0), possibleActions, toStateAction, learning, epsilon);
//	  control = new SarsaControl(acting, toStateAction, learning);
//	  this.agentIsGreedy = false;
//  }
  
  public void saveSarsa(String filename) throws IOException {
	FileOutputStream fos = new FileOutputStream(filename);
	ObjectOutputStream out = new ObjectOutputStream(fos);
	out.writeObject(learning);
	out.close();
	System.out.println("Successfully saved Sarsa Learner");
  }
  
  public double getAverageReward() {
	  return rAverage.getAverage();
  }
  
  public boolean isGreedy() {
	  return agentIsGreedy;
  }
}
