package com.driemworks.common.utils;

import android.util.Log;
import android.util.Pair;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * @author Tony
 */
public class OpenCvUtils {
    /**
     * The tag used for logging
     */
    private static final String TAG = TagUtils.getTag(OpenCvUtils.class);

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
     * Draw a rotated rect on the image
     * @param rect The rotated rect
     * @param image The image
     * @param color The color of the outline of the rect
     */
    public static void drawRotatedRect(RotatedRect rect, Mat image, Scalar color) {
        Point[] points = new Point[4];
        rect.points(points);
        for(int i=0; i<4; ++i){
            Imgproc.line(image, points[i], points[(i+1)%4], color);
        }
    }

    /**
     * Initialize OpenCV
     * @param initCuda - if true then init cuda
     */
    public static void initOpenCV(boolean initCuda)  {
        if (!OpenCVLoader.initDebug(initCuda)) {
            Log.e(TAG, "OvenCVLoader successful: false");
        } else {
            Log.d(TAG, "OpenCVLoader successful");
        }
    }
}
