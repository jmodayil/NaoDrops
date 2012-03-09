package AudioPrediction;

import java.io.Serializable;

import zephyr.plugin.core.api.monitoring.annotations.Monitor;

public class AverageRewardInitialized implements Serializable {
	private static final long serialVersionUID = -3552016939867752720L;
	private final double alpha_j;
  @Monitor
  private double r;
  @Monitor
  private double j;
  @Monitor
  private double r_diff;

  public AverageRewardInitialized(double alpha_j, double initialValue) {
    this.alpha_j = alpha_j;
    this.j = initialValue;
  }

  public double average(double r_tp1) {
    this.r = r_tp1;
    j = (1 - alpha_j) * j + alpha_j * r;
    r_diff = r - j;
    return r_diff;
  }

public double getAverage() {
	// TODO Auto-generated method stub
	return j;
}
}
