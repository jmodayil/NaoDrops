package nao;

import java.awt.image.BufferedImage;

import rlpark.plugin.opencv.MotionMeasure;
import zephyr.plugin.core.api.monitoring.annotations.Monitor;
import zephyr.plugin.core.api.video.ImageProvider;

import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class CenterImage implements ImageProvider {
  YUVProvider bigImage;
  BufferedImage centerImage;

  private static final int DEPTH = opencv_core.IPL_DEPTH_8U;

  @Monitor
  MotionMeasure motion = new MotionMeasure(.99, 32, 24, DEPTH, 1);

  public CenterImage(YUVProvider bigImage) {
    this.bigImage = bigImage;
  }

  public void update() {
    centerImage = bigImage.image().getSubimage(144, 108, 32, 24);
    IplImage image = IplImage.createFrom(centerImage);
    motion.update(image);
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
}