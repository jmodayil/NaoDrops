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

  private static int DEPTH;

  @Monitor
  MotionMeasure[] motionMeasures = new MotionMeasure[4];

  @Monitor
  int binaryMotion0;
  @Monitor
  int binaryMotion1;
  @Monitor
  int binaryMotion2;
  @Monitor
  int binaryMotion3;


  public SubImages(YUVProvider bigImage) {
    this.bigImage = bigImage;
    DEPTH = IplImage.createFrom(bigImage.grayImage().getSubimage(10, 10, 64, 48)).depth();
    int channels = IplImage.createFrom(bigImage.grayImage()).nChannels();
    for (int n = 0; n < 4; n++) {
      motionMeasures[n] = new MotionMeasure(.68, 64, 48, DEPTH, channels);
    }
  }

  public void update() {

    // Update Images:
    smallImages[0] = bigImage.grayImage().getSubimage(10, 10, 64, 48);
    smallImages[1] = bigImage.grayImage().getSubimage(10, 180, 64, 48);
    smallImages[2] = bigImage.grayImage().getSubimage(220, 10, 64, 48);
    smallImages[3] = bigImage.grayImage().getSubimage(220, 180, 64, 48);

    for (int n = 0; n < 4; n++) {
      smallIplImages[n] = IplImage.createFrom(smallImages[n]);
      motionValues[n] = motionMeasures[n].update(smallIplImages[n]);
      if (motionValues[n] > 2.5) {
        switch (n) {
        case 0:
          binaryMotion0 = 1;
          break;
        case 1:
          binaryMotion1 = 1;
          break;
        case 2:
          binaryMotion2 = 1;
          break;
        case 3:
          binaryMotion3 = 1;
          break;
        }
      } else {
        switch (n) {
        case 0:
          binaryMotion0 = 0;
          break;
        case 1:
          binaryMotion1 = 0;
          break;
        case 2:
          binaryMotion2 = 0;
          break;
        case 3:
          binaryMotion3 = 0;
          break;
        }
      }
    }
  }

  public void updateShowCurrentImage(double currentImage) {
    this.currentImage = (int) currentImage;
  }

  @Override
  public BufferedImage image() {
    return smallImages[currentImage];
  }

  public IplImage cvgrayImage() {
    IplImage image = new IplImage();
    image.copyFrom(smallImages[currentImage]);
    return image;
  }

  public double[] getCameraMotion() {
    return motionValues;
  }
}