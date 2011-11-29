package nao;

import rltoys.math.vector.RealVector;
import rltoys.math.vector.implementations.AbstractVector;

public class MathUtilsCD {


  public static double expectation(AbstractVector inputVector) {
    // Only use input vectors with positive values! They are then interpreted as
    // a probability distribution.

    // make a copy of the vector:
    RealVector vector = inputVector.copyAsMutable();

    double sum = 0;
    double val = 0;
    int size = vector.getDimension();


    // Normalization:
    for (int n = 0; n < size; n++) {
      sum += vector.getEntry(n);
    }
    sum = 1 / sum;
    vector = vector.mapMultiply(sum);

    // Calculate expectation:
    for (int n = 0; n < size; n++) {
      val += n * vector.getEntry(n);
    }
    return val;
  }


  public static double variance(AbstractVector inputVector, double mean) {
    // Only use input vectors with positive values! They are then interpreted as
    // a probability distribution.

    // make a copy of the vector:
    RealVector vector = inputVector.copyAsMutable();

    double sum = 0;
    double val = 0;
    int size = vector.getDimension();


    // Normalization:
    for (int n = 0; n < size; n++) {
      sum += vector.getEntry(n);
    }
    sum = 1 / sum;
    vector = vector.mapMultiply(sum);


    // Calculate Variance:
    for (int n = 0; n < size; n++) {
      val += n * n * vector.getEntry(n);
    }
    return val + mean * mean;
  }
}
