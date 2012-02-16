package nao;

import java.util.Random;

public class CDArrays {
  public static double max(double[] array) {
    // find the maximum
    double max = Double.NEGATIVE_INFINITY;
    for (int i = 0; i < array.length; i++) {
      if (array[i] > max)
        max = array[i];
    }
    // System.out.println("max = " + max);
    return max;
  }

  public static double max(int[] array) {
    // find the maximum
    double max = Double.NEGATIVE_INFINITY;
    for (int i = 0; i < array.length; i++) {
      if (array[i] > max)
        max = array[i];
    }
    // System.out.println("max = " + max);
    return max;
  }

  public static double average(double[] array) {
    // average
    double sum = 0.0;
    for (int i = 0; i < array.length; i++) {
      sum += array[i];
    }
    double average = sum / array.length;
    return average;
  }

  public static double min(double[] array) {
    // find the maximum
    double min = Double.POSITIVE_INFINITY;
    for (int i = 0; i < array.length; i++) {
      if (array[i] < min)
        min = array[i];
    }
    // System.out.println("max = " + max);
    return min;
  }

  public static double[] logarithm(double[] array) {
    // average
    double[] logArray = new double[array.length];
    for (int i = 0; i < array.length; i++) {
      logArray[i] = Math.log(array[i]);
    }
    return logArray;
  }

  public static int[] randIntArray(int size, int lowerBound, int upperBound) {
    // Creates an int array of size "size" with lower bound (inclusively) and
    // upper bound (exclusively)
    // be reached.
    int[] finalArray = new int[size];
    Random generator = new Random();

    int offset = upperBound - lowerBound;

    for (int n = 0; n < size; n++) {
      finalArray[n] = generator.nextInt(offset) + lowerBound;
    }
    return finalArray;
  }

  public static double sum(double[] array) {
    double arraySum = 0;
    for (int n = 0; n < array.length; n++) {
      arraySum += array[n];
    }
    return arraySum;
  }

  public static double sum(int[] array) {
    double arraySum = 0;
    for (int n = 0; n < array.length; n++) {
      arraySum += array[n];
    }
    return arraySum;
  }

  public static double[] multiplyTo(double[] array, double factor) {
    double[] outputArray = new double[array.length];
    for (int n = 0; n < array.length; n++) {
      outputArray[n] = array[n] * factor;
    }
    return outputArray;
  }

  public static double[] histArray(double[] array) {
    // Creates a "histogram" array, where the absolute values of the array sum
    // up to 1:
    double sum = 0;
    for (int n = 0; n < array.length; n++) {
      sum += Math.abs(array[n]);
    }
    return multiplyTo(array, 1 / sum);
  }

  public static String arrayToString(double[] array) {
    String out = new String();
    for (int n = 0; n < array.length - 1; n++) {
      out = out.concat(array[n] + ";");
    }
    out = out.concat(Double.toString(array[array.length - 1]));
    return out;
  }

  public static double energy(double[] array) {
    double energy = 0;
    for (int n = 0; n < array.length; n++) {
      energy += array[n] * array[n];
    }
    return energy;
  }

}
