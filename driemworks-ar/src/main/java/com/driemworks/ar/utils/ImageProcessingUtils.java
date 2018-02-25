package com.driemworks.ar.utils;

import android.util.Log;

import com.driemworks.common.utils.TagUtils;

import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.imgproc.Imgproc;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Image Processing utilities
 * @author Tony
 */
public class ImageProcessingUtils {

    /**
     * The constant TAG
     */
    private static final String TAG = TagUtils.getTag(ImageProcessingUtils.class);

    /** The default constructor */
    private ImageProcessingUtils() {}

    /**
     *
     * @param rect
     * @param contours
     * @return
     */
    public static Map<Integer, Rect> getBoundedRect(RotatedRect rect, List<MatOfPoint> contours) {
        double boundWidth = rect.size.width;
        double boundHeight = rect.size.height;
        int boundPos = 0;

        Map<Integer, Rect> rectMap = new HashMap<>();

        for (int i = 1; i < contours.size(); i++) {
            rect = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(i).toArray()));
            if (rect.size.width * rect.size.height > boundWidth * boundHeight) {
                boundWidth = rect.size.width;
                boundHeight = rect.size.height;
                boundPos = i;
            }
        }

        rectMap.put(boundPos, Imgproc.boundingRect(new MatOfPoint(contours.get(boundPos).toArray())));
        return rectMap;
    }

    /**
     * Calculate the vertices of the convex polygon represented by the hull
     * @param contours
     * @param hull
     * @param positiveBound
     * @return
     */
    public static List<Point> getListOfPoints(List<MatOfPoint> contours, MatOfInt hull, int positiveBound) {
        List<Point> listPo = new LinkedList<>();
        for (int j = 0; j < hull.toList().size(); j++) {
            listPo.add(contours.get(positiveBound).toList().get(hull.toList().get(j)));
        }
        return listPo;
    }

    /**
     *
     * @param contours
     * @param points
     * @return
     */
    public static List<MatOfPoint> getMatOfPointFromPoints(List<MatOfPoint> contours, List<Point> points) {
        List<MatOfPoint> matOfPoints = new LinkedList<>();
        MatOfPoint e = new MatOfPoint();
        e.fromList(points);
        matOfPoints.add(e);
        return matOfPoints;
    }

    /**
     * Get the defect points
     * @param contours The contours
     * @param convexDefect The convex defect
     * @param boundPos The positive bound
     * @param iThreshold The threshold
     * @param a The alpha value
     * @return The list of defect points
     */
    public static List<Point> getDefectPoints(List<MatOfPoint> contours, MatOfInt4 convexDefect, int boundPos, double iThreshold, double a) {
        List<Point> listPoDefect = new LinkedList<Point>();
        for (int j = 0; j < convexDefect.toList().size(); j += 4) {
            Point farPoint = contours.get(boundPos).toList().get(convexDefect.toList().get(j+2));
            Integer depth = convexDefect.toList().get(j+3);
            if(depth > iThreshold && farPoint.y < a){
                listPoDefect.add(contours.get(boundPos).toList().get(convexDefect.toList().get(j+2)));
            }
            Log.d(TAG, "defects ["+j+"] " + convexDefect.toList().get(j+3));
        }
        return listPoDefect;
    }

    /**
     * Calculate the alpha value, with multipler 0.7
     * @param boundRect The bound rectabnle
     * @return The alpha value
     */
    public static double calculateAlpha(Rect boundRect) {
        double a = boundRect.br().y - boundRect.tl().y;
        a = a * 0.7;
        a = boundRect.tl().y + a;
        return a;
    }

}
