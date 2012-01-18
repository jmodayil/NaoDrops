package nao;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;

public class YUVProvider implements zephyr.plugin.core.api.viewable.ImageProvider {

  private final int height;
  private final int width;
  private final int[] ARGBpixels;

  public YUVProvider(int width, int height) {
    this.width = width;
    this.height = height;
    ARGBpixels = new int[this.width * this.height];

  }

  int charbound(double d) {
    if (d > 255)
      return 255;
    if (d < 0)
      return 0;
    return (int) d;
  }

  public void stash(byte[] imagedata) {
    int sz = width * height;
    int bi = 0;
    float y, u, v;
    int r, g, b;
    for (int i = 0; i < sz; i += 2, bi += 4) {
      u = (imagedata[bi + 1] & 0xff) - 128;
      v = (imagedata[bi + 3] & 0xff) - 128;
      y = (imagedata[bi + 0] & 0xff) - 16;
      r = charbound(1.164 * y + 2.018 * v);
      g = charbound(1.164 * y - 0.813 * u - .391 * v);
      b = charbound(1.164 * y + 1.596 * u);
      ARGBpixels[i] = 256 * 256 * 256 * 255 + 256 * 256 * r + 256 * g + b;
      y = (imagedata[bi + 2] & 0xff) - 16;
      r = charbound(1.164 * y + 2.018 * v);
      g = charbound(1.164 * y - 0.813 * u - .391 * v);
      b = charbound(1.164 * y + 1.596 * u);
      ARGBpixels[i + 1] = 256 * 256 * 256 * 255 + 256 * 256 * r + 256 * g + b;
    }
    // System.out.println("Stash");
  }

  @Override
  public BufferedImage image() {
    // System.out.println("imaged: " + width + " " + height);
    BufferedImage im = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
    int startX = 0, startY = 0, w = width, h = height, offset = 0, scansize = width;
    // fill the buffer in YUV mode.
    im.setRGB(startX, startY, w, h, ARGBpixels, offset, scansize);

    BufferedImageOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
    return op.filter(im, null);
  }


}
