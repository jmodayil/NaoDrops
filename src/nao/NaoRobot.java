package nao;

import java.util.List;

import rlpark.plugin.robot.RobotEnvironment;
import rlpark.plugin.robot.disco.datatype.LiteByteBuffer;
import rlpark.plugin.robot.disco.drops.Drop;
import rlpark.plugin.robot.disco.drops.DropArray;
import rlpark.plugin.robot.disco.drops.DropData;
import rlpark.plugin.robot.disco.drops.DropFloat;
import rlpark.plugin.robot.sync.ObservationReceiver;
import rlpark.plugin.robot.sync.ObservationVersatile;
import rlpark.plugin.robot.sync.ObservationVersatileArray;
import rltoys.algorithms.representations.actions.Action;
import rltoys.environments.envio.Agent;
import rltoys.environments.envio.observations.Legend;
import rltoys.utils.NotImplemented;
import zephyr.plugin.core.api.monitoring.abstracts.DataMonitor;
import zephyr.plugin.core.api.monitoring.abstracts.MonitorContainer;
import zephyr.plugin.core.api.monitoring.abstracts.Monitored;
import zephyr.plugin.core.api.monitoring.annotations.Monitor;
import zephyr.plugin.core.api.synchronization.Clock;

public class NaoRobot extends RobotEnvironment implements MonitorContainer {

  private static final int NaoControlPort = 6321;

  final static public String[] sensorNames = { "Battery-Charge", "Battery-Current", "Battery-Temperature",
      "Battery-Temperature", "ChestBoard-Button", "Head-Touch-Front", "Head-Touch-Middle", "Head-Touch-Rear",
      "HeadPitch-ElectricCurrent", "HeadPitch-Position", "HeadPitch-Temperature", "HeadYaw-ElectricCurrent",
      "HeadYaw-Position", "HeadYaw-Temperature", "InertialSensor-AccX", "InertialSensor-AccY", "InertialSensor-AccZ",
      "InertialSensor-AngleX", "InertialSensor-AngleY", "InertialSensor-GyrRef", "InertialSensor-GyrX",
      "InertialSensor-GyrY", "LElbowRoll-ElectricCurrent", "LElbowRoll-Position", "LElbowRoll-Temperature",
      "LElbowYaw-ElectricCurrent", "LElbowYaw-Position", "LElbowYaw-Temperature", "LHand-ElectricCurrent",
      "LHand-Position", "LHand-Temperature", "LHand-Touch-Back", "LHand-Touch-Left", "LHand-Touch-Right",
      "LShoulderPitch-ElectricCurrent", "LShoulderPitch-Position", "LShoulderPitch-Temperature",
      "LShoulderRoll-ElectricCurrent", "LShoulderRoll-Position", "LShoulderRoll-Temperature",
      "LWristYaw-ElectricCurrent", "LWristYaw-Position", "LWristYaw-Temperature", "RElbowRoll-ElectricCurrent",
      "RElbowRoll-Position", "RElbowRoll-Temperature", "RElbowYaw-ElectricCurrent", "RElbowYaw-Position",
      "RElbowYaw-Temperature", "RHand-ElectricCurrent", "RHand-Position", "RHand-Temperature", "RHand-Touch-Back",
      "RHand-Touch-Left", "RHand-Touch-Right", "RShoulderPitch-ElectricCurrent", "RShoulderPitch-Position",
      "RShoulderPitch-Temperature", "RShoulderRoll-ElectricCurrent", "RShoulderRoll-Position",
      "RShoulderRoll-Temperature", "RWristYaw-ElectricCurrent", "RWristYaw-Position", "RWristYaw-Temperature",
      "US-Left", "US-Right", "US" };

  // numSensors=67
  final static private DropData[] observationDescriptor = {
      new DropArray(new DropFloat(""), "sensors", -1, sensorNames),
      new DropArray(new DropFloat(""), "soundFeatures", 6144), new DropNaoImage("image", 320, 240) };

  private static final Drop sensorDrop = new Drop("NaoState", observationDescriptor);
  private final NaoConnection naoConnection;

  public NaoAction agentAction;

  @Monitor
  private YUVProvider yuv;

  @Monitor
  private CenterImage centerImage;

  private int bufferSize;

  public LiteByteBuffer argbBuffer;


  public NaoRobot() {
    this(new NaoConnection("localhost", NaoControlPort, sensorDrop)); // CD: is
                                                                      // NaoConnection
                                                                      // of type
                                                                      // ObervationReceiver?
                                                                      // -->
                                                                      // yes,
                                                                      // descendant!
    bufferSize = 320 * 240 * 2;
    argbBuffer = new LiteByteBuffer(bufferSize);

    yuv = new YUVProvider(320, 240);
    centerImage = new CenterImage(yuv);
  }

  public NaoRobot(ObservationReceiver receiver) {
    super(receiver, false);
    // TODO Auto-generated constructor stub
    naoConnection = (NaoConnection) receiver();
  }


  @Override
  public ObservationVersatileArray waitNewRawObs() {
    ObservationVersatileArray result = super.waitNewRawObs();
    updateImage(result);
    return result;
  }

  @Override
  public ObservationVersatileArray newRawObsNow() {
    ObservationVersatileArray result = super.newRawObsNow();
    updateImage(result);
    return result;
  }

  private void updateImage(ObservationVersatileArray result) {
    ObservationVersatile lastObs = result.last();
    if (lastObs == null)
      return;
    DropNaoImage imageDataDrop = (DropNaoImage) sensorDrop.drop("image");
    imageDataDrop.extractImageData(lastObs.rawData(), argbBuffer.array());
    yuv.stash(argbBuffer.array());
    centerImage.update();
  }

  public double getMotion() {
    return centerImage.getMotion();
  }

  public void sendAction(NaoAction action) {
    agentAction = action;

    // lastAction=action.joints + action.stiffness + action.leds + action.sounds
    if (action != null)
      naoConnection.sendActionDrop(action);
    // System.out.println("Sent Action");
  }

  @Override
  public Legend legend() {
    // TODO Auto-generated method stub
    return naoConnection.legend();
  }


  @Override
  public void addToMonitor(DataMonitor monitor) { // CD: output to zephyr

    // observations
    List<String> labelsToLog = legend().getLabels();
    for (String label : labelsToLog) {
      final int obsIndex = legend().indexOf(label);
      monitor.add(label, 0, new Monitored() {
        @Override
        public double monitoredValue() {
          double[] o_t = lastReceivedObs();
          if (o_t == null)
            return -1;
          return o_t[obsIndex];
        }
      });
    }

    String[] actLabels = NaoAction.getNames();
    for (int i = 0; i < actLabels.length; i++) {
      final int obsIndex = i;
      monitor.add(actLabels[i], 0, new Monitored() {
        @Override
        public double monitoredValue() {
          if (agentAction == null)
            return -1;
          return agentAction.getValue(obsIndex);
        }
      });
    }

    /*
     * // actions // CritterbotEnvironments.addActionsLogged(this, monitor);
     * monitor.add("JointMode", 0, new Monitored() {
     * 
     * @Override public double monitoredValue() { return (agentAction == null) ?
     * -1 : agentAction.jointMode ? 1 : 0; } }); monitor.add("LedMode", 0, new
     * Monitored() {
     * 
     * @Override public double monitoredValue() { return (agentAction == null) ?
     * -1 : agentAction.ledMode ? 1 : 0; } }); monitor.add("SoundMode", 0, new
     * Monitored() {
     * 
     * @Override public double monitoredValue() { return (agentAction == null) ?
     * -1 : agentAction.soundMode ? 1 : 0; } }); for (int i = 0; i <
     * NaoAction.actionSize; i++) { String label = String.format("action[%d]",
     * i); final int actionIndex = i; monitor.add(label, 0, new Monitored() {
     * 
     * @Override public double monitoredValue() { NaoAction a_t = agentAction;
     * if (a_t == null || a_t.actions == null) return -1; return
     * a_t.actions[actionIndex]; } }); }
     */

  }

  @Override
  public void sendAction(Action a) {
    // TODO Auto-generated method stub
    sendAction((NaoAction) a);
  }

  @Override
  public void run(Clock clock, Agent agent) {
    throw new NotImplemented();
  }


}
