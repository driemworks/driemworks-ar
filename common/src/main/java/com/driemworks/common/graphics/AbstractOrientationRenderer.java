package com.driemworks.common.graphics;

import android.opengl.GLSurfaceView;

import com.driemworks.common.sensor.orientationProvider.OrientationProvider;
import com.driemworks.common.sensor.representation.Quaternion;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES10.GL_PROJECTION;
import static android.opengl.GLES10.glFrustumf;
import static android.opengl.GLES10.glLoadIdentity;
import static android.opengl.GLES10.glMatrixMode;
import static android.opengl.GLES20.GL_DITHER;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDisable;
import static android.opengl.GLES20.glViewport;

public abstract class AbstractOrientationRenderer implements GLSurfaceView.Renderer {

    protected OrientationProvider orientationProvider;

    public OrientationProvider getOrientationProvider() {
        return orientationProvider;
    }

    /**
     * Sets the orientationProvider of this renderer. Use this method to change which sensor fusion should be currently
     * used for rendering the cube. Simply exchange it with another orientationProvider and the cube will be rendered
     * with another approach.
     *
     * @param orientationProvider The new orientation provider that delivers the current orientation of the device
     */
    public void setOrientationProvider(OrientationProvider orientationProvider) {
        this.orientationProvider = orientationProvider;
    }

    /**
     * Update view-port with the new surface
     *
     * @param unused the surface
     * @param width new width
     * @param height new height
     */
    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // set view-port
        glViewport(0, 0, width, height);
        // set projection matrix
        float ratio = (float) width / height;
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glFrustumf(-ratio, ratio, -1, 1, 1, 10);
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        glDisable(GL_DITHER);
        glClearColor(0, 0, 0, 0);
    }
}
