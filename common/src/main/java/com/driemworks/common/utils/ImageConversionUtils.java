package com.driemworks.common.utils;

import org.opencv.core.CvType;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tony on 4/22/2017.
 */

public class ImageConversionUtils {

    private ImageConversionUtils() {}

    /**
     * Convert a scalar formatted in HSV to a scalar formatted with RGBA
     * @param hsvColor
     * @return
     */
    public static Scalar convertScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

        return new Scalar(pointMatRgba.get(0, 0));
    }

    /**
     * Converts a MatOfKeyPoint object to a MatOfPoint2f object
     * @param mKeyPoint the input MatOfKeyPoint
     * @return the converted MatOfPoint2f
     */
    public static MatOfPoint2f convertMatOfKeyPointsTo2f(MatOfKeyPoint mKeyPoint) {
        List<Point> points = new ArrayList<>();
        for (KeyPoint kp : mKeyPoint.toArray()) {
            points.add(kp.pt);
        }
        MatOfPoint2f pointMat = new MatOfPoint2f();
        pointMat.fromList(points);
        return pointMat;
    }

}
