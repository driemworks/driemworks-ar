package com.driemworks.app.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;

import com.driemworks.app.R;
import com.driemworks.app.builders.CustomSurfaceViewBuilder;
import com.driemworks.app.factories.BaseLoaderCallbackFactory;
import com.driemworks.app.views.CustomSurfaceView;
import com.driemworks.common.enums.Resolution;
import com.driemworks.common.utils.OpenCvUtils;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

/**
 * @author Tony
 */
public abstract class OpenCVActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    /** load the opencv lib */
    static {
        System.loadLibrary("opencv_java3");
    }

    /** The id of the layout */
    private final int layoutResId;

    /** The Resolution of the screen */
    private final Resolution resolution;

    /** The custom surface view */
    private CustomSurfaceView customSurfaceView;

    /** The base loader callback */
    private BaseLoaderCallback baseLoaderCallback;

    /**
     * The current image (rgba)
     */
    private Mat mRgba;

    /**
     * The current image (gray)
     */
    private Mat mGray;

    /**
     * The output image
     */
    private Mat output;
    
    /**
     * The constructor for the OpenCV activity
     * @param layoutResId The layout id
     * @param resolution The resolution
     */
    public OpenCVActivity(int layoutResId, Resolution resolution) {
        this.layoutResId = layoutResId;
        this.resolution = resolution;
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

        // build the views and init callbacks
        customSurfaceView = new CustomSurfaceViewBuilder(this, R.id.main_surface_view)
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
    public abstract Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame frame);

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
}
