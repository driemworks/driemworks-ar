package com.driemworks.common.dto;

import org.opencv.core.Scalar;

import java.io.Serializable;

/**
 * Created by Tony on 5/6/2017.
 */
public class ConfigurationDTO implements Serializable {

    /** The threshold used for opencv defect/contour detection */
    private double threshold;

    /** The color to be detected */
    private Scalar color;

    public ConfigurationDTO() {}

    public ConfigurationDTO(double threshold, Scalar color) {
        this.threshold = threshold;
        this.color = color;
    }

    public void update(double threshold, Scalar color) {
        this.setColor(color);
        this.setThreshold(threshold);
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public Scalar getColor() {
        return color;
    }

    public void setColor(Scalar color) {
        this.color = color;
    }

}
