package com.driemworks.common.builders;

import android.app.Activity;
import android.view.View;

import com.driemworks.common.enums.Resolution;
import com.driemworks.common.views.OpenCVSurfaceView;

import org.opencv.android.CameraBridgeViewBase;

/**
 * Builder for a CustomSurfaceView object
 * @author Tony
 */
public class OpenCVSurfaceViewBuilder {

    /**
     * The custom surface view
     */
    private OpenCVSurfaceView openCVSurfaceView;

    /**
     * Constructor for the OpenCVSurfaceViewBuilder
     * @param activity The activity associated with the view
     * @param id The view id in the activity
     */
    public OpenCVSurfaceViewBuilder(Activity activity, int id) {
        openCVSurfaceView = (OpenCVSurfaceView) activity.findViewById(id);
        hideSystemUI();
    }

    public OpenCVSurfaceViewBuilder(Activity activity, int id, View.OnTouchListener onTouchListener) {
        openCVSurfaceView = (OpenCVSurfaceView) activity.findViewById(id);
        if (onTouchListener != null) {
            setOnTouchListener(onTouchListener);
        }
        hideSystemUI();
    }
    /**
     * Helper function used to hide the system bars.
     */
    public void hideSystemUI() {
        // Set the IMMERSIVE flag. Set the content to appear under the system bars so that the content doesn't resize when the system bars hide and show.
        openCVSurfaceView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    /**
     * Set the CvCameraViewListener
     * @param cameraViewListener The CvCameraViewListener2
     * @return {@link OpenCVSurfaceViewBuilder}
     */
    public OpenCVSurfaceViewBuilder setCvCameraViewListener(
            CameraBridgeViewBase.CvCameraViewListener2 cameraViewListener) {
        openCVSurfaceView.setCvCameraViewListener(cameraViewListener);
        return this;
    }

    /**
     * Set the maximum frame size
     * @param resolution The Resolution to set
     * @return {@link OpenCVSurfaceViewBuilder}
     */
    public OpenCVSurfaceViewBuilder setMaxFrameSize(Resolution resolution) {
        openCVSurfaceView.setMaxFrameSize(resolution.getWidth(), resolution.getHeight());
        return this;
    }

    /**
     * Set the on touch listener
     * @param listener The OnTouchListener
     * @return {@link OpenCVSurfaceViewBuilder}
     */
    public OpenCVSurfaceViewBuilder setOnTouchListener(View.OnTouchListener listener) {
        openCVSurfaceView.setOnTouchListener(listener);
        return this;
    }

    /**
     * Build the CustomSurfaceView
     * @return {@link OpenCVSurfaceView}
     */
    public OpenCVSurfaceView build() {
        return openCVSurfaceView;
    }

}
