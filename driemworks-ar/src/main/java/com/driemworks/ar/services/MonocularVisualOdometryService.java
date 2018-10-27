package com.driemworks.ar.services;

import android.util.Log;
import android.util.Pair;

import com.driemworks.ar.dto.CameraPoseDTO;
import com.driemworks.ar.dto.FeatureData;
import com.driemworks.ar.services.impl.FeatureDetectorServiceImpl;
import com.driemworks.common.utils.ImageConversionUtils;
import com.driemworks.common.utils.TagUtils;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.CvType;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * The MonocularVisualOdometryService
 * @author Tony
 */
public class MonocularVisualOdometryService {

    /**
     * The constant TAG
     */
    private final String TAG = TagUtils.getTag(this.getClass());

    // TODO move to properties file
    // actually calculate these values instead of guessing them...
    /**
     * The FOCAL length
     */
    private static final float FOCAL = 847.22f;

    /**
     * The principal point
     */
    private static final Point PRINCIPAL_POINT = new Point(300, 200);

    /**
     * The feature service
     */
    private FeatureDetectorServiceImpl featureTrackingService;

    /**
     * The default constructor
     */
    public MonocularVisualOdometryService() {
        featureTrackingService = new FeatureDetectorServiceImpl();
    }

    /**
     ** This method performs feature detection and feature tracking on real time
     * camera input
     * @param cameraPoseDTO The cameraPoseDTO
     * @param currentFrame The current frame
     * @param previousFrameGray The previous frame in grayscale
     * @param currentFrameGray The current frame in grayscale
     * @return the updated camera pose
     */
    public CameraPoseDTO calculateCameraPose(CameraPoseDTO cameraPoseDTO, Mat currentFrame,
                                             Mat previousFrameGray, Mat currentFrameGray) {
        Log.d(TAG,"START - monocularVisualOdometry");
        long start = System.currentTimeMillis();
        FeatureData currentData;

        cameraPoseDTO.reset();

        if (previousFrameGray == null
                || cameraPoseDTO.getFeatureData() == null
                || (cameraPoseDTO.getFeatureData().getKeyPoint() != null && cameraPoseDTO.getFeatureData().getKeyPoint().toArray().length < 5)) {
            Log.d(TAG, "previous points are empty.");
//            currentData = featureTrackingService.extractFeatureData(currentFrame);
        } else {
            Log.d(TAG, "previous points are not empty");
            // track feature from the previous image into the current image
            MatOfKeyPoint previousPoints = cameraPoseDTO.getFeatureData().getKeyPoint();
            Pair<MatOfKeyPoint, MatOfKeyPoint> matchedTrackedPoints = featureTrackingService.trackKeyPoints(previousFrameGray, currentFrameGray, previousPoints);
            if (matchedTrackedPoints.second.empty()) {
                Log.d(TAG, "No keypoints were tracked to new frame. Redetecting points.");
//                currentData = featureTrackingService.extractFeatureData(currentFrame);
            } else {
                // convert the lists of points to MatOfPoint2fs ... why?
                MatOfPoint2f currentPoints2f = ImageConversionUtils.convertMatOfKeyPointsTo2f(matchedTrackedPoints.second);
                MatOfPoint2f previousPoints2f = ImageConversionUtils.convertMatOfKeyPointsTo2f(matchedTrackedPoints.first);

                calculateRotationAndTranslation(currentPoints2f, previousPoints2f, cameraPoseDTO);
                currentData = new FeatureData(matchedTrackedPoints.second, null);
            }
        }

//        cameraPoseDTO.setFeatureData(currentData);
        Log.d(TAG,"END - monocularVisualOdometry in " + (System.currentTimeMillis() - start) + " ms");
        return cameraPoseDTO;
    }

    private void calculateRotationAndTranslation(MatOfPoint2f currentPoints2f, MatOfPoint2f previousPoints2f, CameraPoseDTO cameraPoseDTO) {
        Mat rotationMatrix = new Mat(3, 3, CvType.CV_64FC1);
        Mat translationMatrix = new Mat(3, 1, CvType.CV_64FC1);
        if (!currentPoints2f.empty() && currentPoints2f.checkVector(2) > 0) {
            Mat mask = new Mat();
            Log.d(TAG, "Calculating essential matrix.");
            Mat essentialMat = null;
            try {
                essentialMat = Calib3d.findEssentialMat(currentPoints2f, previousPoints2f,
                        FOCAL, PRINCIPAL_POINT, Calib3d.RANSAC, 0.99, 2.5, mask);
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
            } catch (Exception e) {
                Log.e(TAG, "An exception occured while calculating the new camera pose.", e);
            }

            mask.release();
            if (essentialMat != null) {
                essentialMat.release();
                rotationMatrix.release();
                translationMatrix.release();
            }
        } else {
            Log.d(TAG, "previous points or current points empty or checkVector(2) <= 0");
        }
    }

    /**
     * Sorts the keypoints in descending order, so the best response KPs will come first,
     * then chooses the sublist of the sorted keypoints from 0 to numKeyPoints
     * @param notBestKeyPoints The list of keypoints
     * @param numKeyPoints The number of keypoints desired as outcome
     * @return The best numKeyPoints points
     */
    private MatOfKeyPoint getBestPoints(List<KeyPoint> notBestKeyPoints, int numKeyPoints) {
        Collections.sort(notBestKeyPoints, new Comparator<KeyPoint>() {
            @Override
            public int compare(KeyPoint kp1, KeyPoint kp2) {
                return (int) (kp2.response - kp1.response);
            }
        });

        MatOfKeyPoint bestPoints = new MatOfKeyPoint();
        List<KeyPoint> bestKeyPoints;
        if (notBestKeyPoints.size() < numKeyPoints) {
            bestKeyPoints = notBestKeyPoints.subList(0, notBestKeyPoints.size());
        } else {
            bestKeyPoints = notBestKeyPoints.subList(0, numKeyPoints);
        }
        bestPoints.fromList(bestKeyPoints);
        return bestPoints;
    }

}
