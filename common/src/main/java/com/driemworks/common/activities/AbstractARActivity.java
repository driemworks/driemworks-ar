package com.driemworks.common.activities;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.driemworks.common.graphics.AbstractOrientationRenderer;
import com.driemworks.common.utils.OpenCvUtils;

import org.opencv.android.CameraBridgeViewBase;

/**
 * @author Tony
 */
public abstract class AbstractARActivity extends FragmentActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    /** The gl surface view id */
    private int glSurfaceViewId;

    /** The opencv surface view id */
    private int openCVSurfaceViewId;

    private AbstractOrientationRenderer renderer;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OpenCvUtils.initOpenCV(true);
    }

    /**
     * Getter for the glSurfaceViewId
     * @return glSurfaceViewId The glSurfaceViewId
     */
    public int getGlSurfaceViewId() {
        return glSurfaceViewId;
    }

    /**
     * Setter for the glSurfaceViewId
     * @param glSurfaceViewId The glSurfaceViewId to set
     */
    public void setGlSurfaceViewId(int glSurfaceViewId) {
        this.glSurfaceViewId = glSurfaceViewId;
    }

    /**
     * Getter for the openCVSurfaceViewId
     * @return openCVSurfaceViewId The openCVSurfaceViewId
     */
    public int getOpenCVSurfaceViewId() {
        return openCVSurfaceViewId;
    }

    /**
     * Setter for the openCVSurfaceViewId
     * @param openCVSurfaceViewId The openCVSurfaceViewId to set
     */
    public void setOpenCVSurfaceViewId(int openCVSurfaceViewId) {
        this.openCVSurfaceViewId = openCVSurfaceViewId;
    }

    public AbstractOrientationRenderer getRenderer() {
        return renderer;
    }

    public void setRenderer(AbstractOrientationRenderer renderer) {
        this.renderer = renderer;
    }
}
