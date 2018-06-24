package com.driemworks.app.activities;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import com.driemworks.app.R;
import com.driemworks.app.activities.base.AbstractOpenCVActivity;
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
public class CameraActivity extends AbstractOpenCVActivity {

    /** The resolution */

    /** The previous frame (grayscale) */
    private Mat previousFrameGray;

    /** The do draw flag */
    private boolean doDraw = false;

    /**
     * The constructor for the CameraActivity
     */
    public CameraActivity() {
        super(R.layout.opencv_layout, R.id.main_surface_view, Resolution.RES_STANDARD, false);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }
}
