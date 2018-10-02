package com.driemworks.app.activities;

import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.WindowManager;

import com.driemworks.app.R;
import com.driemworks.app.activities.interfaces.OpenARActivityInterface;
import com.driemworks.app.activities.fragments.OpenCVFragment;
import com.driemworks.app.activities.fragments.OpenGLFragment;
import com.driemworks.app.sensor_fusion_demo.CubeRenderer;
import com.driemworks.app.sensor_fusion_demo.orientationProvider.ImprovedOrientationSensorProvider;
import com.driemworks.common.utils.OpenCvUtils;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;

/**
 * The OpenAR Fragment Activity is an example of how to combine opencv and opengl in an activity
 *
 * @author Tony Riemer
 */
public class OpenARFragmentActivity extends AbstractARActivity implements CameraBridgeViewBase.CvCameraViewListener2, OpenARActivityInterface {

    /** The renderer */
    private GLSurfaceView.Renderer renderer;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OpenCvUtils.initOpenCV(true);
        // keep the screen on!
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.opengl_opencv_layout);
        // setup the cube renderer
        renderer = new CubeRenderer(new ImprovedOrientationSensorProvider((SensorManager)getSystemService(SENSOR_SERVICE)));
        // attach the fragments
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // add opengl fragment
        transaction.add(new OpenGLFragment(), "OpenGLGFragment");
        // add opencv fragment
        transaction.add(new OpenCVFragment(), "OpenCVFragment");
        transaction.commit();
    }

    public int getGLSurfaceViewId() {
        return R.id.opengl_surface_view;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GLSurfaceView.Renderer getRenderer() {
        return renderer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRenderer(GLSurfaceView.Renderer renderer) {
        this.renderer = renderer;
    }

    /*
     * OpenCV methods
     */
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCameraViewStarted(int width, int height) {
        Log.i("OpenCvFragActiv: ", "called onCameraViewStarted.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCameraViewStopped() {
        Log.i("OpenCvFragActiv: ", "called onCameraViewStopped.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        return inputFrame.rgba();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getOpenCVSurfaceViewId() {
        return R.id.opencv_surface_view;
    }
}
