package com.driemworks.simplecv.activities;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.OpenCVLoader;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.driemworks.ar.MonocularVisualOdometry.services.impl.FeatureServiceImpl;
import com.driemworks.ar.dto.CameraPoseDTO;
import com.driemworks.ar.dto.FeatureWrapper;
import com.driemworks.ar.dto.SequentialFrameFeatures;
import com.driemworks.common.factories.BaseLoaderCallbackFactory;
import com.driemworks.common.utils.ImageConversionUtils;
import com.driemworks.simplecv.R;
import com.driemworks.simplecv.enums.Resolution;
import com.driemworks.simplecv.services.permission.impl.CameraPermissionServiceImpl;
import com.driemworks.common.views.CustomSurfaceView;
import com.driemworks.simplecv.utils.DisplayUtils;

/**
 * The Monocular Visual Odometry testing activity
 * @author Tony
 */
public class VOActivity extends Activity implements CvCameraViewListener2 {

    /**
     * The camera pose dto - the current pose of the camera
     */
    private CameraPoseDTO currentPose;

    /* load the opencv lib */
    static {
        System.loadLibrary("opencv_java3");
    }

    /**
     * The minimum number of tracked features per image
     */
    private static final int threshold = 20;

    /** The service to request permission to use camera at runtime */
    private CameraPermissionServiceImpl cameraPermissionService;

    /** The tag used for logging */
    private static final String TAG = "VOActivity: ";

    /** The input camera frame in RGBA format */
    private Mat mRgba;
    /**
     * The (current) gray camera frame
     */
    private Mat gray;

    /**
     * The output mat
     */
    private Mat output;

    /** The customSurfaceView surface view */
    private CustomSurfaceView customSurfaceView;
    /** The width of the device screen */
    private int screenWidth;

    /** The height of the device screen */
    private int screenHeight;

    /** The base loader callback */
    private BaseLoaderCallback mLoaderCallback;

    /** The feature service */
    private FeatureServiceImpl featureService;

    /** The previous feature wrapper */
    private FeatureWrapper previousWrapper = null;

    // TODO calculate intrinsic params of camera (camera calibration)
    // the values provided are not actual values
    /**
     * The FOCAL length
     */
    private static final float FOCAL = 718.8560f;
    /**
     * The principal point
     */
    private static final Point PRINCIPAL_POINT = new Point(100, 100);

    /** The current sequential frame features */
    private SequentialFrameFeatures sequentialFrameFeatures;

    /** The currently detected points */
    private MatOfPoint2f currentPoints;
    /** The previously detected points */
    private MatOfPoint2f previousPoints;
    /** The essential matrix */
    private Mat essentialMat;
    /** The rotation matrix */
    private Mat rotationMatrix;
    /** The translation matrix */
    private Mat translationMatrix;

    /** The default constructor */
    public VOActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);

        // get screen dimensions
        android.graphics.Point size = DisplayUtils.getScreenSize(this);
        screenWidth = size.x;
        screenHeight = size.y;

        if (!OpenCVLoader.initDebug()) {
            Log.e("OpvenCVLoader", "OvenCVLoader successful: false");
        } else {
            Log.d("OpenCVLoader", "OpenCVLoader successful");
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        cameraPermissionService = new CameraPermissionServiceImpl(this);

        setContentView(R.layout.main_surface_view);

        // init the matrices
        essentialMat = new Mat(3, 3, CvType.CV_64FC1);
        rotationMatrix = new Mat(3, 3, CvType.CV_64FC1);
        translationMatrix = new Mat(3, 1, CvType.CV_64FC1);

        currentPose = new CameraPoseDTO();

        currentPoints = new MatOfPoint2f();
        previousPoints = new MatOfPoint2f();

        // setup the surface view
        customSurfaceView = (CustomSurfaceView) findViewById(R.id.main_surface_view);
        customSurfaceView.setCvCameraViewListener(this);
        customSurfaceView.setMaxFrameSize(
                Resolution.THREE_TWENTY_BY_TWO_FORTY.getWidth(),
                Resolution.THREE_TWENTY_BY_TWO_FORTY.getHeight());

        // init base loader callback
        mLoaderCallback = BaseLoaderCallbackFactory.getBaseLoaderCallback(
                this, customSurfaceView);

        // init service(s)
        featureService = new FeatureServiceImpl();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "Called onPause");
        super.onPause();
        if (customSurfaceView != null) {
            customSurfaceView.disableView();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
        customSurfaceView.setMaxFrameSize(320, 240);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        customSurfaceView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        Log.d(TAG, "camera view started");
        // init the mats for the current image
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        gray = new Mat(height, width, CvType.CV_8UC1);
        output = new Mat(height, width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
        Log.d(TAG, "camera view stopped");
        // release all (current image) mats
        mRgba.release();
        gray.release();
        output.release();
    }

    /**
     * This method performs feature detection and feature tracking on real time
     * camera input
     * @param inputFrame The input frame
     * @return output The output frame
     */
    private Mat monocularVisualOdometry(CvCameraViewFrame inputFrame) {
        Log.d(TAG, "START - onCameraFrame");
        long startTime = System.currentTimeMillis();

        mRgba = inputFrame.rgba();
        gray = inputFrame.gray();
        output = mRgba.clone();

        // if the previous wrapper has not been initialized
        // or the wrapper is empty (no frame or no keypoints)
        if (previousWrapper == null) {
            previousWrapper = featureService.featureDetection(mRgba);
            return output;
        } else if (!previousWrapper.empty()) {
            // track feature from the previous image into the current image
            sequentialFrameFeatures = featureService.featureTracking(
                    previousWrapper.getFrameAsGrayscale(), gray, previousWrapper.getKeyPoints());

            // check if number of features tracked is less than the threshold value
            // if less than threshold, then redetect features
            if (sequentialFrameFeatures.getCurrentFrameFeaturePoints().size() < threshold) {
                previousWrapper = featureService.featureDetection(previousWrapper.getFrame());
                return output;
            }

            // draws filtered feature points on output
            if (true) {
                for (Point p : sequentialFrameFeatures.getCurrentFrameFeaturePoints()) {
                    Imgproc.circle(output, p, 2, new Scalar(255, 0, 0));
                }
            }

            // convert the lists of points to MatOfPoint2f's
            currentPoints = ImageConversionUtils.convertListToMatOfPoint2f(
                    sequentialFrameFeatures.getCurrentFrameFeaturePoints());
            previousPoints = ImageConversionUtils.convertListToMatOfPoint2f(
                    sequentialFrameFeatures.getPreviousFrameFeaturePoints());

            // check that the list of points detected in the current frames are non empty and of
            // the correct format
            if (!currentPoints.empty() && currentPoints.checkVector(2) > 0) {
                Mat mask = new Mat();
                // calculate the essential matrix
                long startTimeNew = System.currentTimeMillis();
                Log.d(TAG, "START - findEssentialMat - numCurrentPoints: "
                        + currentPoints.size() + " numPreviousPoints: " + previousPoints.size());
                // RANSAC was far too costly...using LMEDS
                essentialMat = Calib3d.findEssentialMat(currentPoints, previousPoints,
                        FOCAL, PRINCIPAL_POINT, Calib3d.LMEDS, 0.99, 1.2, mask);
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

                    if (!translationMatrix.empty() && !rotationMatrix.empty()) {
                        currentPose.update(translationMatrix, rotationMatrix);
                        Log.d(TAG, currentPose.toString());
                    } else {
                        Log.d(TAG, "translation and/or rotation matrix was empty.");
                    }
                }
                mask.release();
            }

            previousWrapper.setFrame(mRgba);
            previousWrapper.setKeyPoints(ImageConversionUtils.convertMatOf2fToKeyPoints(
                    currentPoints, 2, 2));

            Log.d(TAG, "END - onCameraFrame - time elapsed: " +
                    (System.currentTimeMillis() - startTime) + " ms");
        }
        return output;
    }

    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        // should this be multithreaded?
        // i.e. for each frame, start a thread to call the mvo method
        // then have a separate thread for the camera that simply returns the camera capture
        return monocularVisualOdometry(inputFrame);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == CameraPermissionServiceImpl.REQUEST_CODE) {
            cameraPermissionService.handleResponse(requestCode, permissions, grantResults);
        }
    }
}
