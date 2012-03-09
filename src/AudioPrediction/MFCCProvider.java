package AudioPrediction;


import java.io.IOException;
import java.util.Arrays;

import nao.CDArrays;

import org.apache.commons.lang3.ArrayUtils;


import comirva.audio.util.MFCC;

public class MFCCProvider {

  private MFCC mfccGenerator = null;
  private final int hopSize;
  private final boolean performHighPass;
  private final boolean includeSignalEnergy;
  private FIR filter;


  public MFCCProvider() {
    this(16000, 256, 13, true, 20, 16000, 40, false, true);
  }

  public MFCCProvider(float fs, int windowSize, int nc, boolean useFirst, double minFreq, double maxFreq,
      int numberFilters, boolean performHighPass, boolean includeSignalEnergy) {
    this.hopSize = windowSize / 2;
    this.performHighPass = performHighPass;
    this.includeSignalEnergy = includeSignalEnergy;
    if (performHighPass) {
      double[] coefficients = { 1, -0.97 };
      filter = new FIR(coefficients);
    }
    mfccGenerator = new MFCC(fs, windowSize, nc, useFirst, minFreq, maxFreq, numberFilters);
  }


  public double[][] processTestData(double[][] testData) throws IllegalArgumentException, IOException {
    double[][] output = new double[testData.length][];
    double[] singleFrameOutput = null;
    for (int n = 0; n < testData.length; n++) {
      singleFrameOutput = this.getMeanMfccVector(testData[n]);
      output[n] = singleFrameOutput;
    }
    return output;
  }

  public double[][][] processTestData(double[][][] testData) throws IllegalArgumentException, IOException {
    double[][][] output = new double[testData.length][testData[0].length][];
    double[] singleFrameOutput = null;
    for (int n = 0; n < testData.length; n++) {
      for (int m = 0; m < testData[0].length; m++) {
        singleFrameOutput = this.getMeanMfccVector(testData[n][m]);
        output[n][m] = singleFrameOutput;
      }
    }
    return output;
  }

  public double[][] processTimeFrame(double[] frame) throws IllegalArgumentException, IOException {
    int length = frame.length;
    int cutDownLength = length - (length % hopSize);
    double[] cutFrame;

    if (performHighPass) {
      cutFrame = Arrays.copyOf(performFIR(frame), cutDownLength);
    } else {
      cutFrame = Arrays.copyOf(frame, cutDownLength);
    }
    return mfccGenerator.process(cutFrame);
  }

  public double[] getMeanMfccVector(double[] frame) throws IllegalArgumentException, IOException {
    double[][] mfccs = this.processTimeFrame(frame);
    double[] meanMfccs = new double[mfccs[0].length];
    for (int m = 0; m < mfccs[0].length; m++) {
      for (int n = 1; n < mfccs.length-1; n++) {
        meanMfccs[m] += mfccs[n][m];
      }
      meanMfccs[m] /= (mfccs.length-2.0);
    }
//    System.out.println("Number of MFCC Frames: " + mfccs.length);
    if (includeSignalEnergy) {
      return ArrayUtils.add(meanMfccs, 0, Math.log(CDArrays.energy(frame)));
    }
    return meanMfccs;
  }

  private double[] performFIR(double[] data) {
    double[] filteredData = new double[data.length];
    for (int n = 0; n < data.length; n++) {
      filteredData[n] = filter.getOutputSample(data[n]);
    }
    return filteredData;
  }

}
