package soundFeaturesEval;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.commons.lang3.ArrayUtils;


public class Test5Speaker_statisticsCheck {

  /**
   * @param args
   * @throws ClassNotFoundException
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {

    FileWriter logfile = new FileWriter(
                                        "test_data/test_recorded/logfftmag_statsTest_2ndMoment/Test5Speaker_4x4_48_logfftmag_stats_2ndmoment.txt");
    int nbOfSpeakers = 5;

    // Carry out some 4 person Feature evaluation:
    // Clio, Gabor, Joseph, Patrick:

    // Create the corresponding Feature Evaluators:

    // 48kHz features:

    // MFCCs
    String[] args_11_mfcc = {
        "test_data/test_recorded/logfftmag_statsTest_2ndMoment/christian_2mic_180s.txt_48_logfftmag_stats_2ndmoment_train",
        "test_data/test_recorded/logfftmag_statsTest_2ndMoment/clio_2mic_180s.txt_48_logfftmag_stats_2ndmoment_train",
        "test_data/test_recorded/logfftmag_statsTest_2ndMoment/gabor_2mic_180s.txt_48_logfftmag_stats_2ndmoment_train",
        "test_data/test_recorded/logfftmag_statsTest_2ndMoment/joseph_2mic_180s.txt_48_logfftmag_stats_2ndmoment_train",
        "test_data/test_recorded/logfftmag_statsTest_2ndMoment/patrick_2mic_180s.txt_48_logfftmag_stats_2ndmoment_train",
        "test_data/test_recorded/logfftmag_statsTest_2ndMoment/christian_2mic_180s.txt_48_logfftmag_stats_2ndmoment_eval",
        "test_data/test_recorded/logfftmag_statsTest_2ndMoment/clio_2mic_180s.txt_48_logfftmag_stats_2ndmoment_eval",
        "test_data/test_recorded/logfftmag_statsTest_2ndMoment/gabor_2mic_180s.txt_48_logfftmag_stats_2ndmoment_eval",
        "test_data/test_recorded/logfftmag_statsTest_2ndMoment/joseph_2mic_180s.txt_48_logfftmag_stats_2ndmoment_eval",
        "test_data/test_recorded/logfftmag_statsTest_2ndMoment/patrick_2mic_180s.txt_48_logfftmag_stats_2ndmoment_eval" };
    EvaluateFeatures_statisticalCheck fEval_11_MFCC = createFeatureEvaluator(args_11_mfcc, nbOfSpeakers);

    // Check the number of persons, training and evaluation samples for all
    // feature evaluators:

    logfile.append("Number of Speakers: 5\n");
    logfile.append("Sarsa, epsilon=0.1, gamma=lambda=0.0, independentTilings 4x4, activeFeature\n");
    logfile.append("Data about the audio feature sets:\n");

    int[] sampleSizes = fEval_11_MFCC.getSampleSizes();
    logfile.append("48_mfcc: " + fEval_11_MFCC.getSampleSizes()[0] + " " + fEval_11_MFCC.getSampleSizes()[1] + " "
        + fEval_11_MFCC.getSampleSizes()[2] + " " + fEval_11_MFCC.getSampleSizes()[3] + " "
        + fEval_11_MFCC.getSampleSizes()[4] + " " + fEval_11_MFCC.getSampleSizes()[5] + "\n");


    // Carry out some training and evaluation!
    int[] logarithmicScale = { 10, 90, 900, 9000 };
    int totalNbOfSteps = 0;
    String dataOutput = new String();
    int[] chosenPersons;
    int[] chosenSamples;
    logfile.append("\nResults (Accuracy):\n");
    logfile.append("# of History for filled: 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 \n\n");
    for (int averaging = 0; averaging < 10; averaging++) {
      for (int n : logarithmicScale) {
        // Create random chosen sample arrays for iteration:
        chosenPersons = CDArrays.randIntArray(n, 0, sampleSizes[0]);
        chosenSamples = CDArrays.randIntArray(n, 0, sampleSizes[1]);

        // Train all FeatureEvaluatos with the same chosenPersons and
        // chosenSamples:
        fEval_11_MFCC.train(n, chosenPersons, chosenSamples);

        // Evaluate all, and print the Results to the logfile:
        totalNbOfSteps += n;
        dataOutput = new String(totalNbOfSteps + ";" + CDArrays.arrayToString(fEval_11_MFCC.evaluate()) + "\n");
        //
        //
        //
        System.out.print(dataOutput);
        logfile.append(dataOutput);
      }
      fEval_11_MFCC.reset();
    }

    logfile.close();
  }

  private static EvaluateFeatures_statisticalCheck createFeatureEvaluator(String[] args, int nbOfSpeakers) {
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
    EvaluateFeatures_statisticalCheck featureEvaluator = new EvaluateFeatures_statisticalCheck(realTraining,
                                                                                               realEvaluation, 0.0,
                                                                                               0.1, 0.0, 4, 4, false,
                                                                                               true, nbOfSpeakers);
    return featureEvaluator;
  }

}
