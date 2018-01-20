package com.driemworks.ar.dto;

import org.opencv.core.Point;

import java.util.List;

/**
 * @author Tony
 */

public class SequentialFrameFeatures {

    /** The previous frame's features as a list of points */
    private List<Point> previousFrameFeaturePoints;

    /** The current frame's features as a list of points */
    private List<Point> currentFrameFeaturePoints;

    /**
     * Default constructor
     */
    public SequentialFrameFeatures() {super();}

    /**
     * Constructor for the SequentialFrameFeatures
     * @param previousFrameFeaturePoints
     * @param currentFrameFeaturePoints
     */
    public SequentialFrameFeatures(List<Point> previousFrameFeaturePoints, List<Point> currentFrameFeaturePoints) {
        this.previousFrameFeaturePoints = previousFrameFeaturePoints;
        this.currentFrameFeaturePoints = currentFrameFeaturePoints;
    }

    public List<Point> getPreviousFrameFeaturePoints() {
        return previousFrameFeaturePoints;
    }

    public void setPreviousFrameFeaturePoints(List<Point> previousFrameFeaturePoints) {
        this.previousFrameFeaturePoints = previousFrameFeaturePoints;
    }

    public List<Point> getCurrentFrameFeaturePoints() {
        return currentFrameFeaturePoints;
    }

    public void setCurrentFrameFeaturePoints(List<Point> currentFrameFeaturePoints) {
        this.currentFrameFeaturePoints = currentFrameFeaturePoints;
    }
}
