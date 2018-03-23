package com.driemworks.app.activities;

import android.os.Bundle;

import com.driemworks.app.R;
import com.driemworks.ar.dto.CameraPoseDTO;
import com.driemworks.ar.services.impl.MonocularVisualOdometryService;
import com.driemworks.common.enums.Resolution;
import com.driemworks.common.utils.ImageConversionUtils;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

/**
 * @author Tony
 */
public class CameraActivity extends OpenCVActivity {

    /** The monocular visual odometry service */
    private MonocularVisualOdometryService monocularVisualOdometryService;

    /** The resolution */
    private static final Resolution resolution = Resolution.RES_STANDARD;

    /** The camera pose */
    private CameraPoseDTO cameraPoseDTO;

    /** The previous frame (grayscale) */
    private Mat previousFrameGray;

    /** The do draw flag */
    private boolean doDraw = false;

    /**
     * The constructor for the CameraActivity
     */
    public CameraActivity() {
        super(R.layout.opencv_layout, resolution);
        monocularVisualOdometryService = new MonocularVisualOdometryService();
        cameraPoseDTO = new CameraPoseDTO();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause() {
        super.onPause();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        super.setmRgba(inputFrame.rgba());
        super.setmGray(inputFrame.gray());
        super.setOutput(super.getmRgba().clone());
        monocularVisualOdometryService.monocularVisualOdometry(cameraPoseDTO, super.getmRgba(), previousFrameGray, super.getmGray());
        MatOfPoint2f keyPoints2f = ImageConversionUtils.convertMatOfKeyPointsTo2f(cameraPoseDTO.getKeyPoints());

        if (doDraw) {
            for (Point p : keyPoints2f.toList()) {
                Imgproc.circle(super.getOutput(), p, 5, new Scalar(0, 255, 0));
            }
        }

        if (cameraPoseDTO != null) {
            Imgproc.circle(super.getOutput(), new Point(resolution.getWidth()/2, resolution.getHeight()/2),
                    (int) (Math.abs(cameraPoseDTO.getZ()) * 100), new Scalar(0, 255, 0));
        }

        afterCameraFrame();
        return super.getOutput();
    }

    /**
     * Execute after camera frame tasks
     */
    private void afterCameraFrame() {
        previousFrameGray = super.getmGray();
    }
}
