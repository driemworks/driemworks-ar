package com.driemworks.ar.utils;

import android.util.Log;

import com.driemworks.ar.enums.ExtremesEnum;
import com.driemworks.ar.enums.FingerEnum;
import com.driemworks.ar.enums.HandEnum;
import com.driemworks.common.enums.Coordinates;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Tony
 */
public class DetectorUtils {

    private static final String TAG = "DetectorUtils";

    /**
     *
     * @param rawPointData
     * @return
     */
    public static List<Point> filterFingerTips(List<Point> rawPointData, float epsilon) {
        List<Point> filteredPoints = smoothSimilarPoints(rawPointData, epsilon);
        return filteredPoints;
    }


    public static Point getFinger(FingerEnum finger, HandEnum hand, List<Point> points) {
        if (hand.getValue().equals(HandEnum.LEFT.getValue())) {
            if (finger.getIndexLeft() == FingerEnum.THUMB.getIndexLeft()){
                return getPoint(ExtremesEnum.MAX.getValue(), Coordinates.X.getValue(), points);
            } else if (finger.getIndexLeft() == FingerEnum.PINKY.getIndexLeft()) {

            }
        }
        return null;
    }

    /**
     *
     * @param points
     * @param epsilon
     * @return
     */
    private static List<Point> smoothSimilarPoints(List<Point> points, float epsilon) {
        Map<Integer, List<Point>> pointGroupMap = new HashMap<>();
        List<Point> smoothedPoints = new ArrayList<>();
        Point previousPoint = null;

        int currentGroupIndex = 0;
        for (Point p : points) {
            if (previousPoint != null) {
                if (isInNeighborhood(p, previousPoint, epsilon)) {
                    // the previous point is in the map at the current group index, and the point p
                    // is in an epsilon-neighborhood of the previous point,
                    // so we add the point to the list of points in the map corresponding to the
                    // current group index, and we DO NOT increment the index
                    if (pointGroupMap.get(currentGroupIndex).contains(previousPoint)) {
                            smoothedPoints.clear();
                            smoothedPoints = pointGroupMap.get(currentGroupIndex);
                            smoothedPoints.add(p);
                            pointGroupMap.put(currentGroupIndex, smoothedPoints);
                    }
                }
            } else {
                // the point is not in an epsilon-nbhd of previous point,
                // so we make increment the current group index
                // and add a new entry to the map
                currentGroupIndex++;
                smoothedPoints.clear();
                smoothedPoints.add(p);
                pointGroupMap.put(currentGroupIndex, smoothedPoints);

            }

            previousPoint = p;
        }

        smoothedPoints.clear();
        if (!pointGroupMap.isEmpty()) {
            for (Integer group : pointGroupMap.keySet()) {
                if (!pointGroupMap.get(group).isEmpty()) {
                    smoothedPoints.add(selectPoint(pointGroupMap.get(group)));
                }
            }
        }

        Log.d(TAG, "smoothed point count: " + smoothedPoints.size());
        return smoothedPoints;
    }

    /**
     * Determines which point to be selected from the smoothed point grouping
     * @param smoothedPoints
     * @return
     */
    private static Point selectPoint(List<Point> smoothedPoints) {
        if (smoothedPoints.size() == 1) {
            return smoothedPoints.get(0);
        } else if (smoothedPoints.size() == 2) {
            return smoothedPoints.get(1);
        } else if (smoothedPoints.size() % 2 == 0) {
            return smoothedPoints.get(smoothedPoints.size() / 2);
        } else {
            return smoothedPoints.get((smoothedPoints.size() + 1)/2);
        }
    }

    /**
     * determine if p1 or p2 are in epsilon neighborhoods of each other
     * @param p1
     * @param p2
     * @param epsilon
     * @return true if p1 is in an epsilon nbhd of p2, or vice versa
     *         false otherwise
     */
    private static boolean isInNeighborhood(Point p1, Point p2, float epsilon)  {
        return  (Math.abs(p1.x - p2.x) < epsilon && Math.abs(p1.y - p2.y) < epsilon);
    }

    /**
     *
     * @param extreme
     * @param coordinate
     * @param points
     * @return
     */
    private static Point getPoint(String extreme, String coordinate, List<Point> points) {
        if (extreme.equals(ExtremesEnum.MAX.getValue())) {
            if (coordinate.equals(Coordinates.X.getValue())) {
                return PointSortUtils.sortByMaxX(points).get(1);
            }
        } else if (extreme.equals(ExtremesEnum.MIN.getValue())) {
            if (coordinate.equals(Coordinates.X.getValue())) {

            }
        }
        return null;
    }

    public static boolean selectColor(Point correctedCoordinate, Mat rgba) {
        // the value used to bound the size of the area to be sampled
        int sizeThreshold = 8;

        Rect touchedRect = new Rect((int)correctedCoordinate.x, (int)correctedCoordinate.y, sizeThreshold, sizeThreshold);
        if (null == touchedRect) {
            return false;
        }

        // get the rectangle around the point that was touched
        Mat touchedRegionRgba = rgba.submat(touchedRect);

        // format to hsv
        Mat touchedRegionHsv = new Mat();
        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

        // Calculate average color of touched region
        Scalar mBlobColorHsv = Core.sumElems(touchedRegionHsv);
        int pointCount = touchedRect.width * touchedRect.height;

        for (int i = 0; i < mBlobColorHsv.val.length; i++) {
            mBlobColorHsv.val[i] /= pointCount;
        }

        Log.d(TAG, "color has been set");

        touchedRegionRgba.release();
        touchedRegionHsv.release();

        return true;
    }

}
