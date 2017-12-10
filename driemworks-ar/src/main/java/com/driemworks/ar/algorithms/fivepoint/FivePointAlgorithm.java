package com.driemworks.ar.algorithms.fivepoint;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;

/**
 * An implementation of Nister's five-point algorithm.
 * http://www.ee.oulu.fi/research/imag/courses/Sturm/nister04.pdf
 *
 * Given two sequential frames, f_i and f_i+1, and given five keypoints from each image
 * such that the keypoints in f_i are tracked to the keypoints in f_i+1, calculate the
 * rotation matrix, R, and the translation vector, t, of the camera pose between the two images.
 *
 * @author Tony
 */
public class FivePointAlgorithm {

    /*
        1) Nullspace Extraction
        2) Constraint Expansion
        3) Gauss-Jordan Elimination
        4) Determinant Expansion
        5) Root Extraction
        6) R and t Recovery
     */

    /**
     * Find the essential matrix using matching keypoints in two images
     * @param previousPoints The keypoints from the previous image
     * @param currentPoints The keypoints from the current image
     * @return The essential matrix
     */
    public Mat findEssentialMat(MatOfPoint2f previousPoints, MatOfPoint2f currentPoints) {
        Mat E = new Mat();
        return E;
    }

}
