package soundFeaturesEval;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import nao.NaoRobot;
import rltoys.math.vector.implementations.PVector;

public class RecordFeatures {
  double[] obsArray;
  double[] featureArray;
  double[] oldFFTvalues = new double[3];
  int steps;
  NaoRobot robot = new NaoRobot();
  PVector[] features;
  int nbOfFeatures;
  private final String filename;
  FileOutputStream fos = null;
  ObjectOutputStream out = null;

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

    double seconds = 10.0;
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
    featureRecorder.recordFeatures();
  }

  public RecordFeatures(double seconds, String filename) {
    // Convert seconds to number of steps: sampling rate: 48000Hz, 8192 samples
    // per step. --> for n seconds, we need n*48000/8192 steps:
    steps = (int) (seconds * 48000 / 8192.0);
    features = new PVector[steps];
    this.filename = filename;

    obsArray = robot.waitNewObs();
    nbOfFeatures = obsArray.length - 67;
    System.out.println("There are " + nbOfFeatures + " Features per step!");
  }

  public void recordFeatures() {
    int step = 0;
    // Wait for the desired number of Steps and record the sound Data:
    while (step < steps) {
      features[step] = new PVector(nbOfFeatures);
      // Wait for new sound Data:
      this.waitNewSound();
      // Save the sound features to the PVector array:
      for (int n = 0; n < nbOfFeatures; n++) {
        features[step].setEntry(n, obsArray[67 + n]);
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

  private void waitNewSound() {
    obsArray = robot.waitNewObs();
    while (obsArray[100] == oldFFTvalues[0] || obsArray[200] == oldFFTvalues[1] || obsArray[1000] == oldFFTvalues[2]) {
      obsArray = robot.waitNewObs();
    }
    oldFFTvalues[0] = obsArray[100];
    oldFFTvalues[1] = obsArray[200];
    oldFFTvalues[2] = obsArray[300];
    return;
  }
}