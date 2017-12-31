package com.driemworks.ar.utils;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

/**
 * Computer Vision utilities
 * @author Tony
 */
public class CvUtils {

    /**
     *
     * Get the sum of the two matrices
     * @param m1 The first matrix
     * @param m2 The second matrix
     * @return The sum
     */
    public static Mat add(Mat m1, Mat m2) {
        assert m1.size() == m2.size();
        assert m1.type() == m2.type();
        Mat sum = new Mat(m1.size(), CvType.CV_64FC1);
        Core.add(m1, m2, sum);
        return sum;
    }

    /**
     * Get the matrix M = m1 x m2
     * @param m1 The first matrix
     * @param m2 The second matrix
     * @return The product
     */
    public static Mat mult(Mat m1, Mat m2) {
        assert m1.size() == m2.size();
        assert m1.type() == m2.type();
        Mat prod = new Mat(m1.size(), m1.type());
        Core.multiply(m1, m2, prod);
        return prod;
    }

    /**
     * Store the entries in the matrix in a string
     * @param mat The matrix
     * @return The string
     */
    public static String printMat(Mat mat) {
        String out = "";
        if (!mat.empty()) {
            for (int i = 0; i < mat.size().height; i++) {
                out += "[ ";
                for (int j = 0; j < mat.size().width; j++) {
                    Log.d("size of array: ", "" + mat.get(i, j).length);
                    out += mat.get(i, j)[0] + " ";
                }
                out += "]\n";
            }
        }
        return out;
    }

}
