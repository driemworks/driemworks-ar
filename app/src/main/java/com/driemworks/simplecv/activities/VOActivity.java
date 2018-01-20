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
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.driemworks.ar.MonocularVisualOdometry.services.impl.FeatureServiceImpl;
import com.driemworks.ar.MonocularVisualOdometry.services.impl.MonocularVisualOdometryService;
import com.driemworks.ar.dto.CameraPoseDTO;
import com.driemworks.ar.dto.FeatureWrapper;
import com.driemworks.ar.dto.OdometryDataDTO;
import com.driemworks.ar.dto.SequentialFrameFeatures;
import com.driemworks.common.factories.BaseLoaderCallbackFactory;
import com.driemworks.common.utils.ImageConversionUtils;
import com.driemworks.common.utils.TagUtils;
import com.driemworks.sensor.services.OrientationService;
import com.driemworks.simplecv.R;
import com.driemworks.common.enums.Resolution;
import com.driemworks.simplecv.graphics.rendering.GraphicsRenderer;
import com.driemworks.simplecv.graphics.rendering.StaticCubeRenderer;
import com.driemworks.simplecv.services.PropertyReader;
import com.driemworks.simplecv.services.permission.impl.CameraPermissionServiceImpl;
import com.driemworks.common.views.CustomSurfaceView;
import com.driemworks.common.utils.DisplayUtils;

import java.io.IOException;
import java.util.Properties;

/**
 * The Monocular Visual Odometry testing activity
 * @author Tony
 */
public class VOActivity extends Activity implements
        CvCameraViewListener2, View.OnTouchListener, SensorEventListener {

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
    private static final int THRESHOLD = 50;

    /** The service to request permission to use camera at runtime */
    private CameraPermissionServiceImpl cameraPermissionService;

    /** The tag used for logging */
    private final String TAG = TagUtils.getTag(this.getClass());

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
    private static final float FOCAL = 900.00f;
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
    /** The device's trajectory */
    private Mat trajectory;
    /** The is touch flag */
    private boolean isTouch;
    /** The multiplier for mapping trajectory */
    private static final float MULTIPLIER = 18f;

    /** The runnable used to monocularVisualOdometryService the monocular visual odometry */
    private MonocularVisualOdometryService monocularVisualOdometryService;

    /** The runnable used for running the mvo service */
    private Runnable run;

    private boolean isRunning = false;

    /**
     * The static cube renderer
     */
    private StaticCubeRenderer staticCubeRenderer;

    /**
     * The gl surface view
     */
    private GLSurfaceView glSurfaceView;

    private float[] rotationVector = new float[3];

    /**
     * The orientation service
     */
    private OrientationService orientationService;

    /**
     * The sensor manager
     */
    private SensorManager sensorManager;

    /**
     * The is rotation enabled flag
     */
    private boolean isRotationEnabled;

    /**
     * Create the runnable to run the monocular visual odometry
     * @param drawKeypoints
     * @return
     */
    private Runnable createRunnable(final boolean drawKeypoints) {
        return new Runnable() {
            @Override
            public void run() {
                monocularVisualOdometry(drawKeypoints);
            }
        };
    }

    /** The default constructor */
    public VOActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /**
     * {@inheritDoc}
     */
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
        setContentView(R.layout.vo_layout);

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
                Resolution.RES_STANDARD.getWidth(),
                Resolution.RES_STANDARD.getHeight());

        // init base loader callback
        mLoaderCallback = BaseLoaderCallbackFactory.getBaseLoaderCallback(
                this, customSurfaceView);

        // init services
        cameraPermissionService = new CameraPermissionServiceImpl(this);
        featureService = new FeatureServiceImpl();
        monocularVisualOdometryService = new MonocularVisualOdometryService();
        run = createRunnable(false);

        try {
            Properties props = PropertyReader.readProperties(this.getApplicationContext(), "driemworks.properties");
            isRotationEnabled = Boolean.valueOf(props.getProperty("feature.rotation.enabled"));
            Log.d(TAG, "isRotationEnabled ? " + isRotationEnabled);
        } catch (IOException e) {
            e.printStackTrace();
        }

        staticCubeRenderer = new StaticCubeRenderer(Resolution.RES_STANDARD, isRotationEnabled);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        orientationService = new OrientationService(sensorManager);

        glSurfaceView = (GLSurfaceView) findViewById(R.id.gl_surface_view);
        glSurfaceView.setEGLConfigChooser(
                8, 8, 8, 8, 16, 0);
        glSurfaceView.setOnTouchListener(this);
        glSurfaceView.setRenderer(staticCubeRenderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        glSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause() {
        Log.d(TAG, "Called onPause");
        super.onPause();
        glSurfaceView.onPause();
        if (customSurfaceView != null) {
            customSurfaceView.disableView();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();
        glSurfaceView.onResume();
        orientationService.registerListeners(this);
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
        customSurfaceView.setMaxFrameSize(320, 240);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        customSurfaceView.disableView();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCameraViewStarted(int width, int height) {
        Log.d(TAG, "camera view started");
        // init the mats for the current image
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        gray = new Mat(height, width, CvType.CV_8UC1);
        output = new Mat(height, width, CvType.CV_8UC4);
    }

    /**
     * {@inheritDoc}
     */
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
     * @return output The output frame
     */
    private void monocularVisualOdometry(boolean drawKeypoints) {
        Log.d(TAG, "START - onCameraFrame");
        long startTime = System.currentTimeMillis();

        // if the previous wrapper has not been initialized
        // or the wrapper is empty (no frame or no keypoints)
        if (previousWrapper == null || previousWrapper.empty()) {
            Log.d(TAG, "previous wrapper is null!");
            // reset the trajectory
            trajectory = new Mat (Resolution.THREE_TWENTY_BY_TWO_FORTY.getHeight(),
                    Resolution.THREE_TWENTY_BY_TWO_FORTY.getWidth(), CvType.CV_8UC3,
                    new Scalar(0, 0, 0));
            // reset the camera pose
            currentPose.reset();
            staticCubeRenderer.setZ(0);
            previousWrapper = featureService.featureDetection(mRgba);
        } else {
            // track feature from the previous image into the current image
            sequentialFrameFeatures = featureService.featureTracking(
                    previousWrapper.getFrameAsGrayscale(), gray, previousWrapper.getKeyPoints());
            // check if number of features tracked is less than the THRESHOLD value
            // if less than THRESHOLD, then redetect features
            if (sequentialFrameFeatures.getCurrentFrameFeaturePoints().size() < THRESHOLD) {
                // the Error ! if previous wrapper's image is empty... should do it in the current frame instead
                previousWrapper = featureService.featureDetection(mRgba);
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
                essentialMat = Calib3d.findEssentialMat(currentPoints, previousPoints,
                        FOCAL, PRINCIPAL_POINT, Calib3d.LMEDS, 0.99, 1.0, mask);
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
                        // this should be moved to a private method``f
                        currentPose.update(translationMatrix, rotationMatrix);
//                        staticCubeRenderer.setX(2 * (int) (4.77 * currentPose.getCoordinate().get(1, 0)[0]));
//                        staticCubeRenderer.setY(2 * (int) (4.77 * currentPose.getCoordinate().get(0, 0)[0]));
                        staticCubeRenderer.setZ(2 * (int) (5.5 * currentPose.getCoordinate().get(2, 0)[0]));
                        Log.d(TAG,"trajectory " + "x: " + currentPose.getCoordinate().get(0,0)[0]);
                        Log.d("trajectory ", "y: " + currentPose.getCoordinate().get(2,0)[0]);
                        Log.d("trajectory ", "z: " + currentPose.getCoordinate().get(2,0)[0]);
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

        isRunning = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        gray = inputFrame.gray();
        output = mRgba.clone();

        if (!isRunning) {
            Log.d("thread ", "Starting the thread");
            run.run();
        }

        Log.d("thread ", "Returning input frame");
        Log.d(TAG, "isTouch ? = " + isTouch);
        return output;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d(TAG, "called onSensorChanged");
        rotationVector = orientationService.calcDeviceOrientationVector(event);
        if (staticCubeRenderer.getRotationVector() == null) {
            staticCubeRenderer.setPreviousRotationVector(rotationVector);
        }
        staticCubeRenderer.setRotationVector(rotationVector);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG,"called onAccuracyChanged");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == CameraPermissionServiceImpl.REQUEST_CODE) {
            cameraPermissionService.handleResponse(requestCode, permissions, grantResults);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            return true;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {

        }

        return false;
    }

}
