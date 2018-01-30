package com.driemworks.ar.dto;

import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;

/**
 * This object is for transfering odometry data
 * Specifically, the rotation and translation matrices
 * @author Tony
 */
public class OdometryDataDTO {

    /**
     * The (3x3) rotation matrix
     */
    private Mat rotationMatrix;

    /**
     * The (3x1) translation matrix
     */
    private Mat translationMatrix;

    private MatOfKeyPoint currentKeypoints;

    /**
     * The default constructor
     */
    public OdometryDataDTO() {
        super();
    }

    /**
     * Constructor
     * @param rotationMatrix The rotation matrix
     * @param translationMatrix The translation matrix
     */
    public OdometryDataDTO(Mat rotationMatrix, Mat translationMatrix) {
        this.rotationMatrix = rotationMatrix;
        this.translationMatrix = translationMatrix;
    }

    /**
     * Check if the fields are empty
     * @return {@link boolean}
     */
    public boolean empty() {
        return rotationMatrix.empty() || translationMatrix.empty();
    }

    public Mat getRotationMatrix() {
        return rotationMatrix;
    }

    public void setRotationMatrix(Mat rotationMatrix) {
        this.rotationMatrix = rotationMatrix;
    }

    public Mat getTranslationMatrix() {
        return translationMatrix;
    }

    public void setTranslationMatrix(Mat translationMatrix) {
        this.translationMatrix = translationMatrix;
    }
}
