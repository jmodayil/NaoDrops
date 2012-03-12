package nao;

import java.awt.image.BufferedImage;

import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.*;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;

public class RewardFromCardGenerator {
	private CanvasFrame canvas = new CanvasFrame("out");
	IplImage imgThreshed;
	IplImage imgHSV;
	CvMat matArray = new CvMat();
	int count = 0;
	double area = 0.1;
	
	public IplImage getThresholdImage(BufferedImage img) {
	    // Convert the image into an HSV image
		//canvas.showImage(img);
		BufferedImage subImage;
		subImage = img.getSubimage(130, 0, 60, 240);
		IplImage subIpl = IplImage.createFrom(subImage);
	    imgHSV = IplImage.create(subIpl.cvSize(), subIpl.depth(), subIpl.nChannels());
	    cvCvtColor(subIpl, imgHSV, CV_BGR2HSV);
	    
	    imgThreshed = IplImage.create(imgHSV.cvSize(), imgHSV.depth(), 1);
	    
	    cvInRangeS(imgHSV,cvScalar(105,145,145,0),cvScalar(114,255,255,0),imgThreshed);
	    canvas.showImage(imgThreshed);
		return imgThreshed;
	}
	
	public double getReward(BufferedImage img) {
		double value = 0;
		double reward = 0;
		getThresholdImage(img);
		value = cvAvg(imgThreshed, null).getVal(0);
		if (value < 1.5) {
			reward = -1.0;
		}
		else {
			reward = 1.0;
		}
		System.out.println("BlueCard Value: " + value);
//		System.out.println("reward: " + reward);
		return reward;
	}
	
	public double[] getXYcoordinates(BufferedImage img) {
        // Calculate the moments to estimate the position of the ball
        CvMoments moments = new CvMoments();
        double[] xy = new double[2];
        getThresholdImage(img);
        
        cvMoments(imgThreshed, moments, 1);
 
        // The actual moment values
        double moment10 = cvGetSpatialMoment(moments, 1, 0);
        double moment01 = cvGetSpatialMoment(moments, 0, 1);
        area = cvGetCentralMoment(moments, 0, 0);
        
        
        area = area < 1.0 ? 1 : area;
        
        double x = moment10/area;
        double y = moment01/area;
        
//        System.out.println("x and y: " + x + " " + y);
        xy[0] = x; xy[1] = y;
        return xy;
	}
	
	public double getRewardForBlueTracking(BufferedImage img) {
		double[] xy = getXYcoordinates(img);
		double distx = xy[0] - 160;
		double disty = xy[1] - 120;
		
		double squaredDistance = distx*distx + disty*disty;
		//Maximum of SquaredDistance: 160*160 + 140*140 = 45200;
		squaredDistance /= 45200.0;
	
		if (area < 1000.0) {
			squaredDistance = 1.0;
		}
		
		return -Math.sqrt(squaredDistance);
	}

}
