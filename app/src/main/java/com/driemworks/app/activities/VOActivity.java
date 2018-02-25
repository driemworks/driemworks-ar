package com.driemworks.app.activities;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
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
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.driemworks.ar.dto.CameraPoseDTO;
import com.driemworks.ar.imageProcessing.ColorBlobDetector;
import com.driemworks.ar.services.SurfaceDetectionService;
import com.driemworks.ar.services.impl.MonocularVisualOdometryService;
import com.driemworks.app.factories.BaseLoaderCallbackFactory;
import com.driemworks.common.dto.SurfaceDataDTO;
import com.driemworks.common.utils.DisplayUtils;
import com.driemworks.common.utils.ImageConversionUtils;
import com.driemworks.common.utils.OpenCvUtils;
import com.driemworks.common.utils.TagUtils;
import com.driemworks.app.R;
import com.driemworks.common.enums.Resolution;
import com.driemworks.app.builders.CustomSurfaceViewBuilder;
import com.driemworks.app.builders.GLSurfaceViewBuilder;
import com.driemworks.app.graphics.rendering.StaticCubeRenderer;
import com.driemworks.app.services.PropertyReader;
import com.driemworks.app.services.permission.impl.CameraPermissionServiceImpl;
import com.driemworks.app.views.CustomSurfaceView;

import java.io.IOException;
import java.util.Properties;

/**
 * The Monocular Visual Odometry testing activity
 * @author Tony
 */
public class VOActivity extends Activity implements CvCameraViewListener2, View.OnTouchListener, SensorEventListener {


    // TODO THERE ARE TOO MANY FIELDS!!!

    /**
     * The camera pose dto - the current pose of the camera
     */
    private CameraPoseDTO currentPose;

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

    /**
     * The static cube renderer
     */
    private StaticCubeRenderer staticCubeRenderer;

    /**
     * The gl surface view
     */
    private GLSurfaceView glSurfaceView;

    /**
     * The is rotation enabled flag
     */
    private boolean isRotationEnabled;

    /** The width of the device screen */
    private int screenWidth;

    /** The height of the device screen */
    private int screenHeight;

    /**
     * The isRectSet flag
     */
    private boolean isRectSet = false;

    /**
     * The monocular visual odometry service
     */
    private MonocularVisualOdometryService monocularVisualOdometryService;

    /**
     * The resolution
     */
    private Resolution resolution;

    /**
     * The touched rect
     */
    private Rect touchedRect;

    /**
     * The top left touched point
     */
    private Point tl;

    /**
     * The bottom left touched point
     */
    private Point br;

    private SurfaceDetectionService surfaceDetectionService;

    /** The color blob detector */
    private ColorBlobDetector mDetector;

    private boolean mIsColorSelected;

    private Size SPECTRUM_SIZE;
    private Mat mSpectrum;

    private Button setRectButton;

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

        OpenCvUtils.initOpenCV(true);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.vo_layout);

        setRectButton = (Button)findViewById(R.id.set_rect);
        // TODO replace with lambda expression
        setRectButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (null != touchedRect) {
                    monocularVisualOdometryService.setTouchedRect(touchedRect);
                    isRectSet = true;
                }
                return false;
            }
        });

        resolution = Resolution.RES_STANDARD;

        // init the matrices
        currentPose = new CameraPoseDTO();
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
        Log.d(TAG, "Screen size: " + screenWidth + "x" + screenHeight);

        // init services
        cameraPermissionService = new CameraPermissionServiceImpl(this);
        surfaceDetectionService = new SurfaceDetectionService(new Scalar(255, 255, 255, 255),
                new Scalar(222, 040, 255), new Mat(), new Size(200, 64), null);
        mDetector = new ColorBlobDetector();
        monocularVisualOdometryService = new MonocularVisualOdometryService(resolution);
        staticCubeRenderer = new StaticCubeRenderer(resolution, false);

        // build the views and init callbacks
        customSurfaceView = new CustomSurfaceViewBuilder(this, R.id.main_surface_view)
                .setCvCameraViewListener(this)
                .setMaxFrameSize(resolution)
                .build();

        // init base loader callback
        mLoaderCallback = BaseLoaderCallbackFactory.getBaseLoaderCallback(
                this, customSurfaceView);

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
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
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
        mSpectrum = new Mat();
        SPECTRUM_SIZE = new Size(200, 64);
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
     * {@inheritDoc}
     */
    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        Log.d("###", "START - onCameraFrame");

        if (null == inputFrame) {
            return null;
        }

        mRgba = inputFrame.rgba();
        gray = inputFrame.gray();
        output = mRgba.clone();

        if (null != touchedRect) {
            Imgproc.rectangle(output, touchedRect.tl(), touchedRect.br(), new Scalar(0, 255, 0), 5);
        }


        if (mIsColorSelected) {
            SurfaceDataDTO surfaceData = surfaceDetectionService.detect(mRgba, 0, true);
            touchedRect = surfaceData.getBoundRect();
            Log.d(TAG, "Set touched rect.");
            if (null != touchedRect) {
                staticCubeRenderer.setX(touchedRect.x - resolution.getWidth() / 2);
                staticCubeRenderer.setY(touchedRect.y - resolution.getHeight() / 2);
            }
        }

        if (isRectSet) {
            currentPose = monocularVisualOdometryService.monocularVisualOdometry(currentPose, mRgba, previousFrameGray, gray);
            // calculate minimum bounding box around the detected keypoints and resample?
            MatOfPoint2f keyPoints2f = ImageConversionUtils.convertMatOfKeyPointsTo2f(currentPose.getKeyPoints());
//            for (Point p : keyPoints2f.toList()) {
//                Imgproc.circle(output, p, 5, new Scalar(0, 255, 0));
//            }

            if (!keyPoints2f.empty()) {
                Log.d(TAG, "z coordinate: " + currentPose.getZ());
//                Log.d(TAG, "corrected z coordinate: " + (int) (25.50 * currentPose.getZ()));
//                    Log.d(TAG, "bound rect center: (" + boundRect.x + " ," + boundRect.y + ")");

                Log.d(TAG, "Setting rect at (" + touchedRect.x + " , " + touchedRect.y + ")");
//                staticCubeRenderer.setZ((int)(25.50 * currentPose.getZ()));
            }

            // draw the rect from the mvo service...
            Imgproc.rectangle(output,
                    monocularVisualOdometryService.getTouchedRect().tl(),
                    monocularVisualOdometryService.getTouchedRect().br(),
                    new Scalar(255, 0, 0));
        }
        Log.d("###", "END - onCameraFrame");
        previousFrameGray = gray;
        return output;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d(TAG, "called onSensorChanged");
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
            int requestCode, @NonNull String[] permissions, @NonNull  int[] grantResults) {
        if (requestCode == CameraPermissionServiceImpl.REQUEST_CODE) {
            cameraPermissionService.handleResponse(requestCode, permissions, grantResults);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d(TAG, "called on touch");
        touchedRect = null;
        // the value used to bound the size of the area to be sampled
        int sizeThreshold = 10;
//            isRectSet = false;

        Point correctedCoordinate = DisplayUtils.correctCoordinate(event, screenWidth, screenHeight);
        Rect touchedRect = new Rect((int) correctedCoordinate.x, (int) correctedCoordinate.y, sizeThreshold, sizeThreshold);
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

        surfaceDetectionService.setHsvColor(mBlobColorHsv);
        Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);

        Log.d(TAG, "color has been set");
        mIsColorSelected = true;
        touchedRegionRgba.release();
        touchedRegionHsv.release();

        return false;
    }

}
