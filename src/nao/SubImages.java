package nao;

import java.awt.image.BufferedImage;

import rlpark.plugin.opencv.MotionMeasure;
import zephyr.plugin.core.api.monitoring.annotations.Monitor;

import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class SubImages implements zephyr.plugin.core.api.viewable.ImageProvider {
  int currentImage = 0;

  private YUVProvider bigImage = null;
  private final IplImage[] smallIplImages = new IplImage[4];

  private final BufferedImage[] smallImages = new BufferedImage[4];
  private final double[] motionValues = new double[4];

  @Monitor
  private double luminance;

  private static final int DEPTH = opencv_core.IPL_DEPTH_8U;

  @Monitor
  MotionMeasure[] motionMeasures = new MotionMeasure[4];


  public SubImages(YUVProvider bigImage) {
    this.bigImage = bigImage;
    for (int n = 0; n < 4; n++) {
      motionMeasures[n] = new MotionMeasure(.68, 64, 48, DEPTH, 1);
    }
  }

  public void update() {

    // Update Images:
    smallImages[0] = bigImage.image().getSubimage(10, 10, 64, 48);
    smallImages[1] = bigImage.image().getSubimage(10, 180, 64, 48);
    smallImages[2] = bigImage.image().getSubimage(220, 10, 64, 48);
    smallImages[3] = bigImage.image().getSubimage(220, 180, 64, 48);

    for (int n = 0; n < 4; n++) {
      smallIplImages[n] = IplImage.createFrom(smallImages[n]);
      motionValues[n] = motionMeasures[n].update(smallIplImages[n]);
    }
  }

  public void updateShowCurrentImage(double currentImage) {
    this.currentImage = (int) currentImage;
  }

  @Override
  public BufferedImage image() {
    return smallImages[currentImage];
  }

  public IplImage cvImage() {
    IplImage image = new IplImage();
    image.copyFrom(smallImages[currentImage]);
    return image;
  }

  public double[] getCameraMotion() {
    return motionValues;
  }
}