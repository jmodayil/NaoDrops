package AudioPrediction;

import java.util.Random;

import rltoys.algorithms.learning.control.acting.EpsilonGreedy;
import rltoys.algorithms.learning.control.sarsa.Sarsa;
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
public class ObamaMerkelAgent {
  private final TileCodersNoHashing tileCoders;
  private final TabularAction toStateAction;
  private final Sarsa learning;
  private final SarsaControl control;
  private final double epsilon;
  private final Policy acting;
  private final Action[] possibleActions;
  double alpha, gamma, lambda;


  public ObamaMerkelAgent(Range[] obsRanges, Action[] possibleActions) {
    // Initialize tilecoder
    this.possibleActions = possibleActions;
    tileCoders = new TileCodersNoHashing(obsRanges);
    tileCoders.addIndependentTilings(4, 4);
    tileCoders.includeActiveFeature();

    // Associate actions and states...
    toStateAction = new TabularAction(possibleActions, tileCoders.vectorNorm(), tileCoders.vectorSize());
    // Set parameters for Sarsa
    System.out.println("VectorNorm of TileCoder (is supposed to be the number of active Features): "
        + tileCoders.vectorNorm());
    System.out.println("VectorSize of Tilecoder: " + tileCoders.vectorSize());
    alpha = .1 / tileCoders.vectorNorm();
    gamma = 0.2;
    lambda = 0.2;


    // Initialize Sarsa Algorithm:
    // learning = new Sarsa(alpha, gamma, lambda, toStateAction.vectorSize());
    learning = new Sarsa(alpha, gamma, lambda, toStateAction.vectorSize());
    epsilon = 0.1;

    // Use epsilon-greedy policy:
    acting = new EpsilonGreedy(new Random(0), possibleActions, toStateAction, learning, epsilon);


    // Initialize the sarsa control algorithm:
    // control = new SarsaControl(acting, toStateAction, learning);
    control = new SarsaControl(acting, toStateAction, learning);

    System.out.println("tostateaction vectorsize:  " + toStateAction.vectorSize());
  }

  public Action step(RealVector s_t, Action a_t, RealVector s_tp1, double r_tp1) {
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
