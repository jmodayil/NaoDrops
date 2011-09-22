package nao;

import rlpark.plugin.robot.disco.datatype.LiteByteBuffer;
import rlpark.plugin.robot.disco.drops.DropData;

public class DropNaoImage extends DropData {
  private final byte[] value;
  private final int imageX;
  private final int imageY;

  public DropNaoImage(String label, int imageX, int imageY) {
    this(label, imageX, imageY, -1);
  }

  public DropNaoImage(String label, int imageX, int imageY, int index) {
    super(label, false, index);
    this.imageX = imageX;
    this.imageY = imageY;
    value = new byte[imageX * imageY * 2];
  }

  @Override
  public DropData clone(String label, int index) {
    return new DropNaoImage(label, imageX, imageY, index);
  }

  @Override
  public int size() {
    return value.length;
  }

  @Override
  public void putData(LiteByteBuffer buffer) {
    for (byte c : value)
      buffer.put(c);
  }

  public void extractImageData(byte[] srcFullDrop, byte[] dst) {
    System.arraycopy(srcFullDrop, index, dst, 0, value.length);
  }
}
