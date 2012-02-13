package soundFeaturesEval;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.commons.lang3.ArrayUtils;

import AudioPrediction.MFCCProvider;


public class TestGaborChristian_IndexFile {

  /**
   * @param args
   * @throws ClassNotFoundException
   * @throws IOException
   */


  public static void main(String[] args) throws IOException {

    FileWriter logfile = new FileWriter(
                                        "test_data/test_recorded/gaborChristian_Interleaved/resultsGaborChristian_Index_standardParamsFormerAction_gammaLambdaNOTzero_512_20_16000_40.txt");

    // Create the corresponding Feature Evaluators:

    // 48kHz features:

    // FFTs:
    // String[] args_48_fft = {
    // "test_data/test_recorded/christiangabor_4mic_180s.txt_48_fftmag_1400_train",
    // "test_data/test_recorded/christiangabor_4mic_180s.txt_48_fftmag_1400_eval",
    // "test_data/test_recorded/trainingIndex",
    // "test_data/test_recorded/evaluationIndex" };
    // EvaluateFeatures_indexFile fEval_48_fft =
    // createFeatureEvaluator_index(args_48_fft, 4, 4);


    // Log FFTs:
    // String[] args_48_logfft = {
    // "test_data/test_data/test_data/test_recorded/christiangabor_4mic_180s.txt_48_fftmag_log_1400_train",
    // "test_data/test_recorded/christiangabor_4mic_180s.txt_48_fftmag_log_1400_eval",
    // "test_data/test_recorded/trainingIndex",
    // "test_data/test_recorded/evaluationIndex" };
    // EvaluateFeatures_indexFile fEval_48_logfft =
    // createFeatureEvaluator_index(args_48_logfft, 4, 4);


    // 16kHz features:

    // Raw
    // String[] args_16_raw = {
    // "test_data/test_recorded/christiangabor_4mic_180s.txt_16_raw_train",
    // "test_data/test_recorded/christiangabor_4mic_180s.txt_16_raw_eval",
    // "test_data/test_recorded/trainingIndex",
    // "test_data/test_recorded/evaluationIndex" };
    // EvaluateFeatures_indexFile fEval_16_raw =
    // createFeatureEvaluator_index(args_16_raw, 3, 3);

    // MFCC hamming window
    // String[] args_16_mfcc_hamm = {
    // "test_data/test_recorded/christiangabor_4mic_180s.txt_16_melfcc_new_ME0_train",
    // "test_data/test_recorded/christiangabor_4mic_180s.txt_16_melfcc_new_ME0_eval",
    // "test_data/test_recorded/trainingIndex",
    // "test_data/test_recorded/evaluationIndex" };
    // EvaluateFeatures_indexFile fEval_16_mfcc_hamm =
    // createFeatureEvaluator_index(args_16_mfcc_hamm, 16, 16);
    //
    // // MFCC 200 hamming
    // String[] args_16_mfcc_hamm_200 = {
    // "test_data/test_recorded/christiangabor_4mic_180s.txt_16_melfcc_new_ME0_200_train",
    // "test_data/test_recorded/christiangabor_4mic_180s.txt_16_melfcc_new_ME0_200_eval",
    // "test_data/test_recorded/trainingIndex",
    // "test_data/test_recorded/evaluationIndex" };
    // EvaluateFeatures_indexFile fEval_16_mfcc_hamm_200 =
    // createFeatureEvaluator_index(args_16_mfcc_hamm_200, 10, 10);
    //
    // // MFCC 200 Rectangular
    // String[] args_16_mfcc_rect_200 = {
    // "test_data/test_recorded/christiangabor_4mic_180s.txt_16_melfcc_new_RE0_200_train",
    // "test_data/test_recorded/christiangabor_4mic_180s.txt_16_melfcc_new_RE0_200_eval",
    // "test_data/test_recorded/trainingIndex",
    // "test_data/test_recorded/evaluationIndex" };
    // EvaluateFeatures_indexFile fEval_16_mfcc_rect_200 =
    // createFeatureEvaluator_index(args_16_mfcc_rect_200, 10, 10);
    //
    // // logFFTs
    // String[] args_16_logfft = {
    // "test_data/test_recorded/christiangabor_4mic_180s.txt_16_fftmag_log_train",
    // "test_data/test_recorded/christiangabor_4mic_180s.txt_16_fftmag_log_eval",
    // "test_data/test_recorded/trainingIndex",
    // "test_data/test_recorded/evaluationIndex" };
    // EvaluateFeatures_indexFile fEval_16_logfft =
    // createFeatureEvaluator_index(args_16_logfft, 4, 4);

    // MFCC Optimized Means:
    String[] args_16_raw = {
        "test_data/test_recorded/gaborChristian_Interleaved/christianGaborInterleaved_16_raw_training",
        "test_data/test_recorded/gaborChristian_Interleaved/christianGaborInterleaved_16_raw_evaluation",
        "test_data/test_recorded/gaborChristian_Interleaved/trainingIndex",
        "test_data/test_recorded/gaborChristian_Interleaved/evaluationIndex" };
    EvaluateFeatures_indexFile fEval_16_raw2mfcc = createFeatureEvaluator_index(args_16_raw, 4, 4);
    // String[] args_16_logfft = {
    // "test_data/test_recorded/gaborChristian_Interleaved/gaborChristian_16_logfft_training",
    // "test_data/test_recorded/gaborChristian_Interleaved/gaborChristian_16_logfft_evaluation",
    // "test_data/test_recorded/gaborChristian_Interleaved/trainingIndex",
    // "test_data/test_recorded/gaborChristian_Interleaved/evaluationIndex" };
    // EvaluateFeatures_indexFile fEval_16_logfft =
    // createFeatureEvaluator_index(args_16_logfft, 4, 4);


    // Check the number of persons, training and evaluation samples for all
    // feature evaluators:

    logfile.append("Number of Speakers: 4\n");
    logfile
        .append("Sarsa, epsilon=0.1, gamma=lambda=0.1, independentTilings 4x4, activeFeature, FormerActionIncluded\n");
    logfile.append("Data about the audio feature sets:\n");

    logfile.append("16_mfcc_means: " + fEval_16_raw2mfcc.getSampleSizes() + "\n");
    // logfile.append("16_logfft: " + fEval_16_logfft.getSampleSizes() + "\n");


    // Carry out some training and evaluation!
    String dataOutput = new String();
    logfile.append("\nResults (Accuracy):\n");
    logfile.append("# of Training steps | 16_mfcc_means | 16_logfft | \n\n");
    for (int n = 1; n < 10; n++) {
      // Create random chosen sample arrays for iteration:

      // Train all FeatureEvaluatos with the same chosenPersons and
      // chosenSamples:
      fEval_16_raw2mfcc.train();


      // Evaluate all, and print the Results to the logfile:
      dataOutput = new String(n * 452 + ";" + fEval_16_raw2mfcc.evaluate() + "\n");
      //
      //
      //
      System.out.print(dataOutput);
      logfile.append(dataOutput);
      System.out.println("Total Number of performed training samples:" + (n * 452));
    }
    logfile.close();
  }

  private static EvaluateFeatures_indexFile createFeatureEvaluator_index(String[] args, int gridResolution,
      int nbOfTilings) throws IllegalArgumentException, IOException {
    // The first half will be interpreted as training data, the second half as
    // evaluation / Test data.
    // First, read input objects:
    Double[][] training = null;
    Double[][] evaluation = null;
    Integer[] trainingIndex = null;
    Integer[] evaluationIndex = null;

    double[][] realTraining = null;
    double[][] realEvaluation = null;
    int[] realTrainingIndex;
    int[] realEvaluationIndex;

    MFCCProvider calcMFCC = new MFCCProvider();


    FileInputStream fis = null;
    ObjectInputStream in = null;

    // Read the Files:
    // training and evaluation samples:
    try {
      fis = new FileInputStream(args[0]);
      in = new ObjectInputStream(fis);
      training = ((Double[][]) in.readObject());
      in.close();
      System.out.println("Length of training: " + training.length + " " + training[0].length);
      realTraining = new double[training.length][];
    } catch (IOException ex) {
      ex.printStackTrace();
    } catch (ClassNotFoundException ex) {
      ex.printStackTrace();
    }

    try {
      fis = new FileInputStream(args[1]);
      in = new ObjectInputStream(fis);
      evaluation = ((Double[][]) in.readObject()).clone();
      realEvaluation = new double[evaluation.length][];
      in.close();
    } catch (IOException ex) {
      ex.printStackTrace();
    } catch (ClassNotFoundException ex) {
      ex.printStackTrace();
    }

    // Training and evaluation index:
    try {
      fis = new FileInputStream(args[2]);
      in = new ObjectInputStream(fis);
      trainingIndex = ((Integer[]) in.readObject());
      in.close();
    } catch (IOException ex) {
      ex.printStackTrace();
    } catch (ClassNotFoundException ex) {
      ex.printStackTrace();
    }

    try {
      fis = new FileInputStream(args[3]);
      in = new ObjectInputStream(fis);
      evaluationIndex = ((Integer[]) in.readObject());
      in.close();
    } catch (IOException ex) {
      ex.printStackTrace();
    } catch (ClassNotFoundException ex) {
      ex.printStackTrace();
    }
    // Input reading is done...


    // Data has to be "transformed" to double[][][] arrays instead of
    // Double[][][] arrays :-(
    for (int sample = 0; sample < training.length; sample++) {
      realTraining[sample] = new double[training[0].length];
      realTraining[sample] = ArrayUtils.toPrimitive(training[sample]);

    }
    for (int sample = 0; sample < evaluation.length; sample++) {
      realEvaluation[sample] = new double[evaluation[0].length];
      realEvaluation[sample] = ArrayUtils.toPrimitive(evaluation[sample]);
    }
    realTrainingIndex = ArrayUtils.toPrimitive(trainingIndex);
    realEvaluationIndex = ArrayUtils.toPrimitive(evaluationIndex);

    realTraining = calcMFCC.processTestData(realTraining);
    realEvaluation = calcMFCC.processTestData(realEvaluation);

    // Create new Class instance with that data and the percentage of the data
    // to be used as training
    EvaluateFeatures_indexFile featureEvaluator = new EvaluateFeatures_indexFile(realTraining, realEvaluation,
                                                                                 realTrainingIndex,
                                                                                 realEvaluationIndex, 0.1, 0.1, 0.1,
                                                                                 gridResolution, nbOfTilings);
    return featureEvaluator;
  }

}
