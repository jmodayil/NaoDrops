package soundFeaturesEval;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.commons.lang3.ArrayUtils;


public class Test4Speaker_10x10_mfccOnly {

  /**
   * @param args
   * @throws ClassNotFoundException
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {

    FileWriter logfile = new FileWriter("Test4Speaker_8x8_dependent_mfccOnly.txt");

    // Carry out some 4 person Feature evaluation:
    // Clio, Gabor, Joseph, Patrick:

    // Create the corresponding Feature Evaluators:

    // 48kHz features:

    // MFCCs
    String[] args_48_mfcc = { "test_recorded/clio_2mic_180s.txt_48_melfcc_train",
        "test_recorded/gabor_2mic_180s.txt_48_melfcc_train", "test_recorded/joseph_2mic_180s.txt_48_melfcc_train",
        "test_recorded/patrick_2mic_180s.txt_48_melfcc_train", "test_recorded/clio_2mic_180s.txt_48_melfcc_eval",
        "test_recorded/gabor_2mic_180s.txt_48_melfcc_eval", "test_recorded/joseph_2mic_180s.txt_48_melfcc_eval",
        "test_recorded/patrick_2mic_180s.txt_48_melfcc_eval" };
    EvaluateFeatures fEval_48_mfcc = createFeatureEvaluator(args_48_mfcc);


    // 16kHz features:

    // MFCC rectangular window
    String[] args_16_mfcc_rect = { "test_recorded/clio_2mic_180s.txt_16_melfcc_new_RE0_train",
        "test_recorded/gabor_2mic_180s.txt_16_melfcc_new_RE0_train",
        "test_recorded/joseph_2mic_180s.txt_16_melfcc_new_RE0_train",
        "test_recorded/patrick_2mic_180s.txt_16_melfcc_new_RE0_train",
        "test_recorded/clio_2mic_180s.txt_16_melfcc_new_RE0_eval",
        "test_recorded/gabor_2mic_180s.txt_16_melfcc_new_RE0_eval",
        "test_recorded/joseph_2mic_180s.txt_16_melfcc_new_RE0_eval",
        "test_recorded/patrick_2mic_180s.txt_16_melfcc_new_RE0_eval" };
    EvaluateFeatures fEval_16_mfcc_rect = createFeatureEvaluator(args_16_mfcc_rect);

    // MFCC hamming window
    String[] args_16_mfcc_hamm = { "test_recorded/clio_2mic_180s.txt_16_melfcc_new_ME0_train",
        "test_recorded/gabor_2mic_180s.txt_16_melfcc_new_ME0_train",
        "test_recorded/joseph_2mic_180s.txt_16_melfcc_new_ME0_train",
        "test_recorded/patrick_2mic_180s.txt_16_melfcc_new_ME0_train",
        "test_recorded/clio_2mic_180s.txt_16_melfcc_new_ME0_eval",
        "test_recorded/gabor_2mic_180s.txt_16_melfcc_new_ME0_eval",
        "test_recorded/joseph_2mic_180s.txt_16_melfcc_new_ME0_eval",
        "test_recorded/patrick_2mic_180s.txt_16_melfcc_new_ME0_eval" };
    EvaluateFeatures fEval_16_mfcc_hamm = createFeatureEvaluator(args_16_mfcc_hamm);


    // Check the number of persons, training and evaluation samples for all
    // feature evaluators:

    logfile.append("Number of Speakers: 4\n");
    logfile.append("Sarsa, epsilon=0.1, gamma=lambda=0.0, dependentTilings 8x8, activeFeature\n");
    logfile.append("Data about the audio feature sets:\n");


    logfile.append("48_mfcc: " + fEval_48_mfcc.getSampleSizes()[0] + " " + fEval_48_mfcc.getSampleSizes()[1] + " "
        + fEval_48_mfcc.getSampleSizes()[2] + " " + fEval_48_mfcc.getSampleSizes()[3] + " "
        + fEval_48_mfcc.getSampleSizes()[4] + " " + fEval_48_mfcc.getSampleSizes()[5] + "\n");
    logfile.append("16_mfcc_rect: " + fEval_16_mfcc_rect.getSampleSizes()[0] + " "
        + fEval_16_mfcc_rect.getSampleSizes()[1] + " " + fEval_16_mfcc_rect.getSampleSizes()[2] + " "
        + fEval_16_mfcc_rect.getSampleSizes()[3] + " " + fEval_16_mfcc_rect.getSampleSizes()[4] + " "
        + fEval_16_mfcc_rect.getSampleSizes()[5] + "\n");
    logfile.append("16_mfcc_hamm: " + fEval_16_mfcc_hamm.getSampleSizes()[0] + " "
        + fEval_16_mfcc_hamm.getSampleSizes()[1] + " " + fEval_16_mfcc_hamm.getSampleSizes()[2] + " "
        + fEval_16_mfcc_hamm.getSampleSizes()[3] + " " + fEval_16_mfcc_hamm.getSampleSizes()[4] + " "
        + fEval_16_mfcc_hamm.getSampleSizes()[5] + "\n");


    // Carry out some training and evaluation!
    int[] logarithmicScale = { 10, 90, 900, 9000, 90000, 900000 };
    int totalNbOfSteps = 0;
    String dataOutput = new String();
    int[] chosenPersons;
    int[] chosenSamples;
    logfile.append("\nResults (Accuracy):\n");
    logfile.append("# of Training steps | 48_mfcc | 16_mfcc_rect | 16_mfcc_hamm | \n\n");
    for (int n : logarithmicScale) {
      // Create random chosen sample arrays for iteration:
      chosenPersons = Arrays.randIntArray(n, 0, 4);
      chosenSamples = Arrays.randIntArray(n, 0, 527);

      // Train all FeatureEvaluatos with the same chosenPersons and
      // chosenSamples:
      fEval_48_mfcc.train(n, chosenPersons, chosenSamples);
      fEval_16_mfcc_rect.train(n, chosenPersons, chosenSamples);
      fEval_16_mfcc_hamm.train(n, chosenPersons, chosenSamples);

      // Evaluate all, and print the Results to the logfile:
      totalNbOfSteps += n;
      dataOutput = new String(totalNbOfSteps + ";" + fEval_48_mfcc.evaluate() + ";" + fEval_16_mfcc_rect.evaluate()
          + ";" + fEval_16_mfcc_hamm.evaluate() + "\n");
      //
      //
      //
      System.out.print(dataOutput);
      logfile.append(dataOutput);

    }
    logfile.close();
  }

  private static EvaluateFeatures createFeatureEvaluator(String[] args) {
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
    EvaluateFeatures featureEvaluator = new EvaluateFeatures(realTraining, realEvaluation, 0.0, 0.1, 0.0, 2, 2);
    return featureEvaluator;
  }

}
