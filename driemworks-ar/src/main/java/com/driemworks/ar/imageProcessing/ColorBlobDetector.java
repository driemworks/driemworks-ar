package com.driemworks.ar.imageProcessing;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ColorBlobDetector {

    private static final String TAG = "ColorBlobDetector: ";

    // Lower and Upper bounds for range checking in HSV color space
    private Scalar mLowerBound = new Scalar(0);
    private Scalar mUpperBound = new Scalar(0);

    // Minimum contour area in percent for contours filtering
    private static double mMinContourArea = 0.98;

    // Color radius for range checking in HSV color space
    private Scalar mColorRadius = new Scalar(35,50,50,15);
    private Mat mSpectrum = new Mat();
    private List<MatOfPoint> mContours = new ArrayList<>();


    private int threshold = 150;

    private Mat mCanny = new Mat();

    // Cache
    private Mat mPyrDownMat;
    private Mat mHsvMat;
    private Mat mMask;
    private Mat mDilatedMask;
    private Mat mHierarchy;

    /**
     * Constructor for the ColorBlobDetector`
     */
    public ColorBlobDetector() {
        mPyrDownMat = new Mat();
        mHsvMat = new Mat();
        mMask = new Mat();
        mDilatedMask = new Mat();
        mHierarchy = new Mat();
    }

    /**
     * Determine the upper and lower bounds for detection color range based on the
     * hsv color
     * @param hsvColor The hsv color
     */
    public void setHsvColor(Scalar hsvColor) {
        double minH = (hsvColor.val[0] >= mColorRadius.val[0]) ? hsvColor.val[0]-mColorRadius.val[0] : 0;
        double maxH = (hsvColor.val[0]+mColorRadius.val[0] <= 255) ? hsvColor.val[0]+mColorRadius.val[0] : 255;

        mLowerBound.val[0] = minH;
        mUpperBound.val[0] = maxH;

        mLowerBound.val[1] = hsvColor.val[1] - mColorRadius.val[1];
        mUpperBound.val[1] = hsvColor.val[1] + mColorRadius.val[1];

        mLowerBound.val[2] = hsvColor.val[2] - mColorRadius.val[2];
        mUpperBound.val[2] = hsvColor.val[2] + mColorRadius.val[2];

        mLowerBound.val[3] = 0;
        mUpperBound.val[3] = 255;

        Mat spectrumHsv = new Mat(1, (int)(maxH-minH), CvType.CV_8UC3);

        for (int j = 0; j < maxH-minH; j++) {
            byte[] tmp = {(byte)(minH+j), (byte)255, (byte)255};
            spectrumHsv.put(0, j, tmp);
        }

        Imgproc.cvtColor(spectrumHsv, mSpectrum, Imgproc.COLOR_HSV2RGB_FULL, 4);
    }

    /**
     * Finds and filters contours in the image
     * @param rgbaImage The image
     */
    public List<MatOfPoint> process(Mat rgbaImage) {
        Imgproc.GaussianBlur(rgbaImage, rgbaImage, new Size(3, 3), 0);

        Imgproc.pyrDown(rgbaImage, mPyrDownMat);
        Imgproc.pyrDown(mPyrDownMat, mPyrDownMat);

        Imgproc.cvtColor(mPyrDownMat, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL);
        Core.inRange(mHsvMat, mLowerBound, mUpperBound, mMask);
        Imgproc.Canny(mMask, mCanny, threshold, threshold*2, 3, true);
        Imgproc.dilate(mCanny, mDilatedMask, new Mat());

        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(mDilatedMask, contours, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        double maxArea = findMaxContourArea(contours);

        // Filter contours by area and resize to fit the original image size
        mContours.clear();
        filterContours(contours, maxArea);
        return mContours;
    }

    /**
     *
     * @param contours
     */
    private void filterContours(List<MatOfPoint> contours, double maxArea) {
		for (MatOfPoint contour : contours) {
			if (Imgproc.contourArea(contour) > mMinContourArea * maxArea) {
				Core.multiply(contour, new Scalar(4, 4), contour);
				mContours.add(contour);
			}
		}
    }

    /**
     * Find the maximum area of the MatOfPoint with the list contours
     * @param contours The list of MatOfPoint
     * @return maxArea the Maximum Area
     */
    private double findMaxContourArea(List<MatOfPoint> contours) {
        double maxArea = 0;
		for (MatOfPoint wrapper : contours) {
			double area = Imgproc.contourArea(wrapper);
			if (area > maxArea)
				maxArea = area;
		}

        return maxArea;
    }


    public Mat getSpectrum() {
        return mSpectrum;
    }

    public List<MatOfPoint> getContours() {
        return mContours;
    }
}