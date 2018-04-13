package com.driemworks.app.opengl.renderer;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.driemworks.app.opengl.shapes.Triangle;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @author Tony
 */
public class MyGLRenderer implements GLSurfaceView.Renderer {

    /** The triangle */
    private Triangle triangle;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
//        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        triangle = new Triangle();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
//        GLES20.glViewport(0, 0, width, height);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDrawFrame(GL10 gl) {
        // Redraw background color
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        triangle.draw();
    }
}
