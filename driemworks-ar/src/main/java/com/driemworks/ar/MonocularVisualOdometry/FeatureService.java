package com.driemworks.ar.MonocularVisualOdometry;

import android.util.Log;

import com.driemworks.ar.dto.FeatureWrapper;
import com.driemworks.ar.dto.SequentialFrameFeatures;
import com.driemworks.common.utils.ImageConversionUtils;

import org.opencv.core.CvType;
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

import java.util.List;

/**
 * Monocular visual odometry using FAST/ORB/Hamming distance
 *
 * @author Tony
 *
 */
public class FeatureService {

    /** The detector */
    private FeatureDetector detector;

    /** The descriptor descriptorExtractor */
    private DescriptorExtractor descriptorExtractor;

    /** The descriptorMatcher */
    private DescriptorMatcher descriptorMatcher;

    /** The term criteria */
    private static final TermCriteria termCriteria = new TermCriteria(TermCriteria.EPS | TermCriteria.MAX_ITER, 30, 0.01);

    /**
     * Constructor for the FeatureService
     */
    public FeatureService() {
        // FAST feature detector
        detector = FeatureDetector.create(FeatureDetector.FAST);
        // ORB descriptor extraction
        descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        // bruteforce hamming metric
        descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
    }

    /**
     * Detect features in the input frame
     * @param frame the frame in which we are detecting features
     * @return the feature wrapper, containing detected features and descriptors
     */
    public FeatureWrapper featureDetection(Mat frame) {
        Log.d(this.getClass().getCanonicalName(), "START - featureDetection");
        long startTime = System.currentTimeMillis();
        MatOfKeyPoint mKeyPoints = new MatOfKeyPoint();
        Mat mIntermediateMat = new Mat();

        detector.detect(frame, mKeyPoints);
        Log.d("keypoints: ", "" + mKeyPoints.checkVector(2));
        descriptorExtractor.compute(frame, mKeyPoints, mIntermediateMat);
        Log.d("FeatureService: ", "keypoints: " + mKeyPoints);
        Log.d(this.getClass().getCanonicalName(), "END - featureDetection - time elapsed: " + (System.currentTimeMillis() - startTime) + " ms");
        return new FeatureWrapper("fast", "orb", frame, mIntermediateMat, mKeyPoints, null);
    }

    /**
     * Track features from a previous frame into the current frame
     * @param previousFrameGray the previous frame in grayscale
     * @param currentFrameGray the current frame in grayscale
     * @param previousKeyPoints the previously detected key points
     * @param status the status mat
     * @param err the error mat
     * @return the original features, as well as the features tracked into the current frame
     */
    public SequentialFrameFeatures featureTracking(Mat previousFrameGray, Mat currentFrameGray,
                                                   MatOfKeyPoint previousKeyPoints,
                                                   MatOfByte status, MatOfFloat err) {
        Log.d(this.getClass().getCanonicalName(), "START - featureTracking");
        long startTime = System.currentTimeMillis();

        // filter out points not tracked in current frame
        MatOfPoint2f previousKeyPoints2f = ImageConversionUtils.convertMatOfKeyPointsTo2f(previousKeyPoints);
        MatOfPoint2f previousKPConverted = new MatOfPoint2f();
        previousKeyPoints2f.convertTo(previousKPConverted, CvType.CV_32FC2);
        Log.d("previousKeyPoints2f ", "checkVector: " + previousKPConverted.checkVector(2));

        MatOfPoint2f currentKeyPoints2f = new MatOfPoint2f();

        Video.calcOpticalFlowPyrLK(previousFrameGray, currentFrameGray,
                previousKeyPoints2f, currentKeyPoints2f,
                status, err, new Size(21, 21), 0, termCriteria, /** flags */0, 0.001);

        byte[] statusArray = status.toArray();
        int indexCorrection = 0;

        List<Point> previousKeyPointsList = previousKeyPoints2f.toList();
        Log.d("previousKeyPointsList", "size: " + previousKeyPointsList.size());

        List<Point> currentKeyPointsList = currentKeyPoints2f.toList();
        Log.d("currentKeyPointsList", "size: " + currentKeyPointsList.size());

//        for (int i = 0; i < currentKeyPointsList.size(); i++) {
//            Point pt = currentKeyPointsList.get(i - indexCorrection);
//
//            if (statusArray[i] == 0 || (pt.x == 0 || pt.y == 0)) {
//                // removes points which are tracked off screen
//                if (pt.x == 0 || pt.y == 0) {
//                    statusArray[i] = 0;
//                }
//                // remove points for which tracking has failed
//                previousKeyPointsList.remove(i - indexCorrection);
//                currentKeyPointsList.remove(i - indexCorrection);
//                indexCorrection++;
//            }
//        }

        Log.d(this.getClass().getCanonicalName(), "END - featureTracking - time elapsed: " + (System.currentTimeMillis() - startTime) + " ms");
        return new SequentialFrameFeatures(previousKeyPointsList, currentKeyPointsList);
    }

    public FeatureDetector getDetector() {
        return detector;
    }

    public void setDetector(FeatureDetector detector) {
        this.detector = detector;
    }

    public DescriptorExtractor getDescriptorExtractor() {
        return descriptorExtractor;
    }

    public void setDescriptorExtractor(DescriptorExtractor descriptorExtractor) {
        this.descriptorExtractor = descriptorExtractor;
    }

    public DescriptorMatcher getDescriptorMatcher() {
        return descriptorMatcher;
    }

    public void setDescriptorMatcher(DescriptorMatcher descriptorMatcher) {
        this.descriptorMatcher = descriptorMatcher;
    }
}
