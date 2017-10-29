package com.driemworks.ar.services;

import android.icu.text.DateFormat;
import android.util.Log;
import android.view.Surface;

import com.driemworks.ar.enums.FingerEnum;
import com.driemworks.ar.enums.HandEnum;
import com.driemworks.ar.enums.Tags;
import com.driemworks.ar.imageProcessing.ColorBlobDetector;
import com.driemworks.ar.utils.DetectorUtils;
import com.driemworks.ar.utils.ImageProcessingUtils;
import com.driemworks.common.dto.ConfigurationDTO;
import com.driemworks.common.dto.SurfaceDataDTO;

import org.opencv.core.Core;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class SurfaceDetectionService {

    /**
     * The tag used for logging
     */
    private static final String TAG = Tags.SurfaceDetectionService.getTag();

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
     * Constructor for the SurfaceDetectionService
     *
     * @param contourColorWhite
     * @param contourColor
     * @param mSpectrum
     * @param spectrumSize
     * @param configurationDTO
     */
    public SurfaceDetectionService(Scalar contourColorWhite, Scalar contourColor, Mat mSpectrum,
                                   Size spectrumSize, ConfigurationDTO configurationDTO) {
        this.contourColorWhite = contourColorWhite;
        this.contourColor = contourColor;
        this.mSpectrum = mSpectrum;
        this.spectrumRange = spectrumSize;

        colorBlobDetector = new ColorBlobDetector();
        if (configurationDTO != null) {
            colorBlobDetector.setHsvColor(configurationDTO.getColor());
        }
    }

    /**
     * @param mRgba
     * @param threshold
     * @param doDraw
     * @return
     */
    public SurfaceDataDTO detect(Mat mRgba, double threshold, boolean doDraw) {
        SurfaceDataDTO surfaceData = new SurfaceDataDTO();
        surfaceData.setmRgba(mRgba);
        detectSurface(mRgba, threshold, colorBlobDetector, surfaceData, doDraw);
        return surfaceData;
    }

    /**
     * @param mRgba
     * @param threshold
     * @param colorBlobDetector
     * @param doDraw
     * @return
     */
    private boolean detectSurface(Mat mRgba, double threshold, ColorBlobDetector colorBlobDetector, SurfaceDataDTO surfaceData, boolean doDraw) {
        // get contours and process the image
        colorBlobDetector.process(mRgba);
        List<MatOfPoint> contours = colorBlobDetector.getContours();

        Log.d(TAG, "Contours count: " + contours.size());

        if (contours.isEmpty()) {
            return false;
        }

        RotatedRect rect = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(0).toArray()));

        Map<Integer, Rect> rectMap = ImageProcessingUtils.getBoundedRect(rect, contours);
        int boundPos = (int) rectMap.keySet().toArray()[0];
        Rect boundRect = rectMap.get(boundPos);

        Log.w(TAG, "bound rect null? = " + (boundRect == null));

        // draws the rectangle
        if (doDraw) {
            Imgproc.rectangle(mRgba, boundRect.tl(), boundRect.br(), contourColorWhite, 2, 8, 0);
        }

        double a = ImageProcessingUtils.calculateAlpha(boundRect);

        Log.d(TAG, " Row start [" + (int) boundRect.tl().y + "] row end [" + (int) boundRect.br().y + "] Col start ["
                + (int) boundRect.tl().x + "] Col end [" + (int) boundRect.br().x + "]");
        Log.d(TAG, " A [" + a + "] br y - tl y = [" + (boundRect.br().y - boundRect.tl().y) + "]");

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
            return false;
        }

        Imgproc.convexityDefects(new MatOfPoint(contours.get(boundPos).toArray()), hull, convexDefect);

        List<Point> listPo = ImageProcessingUtils.getListOfPoints(contours, hull, boundPos);
        List<MatOfPoint> hullPoints = ImageProcessingUtils.getMatOfPointFromPoints(contours, listPo);

        List<Point> defectPointsList = ImageProcessingUtils.getDefectPoints(contours, convexDefect, boundPos, threshold, a);
        List<MatOfPoint> defectPoints = ImageProcessingUtils.getMatOfPointFromPoints(contours, defectPointsList);

        Imgproc.resize(colorBlobDetector.getSpectrum(), mSpectrum, spectrumRange);

        Log.d(TAG, "hull: " + hull.toList());
        Log.d(TAG, "defects: " + convexDefect.toList());
        int defectsTotal = (int) convexDefect.total();
        Log.d(TAG, "Defect total " + defectsTotal);

        surfaceData.setContours(contours);
        surfaceData.setDefectPoints(defectPointsList);
        surfaceData.setConvexDefect(convexDefect);
        surfaceData.setAlpha(a);
        surfaceData.setHull(hull);
        surfaceData.setHullPoints(listPo);
        surfaceData.setmDefectPoints(defectPoints);
        surfaceData.setmHullPoints(hullPoints);
        surfaceData.setBoundRect(boundRect);

        if (doDraw) {
            Imgproc.drawContours(mRgba, hullPoints, -1, contourColor, 3);
            List<Point> smoothedPoints = DetectorUtils.filterFingerTips(listPo, 1);
            Imgproc.circle(mRgba, DetectorUtils.getFinger(FingerEnum.THUMB, HandEnum.LEFT, listPo), 6, new Scalar(255, 200, 255));
            for (Point p : smoothedPoints) {
                Imgproc.circle(mRgba, p, 6, new Scalar(255, 200, 255));
            }
        }

        surfaceData.setmRgba(mRgba);
        return true;
    }

    public ColorBlobDetector getColorBlobDetector() {
        return colorBlobDetector;
    }

    public void setColorBlobDetector(ColorBlobDetector colorBlobDetector) {
        this.colorBlobDetector = colorBlobDetector;
    }
}