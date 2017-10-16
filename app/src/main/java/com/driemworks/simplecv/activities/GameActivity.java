package com.driemworks.simplecv.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.driemworks.ar.services.SurfaceDetectionService;
import com.driemworks.common.dto.ConfigurationDTO;
import com.driemworks.common.dto.SurfaceDataDTO;
import com.driemworks.common.factories.BaseLoaderCallbackFactory;
import com.driemworks.sensor.services.OrientationService;
import com.driemworks.simplecv.R;
import com.driemworks.simplecv.enums.IntentIdentifer;
import com.driemworks.simplecv.enums.Resolution;
import com.driemworks.simplecv.graphics.rendering.AbstractRenderer;
import com.driemworks.simplecv.graphics.rendering.GraphicsRenderer;
import com.driemworks.simplecv.layout.impl.GameActivityLayoutManager;
import com.driemworks.common.views.CustomSurfaceView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;

import java.lang.reflect.Field;

/**
 * Created by Tony on 5/6/2017.
 */

public class GameActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2,
        SensorEventListener, View.OnTouchListener, LocationListener {

    private static final String TAG = "GameActivity: ";

    /** //////////////   OPEN CV    /////////////////////// */
    private CustomSurfaceView customSurfaceView;
    private ConfigurationDTO configurationDTO;
    private SurfaceDetectionService surfaceDetector;
    private SurfaceDataDTO surfaceData;

    private Mat mRgba;
    private Mat mGray;

    /** /////////////     JPCT    /////////////////////// */
    // Used to handle pause and resume...
    private static GameActivity master = null;

    private GLSurfaceView mGLView;
    private GraphicsRenderer renderer;

    /** ///////////////     ANDROID    ///////////////// */
    public GameActivityLayoutManager layoutManager;

    /** The sensor manager */
    private SensorManager sensorManager;
    private OrientationService orientationService;

    private float[] rotationVector = new float[3];
    private float[] translationVector;
    private float[] mRotationMatrix = new float[16];

    private boolean isInit = true;

    private boolean doDraw;

    private long startTime = System.currentTimeMillis();
    private long elapsedTime;

    /**
     *
     */
    private BaseLoaderCallback baseLoaderCallback;/* = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    customSurfaceView.enableView();
                    customSurfaceView.setMaxFrameSize(800, 480);
//                    customSurfaceView.setOnTouchListener(GameActivity.this);
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };*/

    /**
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        if (master != null) {
            copy(master);
        }

        if (!OpenCVLoader.initDebug()) {
            Log.e("OpvenCVLoader", "OvenCVLoader successful: false");
        } else {
            Log.d("OpenCVLoader", "OpenCVLoader successful");
        }

        setContentView(R.layout.game_layout);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        customSurfaceView = (CustomSurfaceView) findViewById(R.id.main_surface_view);
        customSurfaceView.setCvCameraViewListener(this);
        customSurfaceView.setMaxFrameSize(Resolution.RES_STANDARD.getWidth(), Resolution.RES_STANDARD.getHeight());

        baseLoaderCallback = BaseLoaderCallbackFactory.getBaseLoaderCallback(this, customSurfaceView);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        orientationService = new OrientationService(sensorManager);

        renderer = new GraphicsRenderer();

        mGLView = (GLSurfaceView) findViewById(R.id.gl_surface_view0);
        mGLView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mGLView.setOnTouchListener(this);
        mGLView.setRenderer(renderer);
        mGLView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        mGLView.getHolder().setFormat(PixelFormat.TRANSLUCENT);

        // setup the layout
        layoutManager = GameActivityLayoutManager.getInstance();
        layoutManager.setActivity(this);
        layoutManager.setRenderer(renderer);
        layoutManager.setup(GameActivityLayoutManager.RECONFIG, findViewById(R.id.reconfigure_button));
        layoutManager.setup(GameActivityLayoutManager.SHOW_RECT, findViewById(R.id.show_opencv));
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
        mGLView.onPause();
    }

    /**
     *
     */
    @Override
    protected void onResume() {
        super.onResume();
        mGLView.onResume();
        orientationService.registerListeners(this);
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, baseLoaderCallback);

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
     * @param src
     */
    private void copy(Object src) {
        try {
            Log.d(TAG, "Copying data from master Activity!");
            Field[] fs = src.getClass().getDeclaredFields();
            for (Field f : fs) {
                f.setAccessible(true);
                f.set(this, f.get(src)) ;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param inputFrame
     * @return
     */
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        // get the images from the input frame
        mRgba = inputFrame.rgba();
        renderer.setmRgba(mRgba);
        mGray = inputFrame.gray();
        surfaceData = surfaceDetector.detect(mRgba, configurationDTO.getThreshold(), doDraw);
        Log.d(TAG, "is bound rect null? = " + (null == surfaceData.getBoundRect()));
       if (surfaceData != null) {
           renderer.setSurfaceDataDTO(surfaceData);
       }
        return mRgba;
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
        configurationDTO = (ConfigurationDTO) intent.getSerializableExtra(IntentIdentifer.CONFIG_DTO.getValue());

        surfaceDetector = new SurfaceDetectionService(new Scalar(255, 255, 255, 255),
                new Scalar(222, 040, 255), new Mat(), new Size(200, 64), configurationDTO);
    }

    /**
     *
     */
    public void onCameraViewStopped() {
        Log.d(TAG, "camera view stopped");
//        mGray.release();
        mRgba.release();
    }

    public boolean isDoDraw() {
        return doDraw;
    }

    public void setDoDraw(boolean doDraw) {
        this.doDraw = doDraw;
    }

//     public GraphicsRenderer getRenderer() {
//         return renderer;
//     }

    /**
     *
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        elapsedTime = System.currentTimeMillis() - startTime;
        rotationVector = orientationService.calcDeviceOrientationVector(event);
        translationVector = orientationService.calcTranslationVector(event, elapsedTime);
        if (isInit) {
            isInit = false;
            renderer.setPreviousRotationVector(rotationVector);
        }
        mRotationMatrix = orientationService.updateRotationMatrix(event);
        renderer.setRotationVector(rotationVector);
        startTime = System.currentTimeMillis();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private float pressure;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d(TAG, "screen touched.");
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            pressure = event.getPressure();
            renderer.pushCube(pressure);
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            renderer.pullCube(pressure);
            return false;
        }

        return false;
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}