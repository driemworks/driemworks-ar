package com.driemworks.ar.dto;

import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;

/**
 * Class to hold all info we need to know about the extracted features of the object.
 *
 * It has the original image, the extracted key points, and the extracted features
 *
 */
public class FeatureWrapper extends FeatureWrapperMetaData {

    private Mat frame;
    private Mat descriptors;
    private MatOfKeyPoint keyPoints;

    /**
     *
     * @param featureDetector
     * @param featureExtractor
     * @param frame
     * @param descriptors
     * @param keyPoints
     */
    public FeatureWrapper(String featureDetector, String featureExtractor,
                          Mat frame, Mat descriptors, MatOfKeyPoint keyPoints) {
        super(featureDetector, featureExtractor);
        this.frame = frame;
        this.descriptors = descriptors;
        this.keyPoints = keyPoints;
    }

    public Mat getFrame() {
        return frame;
    }

    public void setFrame(Mat frame) {
        this.frame = frame;
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