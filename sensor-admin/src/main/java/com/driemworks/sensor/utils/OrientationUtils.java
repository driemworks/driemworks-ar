package com.driemworks.sensor.utils;

import android.util.Log;

/**
 * Utility methods for calculating device orientation
 * @author Tony
 */
public class OrientationUtils {

    /**
     * private constructor to override the implicit public one
     */
    private OrientationUtils() {}

    /**
     * Calculates the change in orientation between two rotation vectors
     * @param multiplier an (optional) multiplier
     * @param src1 the orientation vector at time t = t_i;
     * @param src2 the orientation vector at time t = t_j
     * @param dest the vector to store the change
     * @return the change in orientation
     */
    public static void calcDeltaRotation(float multiplier, float[] src1, float[] src2, float[] dest) {
        for (int i = 0; i < 3; i++) {
            Log.d("src minus src: ", ""  + (src1[i] - src2[i]));
            dest[i] = (multiplier != 0) ? (src1[i] - src2[i]) * multiplier : 0;
        }
    }

    /**
     * Copies src array to dest array
     * @param src The source array
     * @param dest The destination array
     */
    public static void copyVectors(float[] src, float[] dest) {
        if (src != null) {
            System.arraycopy(src,0, dest, 0, 3);
        }
    }
}
