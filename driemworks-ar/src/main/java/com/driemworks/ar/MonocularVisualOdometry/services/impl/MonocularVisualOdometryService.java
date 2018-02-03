package com.driemworks.ar.MonocularVisualOdometry.services.impl;

import android.util.Log;

import com.driemworks.ar.dto.CameraPoseDTO;
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

    /**
     * The feature service
     */
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
     */
    public CameraPoseDTO monocularVisualOdometry(CameraPoseDTO cameraPoseDTO, Mat currentFrame,
                                                 Mat previousFrameGray, Mat currentFrameGray,
                                                 MatOfKeyPoint previousPoints) {
        float startTime = System.currentTimeMillis();
        Log.d("###","START - monocularVisualOdometry");

        MatOfKeyPoint currentPoints;
        if (previousPoints.empty()) {
            Log.d(TAG, "previous points are empty.");
            currentPoints = featureService.featureDetection(currentFrame);
        } else {
            Log.d(TAG, "previous points are not empty");
            // track feature from the previous image into the current image
            currentPoints = featureService.featureTracking(
                    previousFrameGray, currentFrameGray, previousPoints);
            if (currentPoints.empty()) {
                Log.d(TAG, "tracked points are empty");
                currentPoints = featureService.featureDetection(currentFrame);
            } else {
                // convert the lists of points to MatOfPoint2fs
                MatOfPoint2f currentPoints2f = ImageConversionUtils.convertMatOfKeyPointsTo2f(currentPoints);
                MatOfPoint2f previousPoints2f = ImageConversionUtils.convertMatOfKeyPointsTo2f(previousPoints);

                if (!currentPoints2f.empty() && currentPoints2f.checkVector(2) > 0) {
                    Mat mask = new Mat();
                    Log.d(TAG, "Calculating essential matrix.");
                    Mat essentialMat = Calib3d.findEssentialMat(currentPoints2f, previousPoints2f,
                                FOCAL, PRINCIPAL_POINT, Calib3d.LMEDS, 0.99, 1.0, mask);
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
            }
        }

        cameraPoseDTO.setKeyPoints(currentPoints);
        Log.d(TAG, "Setting current points");
        Log.d("###","END - monocularVisualOdometry - " + (System.currentTimeMillis() - startTime) + " ms");
        return cameraPoseDTO;
    }
}
