package com.driemworks.ar.MonocularVisualOdometry;

import android.media.Image;
import android.util.Log;

import com.driemworks.ar.dto.FeatureWrapper;
import com.driemworks.common.utils.ImageConversionUtils;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.video.Video;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Monocular visual odometry using FAST/ORB/Hamming distance
 *
 * @author Tony Riemer
 *
 */
public class FeatureService {

    /** The detector */
    private FeatureDetector detector;

    /** The descriptor descriptorExtractor */
    private DescriptorExtractor descriptorExtractor;

    /** The descriptorMatcher */
    private DescriptorMatcher descriptorMatcher;

    /** The extracted descriptor */
    private Mat descriptors;

    /** Extracted Keypoints */
    private MatOfKeyPoint keyPoints;

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

        descriptors = new Mat();
        keyPoints = new MatOfKeyPoint();
    }

    /**
     * Detect features in the input frame
     * @param frame
     * @return the feature wrapper, containing detected features and descriptors
     */
    public FeatureWrapper featureDetection(Mat frame) {
        MatOfKeyPoint mKeyPoints = new MatOfKeyPoint();
        MatOfDMatch matches = new MatOfDMatch();
        Mat mIntermediateMat = new Mat();

        detector.detect(frame, mKeyPoints);
        Log.d("keypoints: ", "" + mKeyPoints.checkVector(2));
        descriptorExtractor.compute(frame, mKeyPoints, mIntermediateMat);
        Log.d("FeatureService: ", "keypoints: " + mKeyPoints);
        return new FeatureWrapper("fast", "orb", frame, mIntermediateMat, mKeyPoints, matches);
    }

    /**
     *
     * @param previousWrapper
     * @param currentWrapper
     */
    public MatOfDMatch featureMatching(FeatureWrapper previousWrapper, FeatureWrapper currentWrapper) {
        MatOfDMatch matches = new MatOfDMatch();
        descriptorMatcher.match(previousWrapper.getDescriptors(), currentWrapper.getDescriptors(), matches);
        return filterMatches(matches);
    }

    /**
     *
     * @param matches
     * @return
     */
    private MatOfDMatch filterMatches(MatOfDMatch matches) {
        double maxDistance = 0.0;
        double minDistance = 100.0;
        double distance;

        List<DMatch> matchesList = matches.toList();

        for (int i = 0; i < matchesList.size(); i++) {
            distance = (double) matchesList.get(i).distance;
            if (distance < minDistance) {
                minDistance = distance;
            }
            if (distance > maxDistance) {
                maxDistance = distance;
            }
        }

        LinkedList<DMatch> goodMatchesList = new LinkedList<>();
        for (int i = 0; i < matchesList.size(); i++) {
            if (matchesList.get(i).distance <= (1.5 * minDistance)) {
                goodMatchesList.addLast(matchesList.get(i));
            }
        }

        MatOfDMatch goodMatches = new MatOfDMatch();
        goodMatches.fromList(goodMatchesList);
        Log.d("Good matches: ", goodMatches.toString());
        return goodMatches;
    }

    /**
     *
     * @param previousWrapper
     * @param currentWrapper
     * @param status
     * @param err
     * @return filteredPointMap
     */
    public Map<String, List<Point>> featureTracking(FeatureWrapper previousWrapper, FeatureWrapper currentWrapper, MatOfByte status, MatOfFloat err) {
//        TermCriteria termCriteria = new TermCriteria(0, 30, 0.01);
        TermCriteria termCriteria = new TermCriteria();

        Video.calcOpticalFlowPyrLK(previousWrapper.getFrame(), currentWrapper.getFrame(),
                ImageConversionUtils.convertMatOfKeyPointsTo2f(previousWrapper.getKeyPoints()),
                ImageConversionUtils.convertMatOfKeyPointsTo2f(currentWrapper.getKeyPoints()),
                status, err, new Size(21, 21), 0, termCriteria, /** flags */0, 0.001);

        Map<String, List<Point>> filteredPointMap = new HashMap<>();
        // filter out points not tracked in current frame
        List<Point> previousKeyPointsList = ImageConversionUtils.convertMatOfKeyPointsTo2f(previousWrapper.getKeyPoints()).toList();
        List<Point> currentKeyPointsList = ImageConversionUtils.convertMatOfKeyPointsTo2f(currentWrapper.getKeyPoints()).toList();
        byte[] statusArray = status.toArray();
        int indexCorrection = 0;
        for (int i = 0; i < currentKeyPointsList.size(); i++) {
            Point pt = currentKeyPointsList.get(i - indexCorrection);

            if (statusArray[i] == 0 || (pt.x == 0 || pt.y == 0)) {
                // removes points which are tracked off screen
                if (pt.x == 0 || pt.y == 0) {
                    statusArray[i] = 0;
                }
                // remove points for which tracking has failed
                previousKeyPointsList.remove(i - indexCorrection);
                currentKeyPointsList.remove(i - indexCorrection);
                indexCorrection++;
            }
        }

        filteredPointMap.put("previousKeyPointsList", previousKeyPointsList);
        filteredPointMap.put("currentKeyPointsList", currentKeyPointsList);
        return filteredPointMap;
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

    public Mat getDescriptors() {
        return descriptors;
    }

    public void setDescriptors(Mat descriptors) {
        this.descriptors = descriptors;
    }

    public MatOfKeyPoint getKeyPoints() {
        return keyPoints;
    }

    public void setKeyPoints(MatOfKeyPoint keyPoints) {
        this.keyPoints = keyPoints;
    }
}
