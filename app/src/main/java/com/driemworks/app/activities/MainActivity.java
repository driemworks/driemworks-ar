package com.driemworks.app.activities;

import android.app.Activity;
import android.content.Intent;
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

import com.driemworks.ar.services.SurfaceDetectionService;
import com.driemworks.common.cs.Constants;
import com.driemworks.common.dto.ConfigurationDTO;
import com.driemworks.common.dto.SurfaceDataDTO;
import com.driemworks.common.enums.Resolution;
import com.driemworks.sensor.services.OrientationService;
import com.driemworks.app.R;
import com.driemworks.app.factories.BaseLoaderCallbackFactory;
import com.driemworks.app.graphics.rendering.MainRenderer;
import com.driemworks.app.layout.impl.MainActivityLayoutManager;
import com.driemworks.app.utils.RenderUtils;
import com.driemworks.app.views.CustomSurfaceView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * @author Tony
 */
public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2,
        View.OnTouchListener, SensorEventListener {

    private static final String TAG = "MainActivity: ";

    /** //////////////   OPEN CV    /////////////////////// */
    private CustomSurfaceView customSurfaceView;
    private ConfigurationDTO configurationDTO;
    private SurfaceDetectionService surfaceDetector;
    private SurfaceDataDTO surfaceData;

    private Mat mRgba;

    /** /////////////    JPCT    /////////////////////// */
    // Used to handle pause and resume...
    private static CubeActivity master = null;

    /** ///////////////     ANDROID    ///////////////// */
    public MainActivityLayoutManager layoutManager;

    private boolean doDraw = false;

    /** The base loader callback (used to init opencv) */
    private BaseLoaderCallback mLoaderCallback;

    private OrientationService orientationService;

    private MainRenderer mainRenderer;

    private GLSurfaceView menuView;

    /**
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        if (master != null) {
            RenderUtils.copy(this, master);
        }

        if (!OpenCVLoader.initDebug()) {
            Log.e("OpvenCVLoader", "OvenCVLoader successful: false");
        } else {
            Log.d("OpenCVLoader", "OpenCVLoader successful");
        }


        setContentView(R.layout.main_layout);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        customSurfaceView = (CustomSurfaceView) findViewById(R.id.main_surface_view);
        // register listeners
        customSurfaceView.setCvCameraViewListener(this);
        customSurfaceView.setMaxFrameSize(800, 480);
        mLoaderCallback = BaseLoaderCallbackFactory.getBaseLoaderCallback(this, customSurfaceView);

        mainRenderer = new MainRenderer(this);

        menuView = (GLSurfaceView) findViewById(R.id.gl_surface_view);
        menuView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        menuView.setOnTouchListener(this);
        menuView.setRenderer(mainRenderer);
        menuView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        menuView.getHolder().setFormat(PixelFormat.TRANSLUCENT);

        orientationService = new OrientationService((SensorManager) getSystemService(SENSOR_SERVICE));

        // setup the layout
        layoutManager = new MainActivityLayoutManager(this);
        layoutManager.setup(MainActivityLayoutManager.BACK_BTN, findViewById(R.id.btn_back));
    }

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    /**
     *
     */
    @Override
    protected void onPause() {
        super.onPause();
        menuView.onPause();
    }

    /**
     *
     */
    @Override
    protected void onResume() {
        super.onResume();
        orientationService.registerListeners(this);
        menuView.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);

    }

    /**
     *
     */
    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     *
     * @param inputFrame
     * @return
     */
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();

        mainRenderer.setmRgba(mRgba);
        surfaceData = surfaceDetector.detect(mRgba, configurationDTO.getThreshold(), true);

        Log.d(TAG, "is bound rect null? = " + (null == surfaceData.getBoundRect()));

        // if surface data is not null, update the dto in the renderer
        if (surfaceData != null) {
            mainRenderer.setSurfaceDataDTO(surfaceData);
            // resample the color of the surface at each frame
            // we need to basically repeat the same procedure we use
            // when we touched the screen
            if (surfaceData.getBoundRect() != null) {
                resampleColors(surfaceData, mRgba);
            }
        }
        return mRgba;
    }

    private void resampleColors(SurfaceDataDTO surfaceData, Mat mRgba) {
        // for now, we'll just resample using the average color
        // of a 5x5 region in the center of the detected contours in the input image mRgba
        Point br = surfaceData.getBoundRect().br();
        Point tl = surfaceData.getBoundRect().tl();

        int centerX = (int)(br.x - tl.x);
        int centerY = (int)(br.y - tl.y);

        Log.d(TAG, "calculated centerX = " + centerX + " calculated centerY = " + centerY);

        if (centerX > 0 && centerY > 0) {
            Rect centerRect = new Rect(centerX, centerY, 10, 10);
            Log.d(TAG, "drawing center rect!");
            Imgproc.rectangle(mRgba, tl, br, new Scalar(0, 255, 0), 2, 8, 0);
            if (false/**centerRect.size().area() > 0*/) {
                Mat centerRegionRgba = mRgba.submat(centerRect);

                // format to hsv
                Mat touchedRegionHsv = new Mat();
                Imgproc.cvtColor(centerRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

                // Calculate average color of touched region
                Scalar mBlobColorHsv = Core.sumElems(touchedRegionHsv);
                int pointCount = 10 * 10;

                for (int i = 0; i < mBlobColorHsv.val.length; i++) {
                    mBlobColorHsv.val[i] /= pointCount;
                }

                surfaceDetector.getColorBlobDetector().setHsvColor(mBlobColorHsv);
            }
        }
    }

    /**
     *
     * @param width -  the width of the frames that will be delivered
     * @param height - the height of the frames that will be delivered
     */
    public void onCameraViewStarted(int width, int height) {
        Log.d(TAG, "camera view started");
        mRgba = new Mat();

        Intent intent = getIntent();
        configurationDTO = (ConfigurationDTO) intent
                .getSerializableExtra(Constants.CONFIG);
        // TODO move params to an enum somewhere, private vars
        surfaceDetector = new SurfaceDetectionService(new Scalar(255, 255, 255, 255),
                new Scalar(222, 040, 255), new Mat(), new Size(200, 64), configurationDTO.getColor());
    }

    /**
     *
     */
    public void onCameraViewStopped() {
        Log.d(TAG, "camera view stopped");
        mRgba.release();
    }

    private int count = 0;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            count++;
            if (count % 2 == 0) {
                mainRenderer.setTouch(!mainRenderer.isTouch());
            }
        }
        return false;
    }

    private float[] rotationVector = new float[3];

    @Override
    public void onSensorChanged(SensorEvent event) {
        rotationVector = orientationService.calcDeviceOrientationVector(event);
        mainRenderer.setRotationVector(rotationVector);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.i("*** ", "Accuracy changed. ");
    }

    public MainRenderer getMainRenderer() {
        return mainRenderer;
    }

    public MainActivityLayoutManager getLayoutManager() {
        return layoutManager;
    }

    public ConfigurationDTO getConfigurationDTO() {
        return configurationDTO;
    }

    public void setConfigurationDTO(ConfigurationDTO configurationDTO) {
        this.configurationDTO = configurationDTO;
    }
}
