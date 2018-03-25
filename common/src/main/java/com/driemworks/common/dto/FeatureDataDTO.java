package com.driemworks.common.dto;

import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;

/**
 * @author Tony
 */
public class FeatureDataDTO {

    private MatOfKeyPoint keyPoints;

    private Mat descriptors;

    private Mat image;

    public FeatureDataDTO() {
        this.keyPoints = new MatOfKeyPoint();
        this.descriptors = new Mat();
        this.image = new Mat();
    }

    public FeatureDataDTO(MatOfKeyPoint keyPoints, Mat descriptors, Mat image) {
        this.keyPoints = keyPoints;
        this.descriptors = descriptors;
        this.image = image;
    }

    public FeatureDataDTO(Mat image) {
        this.image = image;
        this.keyPoints = new MatOfKeyPoint();
        this.descriptors = new Mat();
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
