package nao;

import java.util.ArrayList;

import rlpark.plugin.robot.disco.drops.Drop;
import rlpark.plugin.robot.disco.drops.DropArray;
import rlpark.plugin.robot.disco.drops.DropByteArray;
import rlpark.plugin.robot.disco.drops.DropData;
import rlpark.plugin.robot.disco.drops.DropFloat;
import rltoys.environments.envio.actions.ActionArray;
import rltoys.math.ranges.Range;

public class NaoAction extends ActionArray {


  /**
	 * 
	 */
  private static final long serialVersionUID = 3108227084133068803L;
  final static String JointName = "jointVals";
  final static String MaxVelName = "maxvel";
  final static String StiffName = "stiffVals";
  final static String LedName = "ledVals";
  final static String SoundName = "soundVals";
  final static String ModeName = "ModesJointLedSoundSpare";
  // final static String ledModeName = "ledMode";
  // final static String soundModeName = "soundMode";
  final public static String jointNames[] = { "HeadYaw", "HeadPitch", "LShoulderPitch", "LShoulderRoll", "LElbowYaw",
      "LElbowRoll", "LWristYaw", "LHand", "RShoulderPitch", "RShoulderRoll", "RElbowYaw", "RElbowRoll", "RWristYaw",
      "RHand" };
  final public static String stiffJointNames[] = { "Stiff-HeadYaw", "Stiff-HeadPitch", "Stiff-LShoulderPitch",
      "Stiff-LShoulderRoll", "Stiff-LElbowYaw", "Stiff-LElbowRoll", "Stiff-LWristYaw", "Stiff-LHand",
      "Stiff-RShoulderPitch", "Stiff-RShoulderRoll", "Stiff-RElbowYaw", "Stiff-RElbowRoll", "Stiff-RWristYaw",
      "Stiff-RHand" };
  final public static String ledNames[] = { "ChestBoard/Blue", "ChestBoard/Green", "ChestBoard/Red", "Ears/Left/0Deg",
      "Ears/Left/108Deg", "Ears/Left/144Deg", "Ears/Left/180Deg", "Ears/Left/216Deg", "Ears/Left/252Deg",
      "Ears/Left/288Deg", "Ears/Left/324Deg", "Ears/Left/36Deg", "Ears/Left/72Deg", "Ears/Right/0Deg",
      "Ears/Right/108Deg", "Ears/Right/144Deg", "Ears/Right/180Deg", "Ears/Right/216Deg", "Ears/Right/252Deg",
      "Ears/Right/288Deg", "Ears/Right/324Deg", "Ears/Right/36Deg", "Ears/Right/72Deg", "Face/Blue/Left/0Deg",
      "Face/Blue/Left/135Deg", "Face/Blue/Left/180Deg", "Face/Blue/Left/225Deg", "Face/Blue/Left/270Deg",
      "Face/Blue/Left/315Deg", "Face/Blue/Left/45Deg", "Face/Blue/Left/90Deg", "Face/Blue/Right/0Deg",
      "Face/Blue/Right/135Deg", "Face/Blue/Right/180Deg", "Face/Blue/Right/225Deg", "Face/Blue/Right/270Deg",
      "Face/Blue/Right/315Deg", "Face/Blue/Right/45Deg", "Face/Blue/Right/90Deg", "Face/Green/Left/0Deg",
      "Face/Green/Left/135Deg", "Face/Green/Left/180Deg", "Face/Green/Left/225Deg", "Face/Green/Left/270Deg",
      "Face/Green/Left/315Deg", "Face/Green/Left/45Deg", "Face/Green/Left/90Deg", "Face/Green/Right/0Deg",
      "Face/Green/Right/135Deg", "Face/Green/Right/180Deg", "Face/Green/Right/225Deg", "Face/Green/Right/270Deg",
      "Face/Green/Right/315Deg", "Face/Green/Right/45Deg", "Face/Green/Right/90Deg", "Face/Red/Left/0Deg",
      "Face/Red/Left/135Deg", "Face/Red/Left/180Deg", "Face/Red/Left/225Deg", "Face/Red/Left/270Deg",
      "Face/Red/Left/315Deg", "Face/Red/Left/45Deg", "Face/Red/Left/90Deg", "Face/Red/Right/0Deg",
      "Face/Red/Right/135Deg", "Face/Red/Right/180Deg", "Face/Red/Right/225Deg", "Face/Red/Right/270Deg",
      "Face/Red/Right/315Deg", "Face/Red/Right/45Deg", "Face/Red/Right/90Deg", "Head/Front/Left/0",
      "Head/Front/Left/1", "Head/Front/Right/0", "Head/Front/Right/1", "Head/Middle/Left/0", "Head/Middle/Right/0",
      "Head/Rear/Left/0", "Head/Rear/Left/1", "Head/Rear/Left/2", "Head/Rear/Right/0", "Head/Rear/Right/1",
      "Head/Rear/Right/2" };
  final public static String soundNames[] = { "frequency", "amplitude", "pastDecay" };
  // 14
  public static final int numJoints = jointNames.length;
  public static final int numJointC = 2 * numJoints + 1;
  // 83
  public static final int numLed = ledNames.length;
  // 3
  public static final int numSound = soundNames.length;

  final static private DropData[] actionDescriptor = { new DropByteArray(ModeName, 4),
      new DropArray(new DropFloat(""), JointName, -1, jointNames), new DropFloat(MaxVelName),
      new DropArray(new DropFloat(""), StiffName, -1, stiffJointNames),
      new DropArray(new DropFloat(""), LedName, -1, ledNames),
      new DropArray(new DropFloat(""), SoundName, -1, soundNames) };


  public final static int actionSize = numJoints + 1 + numLed + numSound;


  boolean soundMode;
  boolean jointMode;
  boolean ledMode;

  double[] joints;
  double maxvel;
  double[] stiffness;
  double[] leds;
  double[] sounds;
  private final ArrayList<Double> values;

  public NaoAction() {
    soundMode = false;
    jointMode = false;
    ledMode = false;
    joints = new double[numJoints];
    stiffness = new double[numJoints];
    leds = new double[numLed];
    sounds = new double[numSound];
    values = new ArrayList<Double>();
  }

  public Range[] getRanges() {
    return null;
  }

  public static String[] getNames() {
    ArrayList<String> names = new ArrayList<String>();
    names.add("jointMode");
    names.add("ledMode");
    names.add("soundMode");
    for (String s : jointNames)
      names.add(s);
    for (String s : ledNames)
      names.add(s);
    for (String s : soundNames)
      names.add(s);
    String s[] = new String[0];
    return names.toArray(s);
  }

  public void setValues() {
    values.clear();
    values.add(jointMode ? 1.0 : 0.0);
    values.add(ledMode ? 1.0 : 0.0);
    values.add(soundMode ? 1.0 : 0.0);
    for (int i = 0; i < numJoints; i++)
      values.add(joints[i]);
    values.add(maxvel);
    for (int i = 0; i < numJoints; i++)
      values.add(stiffness[i]);
    for (int i = 0; i < numLed; i++)
      values.add(leds[i]);
    for (int i = 0; i < numSound; i++)
      values.add(sounds[i]);
  }

  public double getValue(int index) {
    return values.get(index);
  }

  public void set(double[] joints, double maxvel, double[] stiffness, double[] leds, double[] sounds) {
    if (joints != null) {
      for (int i = 0; i < numJoints; i++) {
        this.joints[i] = joints[i];
        this.stiffness[i] = stiffness[i];
      }
      this.maxvel = maxvel;
      jointMode = true;
    }
    if (leds != null) {
      ledMode = true;
      for (int i = 0; i < numLed; i++)
        this.leds[i] = leds[i];
    }
    if (sounds != null) {
      soundMode = true;
      for (int i = 0; i < numSound; i++)
        this.sounds[i] = sounds[i];
    }
    setValues();
  }


  public static Drop getDrop() {
    return new Drop("NaoC", actionDescriptor);
  }

  public static double[] setFaceLeds(int color) {
    // Set to blue if color = 0, green if color = 1, red if color = 2
    // sorry for that crappy programming!
    double[] leds = new double[83];
    switch (color) {
    case 0:
      for (int n = 23; n < 39; n++)
        leds[n] = 1.0;
      break;
    case 1:
      for (int n = 39; n < 55; n++)
        leds[n] = 1.0;
      break;

    case 2:
      for (int n = 55; n < 71; n++)
        leds[n] = 1.0;
      break;
    }
    return leds;
  }
}
