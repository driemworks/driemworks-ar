package com.driemworks.ar.utils;

import android.util.Log;

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
 * Created by Tony on 4/23/2017.
 */

public class ImageProcessingUtils {

    private static final String TAG = "ImageProcessingUtils: ";

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
     *
     * @param contours
     * @param hull
     * @param positiveBound
     * @return
     */
    public static List<Point> getListOfPoints(List<MatOfPoint> contours, MatOfInt hull, int positiveBound) {
        List<Point> listPo = new LinkedList<Point>();
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
     *
     * @param contours
     * @param convexDefect
     * @param boundPos
     * @param iThreshold
     * @param a
     * @return
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
     *
     * @param boundRect
     * @return
     */
    public static double calculateAlpha(Rect boundRect) {
        double a = boundRect.br().y - boundRect.tl().y;
        a = a * 0.7;
        a = boundRect.tl().y + a;
        return a;
    }

    /**
     * Sorting methods
     */

    /**
     * Sort the list of points by maximum Y value
     * @param points
     * @return
     */
    public static List<Point> sortByMaxY(List<Point> points) {
        Collections.sort(points, new Comparator<Point>() {
            @Override
            public int compare(Point o1, Point o2) {
                if (o1.y > o2.y) return 1;
                else if (o1.y < o2.y) return - 1;
                else return 0;
            }
        });
        return points;
    }

    /**
     * Sort the list of points by minimum x value
     * @param points
     * @return
     */
    public static List<Point> sortByMinX(List<Point> points) {
        Collections.sort(points, new Comparator<Point>() {
            @Override
            public int compare(Point o1, Point o2) {
                if (o1.x < o2.x) return 1;
                if (o1.x == o2.x) return 0;
                else return -1;
            }
        });
        return points;
    }

}
