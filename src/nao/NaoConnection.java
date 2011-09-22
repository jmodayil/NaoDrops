package nao;

import java.io.IOException;
import java.nio.ByteOrder;

import rlpark.plugin.robot.DiscoConnection;
import rlpark.plugin.robot.disco.datagroup.DropScalarGroup;
import rlpark.plugin.robot.disco.drops.Drop;
import rlpark.plugin.robot.disco.drops.DropByteArray;
import rlpark.plugin.robot.disco.drops.DropFloat;

public class NaoConnection extends DiscoConnection {

  private final Drop actionDrop = NaoAction.getDrop();;
  protected final DropScalarGroup allAction = new DropScalarGroup("", actionDrop);;

  protected final DropScalarGroup joints = new DropScalarGroup(NaoAction.JointName, actionDrop);;
  protected final DropFloat maxvel = (DropFloat) actionDrop.drop(NaoAction.MaxVelName);
  protected final DropScalarGroup stiff = new DropScalarGroup(NaoAction.StiffName, actionDrop);;
  protected final DropScalarGroup led = new DropScalarGroup(NaoAction.LedName, actionDrop);;
  protected final DropScalarGroup sound = new DropScalarGroup(NaoAction.SoundName, actionDrop);;
  protected final DropByteArray modeAll = (DropByteArray) actionDrop.drop(NaoAction.ModeName);

  // protected final DropByteSigned ledMode = (DropByteSigned)
  // actionDrop.drop(NaoAction.ledModeName);
  // protected final DropByteSigned soundMode = (DropByteSigned)
  // actionDrop.drop(NaoAction.soundModeName);

  public NaoConnection(String hostname, int port, Drop sensorDrop) {
    super(hostname, port, sensorDrop, ByteOrder.LITTLE_ENDIAN);

  }

  public long lastObservationDropTime() {
    return 0;
  }


  public void sendActionDrop(NaoAction action) {
    if (action.actions == null || isClosed())
      return;
    // push the data in action into the drop....

    char[] jlse = new char[4];
    jlse[0] = (char) (action.jointMode ? 1 : 0);
    jlse[1] = (char) (action.ledMode ? 1 : 0);
    jlse[2] = (char) (action.soundMode ? 1 : 0);
    jlse[3] = (char) 0;
    modeAll.setValue(jlse);
    joints.set(action.joints);
    maxvel.setDouble(action.maxvel);
    stiff.set(action.stiffness);
    sound.set(action.sounds);
    led.set(action.leds);
    try {
      socket.send(actionDrop);
    } catch (IOException e) {
      e.printStackTrace();
      close();
    }
  }
}
