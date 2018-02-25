package com.driemworks.ar.services.impl;

import android.util.Log;

import com.driemworks.ar.dto.CameraPoseDTO;
import com.driemworks.common.enums.Resolution;
import com.driemworks.common.utils.ImageConversionUtils;
import com.driemworks.common.utils.OpenCvUtils;
import com.driemworks.common.utils.TagUtils;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.CvType;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
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
     * The Resolution
     */
    private final Resolution resolution;

    /**
     * The feature service
     */
    private FeatureServiceImpl featureService;

    /**
     * The touched rectangle
     */
    private Rect touchedRect;

    /**
     * the width of the rectangle
     */
    private int rectWidth;

    /**
     * The height of the rectangle
     */
    private int rectHeight;

    /**
     * The default constructor
     */
    public MonocularVisualOdometryService(Resolution resolution) {
        super();
        rotationMatrix = new Mat(3, 3, CvType.CV_64FC1);
        translationMatrix = new Mat(3, 1, CvType.CV_64FC1);
        featureService = new FeatureServiceImpl();
        touchedRect = null;
        this.resolution = resolution;
    }

    /**
     ** This method performs feature detection and feature tracking on real time
     * camera input
     *
     * @param cameraPoseDTO The cameraPoseDTO
     * @param currentFrame The current frame
     * @param previousFrameGray The previous frame in grayscale
     * @param currentFrameGray The current frame in grayscale
     * @return the updated camera pose
     */
    public CameraPoseDTO monocularVisualOdometry(CameraPoseDTO cameraPoseDTO, Mat currentFrame,
                                                 Mat previousFrameGray, Mat currentFrameGray) {
        Log.d(TAG,"START - monocularVisualOdometry");
        MatOfKeyPoint currentPoints;
        MatOfKeyPoint previousPoints = cameraPoseDTO.getKeyPoints();

        // detect points within the touched region, then correct the points
        if (touchedRect != null && currentFrame != null
                && (null == previousPoints || previousPoints.empty())) {
            Log.d(TAG, "previous points are empty.");
            Mat sharpenedCurrentFrame = OpenCvUtils.sharpenImage(currentFrame);
            // detect current points in the selected sub region
            currentPoints = correctKeyPoints(
                     featureService.featureDetection(sharpenedCurrentFrame.submat(touchedRect)));
        } else {
            Log.d(TAG, "previous points are not empty");
            // track feature from the previous image into the current image
//            Mat sharpenedPreviousFrameGray = OpenCvUtils.sharpenImage(previousFrameGray);
//            Mat sharpenedCurrentFrameGray = OpenCvUtils.sharpenImage(currentFrame);
//            currentPoints = featureService.featureTracking(
//                    sharpenedPreviousFrameGray, sharpenedCurrentFrameGray, previousPoints);
            currentPoints = featureService.featureTracking(
                    previousFrameGray, currentFrameGray, previousPoints);
            // convert the lists of points to MatOfPoint2fs
            MatOfPoint2f currentPoints2f = ImageConversionUtils.convertMatOfKeyPointsTo2f(currentPoints);
            MatOfPoint2f previousPoints2f = ImageConversionUtils.convertMatOfKeyPointsTo2f(previousPoints);

            // try to recover the pose from the essential matrix
            if (!currentPoints2f.empty() && currentPoints2f.checkVector(2) > 0) {
                Log.d(TAG, "Calculating essential matrix.");
                Mat essentialMat;
                try {
                    Mat mask = new Mat();
                    // calculate the essential matrix and recover the camera pose
                    Log.d(TAG, "START - findEssentialMat");
                    essentialMat = Calib3d.findEssentialMat(currentPoints2f, previousPoints2f,
                            FOCAL, PRINCIPAL_POINT, Calib3d.LMEDS, 0.99, 3.00, mask);
                    Log.d(TAG, "END - findEssentialMat");
                    Log.d(TAG, "essential matrix is empty? = " + essentialMat.empty());
                    if (!essentialMat.empty() && essentialMat.rows() == 3 && essentialMat.cols() == 3 && essentialMat.isContinuous()) {
                        Log.d(TAG, "Calculating rotation and translation");
                        Log.d(TAG, "START - recoverPose");
                        Calib3d.recoverPose(essentialMat, currentPoints2f,
                                previousPoints2f, rotationMatrix, translationMatrix, FOCAL, PRINCIPAL_POINT, mask);
                        Log.d(TAG, "END - recoverPose");
                        if (!translationMatrix.empty() && !rotationMatrix.empty()) {
                            cameraPoseDTO.update(translationMatrix, rotationMatrix);
                            Log.d(TAG, "updated camera pose: " + cameraPoseDTO.toString());
                        }
                    }
                    mask.release();
                    essentialMat.release();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    // estimate minimum bounding box around newly detected key points
                    // and redetect keypoints within this box - to be used for tracking into next image
                    Rect boundRect = Imgproc.minAreaRect(
                            ImageConversionUtils.convertMatOfKeyPointsTo2f(currentPoints)).boundingRect();
                    setTouchedRect(new Rect(boundRect.x, boundRect.y, rectWidth, rectHeight));
                    if (Resolution.isInResolution(touchedRect.x, touchedRect.y, resolution)) {
                        Mat sharpenedCurrentFrame = OpenCvUtils.sharpenImage(currentFrame);
                        currentPoints = correctKeyPoints(
                                featureService.featureDetection(sharpenedCurrentFrame.submat(touchedRect)));
                    }
                }
            }
        }

        cameraPoseDTO.setKeyPoints(currentPoints);
        Log.d(TAG,"END - monocularVisualOdometry");
        return cameraPoseDTO;
    }

    /**
     * Correct location of the keypoints based on the size of the touched rect
     * @param detectedKeyPoints The key points to be corrected
     * @return The corrected keypoints
     */
    private MatOfKeyPoint correctKeyPoints(MatOfKeyPoint detectedKeyPoints) {
        List<KeyPoint> keyPoints = new ArrayList<>();
        Point point;
        for (KeyPoint kp : detectedKeyPoints.toList()) {
            point = kp.pt;
            keyPoints.add(new KeyPoint((float)(point.x + touchedRect.x), (float)(point.y + touchedRect.y), 5));
        }
        MatOfKeyPoint correctedMatOfKeyPoints = new MatOfKeyPoint();
        correctedMatOfKeyPoints.fromList(keyPoints);
        return correctedMatOfKeyPoints;
    }

    /**
     * Getter for the touched rectangle
     * @return touchedRect The touched rectangle
     */
    public Rect getTouchedRect() {
        return touchedRect;
    }

    /**
     * Setter for the touched rectangle
     * @param touchedRect The touched rectangle to set
     */
    public void setTouchedRect(Rect touchedRect) {
        this.touchedRect = touchedRect;
        if (touchedRect != null) {
            this.rectWidth = touchedRect.width;
            this.rectHeight = touchedRect.height;
        }
    }
}
