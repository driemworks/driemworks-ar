package com.driemworks.ar.dto;

import android.util.Log;

import com.driemworks.ar.services.MonocularVisualOdometryService;
import com.driemworks.ar.utils.CvUtils;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Scalar;

/**
 * The camera pose dto
 *
 * The camera pose consists of a coordinate in 3-space
 * and a direction in said space relative to a preset position
 *
 * @author Tony
 */
public class CameraPoseDTO {

    /**
     * The coordinate
     */
    private Mat coordinate;

    /**
     * The direction
     */
    private Mat direction;

    /**
     * The current keypoints
     */
    private MatOfKeyPoint keyPoints;

    private FeatureData featureData;

    /**
     * The default constructor
     */
    public CameraPoseDTO() {
        super();
        reset();
    }

    /**
     * Constructor for the CameraPoseDTO
     * @param coordinate The coordinate
     * @param direction The direction
     */
    public CameraPoseDTO(Mat coordinate, Mat direction) {
        this.coordinate = coordinate;
        this.direction = direction;
    }

    /**
     * Constructor for the CameraPoseDTO
     * @param coordinate The coordinate
     * @param direction The direction
     * @param keyPoints The keypoints
     */
    public CameraPoseDTO(Mat coordinate, Mat direction, MatOfKeyPoint keyPoints) {
        this.coordinate = coordinate;
        this.direction = direction;
        this.keyPoints = keyPoints;
    }

    /**
     * Update the coordinate and direction using a translation vector and a rotation matrix
     * @param translation The 3 x 1 translation vector
     * @param rotation The 3 x 3 rotation matrix
     */
    public void update(Mat translation, Mat rotation) {
        Log.d("rotation: ", "" + rotation);
        Log.d("translation: ", "" + translation);
        Log.d("translation z: ", "" + translation.get(2, 0)[0]);
        this.direction = CvUtils.mult(this.direction, rotation);
//        this.coordinate = CvUtils.add(this.coordinate, CvUtils.mult(this.direction, translation));
        this.coordinate = CvUtils.add(this.coordinate, translation);
    }

    /**
     * Set the coordinate to (0,0,0)
     * Set the direction to the identity matrix
     */
    public void reset() {
        resetDirection();
        resetCoordinate();
        keyPoints = new MatOfKeyPoint();
    }

    /**
     * Set the coordinate to (0,0,0)
     */
    private void  resetCoordinate() {
        this.coordinate = new Mat(3, 1, CvType.CV_64FC1);
        coordinate.setTo(new Scalar(0, 0,0));
    }

    /**
     * Set the direction to the (3x3) identity matrix
     */
    private void resetDirection() {
        this.direction = new Mat(3,3, CvType.CV_64FC1);
        direction.setTo(new Scalar(0, 0, 0));

        double[] one = {1};
        direction.put(0, 0, one);
        direction.put(1, 1, one);
        direction.put(2, 2, one);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "CameraPoseDTO: \n" + "current coordinate: \n"
                + CvUtils.printMat(coordinate) + "\ncurrent direction: \n" + CvUtils.printMat(direction);
    }

    /**
     * Getter for the coordinate
     * @return coordinate The coordinate
     */
    public Mat getCoordinate() {
        return coordinate;
    }

    /**
     * Setter for the coordinate
     * @param coordinate The coordinate to set
     */
    public void setCoordinate(Mat coordinate) {
        this.coordinate = coordinate;
    }

    /**
     * Getter for the direction
     * @return direction
     */
    public Mat getDirection() {
        return direction;
    }

    /**
     * Setter for the direction
     * @param direction The direction to set
     */
    public void setDirection(Mat direction) {
        this.direction = direction;
    }

    /**
     * Getter for the keypoints
     * @return keypoints The keypoints
     */
    public MatOfKeyPoint getKeyPoints() {
        return keyPoints;
    }

    /**
     * Setter for the keypoints
     * @param keyPoints The keypoints to set
     */
    public void setKeyPoints(MatOfKeyPoint keyPoints) {
        this.keyPoints = keyPoints;
    }

    /**
     * Get the x coordinae
     * @return The x coordinate
     */
    public double getX() {
        return this.coordinate.get(0,0)[0];
    }

    /**
     * Get the y coordinate
     * @return The y coordinate
     */
    public double getY() {
        return this.coordinate.get(1,0)[0];
    }

    /**
     * Get the z coordinate
     * @return The z coordinate
     */
    public double getZ() {
        return this.coordinate.get(2,0)[0];
    }

    public FeatureData getFeatureData() {
        return featureData;
    }

    public void setFeatureData(FeatureData featureData) {
        this.featureData = featureData;
    }
}
