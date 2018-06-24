package com.driemworks.app.activities.base;

import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;

import com.driemworks.app.builders.GLSurfaceViewBuilder;
import com.driemworks.common.enums.Resolution;

/**
 * This class extends the AbstractOpenCVActivity
 * and sets up the GLSurface view that will be used to
 *  1) register the on touch listener
 *  2) visualize 3d graphics over the current camera frame
 * @author Tony
 */
public abstract class AbstractVRActivity extends AbstractOpenCVActivity implements View.OnTouchListener {

    /** The GL Surface View */
    private GLSurfaceView glSurfaceView;

    /** The renderer */
    private final GLSurfaceView.Renderer renderer;

    /** The glSurfaceView id */
    private final int glSurfaceViewId;

    /**
     * Constructor for the AbstractVRActivity
     * @param layoutResId The layout resource id
     * @param openCVSurfaceViewId The opencv surface view id
     * @param glSurfaceViewId The gl surface view id
     * @param renderer The renderer
     * @param resolution The resolution
     */
    public AbstractVRActivity(int layoutResId, int openCVSurfaceViewId, int glSurfaceViewId,
                              GLSurfaceView.Renderer renderer, Resolution resolution, boolean implementOnTouch) {
        super(layoutResId, openCVSurfaceViewId, resolution, implementOnTouch);
        this.glSurfaceViewId = glSurfaceViewId;
        this.renderer = renderer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        glSurfaceView = new GLSurfaceViewBuilder(this, glSurfaceViewId)
                .setEGLConfigChooser(8, 8, 8, 8, 16, 0)
                .setOnTouchListener(this)
                .setRenderer(renderer)
                .setFormat(PixelFormat.TRANSLUCENT)
                .setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY)
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();
        glSurfaceView.onResume();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause() {
        super.onPause();
        glSurfaceView.onPause();
    }

}
