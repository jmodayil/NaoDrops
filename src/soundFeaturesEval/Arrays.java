package soundFeaturesEval;

import java.util.Random;

public class Arrays {
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

}
