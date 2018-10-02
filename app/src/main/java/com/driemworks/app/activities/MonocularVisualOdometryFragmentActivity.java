package com.driemworks.app.activities;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.WindowManager;

import com.driemworks.app.R;
import com.driemworks.app.activities.fragments.OpenCVFragment;
import com.driemworks.app.activities.interfaces.OpenARActivityInterface;
import com.driemworks.ar.dto.CameraPoseDTO;
import com.driemworks.ar.services.MonocularVisualOdometryService;
import com.driemworks.common.utils.ImageConversionUtils;
import com.driemworks.common.utils.OpenCvUtils;
import com.driemworks.common.utils.TagUtils;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

/**
 * This fragment activity is an example of how to implement the monocular visual odometry service in an activity
 *
 * @author Tony Riemer
 */
public class MonocularVisualOdometryFragmentActivity extends AbstractARActivity implements CameraBridgeViewBase.CvCameraViewListener2, OpenARActivityInterface {

    /** The renderer */
    private GLSurfaceView.Renderer renderer;

    /** The monocular visual odometry service */
    private MonocularVisualOdometryService monocularVisualOdometryService;

    /** The camera pose */
    private CameraPoseDTO cameraPoseDTO;

    // TODO better way to manage state?
    private Mat previousFrameGray;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OpenCvUtils.initOpenCV(true);
        // keep the screen on!
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.opencv_layout);
        // attach the fragments
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // add opencv fragment
        transaction.add(new OpenCVFragment(), "OpenCVFragment");
        transaction.commit();

        monocularVisualOdometryService = new MonocularVisualOdometryService();
        cameraPoseDTO = new CameraPoseDTO();
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
        Mat mRgba = inputFrame.rgba();
        Mat mGray = inputFrame.gray();

        cameraPoseDTO = monocularVisualOdometryService.calculateCameraPose(cameraPoseDTO, mRgba, previousFrameGray, mGray);
        Log.d(TagUtils.getTag(this.getClass()), "translation: " + 10 * cameraPoseDTO.getX() + ", " + 10 * cameraPoseDTO.getY() + ", " + 10 * cameraPoseDTO.getZ());
        MatOfPoint2f keyPoints2f = ImageConversionUtils.convertMatOfKeyPointsTo2f(cameraPoseDTO.getFeatureData().getKeyPoint());

        // draw the tracked keypoints
        for (Point p : keyPoints2f.toList()) {
            Imgproc.circle(mRgba, p, 5, new Scalar(0, 255, 0));
        }

        previousFrameGray = mGray;
        return mRgba;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getOpenCVSurfaceViewId() {
        return R.id.opencv_surface_view;
    }
}
