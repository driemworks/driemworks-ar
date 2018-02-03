package com.driemworks.simplecv.activities;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
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

import com.driemworks.ar.dto.CameraPoseDTO;
import com.driemworks.ar.imageProcessing.ColorBlobDetector;
import com.driemworks.ar.services.SurfaceDetectionService;
import com.driemworks.common.dto.SurfaceDataDTO;
import com.driemworks.common.factories.BaseLoaderCallbackFactory;
import com.driemworks.common.utils.DisplayUtils;
import com.driemworks.common.utils.TagUtils;
import com.driemworks.sensor.services.OrientationService;
import com.driemworks.simplecv.R;
import com.driemworks.common.enums.Resolution;
import com.driemworks.simplecv.builders.CustomSurfaceViewBuilder;
import com.driemworks.simplecv.builders.GLSurfaceViewBuilder;
import com.driemworks.ar.executors.MonocularVisualOdometryExecutor;
import com.driemworks.simplecv.graphics.rendering.StaticCubeRenderer;
import com.driemworks.simplecv.services.PropertyReader;
import com.driemworks.simplecv.services.permission.impl.CameraPermissionServiceImpl;
import com.driemworks.common.views.CustomSurfaceView;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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

    /**
     * The previous frame in grayscale
     */
    private Mat previousFrameGray = null;

    /** The customSurfaceView surface view */
    private CustomSurfaceView customSurfaceView;

    /** The base loader callback */
    private BaseLoaderCallback mLoaderCallback;

    /** The previously detected points */
    private MatOfKeyPoint previousPoints;

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
     * The surface detection service
     */
    private SurfaceDetectionService surfaceDetectionService;

    /**
     * The is rotation enabled flag
     */
    private boolean isRotationEnabled;

    /**
     * The Monocular Visual Odometry Executor
     */
    private MonocularVisualOdometryExecutor executor;

    /**
     * The future object
     */
    private Future<CameraPoseDTO> future;

    /** The width of the device screen */
    private int screenWidth;

    /** The height of the device screen */
    private int screenHeight;

    private boolean mIsColorSelected = false;

    private ColorBlobDetector mDetector;

    /** The color spectrum */
    private Mat mSpectrum;

    /** The size of the spectrum */
    private Size SPECTRUM_SIZE;

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
        currentPose = new CameraPoseDTO();
        previousPoints = new MatOfKeyPoint();

        // load properties
        try {
            Properties props = PropertyReader.readProperties(this.getApplicationContext(), "driemworks.properties");
            isRotationEnabled = Boolean.valueOf(props.getProperty("feature.rotation.enabled"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // get screen dimensions
        android.graphics.Point size = DisplayUtils.getScreenSize(this);
        screenWidth = size.x;
        screenHeight = size.y;

        staticCubeRenderer = new StaticCubeRenderer(Resolution.RES_STANDARD, isRotationEnabled);
        // init services
        cameraPermissionService = new CameraPermissionServiceImpl(this);
        orientationService = new OrientationService((SensorManager) getSystemService(SENSOR_SERVICE));
        surfaceDetectionService = new SurfaceDetectionService(new Scalar(255, 255, 255, 255),
                new Scalar(222, 040, 255), new Mat(), new Size(200, 64), null);
        executor = MonocularVisualOdometryExecutor.getInstance();
        mDetector = new ColorBlobDetector();
        mSpectrum = new Mat();
        SPECTRUM_SIZE = new Size(200, 64);

        // build the views
        customSurfaceView = new CustomSurfaceViewBuilder(this, R.id.main_surface_view)
                .setCvCameraViewListener(this)
                .setMaxFrameSize(Resolution.RES_STANDARD)
                .build();

        glSurfaceView = new GLSurfaceViewBuilder(this, R.id.gl_surface_view)
                .setEGLConfigChooser(8,8,8,8,16,0)
                .setOnTouchListener(this)
                .setRenderer(staticCubeRenderer)
                .setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY)
                .setFormat(PixelFormat.TRANSLUCENT)
                .build();

        // init base loader callback
        mLoaderCallback = BaseLoaderCallbackFactory.getBaseLoaderCallback(
                this, customSurfaceView, Resolution.RES_STANDARD);
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
        customSurfaceView.setMaxFrameSize(Resolution.RES_STANDARD.getWidth(), Resolution.RES_STANDARD.getHeight());
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
     * The runnable for running monocular visual odometry
     */
    private Runnable runner = () -> {
        Log.d(TAG, "Called run");
        isRunning = true;
        future = executor.calculateOdometry(currentPose, mRgba, previousFrameGray, gray);

        while (!future.isDone()) {
            Log.d(TAG, "future is not done");
        }

        if (future.isDone()) {
            try {
                Log.d(TAG, "Getting current pose.");
                currentPose = future.get();
            } catch (InterruptedException | ExecutionException e) {
                Log.e(TAG, e.getMessage());
            } finally {
                Log.d(TAG, "current pose: " + currentPose.toString());
                Log.d(TAG, "z coordinate: " +  currentPose.getCoordinate().get(0, 0)[0]);
                previousPoints = currentPose.getKeyPoints();
                isRunning = false;
            }
        }
    };

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

        if (previousFrameGray != null) {
            if (!isRunning) {
                runner.run();
            }
        }

        if (mIsColorSelected) {
            SurfaceDataDTO surfaceData = surfaceDetectionService.detect(mRgba, 0, true);
            output = surfaceData.getmRgba();
        }
        Log.d("###", "END - onCameraFrame - " + (System.currentTimeMillis() - startTime) + "ms");
        previousFrameGray = gray;
        return output;
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
        } else {
            // the value used to bound the size of the area to be sampled
            int sizeThreshold = 10;

            Point correctedCoordinate = DisplayUtils.correctCoordinate(event, screenWidth, screenHeight);
            Rect touchedRect = new Rect((int)correctedCoordinate.x, (int)correctedCoordinate.y, sizeThreshold, sizeThreshold);
            if (null == touchedRect) {
                return false;
            }

            // get the rectangle around the point that was touched
            Mat touchedRegionRgba = mRgba.submat(touchedRect);

            // format to hsv
            Mat touchedRegionHsv = new Mat();
            Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

            // Calculate average color of touched region
            Scalar mBlobColorHsv = Core.sumElems(touchedRegionHsv);
            int pointCount = touchedRect.width * touchedRect.height;

            for (int i = 0; i < mBlobColorHsv.val.length; i++) {
                mBlobColorHsv.val[i] /= pointCount;
            }

            mDetector = surfaceDetectionService.getColorBlobDetector();
            mDetector.setHsvColor(mBlobColorHsv);
            surfaceDetectionService.setColorBlobDetector(mDetector);
            Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);

            mIsColorSelected = true;
            Log.d(TAG, "color has been set");

            touchedRegionRgba.release();
            touchedRegionHsv.release();

            return false;
        }

        return false;
    }

}
