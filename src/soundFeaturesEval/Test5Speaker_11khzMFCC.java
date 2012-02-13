package soundFeaturesEval;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.commons.lang3.ArrayUtils;


public class Test5Speaker_11khzMFCC {

  /**
   * @param args
   * @throws ClassNotFoundException
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {

    FileWriter logfile = new FileWriter(
                                        "test_data/test_recorded/5people_features_independent/Test5Speaker_48logfft_diffAmoutsOfRealData_subOptimalTiling_DataFromMiddle_100000.txt");
    int nbOfSpeakers = 5;


    // 48kHz features:

    // MFCCs
    String[] featureFiles = {
        "test_data/test_recorded/5people_features_independent/christian_2mic_180s.txt_48_logfftmag_1400_train",
        "test_data/test_recorded/5people_features_independent/clio_2mic_180s.txt_48_logfftmag_1400_train",
        "test_data/test_recorded/5people_features_independent/gabor_2mic_180s.txt_48_logfftmag_1400_train",
        "test_data/test_recorded/5people_features_independent/joseph_2mic_180s.txt_48_logfftmag_1400_train",
        "test_data/test_recorded/5people_features_independent/patrick_2mic_180s.txt_48_logfftmag_1400_train",
        "test_data/test_recorded/5people_features_independent/christian_2mic_180s.txt_48_logfftmag_1400_eval",
        "test_data/test_recorded/5people_features_independent/clio_2mic_180s.txt_48_logfftmag_1400_eval",
        "test_data/test_recorded/5people_features_independent/gabor_2mic_180s.txt_48_logfftmag_1400_eval",
        "test_data/test_recorded/5people_features_independent/joseph_2mic_180s.txt_48_logfftmag_1400_eval",
        "test_data/test_recorded/5people_features_independent/patrick_2mic_180s.txt_48_logfftmag_1400_eval" };
    EvaluateFeatures fEval_2s = createFeatureEvaluator(featureFiles, nbOfSpeakers);
    EvaluateFeatures fEval_5s = createFeatureEvaluator(featureFiles, nbOfSpeakers);
    EvaluateFeatures fEval_10s = createFeatureEvaluator(featureFiles, nbOfSpeakers);
    EvaluateFeatures fEval_20s = createFeatureEvaluator(featureFiles, nbOfSpeakers);
    EvaluateFeatures fEval_45s = createFeatureEvaluator(featureFiles, nbOfSpeakers);
    EvaluateFeatures fEval_90s = createFeatureEvaluator(featureFiles, nbOfSpeakers);

    // Check the number of persons, training and evaluation samples for all
    // feature evaluators:

    logfile.append("Number of Speakers: 5\n");
    logfile.append("Sarsa, epsilon=0.1, gamma=lambda=0.0, independentTilings 4x4, activeFeature\n");
    logfile.append("Data about the audio feature sets:\n");

    int[] sampleSizes = fEval_5s.getSampleSizes();
    logfile.append("16_mfcc: " + fEval_5s.getSampleSizes()[0] + " " + fEval_5s.getSampleSizes()[1] + " "
        + fEval_5s.getSampleSizes()[2] + " " + fEval_5s.getSampleSizes()[3] + " " + fEval_5s.getSampleSizes()[4] + " "
        + fEval_5s.getSampleSizes()[5] + "\n");


    // Carry out some training and evaluation!
    int[] logarithmicScale = { 10, 90, 900, 9000, 90000 };
    int totalNbOfSteps = 0;
    String dataOutput = new String();
    int[] chosenPersons;
    int[] chosenSamples_2s;
    int[] chosenSamples_5s;
    int[] chosenSamples_10s;
    int[] chosenSamples_20s;
    int[] chosenSamples_45s;
    int[] chosenSamples_90s;
    logfile.append("\nResults (Accuracy):\n");
    logfile.append("# of Training steps | 2s | 5s | 10s | 20s | 45s | 90s \n\n");
    for (int n : logarithmicScale) {
      // Create random chosen sample arrays for iteration:
      chosenPersons = CDArrays.randIntArray(n, 0, sampleSizes[0]);
      chosenSamples_2s = CDArrays.randIntArray(n, 250, 262);
      chosenSamples_5s = CDArrays.randIntArray(n, 245, 275);
      chosenSamples_10s = CDArrays.randIntArray(n, 230, 290);
      chosenSamples_20s = CDArrays.randIntArray(n, 200, 320);
      chosenSamples_45s = CDArrays.randIntArray(n, 129, 391);
      chosenSamples_90s = CDArrays.randIntArray(n, 0, sampleSizes[1]);

      // Train all FeatureEvaluatos with the same chosenPersons and
      // chosenSamples:
      fEval_2s.train(n, chosenPersons, chosenSamples_2s);
      fEval_5s.train(n, chosenPersons, chosenSamples_5s);
      fEval_10s.train(n, chosenPersons, chosenSamples_10s);
      fEval_20s.train(n, chosenPersons, chosenSamples_20s);
      fEval_45s.train(n, chosenPersons, chosenSamples_45s);
      fEval_90s.train(n, chosenPersons, chosenSamples_90s);

      // Evaluate all, and print the Results to the logfile:
      totalNbOfSteps += n;
      dataOutput = new String(totalNbOfSteps + "; " + fEval_2s.evaluate() + ";" + fEval_5s.evaluate() + ";"
          + fEval_10s.evaluate() + ";" + fEval_20s.evaluate() + ";" + fEval_45s.evaluate() + ";" + fEval_90s.evaluate()
          + ";" + "\n");
      //
      //
      //
      System.out.print(dataOutput);
      logfile.append(dataOutput);

    }
    logfile.close();
  }

  private static EvaluateFeatures createFeatureEvaluator(String[] args, int nbOfSpeakers) {
    // The first half will be interpreted as training data, the second half as
    // evaluation / Test data.
    // First, read input objects:
    Double[][][] training = new Double[args.length / 2][][];
    Double[][][] evaluation = new Double[args.length / 2][][];

    double[][][] realTraining = new double[args.length / 2][][];
    double[][][] realEvaluation = new double[args.length / 2][][];


    FileInputStream fis = null;
    ObjectInputStream in = null;

    // Read the Files:
    for (int n = 0; n < args.length / 2; n++) {
      try {
        fis = new FileInputStream(args[n]);
        in = new ObjectInputStream(fis);
        training[n] = ((Double[][]) in.readObject()).clone();
        in.close();
      } catch (IOException ex) {
        ex.printStackTrace();
      } catch (ClassNotFoundException ex) {
        ex.printStackTrace();
      }
    }
    for (int n = args.length / 2; n < args.length; n++) {
      try {
        fis = new FileInputStream(args[n]);
        in = new ObjectInputStream(fis);
        evaluation[n - (args.length / 2)] = ((Double[][]) in.readObject()).clone();
        in.close();
      } catch (IOException ex) {
        ex.printStackTrace();
      } catch (ClassNotFoundException ex) {
        ex.printStackTrace();
      }
    }
    // Input reading is done...


    // Data has to be "transformed" to double[][][] arrays instead of
    // Double[][][] arrays :-(
    for (int person = 0; person < training.length; person++) {
      realTraining[person] = new double[training[0].length][];
      for (int step = 0; step < training[0].length; step++) {
        realTraining[person][step] = ArrayUtils.toPrimitive(training[person][step]);
      }
    }
    for (int person = 0; person < evaluation.length; person++) {
      realEvaluation[person] = new double[evaluation[0].length][];
      for (int step = 0; step < training[0].length; step++) {
        realEvaluation[person][step] = ArrayUtils.toPrimitive(evaluation[person][step]);
      }
    }


    // Create new Class instance with that data and the percentage of the data
    // to be used as training
    EvaluateFeatures featureEvaluator = new EvaluateFeatures(realTraining, realEvaluation, 0.0, 0.1, 0.0, 4, 4, false,
                                                             true, nbOfSpeakers, false);
    return featureEvaluator;
  }

}
