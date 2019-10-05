package com.driemworks.ar.services.impl;

import android.util.Log;
import android.util.Pair;

import com.driemworks.ar.dto.FeatureData;
import com.driemworks.ar.services.FeatureDetectorService;
import com.driemworks.common.utils.ImageConversionUtils;
import com.driemworks.common.utils.TagUtils;

import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.video.Video;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * An implementation of the FeatureService
 * By Default, it uses FAST/ORB/Brute force Hamming distance
 * @author Tony
 */
public class FeatureDetectorServiceImpl implements FeatureDetectorService {

    /**
     * The tag used for logging
     */
    private final String TAG = TagUtils.getTag(this);

    /**
     * The detector
     */
    private FeatureDetector detector;

    /**
     * The term criteria
     */
    private TermCriteria termCriteria;

    /** The size */
    private Size size;

    /** The maxmimum level */
    private static final int MAX_LEVEL = 3;

    /**
     * The minimum eigen threshold
     */
    private static final double MIN_EIGEN_THRESHOLD = 0.001;

    /**
     * The maximum count
     */
    private static final int MAX_COUNT = 5;

    /**
     * The epsilon
     */
    private static final double EPISILON = 0.001;

    /**
     * Constructor for the FeatureDetectorServiceImpl with default params (FAST/ORB/HAMMING)
     */
    public FeatureDetectorServiceImpl() {
        detector = FeatureDetector.create(FeatureDetector.FAST);
        size = new Size(21, 21);
        termCriteria = new TermCriteria(TermCriteria.EPS | TermCriteria.MAX_ITER, MAX_COUNT, EPISILON);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MatOfKeyPoint extractFeatureData(Mat frame) {
        Log.d(TAG, "START - extractFeatureData");
        MatOfKeyPoint mKeyPoints = new MatOfKeyPoint();
        detector.detect(frame, mKeyPoints);
//        Mat descriptors = new Mat();
//        descriptorExtractor.compute(frame, mKeyPoints, descriptors);
        Log.d(TAG, "END - extractFeatureData");

        mKeyPoints = getBestPoints(mKeyPoints.toList(), 180);
        return mKeyPoints;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Pair<MatOfKeyPoint,  MatOfKeyPoint> trackKeyPoints(Mat previousFrameGray, Mat currentFrameGray, MatOfKeyPoint previousKeyPoints) {
        Log.d(TAG, "START - trackKeyPoints");
        MatOfByte status = new MatOfByte();
        MatOfFloat err = new MatOfFloat();

        MatOfPoint2f previousKeyPoints2f = ImageConversionUtils.convertMatOfKeyPointsTo2f(previousKeyPoints);
        MatOfPoint2f currentKeyPoints2f = new MatOfPoint2f();
        // previous => current
        List<Mat> pyramids = new ArrayList<>();
        Video.buildOpticalFlowPyramid(previousFrameGray, pyramids, size, 3);

        Video.calcOpticalFlowPyrLK(pyramids.get(0), currentFrameGray,
                previousKeyPoints2f, currentKeyPoints2f,
                status, err, size, MAX_LEVEL, termCriteria,
                Video.OPTFLOW_LK_GET_MIN_EIGENVALS, MIN_EIGEN_THRESHOLD);

        Pair<LinkedList<Point>, LinkedList<Point>> matchedPoints = filterPoints(previousKeyPoints2f.toList(), currentKeyPoints2f.toList(), status.toArray());
        status.release();
        err.release();
        Log.d(TAG, "END - trackKeyPoints");
        return Pair.create(ImageConversionUtils.convertListOfPointsToMatOfKeypoint(matchedPoints.first, 1, 1),
                ImageConversionUtils.convertListOfPointsToMatOfKeypoint(matchedPoints.second, 1, 1));
    }

    private double minX = -1;
    private double minY = -1;

    /**
     * Filters out points which fail tracking, or which were tracked off screen
     * @param previousKeypoints The list of keypoints in the previous image
     * @param currentKeypoints The list of keypoints in the current image
     * @param statusArray The status array
     */
    private Pair<LinkedList<Point>, LinkedList<Point>> filterPoints(List<Point> previousKeypoints, List<Point> currentKeypoints, byte[] statusArray) {
        assert previousKeypoints.equals(currentKeypoints);
        int indexCorrection = 0;
        double averageDx = 0;
        double averageDy = 0;
        // copy lists
        LinkedList<Point> currentCopy = new LinkedList<>(currentKeypoints);
        LinkedList<Point> previousCopy = new LinkedList<>(previousKeypoints);
        for (int i = 0; i < currentKeypoints.size(); i++) {
            Point pt = currentKeypoints.get(i - indexCorrection);
            Point previousPoint = previousKeypoints.get(i - indexCorrection);
            if (statusArray[i] == 0 || (pt.x == 0 || pt.y == 0)) {
                // removes points which are tracked off screen
                if (pt.x == 0 || pt.y == 0) {
                    statusArray[i] = 0;
                }
                // remove points for which tracking has failed
                currentCopy.remove(i - indexCorrection);
                previousCopy.remove(i - indexCorrection);
                indexCorrection++;
            } else {
                // correct keypoint in case of jitters
                // if the difference in any direction between the current keypoint and the previous is below a threshold, we will assume
                // that the real representation of the object has not moved
                double dx = Math.abs(pt.x - previousPoint.x);
                double dy = Math.abs(pt.y - previousPoint.y);

                averageDx += dx;
                averageDy += dy;

                double threshold = 0.01;

                if (dx < threshold && dy < threshold) {
                    Log.d("FeatureDetector", "Removing keypoint at index " + i);
                    // if points haven't moved sufficiently in any direction
                    currentCopy.remove(i - indexCorrection);
                    currentCopy.add(previousPoint);
                }
            }
        }

        averageDx = averageDx / currentCopy.size();
        averageDy = averageDy / currentCopy.size();
        Log.d("Averaged motion: ", "dx = " + averageDx + " dy = " + averageDy);
        return Pair.create(previousCopy, currentCopy);
    }

    /**
     * Sorts the keypoints in descending order, so the best response KPs will come first,
     * then chooses the sublist of the sorted keypoints from 0 to numKeyPoints
     * @param notBestKeyPoints The list of keypoints
     * @param numKeyPoints The number of keypoints desired as outcome
     * @return The best numKeyPoints points
     */
    private MatOfKeyPoint getBestPoints(List<KeyPoint> notBestKeyPoints, int numKeyPoints) {
        Collections.sort(notBestKeyPoints, (kp1, kp2) -> (int) (kp2.response - kp1.response));

        MatOfKeyPoint bestPoints = new MatOfKeyPoint();
        List<KeyPoint> bestKeyPoints;
        if (notBestKeyPoints.size() < numKeyPoints) {
            bestKeyPoints = notBestKeyPoints.subList(0, notBestKeyPoints.size());
        } else {
            bestKeyPoints = notBestKeyPoints.subList(0, numKeyPoints);
        }
        bestPoints.fromList(bestKeyPoints);
        return bestPoints;
    }

}


