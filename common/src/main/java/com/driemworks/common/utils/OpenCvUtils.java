package com.driemworks.common.utils;

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
        Imgproc.GaussianBlur(input, output, new Size(0,0), 3);
        Core.addWeighted(input, 1.7, output, -0.5, 0, output);
        return output;
    }

}
