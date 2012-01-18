package nao;

import java.awt.image.BufferedImage;

import rlpark.plugin.opencv.MotionMeasure;
import zephyr.plugin.core.api.monitoring.annotations.Monitor;

import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class CenterImage implements zephyr.plugin.core.api.viewable.ImageProvider {
  YUVProvider bigImage;
  BufferedImage centerImage;
  double motionValue;

  @Monitor
  double luminance;

  private static final int DEPTH = opencv_core.IPL_DEPTH_8U;

  @Monitor
  MotionMeasure motion = new MotionMeasure(.68, 64, 48, DEPTH, 1);

  public CenterImage(YUVProvider bigImage) {
    this.bigImage = bigImage;
  }

  public void update() {
    centerImage = bigImage.image().getSubimage(108, 72, 64, 48);
    IplImage image = IplImage.createFrom(centerImage);
    motionValue = motion.update(image);
    luminance = 0;
    for (int n = 0; n < 64; n++) {
      for (int k = 0; k < 48; k++) {
        luminance += centerImage.getData().getSampleDouble(n, k, 0);
      }
    }
    luminance /= (64 * 48);
  }

  @Override
  public BufferedImage image() {
    return centerImage;
  }

  public IplImage cvImage() {
    IplImage image = new IplImage();
    image.copyFrom(centerImage);
    return image;
  }

  public double getMotion() {
    return motionValue;
  }

  public double getLuminance() {
    return luminance;
  }
}