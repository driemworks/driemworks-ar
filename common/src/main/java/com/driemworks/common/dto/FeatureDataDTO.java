package com.driemworks.common.dto;

import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;

/**
 * @author Tony
 */
public class FeatureDataDTO {

    /** The keypoints */
    private MatOfKeyPoint keyPoints;

    /** The desriptors */
    private Mat descriptors;

    /** The image */
    private Mat image;

    /**
     * The default constructor the FeatureDataDTO
     */
    public FeatureDataDTO() {
        this.keyPoints = new MatOfKeyPoint();
        this.descriptors = new Mat();
        this.image = new Mat();
    }

    /**
     * Constructor for the FeatureDataDTO
     * @param keyPoints The keypoints
     * @param descriptors The descriptors
     * @param image The image
     */
    public FeatureDataDTO(MatOfKeyPoint keyPoints, Mat descriptors, Mat image) {
        this.keyPoints = keyPoints;
        this.descriptors = descriptors;
        this.image = image;
    }

    /**
     * Constructor for the FeatureDataDTO
     * @param image The image
     */
    public FeatureDataDTO(Mat image) {
        this.image = image;
        this.keyPoints = new MatOfKeyPoint();
        this.descriptors = new Mat();
    }

    public boolean isEmpty() {
        return this.image.empty() || this.descriptors.empty() || this.keyPoints.empty();
    }

    public MatOfKeyPoint getKeyPoints() {
        return keyPoints;
    }

    public void setKeyPoints(MatOfKeyPoint keyPoints) {
        this.keyPoints = keyPoints;
    }

    public Mat getDescriptors() {
        return descriptors;
    }

    public void setDescriptors(Mat descriptors) {
        this.descriptors = descriptors;
    }

    public Mat getImage() {
        return image;
    }

    public void setImage(Mat image) {
        this.image = image;
    }
}
