package soundFeaturesEval;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.commons.lang3.ArrayUtils;


public class Test5speaker_always2outof5 {

  /**
   * @param args
   * @throws ClassNotFoundException
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {

    FileWriter logfile = new FileWriter("Test4Speaker_cg_jp_allFeatures.txt");

    // Carry out some 4 person Feature evaluation:
    // Christian, Clio, Gabor, Joseph, Patrick:

    // Create the corresponding Feature Evaluators:

    // 48kHz features:

    // FFTs:
    String[] args_48_fft_jp = { "test_recorded/5people_features_independent/joseph_2mic_180s.txt_48_fftmag_1400_train",
        "test_recorded/5people_features_independent/patrick_2mic_180s.txt_48_fftmag_1400_train",
        "test_recorded/5people_features_independent/joseph_2mic_180s.txt_48_fftmag_1400_eval",
        "test_recorded/5people_features_independent/patrick_2mic_180s.txt_48_fftmag_1400_eval" };
    String[] args_48_fft_cg = { "test_recorded/5people_features_independent/clio_2mic_180s.txt_48_fftmag_1400_train",
        "test_recorded/5people_features_independent/gabor_2mic_180s.txt_48_fftmag_1400_train",
        "test_recorded/5people_features_independent/clio_2mic_180s.txt_48_fftmag_1400_eval",
        "test_recorded/5people_features_independent/gabor_2mic_180s.txt_48_fftmag_1400_eval" };
    EvaluateFeatures fEval_48_fft_jp = createFeatureEvaluator(args_48_fft_jp, 4, 4);
    EvaluateFeatures fEval_48_fft_cg = createFeatureEvaluator(args_48_fft_cg, 4, 4);


    // logffts
    String[] args_48_logfft_jp = {
        "test_recorded/5people_features_independent/joseph_2mic_180s.txt_48_logfftmag_1400_train",
        "test_recorded/5people_features_independent/patrick_2mic_180s.txt_48_logfftmag_1400_train",
        "test_recorded/5people_features_independent/joseph_2mic_180s.txt_48_logfftmag_1400_eval",
        "test_recorded/5people_features_independent/patrick_2mic_180s.txt_48_logfftmag_1400_eval" };
    String[] args_48_logfft_cg = {
        "test_recorded/5people_features_independent/clio_2mic_180s.txt_48_logfftmag_1400_train",
        "test_recorded/5people_features_independent/gabor_2mic_180s.txt_48_logfftmag_1400_train",
        "test_recorded/5people_features_independent/clio_2mic_180s.txt_48_logfftmag_1400_eval",
        "test_recorded/5people_features_independent/gabor_2mic_180s.txt_48_logfftmag_1400_eval" };
    EvaluateFeatures fEval_48_logfft_jp = createFeatureEvaluator(args_48_logfft_jp, 4, 4);
    EvaluateFeatures fEval_48_logfft_cg = createFeatureEvaluator(args_48_logfft_cg, 4, 4);


    // 16kHz features:

    // Raw
    String[] args_16_raw_jp = { "test_recorded/5people_features_independent/joseph_2mic_180s.txt_16_raw_train",
        "test_recorded/5people_features_independent/patrick_2mic_180s.txt_16_raw_train",
        "test_recorded/5people_features_independent/joseph_2mic_180s.txt_16_raw_eval",
        "test_recorded/5people_features_independent/patrick_2mic_180s.txt_16_raw_eval" };
    String[] args_16_raw_cg = { "test_recorded/5people_features_independent/clio_2mic_180s.txt_16_raw_train",
        "test_recorded/5people_features_independent/gabor_2mic_180s.txt_16_raw_train",
        "test_recorded/5people_features_independent/clio_2mic_180s.txt_16_raw_eval",
        "test_recorded/5people_features_independent/gabor_2mic_180s.txt_16_raw_eval" };
    EvaluateFeatures fEval_16_raw_jp = createFeatureEvaluator(args_16_raw_jp, 3, 3);
    EvaluateFeatures fEval_16_raw_cg = createFeatureEvaluator(args_16_raw_cg, 3, 3);

    // FFTs
    String[] args_16_fft_jp = { "test_recorded/5people_features_independent/joseph_2mic_180s.txt_16_fftmag_train",
        "test_recorded/5people_features_independent/patrick_2mic_180s.txt_16_fftmag_train",
        "test_recorded/5people_features_independent/joseph_2mic_180s.txt_16_fftmag_eval",
        "test_recorded/5people_features_independent/patrick_2mic_180s.txt_16_fftmag_eval" };
    String[] args_16_fft_cg = { "test_recorded/5people_features_independent/clio_2mic_180s.txt_16_fftmag_train",
        "test_recorded/5people_features_independent/gabor_2mic_180s.txt_16_fftmag_train",
        "test_recorded/5people_features_independent/clio_2mic_180s.txt_16_fftmag_eval",
        "test_recorded/5people_features_independent/gabor_2mic_180s.txt_16_fftmag_eval" };
    EvaluateFeatures fEval_16_fft_jp = createFeatureEvaluator(args_16_fft_jp, 4, 4);
    EvaluateFeatures fEval_16_fft_cg = createFeatureEvaluator(args_16_fft_cg, 4, 4);

    // logFFTs
    String[] args_16_logfft_jp = {
        "test_recorded/5people_features_independent/joseph_2mic_180s.txt_16_logfftmag_train",
        "test_recorded/5people_features_independent/patrick_2mic_180s.txt_16_logfftmag_train",
        "test_recorded/5people_features_independent/joseph_2mic_180s.txt_16_logfftmag_eval",
        "test_recorded/5people_features_independent/patrick_2mic_180s.txt_16_logfftmag_eval" };
    String[] args_16_logfft_cg = { "test_recorded/5people_features_independent/clio_2mic_180s.txt_16_logfftmag_train",
        "test_recorded/5people_features_independent/gabor_2mic_180s.txt_16_logfftmag_train",
        "test_recorded/5people_features_independent/clio_2mic_180s.txt_16_logfftmag_eval",
        "test_recorded/5people_features_independent/gabor_2mic_180s.txt_16_logfftmag_eval" };
    EvaluateFeatures fEval_16_logfft_jp = createFeatureEvaluator(args_16_logfft_jp, 4, 4);
    EvaluateFeatures fEval_16_logfft_cg = createFeatureEvaluator(args_16_logfft_cg, 4, 4);


    // MFCC rectangular window
    String[] args_16_mfcc_rect_jp = {
        "test_recorded/5people_features_independent/joseph_2mic_180s.txt_16_melfcc_new_RE0_train",
        "test_recorded/5people_features_independent/patrick_2mic_180s.txt_16_melfcc_new_RE0_train",
        "test_recorded/5people_features_independent/joseph_2mic_180s.txt_16_melfcc_new_RE0_eval",
        "test_recorded/5people_features_independent/patrick_2mic_180s.txt_16_melfcc_new_RE0_eval" };
    String[] args_16_mfcc_rect_cg = {
        "test_recorded/5people_features_independent/clio_2mic_180s.txt_16_melfcc_new_RE0_train",
        "test_recorded/5people_features_independent/gabor_2mic_180s.txt_16_melfcc_new_RE0_train",
        "test_recorded/5people_features_independent/clio_2mic_180s.txt_16_melfcc_new_RE0_eval",
        "test_recorded/5people_features_independent/gabor_2mic_180s.txt_16_melfcc_new_RE0_eval" };

    EvaluateFeatures fEval_16_mfcc_rect_jp = createFeatureEvaluator(args_16_mfcc_rect_jp, 4, 390);
    EvaluateFeatures fEval_16_mfcc_rect_cg = createFeatureEvaluator(args_16_mfcc_rect_cg, 4, 390);

    // MFCC hamming window
    String[] args_16_mfcc_hamm_jp = {
        "test_recorded/5people_features_independent/joseph_2mic_180s.txt_16_melfcc_new_ME0_train",
        "test_recorded/5people_features_independent/patrick_2mic_180s.txt_16_melfcc_new_ME0_train",
        "test_recorded/5people_features_independent/joseph_2mic_180s.txt_16_melfcc_new_ME0_eval",
        "test_recorded/5people_features_independent/patrick_2mic_180s.txt_16_melfcc_new_ME0_eval" };

    String[] args_16_mfcc_hamm_cg = {
        "test_recorded/5people_features_independent/clio_2mic_180s.txt_16_melfcc_new_ME0_train",
        "test_recorded/5people_features_independent/gabor_2mic_180s.txt_16_melfcc_new_ME0_train",
        "test_recorded/5people_features_independent/clio_2mic_180s.txt_16_melfcc_new_ME0_eval",
        "test_recorded/5people_features_independent/gabor_2mic_180s.txt_16_melfcc_new_ME0_eval" };

    EvaluateFeatures fEval_16_mfcc_hamm_jp = createFeatureEvaluator(args_16_mfcc_hamm_jp, 4, 390);
    EvaluateFeatures fEval_16_mfcc_hamm_cg = createFeatureEvaluator(args_16_mfcc_hamm_cg, 4, 390);


    // Check the number of persons, training and evaluation samples for all
    // feature evaluators:

    logfile.append("Number of Speakers: 5\n");
    logfile
        .append("Sarsa, epsilon=0.1, gamma=lambda=0.0, roughly 22000 total features and 5800 active features, activeFeature\n");
    logfile.append("Data about the audio feature sets:\n");


    logfile.append("48_fft: " + fEval_48_fft_cg.getSampleSizes()[0] + " " + fEval_48_fft_cg.getSampleSizes()[1] + " "
        + fEval_48_fft_cg.getSampleSizes()[2] + " " + fEval_48_fft_cg.getSampleSizes()[3] + " "
        + fEval_48_fft_cg.getSampleSizes()[4] + " " + fEval_48_fft_cg.getSampleSizes()[5] + "\n");
    logfile.append("48_logftt: " + fEval_48_logfft_cg.getSampleSizes()[0] + " "
        + fEval_48_logfft_cg.getSampleSizes()[1] + " " + fEval_48_logfft_cg.getSampleSizes()[2] + " "
        + fEval_48_logfft_cg.getSampleSizes()[3] + " " + fEval_48_logfft_cg.getSampleSizes()[4] + " "
        + fEval_48_logfft_cg.getSampleSizes()[5] + "\n");
    logfile.append("16_raw: " + fEval_16_raw_cg.getSampleSizes()[0] + " " + fEval_16_raw_cg.getSampleSizes()[1] + " "
        + fEval_16_raw_cg.getSampleSizes()[2] + " " + fEval_16_raw_cg.getSampleSizes()[3] + " "
        + fEval_16_raw_cg.getSampleSizes()[4] + " " + fEval_16_raw_cg.getSampleSizes()[5] + "\n");
    logfile.append("16_fft: " + fEval_16_fft_cg.getSampleSizes()[0] + " " + fEval_16_fft_cg.getSampleSizes()[1] + " "
        + fEval_16_fft_cg.getSampleSizes()[2] + " " + fEval_16_fft_cg.getSampleSizes()[3] + " "
        + fEval_16_fft_cg.getSampleSizes()[4] + " " + fEval_16_fft_cg.getSampleSizes()[5] + "\n");
    logfile.append("16_mfcc_rect: " + fEval_16_mfcc_rect_cg.getSampleSizes()[0] + " "
        + fEval_16_mfcc_rect_cg.getSampleSizes()[1] + " " + fEval_16_mfcc_rect_cg.getSampleSizes()[2] + " "
        + fEval_16_mfcc_rect_cg.getSampleSizes()[3] + " " + fEval_16_mfcc_rect_cg.getSampleSizes()[4] + " "
        + fEval_16_mfcc_rect_cg.getSampleSizes()[5] + "\n");
    logfile.append("16_mfcc_hamm: " + fEval_16_mfcc_hamm_cg.getSampleSizes()[0] + " "
        + fEval_16_mfcc_hamm_cg.getSampleSizes()[1] + " " + fEval_16_mfcc_hamm_cg.getSampleSizes()[2] + " "
        + fEval_16_mfcc_hamm_cg.getSampleSizes()[3] + " " + fEval_16_mfcc_hamm_cg.getSampleSizes()[4] + " "
        + fEval_16_mfcc_hamm_cg.getSampleSizes()[5] + "\n");
    logfile.append("16_logfft: " + fEval_16_logfft_cg.getSampleSizes()[0] + " "
        + fEval_16_logfft_cg.getSampleSizes()[1] + " " + fEval_16_logfft_cg.getSampleSizes()[2] + " "
        + fEval_16_logfft_cg.getSampleSizes()[3] + " " + fEval_16_logfft_cg.getSampleSizes()[4] + " "
        + fEval_16_logfft_cg.getSampleSizes()[5] + "\n");


    // Carry out some training and evaluation!
    int[] logarithmicScale = { 10, 90, 900, 9000, 90000 };
    int totalNbOfSteps = 0;
    String dataOutput = new String();
    int[] chosenPersons;
    int[] chosenSamples;
    logfile.append("\nResults (Accuracy):\n");
    logfile
        .append("# of Training steps | 48_fft_cg | 48_logfft_cg | 16_raw_cg | 16_fft_cg |16_logfft_cg | 16_mfcc_rect_cg | 16_mfcc_hamm_cg | | 48_fft_jp | 48_logfft_jp | 16_raw_jp | 16_fft_jp |16_logfft_jp | 16_mfcc_rect_jp | 16_mfcc_hamm_jp | \n\n");
    for (int n : logarithmicScale) {
      // Create random chosen sample arrays for iteration:
      chosenPersons = Arrays.randIntArray(n, 0, 2);
      chosenSamples = Arrays.randIntArray(n, 0, 527);

      // Train all FeatureEvaluatos with the same chosenPersons and
      // chosenSamples:
      fEval_48_fft_cg.train(n, chosenPersons, chosenSamples);
      fEval_48_logfft_cg.train(n, chosenPersons, chosenSamples);
      fEval_16_raw_cg.train(n, chosenPersons, chosenSamples);
      fEval_16_fft_cg.train(n, chosenPersons, chosenSamples);
      fEval_16_logfft_cg.train(n, chosenPersons, chosenSamples);
      fEval_16_mfcc_rect_cg.train(n, chosenPersons, chosenSamples);
      fEval_16_mfcc_hamm_cg.train(n, chosenPersons, chosenSamples);

      fEval_48_fft_jp.train(n, chosenPersons, chosenSamples);
      fEval_48_logfft_jp.train(n, chosenPersons, chosenSamples);
      fEval_16_raw_jp.train(n, chosenPersons, chosenSamples);
      fEval_16_fft_jp.train(n, chosenPersons, chosenSamples);
      fEval_16_logfft_jp.train(n, chosenPersons, chosenSamples);
      fEval_16_mfcc_rect_jp.train(n, chosenPersons, chosenSamples);
      fEval_16_mfcc_hamm_jp.train(n, chosenPersons, chosenSamples);


      // Evaluate all, and print the Results to the logfile:
      totalNbOfSteps += n;
      dataOutput = new String(totalNbOfSteps + ";     " + fEval_48_fft_cg.evaluate() + ";     "
          + fEval_48_logfft_cg.evaluate() + ";     " + fEval_16_raw_cg.evaluate() + ";     "
          + fEval_16_fft_cg.evaluate() + ";     " + fEval_16_logfft_cg.evaluate() + ";     "
          + fEval_16_mfcc_rect_cg.evaluate() + ";     " + fEval_16_mfcc_hamm_cg.evaluate() + fEval_48_fft_jp.evaluate()
          + ";     " + fEval_48_logfft_jp.evaluate() + ";     " + fEval_16_raw_jp.evaluate() + ";     "
          + fEval_16_fft_jp.evaluate() + ";     " + fEval_16_logfft_jp.evaluate() + ";     "
          + fEval_16_mfcc_rect_jp.evaluate() + ";     " + fEval_16_mfcc_hamm_jp.evaluate() + "\n");
      //
      //
      //
      System.out.print(dataOutput);
      logfile.append(dataOutput);

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
                                                             gridResolution, nbOfTilings);
    return featureEvaluator;
  }

}
