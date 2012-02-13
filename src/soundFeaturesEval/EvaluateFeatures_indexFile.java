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
  Range[] optimalRanges;

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


    // Calculate the optimal Ranges:
    optimalRanges = calculateRanges(training, evaluation);
    System.out.println("Length of Ranges Vector: " + optimalRanges.length);
    System.out.println("Values of last rangesthing: " + optimalRanges[training[0].length].max() + " "
        + optimalRanges[training[0].length].min());

    // Initialize the learning framework:
    tileCoder = new TileCodersNoHashing(optimalRanges);
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
      currentMax = CDArrays.max(training[step]);
      minMax[1] = minMax[1] > currentMax ? minMax[1] : currentMax;

      currentMin = CDArrays.min(training[step]);
      minMax[0] = minMax[0] < currentMin ? minMax[0] : currentMin;
    }
    for (int step = 0; step < evaluation.length; step++) {
      currentMax = CDArrays.max(evaluation[step]);
      minMax[1] = minMax[1] > currentMax ? minMax[1] : currentMax;

      currentMin = CDArrays.min(evaluation[step]);
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

  private Range[] calculateRanges(double[][] training, double[][] evaluation) {
    Range[] outRanges = new Range[training[0].length + 1];
    double min;
    double max;


    for (int sample = 0; sample < training[0].length; sample++) {
      // Find min and Max for each sample:
      min = Double.POSITIVE_INFINITY;
      max = Double.NEGATIVE_INFINITY;


      for (int step = 0; step < training.length; step++) {
        min = min < training[step][sample] ? min : training[step][sample];
        max = max > training[step][sample] ? max : training[step][sample];

        min = min < evaluation[step][sample] ? min : evaluation[step][sample];
        max = max > evaluation[step][sample] ? max : evaluation[step][sample];
      }

      max = max + 0.00001;
      min = min - 0.00001;
      outRanges[sample] = new Range(min, max);
    }
    outRanges[training[0].length] = new Range(-0.0001, 1.0001);
    return outRanges;
  }

  public String getSampleSizes() {
    int[] sampleSizes = new int[4];
    sampleSizes[0] = training.length;
    sampleSizes[1] = training[0].length;
    sampleSizes[2] = evaluation.length;
    sampleSizes[3] = evaluation[0].length;


    return new String(sampleSizes[0] + " " + sampleSizes[1] + " " + sampleSizes[2] + " " + sampleSizes[3]);
  }
}
