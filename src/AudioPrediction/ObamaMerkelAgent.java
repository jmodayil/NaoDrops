package AudioPrediction;

import java.util.Random;

import rltoys.algorithms.learning.control.acting.EpsilonGreedy;
import rltoys.algorithms.learning.control.acting.Greedy;
import rltoys.algorithms.learning.control.qlearning.QLearning;
import rltoys.algorithms.learning.control.qlearning.QLearningControl;
import rltoys.algorithms.representations.acting.Policy;
import rltoys.algorithms.representations.actions.Action;
import rltoys.algorithms.representations.actions.TabularAction;
import rltoys.algorithms.representations.tilescoding.TileCodersNoHashing;
import rltoys.algorithms.representations.traces.ATraces;
import rltoys.math.ranges.Range;
import rltoys.math.vector.BinaryVector;
import rltoys.math.vector.RealVector;
import rltoys.math.vector.implementations.PVector;
import zephyr.plugin.core.api.monitoring.annotations.Monitor;

@Monitor
public class ObamaMerkelAgent {
  private final TileCodersNoHashing tileCoders;
  private final TabularAction toStateAction;
  private final QLearning learning;
  private QLearningControl control;
  private double epsilon;
  private Policy acting;
  private final Action[] possibleActions;

  public ObamaMerkelAgent(Range[] obsRanges, Action[] possibleActions) {
    // Initialize tilecoder
    this.possibleActions = possibleActions;
    tileCoders = new TileCodersNoHashing(obsRanges);
    tileCoders.addFullTilings(2, 1);

    // Associate actions and states...
    toStateAction = new TabularAction(possibleActions, tileCoders.vectorSize());
    // Set parameters for Sarsa
    double alpha = .1 / tileCoders.vectorNorm();
    double gamma = 0.0;
    double lambda = 0.0;
    // Initialize Sarsa Algorithm:
    // learning = new Sarsa(alpha, gamma, lambda, toStateAction.vectorSize());
    learning = new QLearning(possibleActions, alpha, gamma, lambda, toStateAction, toStateAction.vectorSize(),
                             new ATraces());
    epsilon = 0.33;
    // Use epsilon-greedy policy:
    acting = new EpsilonGreedy(new Random(0), possibleActions, toStateAction, learning, epsilon);
    // Initialize the sarsa control algorithm:
    // control = new SarsaControl(acting, toStateAction, learning);
    control = new QLearningControl(acting, learning);

    System.out.println("tostateaction vectorsize:  " + toStateAction.vectorSize());
  }

  public Action step(RealVector s_t, Action a_t, RealVector s_tp1, double r_tp1) {
    epsilon = epsilon * 0.99;
    System.out.print("Epsilon: " + epsilon + "\n");
    boolean changed = false;
    if (epsilon < 0.005 && changed == false) {
      System.out.println("Change policy to greedy policy!");
      acting = new Greedy(learning, possibleActions, toStateAction);
      control = new QLearningControl(acting, learning);
    }
    return this.control.step(s_t, a_t, s_tp1, r_tp1);
  }

  public BinaryVector project(double[] inputs) {
    return this.tileCoders.project(inputs);
  }

  public void inspect(BinaryVector x_tp1) {
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
}
