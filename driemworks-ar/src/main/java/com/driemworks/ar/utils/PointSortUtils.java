package com.driemworks.ar.utils;

import org.opencv.core.Point;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Tony
 */
public class PointSortUtils {

    /**
     * Sort the raw point data by minimum x value
     * @param rawPointData the input raw point data
     * @return the sorted raw point data
     */
    public static List<Point> sortByMaxX(List<Point> rawPointData) {
        Collections.sort(rawPointData, new Comparator<Point>() {
            @Override
            public int compare(Point o1, Point o2) {
                if (o1.x < o2.x) return 1;
                else if (o1.x > o2.x) return -1;
                return 0;
            }
        });
        return rawPointData;
    }

    /**
     * Sort the raw point data by maximum x value
     * @param rawPointData
     * @return the sorted raw point data
     */
    public static List<Point> sortByMinX(List<Point> rawPointData) {
        Collections.sort(rawPointData, new Comparator<Point>() {
            @Override
            public int compare(Point o1, Point o2) {
                if (o1.x < o2.x) return -1;
                else if (o1.x > o2.x) return 1;
                return 0;
            }
        });
        return rawPointData;
    }

    /**
     * Sort the raw point data by minimum y value
     * @param rawPointData
     * @return
     */
    public static List<Point> sortByMaxY(List<Point> rawPointData) {
        Collections.sort(rawPointData, new Comparator<Point>() {
            @Override
            public int compare(Point o1, Point o2) {
                if (o1.y < o2.y) return 1;
                else if (o1.y > o2.y) return -1;
                return 0;
            }
        });
        return rawPointData;
    }

    /**
     * Sort the raw point data by maximum y value
     * @param rawPointData
     * @return
     */
    public static List<Point> sortByMinY(List<Point> rawPointData) {
        Collections.sort(rawPointData, new Comparator<Point>() {
            @Override
            public int compare(Point o1, Point o2) {
                if (o1.y < o2.y) return -1;
                else if (o1.y > o2.y) return 1;
                return 0;
            }
        });
        return rawPointData;
    }
}


























