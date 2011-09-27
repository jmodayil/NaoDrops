package nao;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import rltoys.environments.envio.observations.TStep;
import zephyr.plugin.core.api.monitoring.fileloggers.TimedFileLogger;
import zephyr.plugin.core.api.synchronization.Clock;

//I should add monitoring and logging.

public class NaoTest {
  NaoAction na;
  int i;
  int j;
  int mm;
  double[] joints;
  double[] stiffness;
  double[] leds;
  double[] sounds;
  int mstep;
  private final TimedFileLogger logger;
  private int stage;
  long lastTime;

  Random random;
  private final Clock clock;
  private final NaoRobot R;

  public static void main(String[] args) throws IOException {
    NaoRobot N = new NaoRobot();
    new NaoTest(N, null).run();
  }


  public NaoTest(NaoRobot R, Clock clock) throws IOException {
    na = new NaoAction();
    this.clock = clock;
    na.set(null, .1, null, null, null);
    lastTime = System.currentTimeMillis();
    this.R = R;

    i = 0;
    j = 0;
    mm = 0;
    mstep = 0;
    joints = new double[14];
    for (int i = 0; i < 14; i++)
      joints[i] = 0.5;
    stiffness = new double[14];

    logger = new TimedFileLogger("./start" + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()) + ".naolog");

    random = new Random();
    stage = 0;
    leds = new double[83];
    sounds = new double[3];
    logger.add(R);
  }

  public void run() {
    run(new Clock("NaoEnvironment"));
  }

  public void run(Clock clock) {
    double[] obsArray = R.waitNewObs(); // CD: R is the NaoRobot instance


    // TStep currentStep = new TStep(naoConnection.lastObservationDropTime(),
    // (double[]) null, null, obsArray);
    while (!R.isClosed() && !clock.isTerminated()) {
      clock.tick(); // observes all variables for zephyr plot function
      NaoAction action = getAtp1(obsArray);
      R.sendAction(action);
      if (action == null)
        break;
      obsArray = R.waitNewObs();
      System.out.println(obsArray.length);
      // TStep lastStep = currentStep;
      // long time = naoConnection.lastObservationDropTime();
      // currentStep = new TStep(time, lastStep, action, obsArray);
    }
  }


  public NaoAction wgetAtp1(TStep step) {
    // TODO Auto-generated method stub
    // System.out.println("observation size " + step.o_tp1.length); // image
    // buffer
    // not reported
    // for (int i = 0; i < 60 && i < step.o_tp1.length; i++)
    // System.out.println(i + " " + step.o_tp1[i]);
    logger.update();
    if (i == 0 && j == 0)
      for (int k = 0; k < 83; k++)
        leds[k] = 0;
    leds[i] = j / 7.0;
    sounds[0] = i + 2;
    sounds[1] = (j + 1) * 1024;
    sounds[2] = .5;
    // joints[mm] = 0.5 + 0.5 * Math.sin(mstep * 2 * Math.PI / 100.0);
    // stiffness[mm] = .6;
    na.set(joints, .2, stiffness, leds, sounds);

    joints[mm] = 0.5;
    stiffness[mm] = 0;
    System.out.println("Motor: " + mm + " " + mstep + " ij: " + i + " " + j);

    j = (j + 1) % 8;
    if (j == 0)
      i = (i + 1) % 83;

    mstep = (mstep + 1) % 100;
    if (mstep == 0)
      mm = (mm + 1) % 14;

    return na;
  }


  public NaoAction getAtp1(double[] obs) {
    int end = 500;
    stage++;
    if (stage < 100) { // gradually stiffen the body to midpoint
      for (int i = 0; i < stiffness.length; i++)
        stiffness[i] = 0.8 * stage / 100.0;
      for (int i = 0; i < leds.length; i++)
        leds[i] = stage / 100.;
    } else if (stage < end) {
      // random walk the joints
      for (int i = 0; i < stiffness.length; i++) {
        double val = joints[i];
        val += (random.nextDouble() - .5) * .04;
        val = val > .7 ? .7 : val < .4 ? .4 : val;
        joints[i] = val;
      }
      for (int i = 0; i < leds.length; i++) {
        double val = leds[i];
        val += (random.nextDouble() - .5) * .1;
        val = val > 1. ? 1. : val < 0. ? 0. : val;
        leds[i] = val;

      }
    } else
      for (int i = 0; i < stiffness.length; i++)
        stiffness[i] = 0;
    // if (stage > end + 50)
    // return null;
    long nowTime = System.currentTimeMillis();


    System.out.print(" Count:" + stage + " time: " + (nowTime - lastTime));
    lastTime = nowTime;
    // for (int i = 0; i < 14; i++)
    // System.out.print(" " + joints[i]);
    System.out.println("\n");
    // joints[mm] = 0.5 + 0.5 * Math.sin(mstep * 2 * Math.PI / 100.0);
    // stiffness[mm] = .6;
    na.set(joints, .035, stiffness, leds, null);


    System.out.println("Motor: " + mm + " " + mstep + " ij: " + i + " " + j);

    logger.update();
    if (clock != null)
      clock.tick();

    return na;
  }


}
