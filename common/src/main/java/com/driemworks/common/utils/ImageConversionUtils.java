package com.driemworks.common.utils;

import org.opencv.core.CvType;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for image conversions
 * @author Tony
 */
public class ImageConversionUtils {

    /** private constructor */
    private ImageConversionUtils() {}

    /**
     * Convert rgba mat to rgb
     * @param rgba
     * @return
     */
    public static Mat convertRgbaToGrayscale(Mat rgba) {
        Mat gray = new Mat();
        Imgproc.cvtColor(rgba, gray, Imgproc.COLOR_RGBA2GRAY);
        return gray;
    }

    /**
     * Convert a scalar formatted in HSV to a scalar formatted with RGBA
     * @param hsvColor the HSV color
     * @return the converted RGBA scalar
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

    /**
     * Create a MatOfPoint2f from a list of points
     * @param points the list of points
     * @return the MatOfPoint2f
     */
    public static MatOfPoint2f convertListToMatOfPoint2f(List<Point> points) {
        MatOfPoint2f mat = new MatOfPoint2f();
        mat.fromList(points);
        return mat;
    }

    public static MatOfKeyPoint convertMatOf2fToKeyPoints(MatOfPoint2f matOfPoint2f, float size, float angle) {
        MatOfKeyPoint matOfKeyPoint = new MatOfKeyPoint();
        List<KeyPoint> keypoints = new ArrayList<>();
        for (Point p : matOfPoint2f.toList()) {
            keypoints.add(new KeyPoint((float)p.x, (float)p.y, size, angle));
        }
        matOfKeyPoint.fromList(keypoints);
        return matOfKeyPoint;
    }

    /**
     * Converts a MatOfPoint2f to a MatOfPoint3f
     * @param mat The MatOfPoint2f
     * @return The MatOfPoint3f
     */
    public static MatOfPoint3f convertMatOfPoint2fTo3f(MatOfPoint2f mat, double z) {
        List<Point3> point3fList = new ArrayList<>();
        MatOfPoint3f matOfPoint3f = new MatOfPoint3f();

        for (Point point : mat.toList()) {
            point3fList.add(new Point3(point.x, point.y, z));
        }

        matOfPoint3f.fromList(point3fList);
        return matOfPoint3f;
    }

}
