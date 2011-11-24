package AudioPrediction;

import nao.NaoAction;
import nao.NaoRobot;
import rltoys.algorithms.representations.actions.Action;
import rltoys.environments.envio.actions.ActionArray;
import rltoys.environments.envio.observations.Legend;
import rltoys.environments.envio.observations.TRStep;
import rltoys.environments.envio.problems.ProblemBounded;
import rltoys.environments.envio.problems.ProblemDiscreteAction;
import rltoys.math.ranges.Range;
import rltoys.math.vector.implementations.PVector;
import zephyr.plugin.core.api.monitoring.annotations.Monitor;

@Monitor
public class SoundEnergyProblem implements ProblemBounded, ProblemDiscreteAction {
  protected static final ActionArray REDLED = new ActionArray(0.0, 0.0, 1.0);
  protected static final ActionArray GREENLED = new ActionArray(0.0, 1.0, 0.0);
  protected static final ActionArray BLUELED = new ActionArray(1.0, 0.0, 0.0);
  protected static final Action[] Actions = { REDLED, GREENLED, BLUELED };


  private double[] leds = new double[83];
  private final NaoRobot robot;
  private final NaoAction naoAct = new NaoAction();
  protected static final Range soundEnergyRange = new Range(60.0, 500.0);

  protected double soundEnergy = 0;

  private final double LOWERBORDER;
  private final double UPPERBORDER;

  protected static final String SOUNDENERGY = "soundEnergy";
  protected static final Legend legend = new Legend(SOUNDENERGY);

  double[] obsArray;

  private double reward;

  private final PVector soundMagnitudes0 = new PVector(1024);
  private final PVector soundMagnitudes1 = new PVector(1024);
  private final PVector soundMagnitudes2 = new PVector(1024);

  private TRStep lastTStep;

  public SoundEnergyProblem(NaoRobot R) {
    this(R, 150.0, 300.0);
  }

  public SoundEnergyProblem(NaoRobot R, double lowerBorder, double upperBorder) {
    robot = R;
    LOWERBORDER = lowerBorder;
    UPPERBORDER = upperBorder;
  }

  protected void update(ActionArray action) {
    // Get new observations from robot:
    obsArray = robot.waitNewObs();
    // Copy the sound observations to the soundMagnitudes vectors:
    for (int n = 0; n < 1024; n++) {
      soundMagnitudes0.setEntry(n, obsArray[67 + n]);
      soundMagnitudes1.setEntry(n, obsArray[67 + n + 1024]);
      soundMagnitudes2.setEntry(n, obsArray[67 + n + 2 * 1024]);
    }
    soundMagnitudes0.mapMultiplyToSelf(0.00001);
    soundMagnitudes1.mapMultiplyToSelf(0.00001);
    soundMagnitudes2.mapMultiplyToSelf(0.00001);
    // Calculate Sound Energy:
    soundEnergy = Math.sqrt(soundMagnitudes0.dotProduct(soundMagnitudes0))
        + Math.sqrt(soundMagnitudes1.dotProduct(soundMagnitudes1))
        + Math.sqrt(soundMagnitudes2.dotProduct(soundMagnitudes2));


    // Calculate the current reward for the action that was taken LAST time:
    System.out.println(soundEnergy);
    if (soundEnergy < LOWERBORDER) {
      if (leds[0] > 0.9)
        reward = 1.0;
      else
        reward = -10.0;
      System.out.println("LOW SOUND!");
    } else if (soundEnergy < UPPERBORDER) {
      if (leds[1] > 0.9)
        reward = 1.0;
      else
        reward = -10.0;
      System.out.println("MEDIUM SOUND!");
    } else {
      if (leds[2] > 0.9)
        reward = 1.0;
      else
        reward = -10.0;
      System.out.println("HIGH SOUND!");
    }

    if (reward < 0)
      leds = NaoAction.setFaceLeds(2);
    else
      leds = NaoAction.setFaceLeds(1);


    // Set LEDs according to the action array...
    leds[0] = action.actions[0];
    leds[1] = action.actions[1];
    leds[2] = action.actions[2];

    System.out.print(action.actions[0] + "  " + action.actions[1] + "  " + action.actions[2] + "\n");

    // Put these LEDs to the nao...
    naoAct.set(null, 0, null, leds, null);
    robot.sendAction(naoAct);
  }

  @Override
  public TRStep step(Action action) {
    update((ActionArray) action);
    TRStep tstep;
    tstep = new TRStep(lastTStep, action, new double[] { soundEnergy }, reward);
    lastTStep = tstep;
    System.out.print("Current step: " + tstep.time + "\n");
    return tstep;
  }

  @Override
  public TRStep initialize() {
    reward = 0.0;
    lastTStep = new TRStep(new double[] { 0.0 }, reward);
    return lastTStep;
  }

  @Override
  public Action[] actions() {
    return Actions;
  }

  @Override
  public Range[] getObservationRanges() {
    return new Range[] { soundEnergyRange };
  }

  @Override
  public Legend legend() {
    return legend;
  }
}
