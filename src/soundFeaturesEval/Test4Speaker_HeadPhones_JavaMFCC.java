package soundFeaturesEval;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;


public class Test4Speaker_HeadPhones_JavaMFCC {

  /**
   * @param args
   * @throws ClassNotFoundException
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {

    FileWriter logfile = new FileWriter(
                                        "test_data/test_recorded/4speaker_obamaMerkelBush_Headphones_JavaMFCC/results_TEST.txt");

    // Carry out some 4 person Feature evaluation:
    // Christian, Clio, Gabor, Joseph, Patrick:

    // Create the corresponding Feature Evaluators:

    // 48kHz features:

    // FFTs:
    String[] featureSet = { "test_data/test_recorded/4speaker_obamaMerkelBush_Headphones_JavaMFCC/obama",
        "test_data/test_recorded/4speaker_obamaMerkelBush_Headphones_JavaMFCC/merkel",
        "test_data/test_recorded/4speaker_obamaMerkelBush_Headphones_JavaMFCC/bush_senior",
        "test_data/test_recorded/4speaker_obamaMerkelBush_Headphones_JavaMFCC/bush_junior" };
    EvaluateFeatures featureEvaluator = createFeatureEvaluator(featureSet, 4, 4);


    // Check the number of persons, training and evaluation samples for all
    // feature evaluators:

    logfile.append("Number of Speakers: 4\n");
    logfile.append("Sarsa, epsilon=0.1, gamma=lambda=0.0, activeFeature\n");
    logfile.append("Data about the audio feature sets:\n");


    logfile.append("48_logFFTmag: " + featureEvaluator.getSampleSizes()[0] + " " + featureEvaluator.getSampleSizes()[1]
        + " " + featureEvaluator.getSampleSizes()[2] + " " + featureEvaluator.getSampleSizes()[3] + " "
        + featureEvaluator.getSampleSizes()[4] + " " + featureEvaluator.getSampleSizes()[5] + "\n");


    // Carry out some training and evaluation!
    int[] logarithmicScale = { 10, 90, 900, 9000, 900000 };
    int totalNbOfSteps = 0;
    String dataOutput = new String();
    int[] chosenPersons;
    int[] chosenSamples;
    logfile.append("\nResults (Accuracy):\n");
    logfile.append("# of Training steps \n\n");
    for (int iteration = 0; iteration < 1; iteration++) {
      for (int n : logarithmicScale) {
        // Create random chosen sample arrays for iteration:
        chosenPersons = CDArrays.randIntArray(n, 0, 4);
        chosenSamples = CDArrays.randIntArray(n, 0, 175);

        // Train all FeatureEvaluatos with the same chosenPersons and
        // chosenSamples:
        featureEvaluator.train(n, chosenPersons, chosenSamples);


        // Evaluate all, and print the Results to the logfile:
        totalNbOfSteps += n;
        dataOutput = new String(totalNbOfSteps + ";" + featureEvaluator.evaluate() + "\n");
        //
        //
        //
        System.out.print(dataOutput);
        logfile.append(dataOutput);
      }
      featureEvaluator.reset();
      logfile.append("\n\n");
    }
    logfile.close();
  }

  private static EvaluateFeatures createFeatureEvaluator(String[] args, int gridResolution, int nbOfTilings) {
    // The first half will be interpreted as training data, the second half as
    // evaluation / Test data.
    // First, read input objects:
    double[][][] inputData = new double[args.length][][];
    double[][][] training = new double[4][175][14];
    double[][][] evaluation = new double[4][175][14];

    FileInputStream fis = null;
    ObjectInputStream in = null;

    // Read the Files:
    for (int n = 0; n < args.length; n++) {
      try {
        fis = new FileInputStream(args[n]);
        in = new ObjectInputStream(fis);
        inputData[n] = ((double[][]) in.readObject()).clone();
        in.close();
      } catch (IOException ex) {
        ex.printStackTrace();
      } catch (ClassNotFoundException ex) {
        ex.printStackTrace();
      }
    }
    // Input reading is done...
    for (int step = 0; step < 175; step++) {
      for (int person = 0; person < 4; person++) {
        training[person][step] = inputData[person][step];
        evaluation[person][step] = inputData[person][step + 175];
      }
    }


    // Create new Class instance with that data and the percentage of the data
    // to be used as training
    EvaluateFeatures featureEvaluator = new EvaluateFeatures(training, evaluation, 0.0, 0.1, 0.0, gridResolution,
                                                             nbOfTilings, false, true, 4, true);
    return featureEvaluator;
  }

}
