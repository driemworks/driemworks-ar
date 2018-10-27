package com.driemworks.common.builders;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.view.View;

/**
 * @author Tony
 */
public class GLSurfaceViewBuilder {

    /**
     * The GLSurfaceView
     */
    private GLSurfaceView glSurfaceView;

    public GLSurfaceViewBuilder(Activity activity, int id) {
        glSurfaceView = activity.findViewById(id);
    }

    /**
     * Build the GLSurfaceView
     * @return glSurfaceView the GLSurfaceView
     */
    public GLSurfaceView build() {
        return glSurfaceView;
    }

    /**
     * Set the EGLConfigChooser
     * @param redSize The red size
     * @param greenSize The green size
     * @param blueSize The blue size
     * @param alphaSize The alpha size
     * @param depthSize The depth size
     * @param stencilSize The stencil size
     * @return glSurfaceView the GLSurfaceView
     */
    public GLSurfaceViewBuilder setEGLConfigChooser(int redSize, int greenSize, int blueSize, int alphaSize,
                                             int depthSize, int stencilSize) {
        glSurfaceView.setEGLConfigChooser(
                redSize, greenSize, blueSize, alphaSize, depthSize, stencilSize);
        return this;
    }

    /**
     * Set the onTouchListener
     * @param listener The OnTouchListener
     * @return glSurfaceView the GLSurfaceView
     */
    public GLSurfaceViewBuilder setOnTouchListener(View.OnTouchListener listener) {
        glSurfaceView.setOnTouchListener(listener);
        return this;
    }

    /**
     * Set the renderer
     * @param renderer The renderer
     * @return glSurfaceView the GLSurfaceView
     */
    public GLSurfaceViewBuilder setRenderer(GLSurfaceView.Renderer renderer) {
        glSurfaceView.setRenderer(renderer);
        return this;
    }

    /**
     * Set the render mode
     * @param renderMode The render mode
     * @return glSurfaceView the GLSurfaceView

     */
    public GLSurfaceViewBuilder setRenderMode(int renderMode) {
        glSurfaceView.setRenderMode(renderMode);
        return this;
    }

    /**
     * Set the format
     * @param format The format
     * @return glSurfaceView the GLSurfaceView
     */
    public GLSurfaceViewBuilder setFormat(int format) {
        glSurfaceView.getHolder().setFormat(format);
        return this;
    }
}
