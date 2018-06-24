package com.driemworks.app.activities.base;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.driemworks.app.builders.OpenCVSurfaceViewBuilder;
import com.driemworks.app.factories.BaseLoaderCallbackFactory;
import com.driemworks.app.views.OpenCVSurfaceView;
import com.driemworks.common.enums.Resolution;
import com.driemworks.common.utils.DisplayUtils;
import com.driemworks.common.utils.OpenCvUtils;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

/**
 * The abstract AbstractOpenCVActivity handles calls to opencv to get
 * the current camera frame as a {@link Mat}
 *
 * This class loads OpenCV libs and sets up the
 * BaseLoaderCallback and the OpenCVSurfaceView
 *
 * @author Tony
 */
public abstract class AbstractOpenCVActivity extends Activity implements View.OnTouchListener, CameraBridgeViewBase.CvCameraViewListener2 {

    /** load the opencv lib */
    static {
        System.loadLibrary("opencv_java3");
    }

    /** The id of the layout */
    private final int layoutResId;

    /** The id of the OpenCVSurfaceView */
    private int openCVSurfaceViewId;

    /** The Resolution of the screen */
    private final Resolution resolution;

    /** The custom surface view */
    private OpenCVSurfaceView customSurfaceView;

    /** The base loader callback */
    private BaseLoaderCallback baseLoaderCallback;

    /** The current image (rgba) */
    private Mat mRgba;

    /** The current image (gray) */
    private Mat mGray;

    /** The output image */
    private Mat output;

    /** The implement on touch flag */
    private boolean implementOnTouch;

    /** The width of the device screen */
    private int screenWidth;

    /** The height of the device screen */
    private int screenHeight;

    /**
     * The constructor for the OpenCV activity
     * @param layoutResId The layout id
     * @param resolution The resolution
     */
    public AbstractOpenCVActivity(int layoutResId, int openCVSurfaceViewId, Resolution resolution, boolean implementOnTouch) {
        this.layoutResId = layoutResId;
        this.openCVSurfaceViewId = openCVSurfaceViewId;
        this.resolution = resolution;
        this.implementOnTouch = implementOnTouch;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OpenCvUtils.initOpenCV(true);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(layoutResId);

        // get screen dimensions
        android.graphics.Point size = DisplayUtils.getScreenSize(this);
        screenWidth = size.x;
        screenHeight = size.y;

        View.OnTouchListener listener = implementOnTouch ? this : null;
        // build the views and init callbacks
        customSurfaceView = new OpenCVSurfaceViewBuilder(this, openCVSurfaceViewId, listener)
                .setCvCameraViewListener(this)
                .setMaxFrameSize(resolution)
                .build();

        // init base loader callback
        baseLoaderCallback = BaseLoaderCallbackFactory.getBaseLoaderCallback(
                this, customSurfaceView);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause() {
        super.onPause();
        if (customSurfaceView != null) {
            customSurfaceView.disableView();
        }
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
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, baseLoaderCallback);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCameraViewStarted(int width, int height) {
        // init the mats for the current image
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC1);
        output = new Mat(height, width, CvType.CV_8UC4);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCameraViewStopped() {
        // release all (current image) mats
        mRgba.release();
        mGray.release();
        output.release();
    }


    /**
     * {@inheritDoc}
     */
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame frame) {
        return frame.rgba();
    }

    /**
     * Update the rgba, gray, and output mats with the input frame
     * @param frame The frame
     */
    public void updateCurrentMat(CameraBridgeViewBase.CvCameraViewFrame frame) {
        setmRgba(frame.rgba());
        setmGray(frame.gray());
        setOutput(getmRgba().clone());
    }

    /**
     * Getter for the mRgba
     * @return mRgba The rgba image
     */
    public Mat getmRgba() {
        return mRgba;
    }

    /**
     * Setter for the mRgba
     * @param mRgba The rgba image to set
     */
    public void setmRgba(Mat mRgba) {
        this.mRgba = mRgba;
    }

    /**
     * Getter for the mGray
     * @return mGray The gray image
     */
    public Mat getmGray() {
        return mGray;
    }

    /**
     * Setter for the gray image
     * @param mGray The gray image to set
     */
    public void setmGray(Mat mGray) {
        this.mGray = mGray;
    }

    /**
     * Getter for the output
     * @return output The output image
     */
    public Mat getOutput() {
        return output;
    }

    /**
     * Setter for the output image
     * @param output The output image to set
     */
    public void setOutput(Mat output) {
        this.output = output;
    }

    public Resolution getResolution() {
        return resolution;
    }


    /**
     * Getter for the screen width
     * @return The screen width
     */
    public int getScreenWidth() {
        return screenWidth;
    }

    /**
     * Setter for the screen width
     * @param screenWidth The screen width to set
     */
    public void setScreenWidth(int screenWidth) {
        this.screenWidth = screenWidth;
    }

    /**
     * Getter for the screen height
     * @return The screen height
     */
    public int getScreenHeight() {
        return screenHeight;
    }

    /**
     * Setter for the screen height
     * @param screenHeight The screen height to set
     */
    public void setScreenHeight(int screenHeight) {
        this.screenHeight = screenHeight;
    }
}
