package com.driemworks.ar.dto;

import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.imgproc.Imgproc;

/**
 * Class to hold all info we need to know about the extracted features of the object.
 * @author Tony
 *
 */
public class FeatureWrapper {

    /**
     * The feature wrapper meta data
     */
    private FeatureWrapperMetaData featureWrapperMetaData;

    /**
     * The current camera frame
     */
    private Mat frame;

    /**
     * The descriptors
     */
    private Mat descriptors;

    /**
     * The mat of key points
     */
    private MatOfKeyPoint keyPoints;

    /**
     * The mat of matches
     */
    private MatOfDMatch matches;
    /**
     * Creates a clone of a FeatureWrapper
     * @param wrapper the FeatureWrapper to be cloned
     * @return {@link FeatureWrapper}
     */
    public static FeatureWrapper clone(FeatureWrapper wrapper) {
        return new FeatureWrapper(wrapper.getFeatureWrapperMetaData(),
                wrapper.getFrame(), wrapper.getDescriptors(), wrapper.getKeyPoints(), wrapper.getMatches());
    }

    /**
     * Release all mat objects from memory, provided they are not null
     */
    public void release() {
        if (frame != null) {
            frame.release();
        }

        if (descriptors != null) {
            descriptors.release();
        }

        if (keyPoints != null) {
            keyPoints.release();
        }

        if (matches != null) {
            matches.release();
        }
    }

    /**
     * Constructor for the FeatureWrapper
     * @param metaData The FeatureWrapperMetaData
     * @param frame The current frame
     * @param descriptors The descriptors
     * @param keyPoints The key points
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
     * Constructor for the FeatureWrapper
     * @param featureDetector The FeatureDetector
     * @param featureExtractor The FeatureExtractor
     * @param frame The camera image
     * @param descriptors The descriptors
     * @param keyPoints The keypoints
     */
    public FeatureWrapper(String featureDetector, String featureExtractor,
                          Mat frame, Mat descriptors, MatOfKeyPoint keyPoints, MatOfDMatch matches) {
        this.featureWrapperMetaData = new FeatureWrapperMetaData(featureDetector, featureExtractor);
        if (!frame.empty()) {
            this.frame = frame.clone();
        } else{
            // could possibly slow things down?
            this.frame = new Mat();
        }
        this.descriptors = descriptors.clone();
        this.keyPoints = keyPoints;
        this.matches = matches;
    }

    /**
     * Getter for the frame
     * @return frame The frame
     */
    public Mat getFrame() {
        return frame;
    }

    /**
     * Setter for the frame
     * @param frame The frame to set
     */
    public void setFrame(Mat frame) {
        this.frame = frame;
    }

    /**
     * Getter for the descriptors
     * @return descriptors The descriptors
     */
    public Mat getDescriptors() {
        return descriptors;
    }

    /**
     * Setter for the descriptors
     * @param descriptors The descriptors to set
     */
    public void setDescriptors(Mat descriptors) {
        this.descriptors = descriptors;
    }

    /**
     * Getter for the keypoints
     * @return keyPoints
     */
    public MatOfKeyPoint getKeyPoints() {
        return keyPoints;
    }

    /**
     * Setter for the keyPoints
     * @param keyPoints The keyPoints to set
     */
    public void setKeyPoints(MatOfKeyPoint keyPoints) {
        this.keyPoints = keyPoints;
    }

    /**
     * Getter for the featureWrapperMetaData
     * @return featureWrapperMetaData
     */
    public FeatureWrapperMetaData getFeatureWrapperMetaData() {
        return featureWrapperMetaData;
    }

    /**
     * Setter for the featureWrapperMetaData
     * @param featureWrapperMetaData The featureWrapperMetaData to set
     */
    public void setFeatureWrapperMetaData(FeatureWrapperMetaData featureWrapperMetaData) {
        this.featureWrapperMetaData = featureWrapperMetaData;
    }

    /**
     * Getter for the matches
     * @return matches
     */
    public MatOfDMatch getMatches() {
        return matches;
    }

    /**
     * Setter for the matches
     * @param matches The matches to set
     */
    public void setMatches(MatOfDMatch matches) {
        this.matches = matches;
    }

    /**
     * Convert the frame (rgba) to gray
     * @return the converted frame
     */
    public Mat getFrameAsGrayscale() {
        Mat gray = new Mat();
        Imgproc.cvtColor(frame, gray, Imgproc.COLOR_RGBA2GRAY);
        return gray;
    }
}