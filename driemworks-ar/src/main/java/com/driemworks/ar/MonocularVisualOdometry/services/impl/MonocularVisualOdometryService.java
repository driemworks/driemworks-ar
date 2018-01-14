package com.driemworks.ar.MonocularVisualOdometry.services.impl;

import android.util.Log;

import com.driemworks.ar.dto.OdometryDataDTO;
import com.driemworks.ar.dto.SequentialFrameFeatures;
import com.driemworks.common.utils.ImageConversionUtils;
import com.driemworks.common.utils.TagUtils;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;

/**
 * @author Tony
 */
public class MonocularVisualOdometryService {

    /**
     * The constant TAG
     */
    private final String TAG = TagUtils.getTag(this.getClass());

    /**
     * The rotation matrix (3 x 3)
     */
    private Mat rotationMatrix;

    /**
     * The translation matrix (3 x 1)
     */
    private Mat translationMatrix;

    /**
     * The FOCAL length
     */
    private static final float FOCAL = 718.8560f;

    /**
     * The principal point
     */
    private static final Point PRINCIPAL_POINT = new Point(100, 100);

    /**
     * The default constructor
     */
    public MonocularVisualOdometryService() {
        super();
        rotationMatrix = new Mat(3, 3, CvType.CV_64FC1);
        translationMatrix = new Mat(3, 1, CvType.CV_64FC1);
    }

    /**
     * This method performs feature detection and feature tracking on real time
     * camera input
     * @return output The output frame
     */
    public OdometryDataDTO monocularVisualOdometry(SequentialFrameFeatures sequentialFrameFeatures) {
        Log.d(TAG, "START - onCameraFrame");
        long startTime = System.currentTimeMillis();
        OdometryDataDTO odometryDataDTO = new OdometryDataDTO();

        // convert the lists of points to MatOfPoint2f's
        MatOfPoint2f currentPoints = ImageConversionUtils.convertListToMatOfPoint2f(
                sequentialFrameFeatures.getCurrentFrameFeaturePoints());
        MatOfPoint2f previousPoints = ImageConversionUtils.convertListToMatOfPoint2f(
                sequentialFrameFeatures.getPreviousFrameFeaturePoints());

        // check that the list of points detected in the current frames are non empty and of
        // the correct format
        if (!currentPoints.empty() && currentPoints.checkVector(2) > 0) {
            // released? √
            Mat mask = new Mat();
            // calculate the essential matrix
            long startTimeNew = System.currentTimeMillis();
            Log.d(TAG, "START - findEssentialMat - numCurrentPoints: "
                    + currentPoints.size() + " numPreviousPoints: " + previousPoints.size());
            // released? √
            Mat essentialMat = Calib3d.findEssentialMat(currentPoints, previousPoints,
                    FOCAL, PRINCIPAL_POINT, Calib3d.LMEDS, 0.99, 2.0, mask);
            Log.d(TAG, "END - findEssentialMat - time elapsed "
                    + (System.currentTimeMillis() - startTimeNew) + " ms");

            // calculate rotation and translation matrices
            if (!essentialMat.empty() && essentialMat.rows() == 3 &&
                    essentialMat.cols() == 3 && essentialMat.isContinuous()) {
                Log.d(TAG, "START - recoverPose");
                startTimeNew = System.currentTimeMillis();
                Calib3d.recoverPose(essentialMat, currentPoints,
                        previousPoints, rotationMatrix, translationMatrix, FOCAL, PRINCIPAL_POINT, mask);
                Log.d(TAG, "END - recoverPose - time elapsed " +
                        (System.currentTimeMillis() - startTimeNew) + " ms");

                Log.d(TAG, "Calculated rotation matrix: " + rotationMatrix.toString());
                Log.d(TAG, "Calculated translation matrix: " + translationMatrix.toString());
                Log.d(TAG, "z coordinate: " + translationMatrix.get(2,0)[0]);
                odometryDataDTO.setRotationMatrix(rotationMatrix);
                odometryDataDTO.setTranslationMatrix(translationMatrix);
            }
            mask.release();
            essentialMat.release();
        }

        Log.d(TAG, "END - onCameraFrame - time elapsed: " +
                (System.currentTimeMillis() - startTime) + " ms");

        return odometryDataDTO;
    }

    public Mat getRotationMatrix() {
        return rotationMatrix;
    }

    public void setRotationMatrix(Mat rotationMatrix) {
        this.rotationMatrix = rotationMatrix;
    }

    public Mat getTranslationMatrix() {
        return translationMatrix;
    }

    public void setTranslationMatrix(Mat translationMatrix) {
        this.translationMatrix = translationMatrix;
    }
}
