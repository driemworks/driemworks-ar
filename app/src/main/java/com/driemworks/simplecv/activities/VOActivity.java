package com.driemworks.simplecv.activities;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.OpenCVLoader;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.driemworks.ar.MonocularVisualOdometry.services.impl.FeatureServiceImpl;
import com.driemworks.ar.dto.CameraPoseDTO;
import com.driemworks.ar.dto.FeatureWrapper;
import com.driemworks.ar.dto.SequentialFrameFeatures;
import com.driemworks.ar.utils.CvUtils;
import com.driemworks.common.factories.BaseLoaderCallbackFactory;
import com.driemworks.common.utils.ImageConversionUtils;
import com.driemworks.simplecv.R;
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

    /** The service to request permission to use camera at runtime */
    private CameraPermissionServiceImpl cameraPermissionService;

    /** The tag used for logging */
    private static final String TAG = "VOActivity: ";

    /** The input camera frame in RGBA format */
    private Mat mRgba;
    private Mat gray;
    private Mat intermediateMat;
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

    /** The current feature wrapper */
    private FeatureWrapper wrapper = null;

    // TODO calculate intrinsic params of camera (camera calibration)
    // the values provided are not actual values
    /**
     * The focal length
     */
    private static final float focal = 718.8560f;
    /**
     * The principal point
     */
    private static final Point pp = new Point(100, 100);

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
    /** The mask matrix */
    private Mat mask;

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
        customSurfaceView.setMaxFrameSize(320, 240);

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
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        gray = new Mat(height, width, CvType.CV_8UC1);
        intermediateMat = new Mat(height, width, CvType.CV_8UC3);
        output = new Mat(height, width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
        Log.d(TAG, "camera view stopped");
        mRgba.release();
        gray.release();
        intermediateMat.release();
        output.release();
    }

    /**
     * The minimum number of tracked features per image
     */
    private static final int threshold = 20;
    private int retry;

    /**
     *
     * @param inputFrame
     * @return
     */
    private Mat monocularVisualOdometry(CvCameraViewFrame inputFrame) {
        Log.d(TAG, "START - onCameraFrame");
        long startTime = System.currentTimeMillis();
        mRgba = inputFrame.rgba();
        gray = inputFrame.gray();
        output = mRgba.clone();

        if (previousWrapper == null) {
            previousWrapper = featureService.featureDetection(mRgba);
            return output;
        } else if (previousWrapper != null && previousWrapper.getFrame() != null && ! previousWrapper.getKeyPoints().empty()) {

            // track feature from the previous image into the current image
            sequentialFrameFeatures = featureService.featureTracking(
                    previousWrapper.getFrameAsGrayscale(), gray, previousWrapper.getKeyPoints());

            // now check if number of features tracked is less than the threshold value
            if (sequentialFrameFeatures.getCurrentFrameFeaturePoints().size() < threshold) {
                // just redetect - not redetection AND tracking
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


            if (!currentPoints.empty() && currentPoints.checkVector(2) > 0) {
                mask = new Mat();
                // calculate the essential matrix
                long startTimeNew = System.currentTimeMillis();
                Log.d(TAG, "START - findEssentialMat - numCurrentPoints: "
                        + currentPoints.size() + " numPreviousPoints: " + previousPoints.size());

                // RANSAC was far too costly...using LMEDS
                essentialMat = Calib3d.findEssentialMat(currentPoints, previousPoints,
                        focal, pp, Calib3d.LMEDS, 0.99, 1, mask);
                Log.d(TAG, "END - findEssentialMat - time elapsed "
                        + (System.currentTimeMillis() - startTimeNew) + " ms");

                // calculate rotation and translation matrices
                if (!essentialMat.empty() && essentialMat.rows() == 3 &&
                        essentialMat.cols() == 3 && essentialMat.isContinuous()) {
                    Log.d(TAG, "START - recoverPose");
                    startTimeNew = System.currentTimeMillis();
                    Calib3d.recoverPose(essentialMat, currentPoints,
                            previousPoints, rotationMatrix, translationMatrix, focal, pp, mask);
                    Log.d(TAG, "END - recoverPose - time elapsed " +
                            (System.currentTimeMillis() - startTimeNew) + " ms");
                    Log.d(TAG, "Calculated rotation matrix: " + rotationMatrix.toString());

                    if (!translationMatrix.empty() && !rotationMatrix.empty()) {
                        currentPose.update(translationMatrix, rotationMatrix);
                        Log.d(TAG, currentPose.toString());
                        Log.d(TAG, "#= translationMatrix " + CvUtils.printMat(translationMatrix));
                        Log.d(TAG, "#= rotationMatrix " + CvUtils.printMat(rotationMatrix));
                    } else {
                        Log.d(TAG, "translation and/or rotation matrix was empty.");
                    }
                }
                mask.release();
            }

            previousWrapper.setFrame(mRgba);
            previousWrapper.setKeyPoints(ImageConversionUtils.convertMatOf2fToKeyPoints(currentPoints, 2, 2));

            Log.d(TAG, "END - onCameraFrame - time elapsed: " +
                    (System.currentTimeMillis() - startTime) + " ms");
        }
        return output;
    }

    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        if (true) {
            return monocularVisualOdometry(inputFrame);
        } else {
            Log.d(TAG, "START - onCameraFrame");
            long startTime = System.currentTimeMillis();
            // get the image from the input frame
            mRgba = inputFrame.rgba();
            gray = inputFrame.gray();
            output = mRgba.clone();

            Imgproc.cvtColor(mRgba, intermediateMat, Imgproc.COLOR_RGBA2RGB);

            // this needs to be moved...
            // we only want to redetect features if the number of tracked features has fallen below a
            // specific threshold
            wrapper = featureService.featureDetection(mRgba);

            // track into next image
            if (previousWrapper != null && previousWrapper.getFrame() != null
                    && !previousWrapper.getKeyPoints().empty()) {
                // I think these can all be initialized in the onCreate function, as well as be local to the class
                mask = new Mat();

                // track features into next image, filters out bad points
                sequentialFrameFeatures = featureService.featureTracking(
                        previousWrapper.getFrameAsGrayscale(), gray,
                        previousWrapper.getKeyPoints());

                // draws filtered feature points on output
                if (true) {
                    for (Point p : sequentialFrameFeatures.getCurrentFrameFeaturePoints()) {
                        Imgproc.circle(output, p, 2, new Scalar(255, 0, 0));
                    }
                }

                // convert the points
                currentPoints = ImageConversionUtils.convertListToMatOfPoint2f(
                        sequentialFrameFeatures.getCurrentFrameFeaturePoints());
                previousPoints = ImageConversionUtils.convertListToMatOfPoint2f(
                        sequentialFrameFeatures.getPreviousFrameFeaturePoints());


                // calculate the essential matrix
                long startTimeNew = System.currentTimeMillis();
                Log.d(TAG, "START - findEssentialMat - numCurrentPoints: "
                        + currentPoints.size() + " numPreviousPoints: " + previousPoints.size());

                if (!currentPoints.empty() && currentPoints.checkVector(2) > 0) {
                    // RANSAC was far too costly...using LMEDS
                    essentialMat = Calib3d.findEssentialMat(currentPoints, previousPoints,
                            focal, pp, Calib3d.LMEDS, 0.99, 1, mask);
                    Log.d(TAG, "END - findEssentialMat - time elapsed "
                            + (System.currentTimeMillis() - startTimeNew) + " ms");

                    // calculate rotation and translation matrices
                    if (!essentialMat.empty() && essentialMat.rows() == 3 &&
                            essentialMat.cols() == 3 && essentialMat.isContinuous()) {
                        Log.d(TAG, "START - recoverPose");
                        startTimeNew = System.currentTimeMillis();
                        Calib3d.recoverPose(essentialMat, currentPoints,
                                previousPoints, rotationMatrix, translationMatrix, focal, pp, mask);
                        Log.d(TAG, "END - recoverPose - time elapsed " +
                                (System.currentTimeMillis() - startTimeNew) + " ms");
                        Log.d(TAG, "Calculated rotation matrix: " + rotationMatrix.toString());

                        if (!translationMatrix.empty() && !rotationMatrix.empty()) {
                            currentPose.update(translationMatrix, rotationMatrix);
                            Log.d(TAG, currentPose.toString());
                            Log.d(TAG, "#= translationMatrix " + CvUtils.printMat(translationMatrix));
                            Log.d(TAG, "#= rotationMatrix " + CvUtils.printMat(rotationMatrix));
                        } else {
                            Log.d(TAG, "translation and/or rotation matrix was empty.");
                        }

                    }

                    mask.release();
                }
            }

            if (!wrapper.getFrame().empty()) {
                Log.d(TAG, "Cloning feature wrapper");
                previousWrapper = FeatureWrapper.clone(wrapper);
            }

            Log.d(TAG, "END - onCameraFrame - time elapsed: " +
                    (System.currentTimeMillis() - startTime) + " ms");
            return output;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == CameraPermissionServiceImpl.REQUEST_CODE) {
            cameraPermissionService.handleResponse(requestCode, permissions, grantResults);
        }
    }
}
