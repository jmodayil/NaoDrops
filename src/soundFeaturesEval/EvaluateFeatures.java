package soundFeaturesEval;

import java.util.Random;

import rltoys.algorithms.learning.control.acting.EpsilonGreedy;
import rltoys.algorithms.learning.control.sarsa.Sarsa;
import rltoys.algorithms.learning.control.sarsa.SarsaControl;
import rltoys.algorithms.representations.actions.Action;
import rltoys.algorithms.representations.actions.TabularAction;
import rltoys.algorithms.representations.tilescoding.TileCodersNoHashing;
import rltoys.environments.envio.actions.ActionArray;
import rltoys.math.ranges.Range;
import rltoys.math.vector.BinaryVector;

public class EvaluateFeatures {
  Sarsa learning;
  SarsaControl control;
  EpsilonGreedy policy;
  TabularAction toStateAction;
  TileCodersNoHashing tileCoder;

  final static ActionArray FIRST = new ActionArray(0);
  final static ActionArray SECOND = new ActionArray(1);
  final static ActionArray THIRD = new ActionArray(2);
  final static ActionArray FOURTH = new ActionArray(3);
  final static ActionArray FIFTH = new ActionArray(4);
  Action[] Actions = null;
  // Parameters for Learning:
  double gamma = 0.0;
  double epsilon = 0.0;
  double lambda = 0.0;
  double alpha = 0.0;
  int gridResolution = 0;
  int nbOfTilings = 0;

  // training and evaluatin set:
  double[][][] training;
  double[][][] evaluation;

  // Variables for learning:
  double[] o_tp1;
  double r_tp1;
  BinaryVector x_tp1;
  BinaryVector x_t;
  ActionArray a_tp1;
  ActionArray a_t;
  int chosenPerson;
  int chosenSample;

  boolean dependentTilings = false;
  boolean includeActiveFeature = true;
  boolean optimalTiling = false;
  double[] minmax;

  public EvaluateFeatures(double[][][] training, double[][][] evaluation, double gamma, double epsilon, double lambda,
      int gridResolution, int nbOfTilings, boolean dependentTilings, boolean includeActiveFeature, int nbOfSpeakers,
      boolean optimalTiling) {
    // TODO Auto-generated constructor stub
    this.training = training;
    this.evaluation = evaluation;
    this.gamma = gamma;
    this.epsilon = epsilon;
    this.lambda = lambda;
    this.gridResolution = gridResolution;
    this.nbOfTilings = nbOfTilings;
    this.dependentTilings = dependentTilings;
    this.optimalTiling = optimalTiling;
    this.Actions = createActions(nbOfSpeakers);
    System.out.println("Number of Actions: " + this.Actions.length);


    if (optimalTiling) {
      // Find the optimal Ranges:
      Range[] ranges = calculateRanges(training, evaluation);
      System.out.println("Using optimal Tilings...");
      tileCoder = new TileCodersNoHashing(ranges);
    } else {
      System.out.println("Using suboptimal Tilings...");
      minmax = findMinMax(training, evaluation);
      System.out.println("Minimum and Maximum: " + minmax[0] + " " + minmax[1]);
      minmax[0] = minmax[0] - 0.0001;
      minmax[1] = minmax[1] + 0.0001;
      tileCoder = new TileCodersNoHashing(training[0][0].length, minmax[0], minmax[1]);
    }


    // Initialize the learning framework:

    if (dependentTilings) {
      System.out.println("Creating Dependent Tilings...");
      for (int n = 0; n < this.getSampleSizes()[3]; n++) {
        for (int o = n + 1; o < this.getSampleSizes()[3]; o++) {
          int[] inputIndexes = { n, o };
          tileCoder.addTileCoder(inputIndexes, gridResolution, nbOfTilings);
        }
      }
    } else {
      System.out.println("Creating Independent Tilings...");
      tileCoder.addIndependentTilings(gridResolution, nbOfTilings);
    }
    if (includeActiveFeature) {
      System.out.println("Including Active Feature...");
      tileCoder.includeActiveFeature();
    } else {
      System.out.println("NOT Including Active Feature...");
    }


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
    o_tp1 = training[0][0];
    x_tp1 = tileCoder.project(o_tp1);
    x_t = tileCoder.project(o_tp1);
    a_tp1 = (ActionArray) Actions[0];
    a_t = (ActionArray) Actions[0];
    chosenPerson = 0;
    chosenSample = 0;
  }

  private Action[] createActions(int nbOfSpeakers) {
    switch (nbOfSpeakers) {
    case 2:
      Action[] Actions2 = { FIRST, SECOND };
      return Actions2;
    case 3:
      Action[] Actions3 = { FIRST, SECOND, THIRD };
      return Actions3;
    case 4:
      Action[] Actions4 = { FIRST, SECOND, THIRD, FOURTH };
      return Actions4;
    case 5:
      Action[] Actions5 = { FIRST, SECOND, THIRD, FOURTH, FIFTH };
      return Actions5;
    default:
      System.out.println("NUMBER OF SPEAKERS IS NOT VALID!");
      return null;
    }
  }

  private double[] findMinMax(double[][][] training, double[][][] evaluation) {
    double currentMax;
    double currentMin;
    double[] minMax = { Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY };
    // Find max and min of training set:
    for (int person = 0; person < training.length; person++) {
      for (int step = 0; step < training[0].length; step++) {
        currentMax = CDArrays.max(training[person][step]);
        minMax[1] = minMax[1] > currentMax ? minMax[1] : currentMax;

        currentMin = CDArrays.min(training[person][step]);
        minMax[0] = minMax[0] < currentMin ? minMax[0] : currentMin;
      }
      for (int step = 0; step < evaluation[0].length; step++) {
        currentMax = CDArrays.max(evaluation[person][step]);
        minMax[1] = minMax[1] > currentMax ? minMax[1] : currentMax;

        currentMin = CDArrays.min(evaluation[person][step]);
        minMax[0] = minMax[0] < currentMin ? minMax[0] : currentMin;
      }
    }
    return minMax;
  }

  private Range[] calculateRanges(double[][][] training, double[][][] evaluation) {
    Range[] outRanges = new Range[training[0][0].length];
    double min;
    double max;


    for (int sample = 0; sample < training[0][0].length; sample++) {
      // Find min and Max for each sample:
      min = Double.POSITIVE_INFINITY;
      max = Double.NEGATIVE_INFINITY;

      for (int person = 0; person < training.length; person++) {
        for (int step = 0; step < training[0].length; step++) {
          min = min < training[person][step][sample] ? min : training[person][step][sample];
          max = max > training[person][step][sample] ? max : training[person][step][sample];

          min = min < evaluation[person][step][sample] ? min : evaluation[person][step][sample];
          max = max > evaluation[person][step][sample] ? max : evaluation[person][step][sample];
        }
      }
      max = max + 0.00001;
      min = min - 0.00001;
      outRanges[sample] = new Range(min, max);
    }
    return outRanges;
  }

  public double evaluate() {
    // TODO Auto-generated method stub
    // Change policy to greedy policy:
    double[] obs;
    BinaryVector projection;
    ActionArray action;
    int[] correct = new int[evaluation.length];
    for (int n = 0; n < evaluation.length; n++) {
      correct[n] = 0;
    }
    int totalSteps = 0;

    for (int person = 0; person < evaluation.length; person++) {
      for (int sample = 0; sample < evaluation[person].length; sample++) {
        obs = evaluation[person][sample];
        projection = tileCoder.project(obs);

        action = (ActionArray) policy.computeBestAction(projection);

        if (action.actions[0] == person) {
          correct[person]++;
        }
        totalSteps++;
      }
    }
    return (CDArrays.sum(correct) / totalSteps);
  }

  public void train(int nbOfSteps, int[] chosenPersons, int[] chosenSamples) {
    // TODO Auto-generated method stub
    // This function ONLY operates on the training set, which is presented to
    // the learning / control algorithm.

    if (nbOfSteps != chosenPersons.length || nbOfSteps != chosenSamples.length) {
      System.out.println("FATAL ERROR! number of steps is not consistent!");
      return;
    }
    System.out.println("Maximum of chosenPersons and chosenSamples Arrays: " + CDArrays.max(chosenPersons) + " "
        + CDArrays.max(chosenSamples));

    // Perform ITERATIONS of training
    for (int step = 0; step < nbOfSteps; step++) {
      if (step % 500 == 0) {
        System.out.println(step + "th iteration");
      }

      if (a_tp1.actions[0] == chosenPerson) {
        r_tp1 = 1;
      } else {
        r_tp1 = -1;
      }

      a_t = a_tp1;

      chosenPerson = chosenPersons[step];
      chosenSample = chosenSamples[step];

      o_tp1 = training[chosenPerson][chosenSample];
      x_tp1 = tileCoder.project(o_tp1);

      a_tp1 = (ActionArray) control.step(x_t, a_t, x_tp1, r_tp1);

      x_t = x_tp1;
    }
  }


  public int[] getSampleSizes() {
    int[] sampleSizes = new int[6];
    sampleSizes[0] = training.length;
    sampleSizes[1] = training[0].length;
    sampleSizes[2] = training[0][0].length;
    sampleSizes[3] = evaluation.length;
    sampleSizes[4] = evaluation[0].length;
    sampleSizes[5] = evaluation[0][0].length;
    return sampleSizes;
  }

  public void reset() {
    learning = new Sarsa(alpha, gamma, lambda, toStateAction.vectorSize());
    policy = new EpsilonGreedy(new Random(0), Actions, toStateAction, learning, epsilon);
    control = new SarsaControl(policy, toStateAction, learning);
  }
}