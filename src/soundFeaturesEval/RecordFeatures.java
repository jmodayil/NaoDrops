package soundFeaturesEval;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import nao.NaoRobot;

import org.apache.commons.lang3.ArrayUtils;

import AudioPrediction.MFCCProvider;

public class RecordFeatures {
  double[] obsArray;
  double[] featureArray;
  double[] oldFFTvalues = new double[3];
  int steps;
  NaoRobot robot = new NaoRobot();
  double[][] features;
  int nbOfFeatures;
  private final String filename;
  FileOutputStream fos = null;
  ObjectOutputStream out = null;

  // MFCC Processor:
  MFCCProvider mfccProc = new MFCCProvider();
  double[] meanMFCCs;

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub
    // seconds, filename as parameters

    double seconds = 90.0;
    String filename = "featuresObject";

    if (args.length > 0) {
      try {
        seconds = Double.parseDouble(args[0]);
      } catch (NumberFormatException e) {
        System.err.println("Argument must be a double");
        System.exit(1);
      }
      filename = args[1];
    }


    RecordFeatures featureRecorder = new RecordFeatures(seconds, filename);
    try {
      featureRecorder.recordFeatures();
    } catch (IllegalArgumentException e) {
      System.out.println("ERROR!!");
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      System.out.println("ERROR!!");
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public RecordFeatures(double seconds, String filename) {
    // Convert seconds to number of steps: sampling rate: 48000Hz, 8192 samples
    // per step. --> for n seconds, we need n*48000/8192 steps:
    steps = (int) (seconds * 48000.0 / 8192.0);

    this.filename = filename;

    obsArray = robot.waitNewObs();
    nbOfFeatures = obsArray.length - 83;
    features = new double[steps][14];
    System.out.println("There are " + nbOfFeatures + " Features per step!");
  }

  public void recordFeatures() throws IllegalArgumentException, IOException {
    int step = 0;
    // Wait for the desired number of Steps and record the sound Data:
    while (step < steps) {
      // Wait for new sound Data:
      obsArray = robot.waitNewObs();

      meanMFCCs = mfccProc.getMeanMfccVector(ArrayUtils.subarray(obsArray, 83, obsArray.length));

      // Save the sound features to the double array:
      for (int n = 0; n < 14; n++) {
        features[step][n] = meanMFCCs[n];
      }
      System.out.println("Current Step: " + step);
      step++;
    }
    // Save the features to a file:
    writeToDisk();
  }

  private void writeToDisk() {
    try {
      fos = new FileOutputStream(filename);
      out = new ObjectOutputStream(fos);
      out.writeObject(features);
      out.close();
    } catch (IOException ex) {
      ex.printStackTrace();
    }

  }
}