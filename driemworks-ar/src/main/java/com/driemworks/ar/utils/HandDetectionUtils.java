package com.driemworks.ar.utils;

import android.util.Log;

import org.opencv.core.Point;

import java.util.List;

/**
 * Created by Tony on 7/3/2017.
 */

public class HandDetectionUtils {

    /**
     *
     * @param points
     * @return
     */
    public static Point getGunTip(List<Point> points) {
        // sort by minimum y value
        points = PointSortUtils.sortByMinY(points);
        // get the first 5 entries in the list
        List<Point> minYPoints = points.subList(0, 4);
        // sort by minimum x value, return the first point of that list
        return PointSortUtils.sortByMinX(minYPoints).get(0);
    }

}