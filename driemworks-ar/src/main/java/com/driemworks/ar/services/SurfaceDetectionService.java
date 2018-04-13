package com.driemworks.ar.services;

import android.util.Log;

import com.driemworks.ar.enums.FingerEnum;
import com.driemworks.ar.enums.HandEnum;
import com.driemworks.ar.imageProcessing.ColorBlobDetector;
import com.driemworks.ar.utils.DetectorUtils;
import com.driemworks.ar.utils.ImageProcessingUtils;
import com.driemworks.common.dto.SurfaceDataDTO;
import com.driemworks.common.utils.TagUtils;

import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.List;
import java.util.Map;

/**
 * The service for detecting surfaces based on color
 * @author Tony
 */
public class SurfaceDetectionService {

    /**
     * The tag used for logging
     */
    private final String TAG = TagUtils.getTag(this);

    /**
     * The contour color used to draw box
     */
    private Scalar contourColorWhite;

    /**
     * The color used to draw contours on the image
     */
    private Scalar contourColor;

    /**
     * The (hsv) spectrum
     */
    private Mat mSpectrum;

    /**
     * The range of the spectrum
     */
    private Size spectrumRange;

    /**
     * The color blob detector
     */
    private ColorBlobDetector colorBlobDetector;

    /**
     * The default constructor
     */
    public SurfaceDetectionService() {
        this.contourColorWhite = new Scalar(255, 255, 255, 255);
        this.contourColor = new Scalar(222, 040, 255);
        this.mSpectrum = new Mat();
        this.spectrumRange = new Size(200, 64);
        colorBlobDetector = new ColorBlobDetector();
    }

    /**
     * Constructor for the SurfaceDetectionService
     *
     * @param contourColorWhite The primary color of the contour
     * @param contourColor The color of the contours
     * @param mSpectrum The spectrum,
     * @param spectrumSize The size of the spectrum (to detect)
     * @param hsvColor The hsv color
     */
    public SurfaceDetectionService(Scalar contourColorWhite, Scalar contourColor, Mat mSpectrum,
                                   Size spectrumSize, Scalar hsvColor) {
        this.contourColorWhite = contourColorWhite;
        this.contourColor = contourColor;
        this.mSpectrum = mSpectrum;
        this.spectrumRange = spectrumSize;

        colorBlobDetector = new ColorBlobDetector();
        if (hsvColor != null) {
            colorBlobDetector.setHsvColor(hsvColor);
        }
    }

    /**
     * Detect the surface
     * @param mRgba The image (in rgba format)
     * @param threshold The threshold value used for detecting defects
     * @param doDraw The do draw flag - if true, will draw on image
     * @return sufaceData The {@link SurfaceDataDTO}
     */
    public SurfaceDataDTO detect(Mat mRgba, double threshold, boolean doDraw) {
        return detectSurface(mRgba, threshold, doDraw);
    }

    /**
     * Detect the surface based on the hsv color set in the color blob detector
     * @param mRgba The rgba image
     * @param threshold The threshold
     * @param doDraw The do draw flag
     * @return surfaceData The detected surface
     */
    private SurfaceDataDTO detectSurface(Mat mRgba, double threshold, boolean doDraw) {
        // get contours and process the image
        SurfaceDataDTO surfaceData = new SurfaceDataDTO();
        List<MatOfPoint> contours = colorBlobDetector.process(mRgba);
        if (contours.isEmpty()) {
            return surfaceData;
        }

        RotatedRect rect = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(0).toArray()));

        Map<Integer, Rect> rectMap = ImageProcessingUtils.getBoundedRect(rect, contours);
        int boundPos = (int) rectMap.keySet().toArray()[0];
        Rect boundRect = rectMap.get(boundPos);

        Log.w(TAG, "bound rect null? = " + (boundRect == null));

        if (doDraw) {
            Imgproc.rectangle(mRgba, boundRect.tl(), boundRect.br(), contourColorWhite, 2, 8, 0);
        }

        double a = ImageProcessingUtils.calculateAlpha(boundRect);

        if (doDraw) {
             Imgproc.rectangle(mRgba, boundRect.tl(), new Point(boundRect.br().x, a), contourColor, 2, 8, 0);
        }

        MatOfPoint2f pointMat = new MatOfPoint2f();
        Imgproc.approxPolyDP(new MatOfPoint2f(contours.get(boundPos).toArray()), pointMat, 1.7, true);
        contours.set(boundPos, new MatOfPoint(pointMat.toArray()));

        MatOfInt hull = new MatOfInt();
        MatOfInt4 convexDefect = new MatOfInt4();
        Imgproc.convexHull(new MatOfPoint(contours.get(boundPos).toArray()), hull);

        // ensure that there are at least three points, since we must have a convex polygon
        if (hull.toArray().length <= 3) {
            return surfaceData;
        }

        Imgproc.convexityDefects(new MatOfPoint(contours.get(boundPos).toArray()), hull, convexDefect);

        List<Point> listPo = ImageProcessingUtils.getListOfPoints(contours, hull, boundPos);
        List<MatOfPoint> hullPoints = ImageProcessingUtils.getMatOfPointFromPoints(contours, listPo);

        List<Point> defectPointsList = ImageProcessingUtils.getDefectPoints(contours, convexDefect, boundPos, threshold, a);
        List<MatOfPoint> defectPoints = ImageProcessingUtils.getMatOfPointFromPoints(contours, defectPointsList);
        Imgproc.resize(colorBlobDetector.getSpectrum(), mSpectrum, spectrumRange);


        if (doDraw) {
            Imgproc.drawContours(mRgba, hullPoints, -1, contourColor, 3);
            List<Point> smoothedPoints = DetectorUtils.filterFingerTips(listPo, 1);
            Imgproc.circle(mRgba, DetectorUtils.getFinger(FingerEnum.THUMB, HandEnum.LEFT, listPo), 6, new Scalar(255, 200, 255));
            for (Point p : smoothedPoints) {
                Imgproc.circle(mRgba, p, 6, new Scalar(255, 200, 255));
            }
        }

        surfaceData.setContours(contours);
        surfaceData.setDefectPoints(defectPointsList);
        surfaceData.setConvexDefect(convexDefect);
        surfaceData.setAlpha(a);
        surfaceData.setHull(hull);
        surfaceData.setHullPoints(listPo);
        surfaceData.setmDefectPoints(defectPoints);
        surfaceData.setmHullPoints(hullPoints);
        surfaceData.setBoundRect(boundRect);
        surfaceData.setmRgba(mRgba);
        return surfaceData;
    }

    public void setHsvColor(Scalar hsvColor) {
        this.colorBlobDetector.setHsvColor(hsvColor);
    }

    public ColorBlobDetector getColorBlobDetector() {
        return colorBlobDetector;
    }

    public void setColorBlobDetector(ColorBlobDetector colorBlobDetector) {
        this.colorBlobDetector = colorBlobDetector;
    }
}