package com.driemworks.ar.dto;

import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;

/**
 * Class to hold all info we need to know about the extracted features of the object.
 *
 * It has the original image, the extracted key points, and the extracted features
 *
 */
public class FeatureWrapper {

    private FeatureWrapperMetaData featureWrapperMetaData;
    private Mat frame;
    private Mat descriptors;
    private MatOfKeyPoint keyPoints;
    private MatOfDMatch matches;
    /**
     *
     * @param wrapper
     * @return
     */
    public static FeatureWrapper clone(FeatureWrapper wrapper) {
        return new FeatureWrapper(wrapper.getFeatureWrapperMetaData(),
                wrapper.getFrame(), wrapper.getDescriptors(), wrapper.getKeyPoints(), wrapper.getMatches());
    }

    /**
     *
     * @param metaData
     * @param frame
     * @param descriptors
     * @param keyPoints
     */
    public FeatureWrapper(FeatureWrapperMetaData metaData,
                          Mat frame, Mat descriptors, MatOfKeyPoint keyPoints, MatOfDMatch matches) {
        this.featureWrapperMetaData = metaData;
        this.frame = frame.clone();
        this.descriptors = descriptors.clone();
        this.keyPoints = keyPoints;
        this.matches = matches;
    }

    /**
     *
     * @param featureDetector
     * @param featureExtractor
     * @param frame
     * @param descriptors
     * @param keyPoints
     */
    public FeatureWrapper(String featureDetector, String featureExtractor,
                          Mat frame, Mat descriptors, MatOfKeyPoint keyPoints, MatOfDMatch matches) {
        this.featureWrapperMetaData = new FeatureWrapperMetaData(featureDetector, featureExtractor);
        this.frame = frame.clone();
        this.descriptors = descriptors.clone();
        this.keyPoints = keyPoints;
        this.matches = matches;
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

    public FeatureWrapperMetaData getFeatureWrapperMetaData() {
        return featureWrapperMetaData;
    }

    public void setFeatureWrapperMetaData(FeatureWrapperMetaData featureWrapperMetaData) {
        this.featureWrapperMetaData = featureWrapperMetaData;
    }

    public MatOfDMatch getMatches() {
        return matches;
    }

    public void setMatches(MatOfDMatch matches) {
        this.matches = matches;
    }
}