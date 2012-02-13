package soundFeaturesEval;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import nao.NaoRobot;

public class RecordSoundDataFromNao {
  double[] obsArray;
  double[] featureArray;
  double[] oldValues = new double[3];
  int steps;
  NaoRobot robot = new NaoRobot();
  double[][] features;
  int nbOfFeatures;

  FileWriter fstream;
  BufferedWriter out;

  /**
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {
    // TODO Auto-generated method stub
    // seconds, filename as parameters

    double seconds = 45.0;


    RecordSoundDataFromNao featureRecorder = new RecordSoundDataFromNao(seconds, args[0]);
    featureRecorder.recordFeatures();
  }

  public RecordSoundDataFromNao(double seconds, String filename) throws IOException {
    // Convert seconds to number of steps: sampling rate: 48000Hz, 8192 samples
    // per step. --> for n seconds, we need n*48000/8192 steps:
    steps = (int) (seconds * 16000 / 2731.0);
    System.out.println("Total Steps: " + steps);
    obsArray = robot.waitNewObs();
    nbOfFeatures = obsArray.length - 67;
    features = new double[steps][nbOfFeatures];
    System.out.println("There are " + nbOfFeatures + " Features per step!");

    fstream = new FileWriter(filename);
    out = new BufferedWriter(fstream);
  }

  public void recordFeatures() throws IOException {
    int step = 0;
    double starttime = 0;
    double endtime = 0;
    starttime = System.currentTimeMillis();
    // Wait for the desired number of Steps and record the sound Data:
    while (step < steps) {
      // Wait for new sound Data:
      this.waitNewSound();
      // Save the sound features to the double array:
      for (int n = 0; n < nbOfFeatures; n++) {
        out.write(obsArray[67 + n] + "; ");
      }
      out.write("\n");
      System.out.println("Current Step: " + step);
      step++;
    }
    endtime = System.currentTimeMillis();
    System.out.println("Time:" + (endtime - starttime));
    out.close();

  }

  private void waitNewSound() {
    obsArray = robot.waitNewObs();
    while (obsArray[100] == oldValues[0] || obsArray[200] == oldValues[1] || obsArray[300] == oldValues[2]) {
      obsArray = robot.waitNewObs();
      System.out.println("no new sound :-(");
    }
    oldValues[0] = obsArray[100];
    oldValues[1] = obsArray[200];
    oldValues[2] = obsArray[300];
    return;
  }
}