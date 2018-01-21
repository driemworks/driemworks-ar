package com.driemworks.simplecv.builders;

import android.app.Activity;

import com.driemworks.common.enums.Resolution;
import com.driemworks.common.views.CustomSurfaceView;

import org.opencv.android.CameraBridgeViewBase;

/**
 * Builder for a CustomSurfaceView object
 * @author Tony
 */
public class CustomSurfaceViewBuilder {

    /**
     * The custom surface view
     */
    private CustomSurfaceView customSurfaceView;

    /**
     * Constructor for the CustomSurfaceViewBuilder
     * @param activity The activity associated with the view
     * @param id The view id in the activity
     */
    public CustomSurfaceViewBuilder(Activity activity, int id) {
        customSurfaceView = (CustomSurfaceView) activity.findViewById(id);
    }

    /**
     * Set the CvCameraViewListener
     * @param cameraViewListener The CvCameraViewListener2
     * @return {@link CustomSurfaceViewBuilder}
     */
    public CustomSurfaceViewBuilder setCvCameraViewListener(
            CameraBridgeViewBase.CvCameraViewListener2 cameraViewListener) {
        customSurfaceView.setCvCameraViewListener(cameraViewListener);
        return this;
    }

    /**
     * Set the maximum frame size
     * @param resolution The Resolution to set
     * @return {@link CustomSurfaceViewBuilder}
     */
    public CustomSurfaceViewBuilder setMaxFrameSize(Resolution resolution) {
        customSurfaceView.setMaxFrameSize(resolution.getWidth(), resolution.getHeight());
        return this;
    }

    /**
     * Build the CustomSurfaceView
     * @return {@link CustomSurfaceView}
     */
    public CustomSurfaceView build() {
        return customSurfaceView;
    }

}
