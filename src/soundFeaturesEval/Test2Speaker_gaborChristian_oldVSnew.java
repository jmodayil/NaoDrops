package soundFeaturesEval;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.commons.lang3.ArrayUtils;


public class Test2Speaker_gaborChristian_oldVSnew {

  /**
   * @param args
   * @throws ClassNotFoundException
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {

    FileWriter logfile = new FileWriter(
                                        "test_data/test_recorded/gaborChristian_melfcc_sev_newVSold/results_gabChr_oldVSnew_melfcc_sev_4x4_cepsRem_loc&Glo_globalHanning.txt");

    // Carry out some 4 person Feature evaluation:
    // Christian, Clio, Gabor, Joseph, Patrick:

    // Create the corresponding Feature Evaluators:

    // 48kHz features:

    // FFTs:
    String[] oldVSnew_locMean = {
        "test_data/test_recorded/gaborChristian_melfcc_sev_newVSold/christian_2mic_180s.txt_16_melfcc_sev_cepmeanrem_local_hanning",
        "test_data/test_recorded/gaborChristian_melfcc_sev_newVSold/gabor_2mic_180s.txt_16_melfcc_sev_cepmeanrem_local_hanning",
        "test_data/test_recorded/gaborChristian_melfcc_sev_newVSold/christian_4mic_90s_02feb.txt_16_melfcc_sev_cepmeanrem_local_hanning",
        "test_data/test_recorded/gaborChristian_melfcc_sev_newVSold/gabor_4mic_90s_02feb.txt_16_melfcc_sev_cepmeanrem_local_hanning" };
    String[] newVSold_locMean = {
        "test_data/test_recorded/gaborChristian_melfcc_sev_newVSold/christian_4mic_90s_02feb.txt_16_melfcc_sev_cepmeanrem_local_hanning",
        "test_data/test_recorded/gaborChristian_melfcc_sev_newVSold/gabor_4mic_90s_02feb.txt_16_melfcc_sev_cepmeanrem_local_hanning",
        "test_data/test_recorded/gaborChristian_melfcc_sev_newVSold/christian_2mic_180s.txt_16_melfcc_sev_cepmeanrem_local_hanning",
        "test_data/test_recorded/gaborChristian_melfcc_sev_newVSold/gabor_2mic_180s.txt_16_melfcc_sev_cepmeanrem_local_hanning" };
    EvaluateFeatures fEval_oldVSnew_locMean = createFeatureEvaluator(oldVSnew_locMean, 4, 4);
    EvaluateFeatures fEval_newVSold_locMean = createFeatureEvaluator(newVSold_locMean, 4, 4);

    String[] oldVSnew_gloMean = {
        "test_data/test_recorded/gaborChristian_melfcc_sev_newVSold/christian_2mic_180s.txt_16_melfcc_sev_cepmeanrem_global_hanning",
        "test_data/test_recorded/gaborChristian_melfcc_sev_newVSold/gabor_2mic_180s.txt_16_melfcc_sev_cepmeanrem_global_hanning",
        "test_data/test_recorded/gaborChristian_melfcc_sev_newVSold/christian_4mic_90s_02feb.txt_16_melfcc_sev_cepmeanrem_global_hanning",
        "test_data/test_recorded/gaborChristian_melfcc_sev_newVSold/gabor_4mic_90s_02feb.txt_16_melfcc_sev_cepmeanrem_global_hanning" };
    String[] newVSold_gloMean = {
        "test_data/test_recorded/gaborChristian_melfcc_sev_newVSold/christian_4mic_90s_02feb.txt_16_melfcc_sev_cepmeanrem_global_hanning",
        "test_data/test_recorded/gaborChristian_melfcc_sev_newVSold/gabor_4mic_90s_02feb.txt_16_melfcc_sev_cepmeanrem_global_hanning",
        "test_data/test_recorded/gaborChristian_melfcc_sev_newVSold/christian_2mic_180s.txt_16_melfcc_sev_cepmeanrem_global_hanning",
        "test_data/test_recorded/gaborChristian_melfcc_sev_newVSold/gabor_2mic_180s.txt_16_melfcc_sev_cepmeanrem_global_hanning" };
    EvaluateFeatures fEval_oldVSnew_gloMean = createFeatureEvaluator(oldVSnew_gloMean, 4, 4);
    EvaluateFeatures fEval_newVSold_gloMean = createFeatureEvaluator(newVSold_gloMean, 4, 4);


    // Check the number of persons, training and evaluation samples for all
    // feature evaluators:

    logfile.append("Number of Speakers: 2\n");
    logfile.append("Sarsa, epsilon=0.1, gamma=lambda=0.0, activeFeature\n");
    logfile.append("Data about the audio feature sets:\n");


    logfile.append("48_logFFTmag: " + fEval_oldVSnew_locMean.getSampleSizes()[0] + " "
        + fEval_oldVSnew_locMean.getSampleSizes()[1] + " " + fEval_oldVSnew_locMean.getSampleSizes()[2] + " "
        + fEval_oldVSnew_locMean.getSampleSizes()[3] + " " + fEval_oldVSnew_locMean.getSampleSizes()[4] + " "
        + fEval_oldVSnew_locMean.getSampleSizes()[5] + "\n");


    // Carry out some training and evaluation!
    int[] logarithmicScale = { 10, 90, 900, 9000, 90000 };
    int totalNbOfSteps = 0;
    String dataOutput = new String();
    int[] chosenPersons;
    int[] chosenSamples;
    logfile.append("\nResults (Accuracy):\n");
    logfile
        .append("# of Training steps |  oldVSnew_localMean | newVSold_localMean | oldVSnew_globalMean | newVSold_globalMean \n\n");
    for (int iteration = 0; iteration < 5; iteration++) {
      for (int n : logarithmicScale) {
        // Create random chosen sample arrays for iteration:
        chosenPersons = CDArrays.randIntArray(n, 0, 2);
        chosenSamples = CDArrays.randIntArray(n, 0, 527);

        // Train all FeatureEvaluatos with the same chosenPersons and
        // chosenSamples:
        fEval_oldVSnew_locMean.train(n, chosenPersons, chosenSamples);
        fEval_newVSold_locMean.train(n, chosenPersons, chosenSamples);

        fEval_oldVSnew_gloMean.train(n, chosenPersons, chosenSamples);
        fEval_newVSold_gloMean.train(n, chosenPersons, chosenSamples);


        // Evaluate all, and print the Results to the logfile:
        totalNbOfSteps += n;
        dataOutput = new String(totalNbOfSteps + ";" + fEval_oldVSnew_locMean.evaluate() + ";"
            + fEval_newVSold_locMean.evaluate() + fEval_oldVSnew_gloMean.evaluate() + ";"
            + fEval_newVSold_gloMean.evaluate() + "\n");
        //
        //
        //
        System.out.print(dataOutput);
        logfile.append(dataOutput);
      }
      fEval_oldVSnew_locMean.reset();
      fEval_newVSold_locMean.reset();
      fEval_oldVSnew_gloMean.reset();
      fEval_newVSold_gloMean.reset();

      logfile.append("\n\n");
    }
    logfile.close();
  }

  private static EvaluateFeatures createFeatureEvaluator(String[] args, int gridResolution, int nbOfTilings) {
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
                                                             gridResolution, nbOfTilings, false, true, 2, true);
    return featureEvaluator;
  }

}
