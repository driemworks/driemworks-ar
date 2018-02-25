package com.driemworks.common.utils;

import android.util.Log;
import android.util.Pair;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * @author Tony
 */
public class OpenCvUtils {

    /**
     * Private constructor
     */
    private OpenCvUtils() {}

    /**
     * Sharpen the input image
     * @param input The image to sharpen
     * @return {@link Mat} The sharpened image
     */
    public static Mat sharpenImage(Mat input) {
        Mat output = input.clone();
        Imgproc.GaussianBlur(input, output, new Size(3,3), 3);
        Core.addWeighted(input, 1.7, output, -0.3, 1.0, output);
        return output;
    }

    /**
     * Initialize OpenCV
     * @param initCuda - if true then init cuda
     */
    public static void initOpenCV(boolean initCuda)  {
        if (!OpenCVLoader.initDebug(initCuda)) {
            Log.e("OpvenCVLoader", "OvenCVLoader successful: false");
        } else {
            Log.d("OpenCVLoader", "OpenCVLoader successful");
        }
    }

}
