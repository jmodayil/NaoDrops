package AudioPrediction;

import java.io.IOException;

import nao.NaoAction;
import nao.NaoRobot;

import org.apache.commons.lang3.ArrayUtils;

public class ResponsivenessCheck {

  /**
   * @param args
   * @throws IOException
   * @throws IllegalArgumentException
   */
  public static void main(String[] args) throws IllegalArgumentException, IOException {
    // TODO Auto-generated method stub
    // seconds, filename as parameters
    NaoRobot robot = new NaoRobot();
    double[] obsArray;
    double[] meanMFCCs;
    MFCCProvider mfcc = new MFCCProvider();
    NaoAction action = new NaoAction();
    double[] leds = null;

    // Wait for the desired number of Steps and record the sound Data:
    while (true) {
      // Wait for new sound Data:
      obsArray = robot.waitNewObs();
      meanMFCCs = mfcc.getMeanMfccVector(ArrayUtils.subarray(obsArray, 83, obsArray.length));

      if (obsArray[67] > 0.1) {
        System.out.println("NaoMarks found!");
        leds = NaoAction.setFaceLeds("green");
      } else {
        System.out.println("NO!! NaoMarks found!");
        leds = NaoAction.setFaceLeds("red");
      }
      action.set(null, 0.0, null, leds, null);
      robot.sendAction(action);
    }

  }
}