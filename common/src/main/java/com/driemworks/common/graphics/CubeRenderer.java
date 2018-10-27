package com.driemworks.common.graphics;

import android.opengl.GLSurfaceView;

import com.driemworks.common.sensor.orientationProvider.OrientationProvider;
import com.driemworks.common.sensor.representation.Quaternion;


import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Class that implements the rendering of a cube with the current rotation of the device that is provided by an
 * OrientationProvider
 * 
 * @author Alexander Pacha
 * 
 */
public class CubeRenderer extends AbstractOrientationRenderer implements GLSurfaceView.Renderer {
    /**
     * The colour-cube that is drawn repeatedly
     */
    private Cube mCube;


    private Quaternion quaternion = new Quaternion();

    /**
     * Initialises a new CubeRenderer
     */
    public CubeRenderer() {
        mCube = new Cube();
    }

    public CubeRenderer(OrientationProvider orientationProvider) {
        setOrientationProvider(orientationProvider);
        mCube = new Cube();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDrawFrame(GL10 gl) {
        // clear screen
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

        // set-up modelview matrix
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();

        if (orientationProvider != null) {
            // All Orientation providers deliver Quaternion as well as rotation matrix.
            // Use your favourite representation:

            // Get the rotation from the current orientationProvider as rotation matrix
//                gl.glMultMatrixf(orientationProvider.getRotationMatrix().getMatrix(), 0);

            // Get the rotation from the current orientationProvider as quaternion
            orientationProvider.getQuaternion(quaternion);
            gl.glRotatef((float) (2.0f * Math.acos(quaternion.getW()) * 180.0f / Math.PI), quaternion.getY(), quaternion.getX(), quaternion.getZ());
        }

        float dist = 3;
        drawTranslatedCube(gl, 0, 0, -dist);
        drawTranslatedCube(gl, 0, 0, dist);
        drawTranslatedCube(gl, 0, -dist, 0);
        drawTranslatedCube(gl, 0, dist, 0);
        drawTranslatedCube(gl, -dist, 0, 0);
        drawTranslatedCube(gl, dist, 0, 0);

        // draw our object
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

        mCube.draw(gl);
    }

    /**
     * Draws a translated cube
     * 
     * @param gl the surface
     * @param translateX x-translation
     * @param translateY y-translation
     * @param translateZ z-translation
     */
    private void drawTranslatedCube(GL10 gl, float translateX, float translateY, float translateZ) {
        gl.glPushMatrix();
        gl.glTranslatef(translateX, translateY, translateZ);

        // draw our object
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

        mCube.draw(gl);
        gl.glPopMatrix();
    }

    /**
     * Update view-port with the new surface
     * 
     * @param gl the surface
     * @param width new width
     * @param height new height
     */
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // set view-port
        gl.glViewport(0, 0, width, height);
        // set projection matrix
        float ratio = (float) width / height;
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        gl.glDisable(GL10.GL_DITHER);
        gl.glClearColor(0, 0, 0, 0);
    }

}
