package com.driemworks.simplecv.utils;

import android.util.Log;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

/**
 * Created by Tony on 7/7/2017.
 */

public class OpenCvUtils {

    private OpenCvUtils() {}

    /**
     * Load opencv
     */
    public static void loadOpenCv() {
        if (!OpenCVLoader.initDebug()) {
            Log.e("OpvenCVLoader", "OvenCVLoader successful: false");
        } else {
            Log.d("OpenCVLoader", "OpenCVLoader successful");
        }
    }

    public static void colorConvexPolygon(Mat mRgba) {

    }

}
