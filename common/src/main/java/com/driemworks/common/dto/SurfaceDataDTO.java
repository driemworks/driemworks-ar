package com.driemworks.common.dto;

import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;

import java.util.List;

/**
 * @author Tony
 */
public class SurfaceDataDTO {

    private Mat mRgba;

    private List<MatOfPoint> contours;

    private Rect boundRect;

    private double alpha;

    private MatOfInt hull;

    private MatOfInt4 convexDefect;

    private List<Point> hullPoints;
    private List<Point> defectPoints;
    private List<MatOfPoint> mHullPoints;
    private List<MatOfPoint> mDefectPoints;

    private int zCoordinate;

    public SurfaceDataDTO() {}

    public SurfaceDataDTO(List<MatOfPoint> contours, Rect boundRect, double alpha, MatOfInt4 convexDefect,
                          MatOfInt hull,List<Point> hullPoints, List<Point> defectPoints, List<MatOfPoint> mDefectPoints, List<MatOfPoint> mHullPoints, int zCoordinate) {
        this.contours = contours;
        this.boundRect = boundRect;
        this.alpha = alpha;
        this.convexDefect = convexDefect;
        this. hull = hull;
        this.hullPoints = hullPoints;
        this.defectPoints = defectPoints;
        this.mDefectPoints = mDefectPoints;
        this.mHullPoints = mHullPoints;
        this.zCoordinate = zCoordinate;
    }

    public int getzCoordinate() {
        return zCoordinate;
    }

    public void setzCoordinate(int zCoordinate) {
        this.zCoordinate = zCoordinate;
    }

    public Mat getmRgba() {
        return mRgba;
    }

    public void setmRgba(Mat mRgba) {
        this.mRgba = mRgba;
    }

    public List<MatOfPoint> getContours() {
        return contours;
    }

    public void setContours(List<MatOfPoint> contours) {
        this.contours = contours;
    }

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public MatOfInt getHull() {
        return hull;
    }

    public void setHull(MatOfInt hull) {
        this.hull = hull;
    }

    public MatOfInt4 getConvexDefect() {
        return convexDefect;
    }

    public void setConvexDefect(MatOfInt4 convexDefect) {
        this.convexDefect = convexDefect;
    }

    public List<Point> getHullPoints() {
        return hullPoints;
    }

    public void setHullPoints(List<Point> hullPoints) {
        this.hullPoints = hullPoints;
    }

    public List<Point> getDefectPoints() {
        return defectPoints;
    }

    public void setDefectPoints(List<Point> defectPoints) {
        this.defectPoints = defectPoints;
    }

    public List<MatOfPoint> getmHullPoints() {
        return mHullPoints;
    }

    public void setmHullPoints(List<MatOfPoint> mHullPoints) {
        this.mHullPoints = mHullPoints;
    }

    public List<MatOfPoint> getmDefectPoints() {
        return mDefectPoints;
    }

    public void setmDefectPoints(List<MatOfPoint> mDefectPoints) {
        this.mDefectPoints = mDefectPoints;
    }

    public Rect getBoundRect() {
        return boundRect;
    }

    public void setBoundRect(Rect boundRect) {
        this.boundRect = boundRect;
    }
}
