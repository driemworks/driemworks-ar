package com.driemworks.ar.dto;

import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;

public class FeatureData {

    private MatOfKeyPoint keyPoint;
    private Mat descriptors;

    public FeatureData(MatOfKeyPoint mKeyPoints, Mat descriptors) {
        this.keyPoint = mKeyPoints;
        this.descriptors = descriptors;
    }

    public MatOfKeyPoint getKeyPoint() {
        return keyPoint;
    }

    public void setKeyPoint(MatOfKeyPoint keyPoint) {
        this.keyPoint = keyPoint;
    }
}
