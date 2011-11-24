package naoDemos;

import nao.NaoRobot;
import rltoys.algorithms.learning.predictions.td.TDLambda;
import rltoys.environments.envio.observations.Legend;
import rltoys.math.vector.implementations.SVector;
import zephyr.plugin.core.api.Zephyr;
import zephyr.plugin.core.api.monitoring.annotations.Monitor;
import zephyr.plugin.core.api.synchronization.Clock;

public class VisualNexting implements Runnable {
  @Monitor
  private final NaoRobot robot;
  private final Clock clock = new Clock();
  @Monitor
  double reward;
  TDLambda tdl;
  @Monitor
  double prediction;
  int neckAngle;
  int nbFeatures = 64;

  public VisualNexting() {
    robot = new NaoRobot();
    Legend legend = robot.legend();
    neckAngle = legend.indexOf("HeadYaw-Position");
    double alpha = .1 / nbFeatures;
    double gamma = .9;
    double lambda = 0;
    tdl = new TDLambda(lambda, gamma, alpha, nbFeatures);
    Zephyr.advertise(clock, this);
  }

  @Override
  public void run() {
    SVector x_t = null;
    while (!clock.isTerminated()) {
      double[] obs = robot.newObsNow();
      SVector x_tp1 = new SVector(nbFeatures);
      reward = extractGreen();
      prediction = tdl.predict(x_tp1);
      tdl.update(x_t, x_tp1, reward);
      clock.tick();
    }
  }

  private double extractGreen() {

    int blue = 0, count = 0;
    for (int i : robot.argbBuffer.array()) {
      int r = i / 256 * 256 % 256;
      int g = i / 256 % 256;
      int b = i % 256;
      if (b > 240 & r < 200 & g < 200)
        blue++;
      count++;
    }
    return (double) blue / count;
  }


}
