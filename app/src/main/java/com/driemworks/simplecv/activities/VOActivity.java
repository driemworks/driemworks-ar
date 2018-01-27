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
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.driemworks.ar.MonocularVisualOdometry.services.impl.FeatureServiceImpl;
import com.driemworks.ar.dto.CameraPoseDTO;
import com.driemworks.ar.dto.FeatureWrapper;
import com.driemworks.ar.dto.SequentialFrameFeatures;
import com.driemworks.common.cs.Constants;
import com.driemworks.common.factories.BaseLoaderCallbackFactory;
import com.driemworks.common.utils.ImageConversionUtils;
import com.driemworks.common.utils.TagUtils;
import com.driemworks.sensor.services.OrientationService;
import com.driemworks.simplecv.R;
import com.driemworks.common.enums.Resolution;
import com.driemworks.simplecv.builders.CustomSurfaceViewBuilder;
import com.driemworks.simplecv.builders.GLSurfaceViewBuilder;
import com.driemworks.simplecv.graphics.rendering.StaticCubeRenderer;
import com.driemworks.simplecv.services.PropertyReader;
import com.driemworks.simplecv.services.permission.impl.CameraPermissionServiceImpl;
import com.driemworks.common.views.CustomSurfaceView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * The Monocular Visual Odometry testing activity
 * @author Tony
 */
public class VOActivity extends Activity implements CvCameraViewListener2, View.OnTouchListener, SensorEventListener {

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
    private final String TAG = TagUtils.getTag(this);

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
    private static final float FOCAL = 800.00f;
    /**
     * The principal point
     */
    private static final Point PRINCIPAL_POINT = new Point(100, 100);

    /** The currently detected points */
    private MatOfPoint2f currentPoints;

    /** The previously detected points */
    private MatOfPoint2f previousPoints;

    /** The rotation matrix */
    private Mat rotationMatrix;

    /** The translation matrix */
    private Mat translationMatrix;

    /** The runnable used for running the mvo service */
    private Runnable run;

    /**
     * The is running flag
     */
    private boolean isRunning = false;

    /**
     * The static cube renderer
     */
    private StaticCubeRenderer staticCubeRenderer;

    /**
     * The gl surface view
     */
    private GLSurfaceView glSurfaceView;

    /**
     * The orientation service
     */
    private OrientationService orientationService;

    /**
     * The is rotation enabled flag
     */
    private boolean isRotationEnabled;

    /**
     * Create the runnable to run the monocular visual odometry
     * @return {@link Runnable}
     */
    private Runnable createRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                monocularVisualOdometry();
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

        if (!OpenCVLoader.initDebug()) {
            Log.e("OpvenCVLoader", "OvenCVLoader successful: false");
        } else {
            Log.d("OpenCVLoader", "OpenCVLoader successful");
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.vo_layout);

        // init the matrices
        rotationMatrix = new Mat(3, 3, CvType.CV_64FC1);
        translationMatrix = new Mat(3, 1, CvType.CV_64FC1);

        currentPose = new CameraPoseDTO();
        currentPoints = new MatOfPoint2f();
        previousPoints = new MatOfPoint2f();

        customSurfaceView = new CustomSurfaceViewBuilder(this, R.id.main_surface_view)
                .setCvCameraViewListener(this)
                .setMaxFrameSize(Resolution.RES_STANDARD)
                .build();

        // init base loader callback
        mLoaderCallback = BaseLoaderCallbackFactory.getBaseLoaderCallback(
                this, customSurfaceView, Resolution.RES_STANDARD);

        // init services
        cameraPermissionService = new CameraPermissionServiceImpl(this);
        featureService = new FeatureServiceImpl();
        run = createRunnable();

        try {
            Properties props = PropertyReader.readProperties(this.getApplicationContext(), "driemworks.properties");
            isRotationEnabled = Boolean.valueOf(props.getProperty("feature.rotation.enabled"));
            Log.d(TAG, "isRotationEnabled ? " + isRotationEnabled);
        } catch (IOException e) {
            e.printStackTrace();
        }

        staticCubeRenderer = new StaticCubeRenderer(Resolution.RES_STANDARD, isRotationEnabled);
        orientationService = new OrientationService((SensorManager) getSystemService(SENSOR_SERVICE));

        glSurfaceView = new GLSurfaceViewBuilder(this, R.id.gl_surface_view)
                .setEGLConfigChooser(8,8,8,8,16,0)
                .setOnTouchListener(this)
                .setRenderer(staticCubeRenderer)
                .setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY)
                .setFormat(PixelFormat.TRANSLUCENT)
                .build();
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
     */
    private void monocularVisualOdometry() {
        float startTime = System.currentTimeMillis();
        Log.d("###","START - monocularVisualOdometry");
        if (previousWrapper == null || previousWrapper.empty()) {
            currentPose.reset();
            staticCubeRenderer.setCameraCoordinate(Constants.ZERO3D);
            previousWrapper = featureService.featureDetection(output);
        } else {
            // track feature from the previous image into the current image
            SequentialFrameFeatures sequentialFrameFeatures = featureService.featureTracking(
                    previousWrapper.getFrameAsGrayscale(), gray, previousWrapper.getKeyPoints());
            if (sequentialFrameFeatures.getCurrentFrameFeaturePoints().size() < THRESHOLD) {
                previousWrapper = featureService.featureDetection(output);
            } else {

                // draws filtered feature points on output
                if (true) {
                    for (Point p : sequentialFrameFeatures.getCurrentFrameFeaturePoints()) {
                        Imgproc.circle(mRgba, p, 2, new Scalar(255, 0, 0));
                    }
                }

                // convert the lists of points to MatOfPoint2f's
                currentPoints = ImageConversionUtils.convertListToMatOfPoint2f(sequentialFrameFeatures.getCurrentFrameFeaturePoints());
                previousPoints = ImageConversionUtils.convertListToMatOfPoint2f(sequentialFrameFeatures.getPreviousFrameFeaturePoints());

                if (!currentPoints.empty() && currentPoints.checkVector(2) > 0) {
                    Mat mask = new Mat();
                    Mat essentialMat = Calib3d.findEssentialMat(currentPoints, previousPoints,
                            FOCAL, PRINCIPAL_POINT, Calib3d.LMEDS, 0.99,1.0, mask);
                    // calculate rotation and translation matrices
                    if (!essentialMat.empty() && essentialMat.rows() == 3 &&
                            essentialMat.cols() == 3 && essentialMat.isContinuous()) {
                        Calib3d.recoverPose(essentialMat, currentPoints,
                                previousPoints, rotationMatrix, translationMatrix, FOCAL, PRINCIPAL_POINT, mask);
                        if (!translationMatrix.empty() && !rotationMatrix.empty()) {
                            currentPose.update(translationMatrix, rotationMatrix);
//                            staticCubeRenderer.setX(2 * (int) (5.77 * currentPose.getCoordinate().get(1 , 0)[0]));
//                            staticCubeRenderer.setY(2 * (int) (5.77  * currentPose.getCoordinate().get(0, 0)[0]));
                            staticCubeRenderer.setZ(2 * (int) (5.77 * currentPose.getCoordinate().get(2, 0)[0]));
                        }
                    }

                    mask.release();
                    essentialMat.release();
                }

                previousWrapper.setFrame(mRgba);
                previousWrapper.setKeyPoints(ImageConversionUtils.convertMatOf2fToKeyPoints(
                        currentPoints, 2, 2));
            }
        }

        Log.d("###","END - monocularVisualOdometry - " + (System.currentTimeMillis() - startTime) + " ms");
        isRunning = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        float startTime = System.currentTimeMillis();

        Log.d("###", "START - onCameraFrame");

        mRgba = inputFrame.rgba();
        gray = inputFrame.gray();
        output = mRgba.clone();

        if (!isRunning) {
            Log.d("###", "Starting the thread");
            run.run();
        }

        Log.d("###", "Returning input frame");
        Log.d("###", "END - onCameraFrame - " + (System.currentTimeMillis() - startTime) + "ms");
        return mRgba;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d(TAG, "called onSensorChanged");
        float[] rotationVector = orientationService.calcDeviceOrientationVector(event);
        if (staticCubeRenderer.getCurrentRotationVector() == null) {
            staticCubeRenderer.setPreviousRotationVector(rotationVector);
        }

        staticCubeRenderer.setCurrentRotationVector(rotationVector);
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
        if (isRotationEnabled) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                staticCubeRenderer.setRotationEnabled(false);
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                staticCubeRenderer.setRotationEnabled(true);
            }
        }

        return false;
    }

}
