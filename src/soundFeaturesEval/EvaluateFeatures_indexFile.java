package soundFeaturesEval;

import java.util.Random;

import org.apache.commons.lang3.ArrayUtils;

import rltoys.algorithms.learning.control.acting.EpsilonGreedy;
import rltoys.algorithms.learning.control.sarsa.Sarsa;
import rltoys.algorithms.learning.control.sarsa.SarsaControl;
import rltoys.algorithms.representations.actions.Action;
import rltoys.algorithms.representations.actions.TabularAction;
import rltoys.algorithms.representations.tilescoding.TileCodersNoHashing;
import rltoys.environments.envio.actions.ActionArray;
import rltoys.math.ranges.Range;
import rltoys.math.vector.BinaryVector;

public class EvaluateFeatures_indexFile {
  Sarsa learning;
  SarsaControl control;
  EpsilonGreedy policy;
  TabularAction toStateAction;
  TileCodersNoHashing tileCoder;

  final static ActionArray FIRST = new ActionArray(0);
  final static ActionArray SECOND = new ActionArray(1);
  final static ActionArray THIRD = new ActionArray(2);
  final static ActionArray FOURTH = new ActionArray(3);
  final static Action[] Actions = { FIRST, SECOND };

  // Parameters for Learning:
  double gamma = 0.0;
  double epsilon = 0.0;
  double lambda = 0.0;
  double alpha = 0.0;
  int gridResolution = 0;
  int nbOfTilings = 0;

  // training and evaluatin set:
  double[][] training;
  double[][] evaluation;
  int[] trainingIndex;
  int[] evaluationIndex;

  // Variables for learning:
  double[] o_tp1;
  double r_tp1;
  BinaryVector x_tp1;
  BinaryVector x_t;
  ActionArray a_tp1;
  ActionArray a_t;
  int chosenPerson;
  int chosenSample;

  public EvaluateFeatures_indexFile(double[][] training, double[][] evaluation, int[] trainingIndex,
      int[] evaluationIndex, double gamma, double epsilon, double lambda, int gridResolution, int nbOfTilings) {
    // TODO Auto-generated constructor stub
    this.training = training;
    this.evaluation = evaluation;
    this.trainingIndex = trainingIndex;
    this.evaluationIndex = evaluationIndex;
    this.gamma = gamma;
    this.epsilon = epsilon;
    this.lambda = lambda;
    this.gridResolution = gridResolution;
    this.nbOfTilings = nbOfTilings;


    // Find maximum and minimum values amongst both training and evaluation
    // samples:
    double[] minmax = findMinMax(training, evaluation);
    System.out.println();

    System.out.println("Minimum and Maximum: " + minmax[0] + " " + minmax[1]);
    minmax[0] = minmax[0] - 0.0001;
    minmax[1] = minmax[1] + 0.0001;

    // Create a Ranges Array with the size of training[0].length + 1, and fill
    // with the Ranges.
    Range[] ranges = new Range[training[0].length + 1];
    for (int n = 0; n < training[0].length; n++) {
      ranges[n] = new Range(minmax[0], minmax[1]);
    }
    ranges[training[0].length] = new Range(-0.001, 1.001);


    // Initialize the learning framework:
    tileCoder = new TileCodersNoHashing(ranges);
    tileCoder.addIndependentTilings(gridResolution, nbOfTilings);
    tileCoder.includeActiveFeature();

    // Print the tileCoder vectorSize and vectorNorm:
    System.out.println("tilecoder.vectorSize and tilecoder.vectorNorm: " + tileCoder.vectorSize() + " "
        + tileCoder.vectorNorm());

    toStateAction = new TabularAction(Actions, tileCoder.vectorNorm(), tileCoder.vectorSize());

    alpha = .1 / tileCoder.vectorNorm();
    System.out.println("Alpha: " + alpha);


    learning = new Sarsa(alpha, gamma, lambda, toStateAction.vectorSize());
    policy = new EpsilonGreedy(new Random(0), Actions, toStateAction, learning, epsilon);
    control = new SarsaControl(policy, toStateAction, learning);


    // Initialize actions and observations:
    a_tp1 = (ActionArray) Actions[0];
    a_t = (ActionArray) Actions[0];
    o_tp1 = training[0];
    x_tp1 = tileCoder.project(ArrayUtils.add(o_tp1, a_t.actions[0]));
    x_t = tileCoder.project(ArrayUtils.add(o_tp1, a_t.actions[0]));
    chosenPerson = 0;
    chosenSample = 0;
  }

  private double[] findMinMax(double[][] training, double[][] evaluation) {
    double currentMax;
    double currentMin;
    double[] minMax = { Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY };
    // Find max and min of training set:
    for (int step = 0; step < training.length; step++) {
      currentMax = Arrays.max(training[step]);
      minMax[1] = minMax[1] > currentMax ? minMax[1] : currentMax;

      currentMin = Arrays.min(training[step]);
      minMax[0] = minMax[0] < currentMin ? minMax[0] : currentMin;
    }
    for (int step = 0; step < evaluation.length; step++) {
      currentMax = Arrays.max(evaluation[step]);
      minMax[1] = minMax[1] > currentMax ? minMax[1] : currentMax;

      currentMin = Arrays.min(evaluation[step]);
      minMax[0] = minMax[0] < currentMin ? minMax[0] : currentMin;
    }
    return minMax;
  }

  public double evaluate() {
    // TODO Auto-generated method stub
    // Change policy to greedy policy:
    double[] obs;
    BinaryVector projection;
    ActionArray action = new ActionArray(0);
    int correct = 0;
    int totalSteps = 0;


    for (int sample = 0; sample < evaluation.length; sample++) {
      obs = evaluation[sample];

      projection = tileCoder.project(ArrayUtils.add(obs, action.actions[0]));

      action = (ActionArray) policy.computeBestAction(projection);

      if (action.actions[0] == evaluationIndex[sample]) {
        correct++;
      }
      totalSteps++;
    }

    return (correct / (double) totalSteps);
  }

  public void train() {
    // TODO Auto-generated method stub
    // This function ONLY operates on the training set, which is presented to
    // the learning / control algorithm.

    // Perform ITERATIONS of training
    for (int step = 0; step < training.length; step++) {

      if (a_tp1.actions[0] == chosenPerson) {
        r_tp1 = 1;
      } else {
        r_tp1 = -1;
      }

      a_t = a_tp1;

      chosenPerson = trainingIndex[step];

      o_tp1 = training[step];
      x_tp1 = tileCoder.project(ArrayUtils.add(o_tp1, a_t.actions[0]));

      a_tp1 = (ActionArray) control.step(x_t, a_t, x_tp1, r_tp1);

      x_t = x_tp1;
    }
  }


  public int[] getSampleSizes() {
    int[] sampleSizes = new int[4];
    sampleSizes[0] = training.length;
    sampleSizes[1] = training[0].length;
    sampleSizes[2] = evaluation.length;
    sampleSizes[3] = evaluation[0].length;
    return sampleSizes;
  }
}
