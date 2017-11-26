package com.driemworks.ar.MonocularVisualOdometry;

import com.driemworks.ar.dto.FeatureWrapper;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point3;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.video.Video;

/**
 * Monocular visual odometry using FAST/ORB/Hamming distance
 *
 * @author Tony Riemer
 *
 */
public class MonocularVisualOdometryService {

    /** The detector */
    private FeatureDetector detector;

    /** The descriptor descriptorExtractor */
    private DescriptorExtractor descriptorExtractor;

    /** The matcher */
    private DescriptorMatcher matcher;

    /** The extracted descriptor */
    private Mat descriptors;

    /** Extracted Keypoints */
    private MatOfKeyPoint keyPoints;

    /**
     * Constructor for the MonocularVisualOdometryService
     */
    public MonocularVisualOdometryService() {
        // FAST feature detector
        detector = FeatureDetector.create(FeatureDetector.FAST);
        // ORB descriptor extraction
        descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        // bruteforce hamming metric
        matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

        descriptors = new Mat();
        keyPoints = new MatOfKeyPoint();
    }

    /**
     *
     * @param frame
     * @return
     */
    public FeatureWrapper featureDetection(Mat frame) {
        MatOfKeyPoint mKeyPoints = new MatOfKeyPoint();
        MatOfDMatch matches = new MatOfDMatch();
        Mat mIntermediateMat = new Mat();

        detector.detect(frame, mKeyPoints);
        descriptorExtractor.compute(frame, mKeyPoints, mIntermediateMat);

        return new FeatureWrapper("fast", "orb", frame, mIntermediateMat, mKeyPoints);
    }

    /**
     *
     * @param previousImage
     * @param currentImage
     * @param previousKeyPoints
     * @param currentKeyPoints
     * @param status
     * @param err
     */
    public void featureTracking(Mat previousImage, Mat currentImage, MatOfPoint2f previousKeyPoints,
                                MatOfPoint2f currentKeyPoints, MatOfByte status, MatOfFloat err) {
//        TermCriteria termCriteria = new TermCriteria(0, 30, 0.01);
        TermCriteria termCriteria = new TermCriteria();

        Video.calcOpticalFlowPyrLK(previousImage, currentImage, previousKeyPoints, currentKeyPoints,
                status, err, new Size(21, 21), 0, termCriteria, /** flags */0, 0.001);

        // filter out points not tracked in current frame
        int indexCorrection = 0;
        for (int i = 0; i < status.toArray().length; i++) {

        }

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

    public DescriptorMatcher getMatcher() {
        return matcher;
    }

    public void setMatcher(DescriptorMatcher matcher) {
        this.matcher = matcher;
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
