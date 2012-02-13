package soundFeaturesEval;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.commons.lang3.ArrayUtils;


public class Test4Speaker_melfcc_cemmeanrem_local_2environments {

  /**
   * @param args
   * @throws ClassNotFoundException
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {

    FileWriter logfile = new FileWriter(
                                        "test_data/test_recorded/4speaker_feb02_melfcc_means+variances/results_melcepst_voicebox_means+variances.txt");

    // Carry out some 4 person Feature evaluation:
    // Christian, Clio, Gabor, Joseph, Patrick:

    // Create the corresponding Feature Evaluators:

    // 48kHz features:
    // FFTs:
    String[] gabVSchr_workVSgrem = {
        "test_data/test_recorded/4speaker_feb02_melfcc_means+variances/christian_centermic_workstation.txt_16_melfcc_means+variances",
        "test_data/test_recorded/4speaker_feb02_melfcc_means+variances/gabor_centermic_workstation.txt_16_melfcc_means+variances",
        "test_data/test_recorded/4speaker_feb02_melfcc_means+variances/christian_centermic_gremlin2.txt_16_melfcc_means+variances",
        "test_data/test_recorded/4speaker_feb02_melfcc_means+variances/gabor_centermic_gremlin2.txt_16_melfcc_means+variances" };
    String[] gabVSchrVSthomasVSmoham_workVSgrem = {
        "test_data/test_recorded/4speaker_feb02_melfcc_means+variances/thomas_centermic_workstation.txt_16_melfcc_means+variances",
        "test_data/test_recorded/4speaker_feb02_melfcc_means+variances/mohammed_centermic_workstation.txt_16_melfcc_means+variances",
        "test_data/test_recorded/4speaker_feb02_melfcc_means+variances/thomas_centermic_gremlin2.txt_16_melfcc_means+variances",
        "test_data/test_recorded/4speaker_feb02_melfcc_means+variances/mohammed_centermic_gremlin2.txt_16_melfcc_means+variances" };
    EvaluateFeatures fEval_gabVSchr_workVSgrem = createFeatureEvaluator(gabVSchr_workVSgrem, 4, 4, 2);
    EvaluateFeatures fEval_gabVSchrVSthomasVSmoham_workVSgrem = createFeatureEvaluator(gabVSchrVSthomasVSmoham_workVSgrem,
                                                                                       4, 4, 2);

    String[] gabVSchr_gremVSwork = {
        "test_data/test_recorded/4speaker_feb02_melfcc_means+variances/christian_centermic_gremlin2.txt_16_melfcc_means+variances",
        "test_data/test_recorded/4speaker_feb02_melfcc_means+variances/gabor_centermic_gremlin2.txt_16_melfcc_means+variances",
        "test_data/test_recorded/4speaker_feb02_melfcc_means+variances/christian_centermic_workstation.txt_16_melfcc_means+variances",
        "test_data/test_recorded/4speaker_feb02_melfcc_means+variances/gabor_centermic_workstation.txt_16_melfcc_means+variances" };
    String[] gabVSchrVSthomasVSmoham_gremVSwork = {
        "test_data/test_recorded/4speaker_feb02_melfcc_means+variances/thomas_centermic_gremlin2.txt_16_melfcc_means+variances",
        "test_data/test_recorded/4speaker_feb02_melfcc_means+variances/mohammed_centermic_gremlin2.txt_16_melfcc_means+variances",
        "test_data/test_recorded/4speaker_feb02_melfcc_means+variances/thomas_centermic_workstation.txt_16_melfcc_means+variances",
        "test_data/test_recorded/4speaker_feb02_melfcc_means+variances/mohammed_centermic_workstation.txt_16_melfcc_means+variances" };
    EvaluateFeatures fEval_gabVSchr_gremVSwork = createFeatureEvaluator(gabVSchr_gremVSwork, 4, 4, 2);
    EvaluateFeatures fEval_gabVSchrVSthomasVSmoham_gremVSwork = createFeatureEvaluator(gabVSchrVSthomasVSmoham_gremVSwork,
                                                                                       4, 4, 2);


    // Check the number of persons, training and evaluation samples for all
    // feature evaluators:

    logfile.append("Number of Speakers: 2\n");
    logfile.append("Sarsa, epsilon=0.1, gamma=lambda=0.0, activeFeature\n");
    logfile.append("Data about the audio feature sets:\n");


    logfile.append("48_logFFTmag: " + fEval_gabVSchr_workVSgrem.getSampleSizes()[0] + " "
        + fEval_gabVSchr_workVSgrem.getSampleSizes()[1] + " " + fEval_gabVSchr_workVSgrem.getSampleSizes()[2] + " "
        + fEval_gabVSchr_workVSgrem.getSampleSizes()[3] + " " + fEval_gabVSchr_workVSgrem.getSampleSizes()[4] + " "
        + fEval_gabVSchr_workVSgrem.getSampleSizes()[5] + "\n");


    // Carry out some training and evaluation!
    int[] logarithmicScale = { 10, 90, 900, 9000 };
    int totalNbOfSteps = 0;
    String dataOutput = new String();
    int[] chosenPersons_2;
    int[] chosenSamples;
    logfile.append("\nResults (Accuracy):\n");
    logfile
        .append("# of Training steps |  gabVSchr_workVSgrem | mohamVSthoma_workVSgrem | gabVSchr_gremVSwork | mohamVSthoma_gremVSwork \n\n");
    for (int iteration = 0; iteration < 5; iteration++) {
      for (int n : logarithmicScale) {
        // Create random chosen sample arrays for iteration:
        chosenPersons_2 = CDArrays.randIntArray(n, 0, 2);
        chosenSamples = CDArrays.randIntArray(n, 0, fEval_gabVSchr_workVSgrem.getSampleSizes()[1]);

        // Train all FeatureEvaluatos with the same chosenPersons and
        // chosenSamples:
        fEval_gabVSchr_workVSgrem.train(n, chosenPersons_2, chosenSamples);
        fEval_gabVSchrVSthomasVSmoham_workVSgrem.train(n, chosenPersons_2, chosenSamples);

        fEval_gabVSchr_gremVSwork.train(n, chosenPersons_2, chosenSamples);
        fEval_gabVSchrVSthomasVSmoham_gremVSwork.train(n, chosenPersons_2, chosenSamples);


        // Evaluate all, and print the Results to the logfile:
        totalNbOfSteps += n;
        dataOutput = new String(totalNbOfSteps + ";" + fEval_gabVSchr_workVSgrem.evaluate() + ";"
            + fEval_gabVSchrVSthomasVSmoham_workVSgrem.evaluate() + ";" + fEval_gabVSchr_gremVSwork.evaluate() + ";"
            + fEval_gabVSchrVSthomasVSmoham_gremVSwork.evaluate() + "\n");
        //
        //
        //
        System.out.print(dataOutput);
        logfile.append(dataOutput);
      }
      fEval_gabVSchr_workVSgrem.reset();
      fEval_gabVSchrVSthomasVSmoham_workVSgrem.reset();
      fEval_gabVSchr_gremVSwork.reset();
      fEval_gabVSchrVSthomasVSmoham_gremVSwork.reset();

      logfile.append("\n\n");
    }
    logfile.close();
  }

  private static EvaluateFeatures createFeatureEvaluator(String[] args, int gridResolution, int nbOfTilings,
      int nbOfSpeakers) {
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
    EvaluateFeatures featureEvaluator = new EvaluateFeatures(realTraining, realEvaluation, 0.0, 0.1, 0.0,
                                                             gridResolution, nbOfTilings, false, true, nbOfSpeakers,
                                                             true);
    return featureEvaluator;
  }

}
