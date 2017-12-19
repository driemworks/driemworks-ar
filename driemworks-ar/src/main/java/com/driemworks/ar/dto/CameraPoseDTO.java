package com.driemworks.ar.dto;

import com.driemworks.ar.utils.CvUtils;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
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
     * The default constructor
     */
    public CameraPoseDTO() {
        super();

        // init coordinate to (0, 0, 0)
        this.coordinate = new Mat(3, 1, CvType.CV_64FC1);
        coordinate.setTo(new Scalar(0, 0,0));

        // init direction to identity
        this.direction = new Mat(3,3, CvType.CV_64FC1);
        direction.setTo(new Scalar(0, 0, 0));
        double[] one = {1};
        direction.put(0, 0,one);
        direction.put(1, 1, one);
        direction.put(2, 2, one);
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
     * Update the coordinate and direction using a translation vector and a rotation matrix
     * @param translation The 3 x 1 translation vector
     * @param rotation The 3 x 3 rotation matrix
     */
    public void update(Mat translation, Mat rotation) {
        this.coordinate = CvUtils.add(this.coordinate, translation);
        this.direction = CvUtils.mult(this.direction, rotation);
    }

    @Override
    public String toString() {
        return "CameraPoseDTO: " + "current coordinate: "
                + CvUtils.printMat(coordinate) + "current direction: " + CvUtils.printMat(direction);
    }

    public Mat getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Mat coordinate) {
        this.coordinate = coordinate;
    }

    public Mat getDirection() {
        return direction;
    }

    public void setDirection(Mat direction) {
        this.direction = direction;
    }
}
