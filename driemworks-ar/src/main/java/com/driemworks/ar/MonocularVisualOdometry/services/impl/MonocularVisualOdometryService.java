package com.driemworks.ar.MonocularVisualOdometry.services.impl;

import android.graphics.Camera;
import android.util.Log;

import com.driemworks.ar.MonocularVisualOdometry.services.FeatureService;
import com.driemworks.ar.dto.CameraPoseDTO;
import com.driemworks.ar.dto.OdometryDataDTO;
import com.driemworks.ar.dto.SequentialFrameFeatures;
import com.driemworks.common.cs.Constants;
import com.driemworks.common.utils.ImageConversionUtils;
import com.driemworks.common.utils.TagUtils;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
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

    private FeatureServiceImpl featureService;

    /**
     * The default constructor
     */
    public MonocularVisualOdometryService() {
        super();
        rotationMatrix = new Mat(3, 3, CvType.CV_64FC1);
        translationMatrix = new Mat(3, 1, CvType.CV_64FC1);
        featureService = new FeatureServiceImpl();
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

    /**
     * This method performs feature detection and feature tracking on real time
     * camera input
     */
    public CameraPoseDTO monocularVisualOdometry(Mat currentFrame,
                                                  Mat previousFrameGray, Mat currentFrameGray,
                                                  MatOfKeyPoint previousPoints) {
        float startTime = System.currentTimeMillis();
        Log.d("###","START - monocularVisualOdometry");

        CameraPoseDTO cameraPoseDTO = new CameraPoseDTO();

        MatOfKeyPoint currentPoints;
        if (previousPoints.empty()) {
            Log.d(TAG, "previous points are empty.");
            currentPoints = featureService.featureDetectionOnlyKeypoints(currentFrame);
        } else {
            Log.d(TAG, "previous points are not empty");
            // track feature from the previous image into the current image
            currentPoints = featureService.featureTrackingOnlyKeypoints(
                    previousFrameGray, currentFrameGray, previousPoints);
            if (currentPoints.empty()) {
                Log.d(TAG, "tracked points are empty");
                currentPoints = featureService.featureDetectionOnlyKeypoints(currentFrame);
            } else {
                // convert the lists of points to MatOfPoint2f's
                MatOfPoint2f currentPoints2f = ImageConversionUtils.convertMatOfKeyPointsTo2f(currentPoints);
                MatOfPoint2f previousPoints2f = ImageConversionUtils.convertMatOfKeyPointsTo2f(previousPoints);

                if (!currentPoints2f.empty() && currentPoints2f.checkVector(2) > 0) {
                    Mat mask = new Mat();
                    Log.d(TAG, "Calculating essential matrix.");
                    Mat essentialMat = null;
                    try {
                        essentialMat = Calib3d.findEssentialMat(currentPoints2f, previousPoints2f,
                                FOCAL, PRINCIPAL_POINT, Calib3d.LMEDS, 0.99, 1.0, mask);
                    } catch (Exception e) {
                        Log.e(TAG, "oh no");
                        e.printStackTrace();
                    }
                    Log.d(TAG, "essential matrix is empty? = " + essentialMat.empty());
                    if (!essentialMat.empty() && essentialMat.rows() == 3 && essentialMat.cols() == 3 && essentialMat.isContinuous()) {
                        Log.d(TAG, "Calculating rotation and translation");
                        Calib3d.recoverPose(essentialMat, currentPoints2f,
                                previousPoints2f, rotationMatrix, translationMatrix, FOCAL, PRINCIPAL_POINT, mask);
                        if (!translationMatrix.empty() && !rotationMatrix.empty()) {
                            cameraPoseDTO.update(translationMatrix, rotationMatrix);
                            Log.d(TAG, "updated camera pose: " + cameraPoseDTO.toString());
                        }
                    }

                    mask.release();
                    essentialMat.release();
                }
                currentPoints2f.release();
                previousPoints2f.release();
            }
        }

        cameraPoseDTO.setKeyPoints(currentPoints);
        Log.d(TAG, "Setting current points");
        Log.d("###","END - monocularVisualOdometry - " + (System.currentTimeMillis() - startTime) + " ms");
        return cameraPoseDTO;
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
