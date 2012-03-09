package nao;

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
	
	public IplImage getThresholdImage(IplImage img) {
	    // Convert the image into an HSV image
		//canvas.showImage(img);
	    imgHSV = IplImage.create(img.cvSize(), img.depth(), img.nChannels());
	    cvCvtColor(img, imgHSV, CV_BGR2HSV);
	    
	    imgThreshed = IplImage.create(imgHSV.cvSize(), imgHSV.depth(), 1);
	    
	    cvInRangeS(imgHSV,cvScalar(105,145,145,0),cvScalar(114,255,255,0),imgThreshed);
	    canvas.showImage(imgThreshed);
		return imgThreshed;
	}
	
	public double getReward(IplImage img) {
		double value = 0;
		double reward = 0;
		getThresholdImage(img);
		value = cvAvg(imgThreshed, null).getVal(0);
		
		if (value < 1.0) {
			reward = -1.0;
		}
		else if (value < 5.0) {
			reward = (value - 1.0)*2/(4.0) - 1;
		}
		else {
			reward = 1.0;
		}
//		System.out.println("reward: " + reward);
		return reward;
	}
	
	public double[] getXYcoordinates(IplImage img) {
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
	
	public double getRewardForBlueTracking(IplImage img) {
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
