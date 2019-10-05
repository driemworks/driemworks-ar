package graphics;

import android.opengl.GLSurfaceView;

import com.driemworks.common.graphics.AbstractOrientationRenderer;
import com.driemworks.common.sensor.orientationProvider.OrientationProvider;
import com.driemworks.common.sensor.representation.Quaternion;


import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES10.GL_COLOR_ARRAY;
import static android.opengl.GLES10.GL_PROJECTION;
import static android.opengl.GLES10.GL_VERTEX_ARRAY;
import static android.opengl.GLES10.glEnableClientState;
import static android.opengl.GLES10.glFrustumf;
import static android.opengl.GLES10.glLoadIdentity;
import static android.opengl.GLES10.glMatrixMode;
import static android.opengl.GLES10.glPopMatrix;
import static android.opengl.GLES10.glPushMatrix;
import static android.opengl.GLES10.glTranslatef;
import static android.opengl.GLES20.GL_DITHER;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDisable;
import static android.opengl.GLES20.glViewport;

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
     * Perform the actual rendering of the cube for each frame
     *
     * @param gl The surface on which the cube should be rendered
     */
    public void onDrawFrame(GL10 gl) {
        // clear screen
//        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
//
//        // set-up modelview matrix
//        gl.glMatrixMode(GL10.GL_MODELVIEW);
//        gl.glLoadIdentity();
//
//        if (showCubeInsideOut) {
//            float dist = 3;
//            gl.glTranslatef(0, 0, -dist);
//
//            if (orientationProvider != null) {
//                // All Orientation providers deliver Quaternion as well as rotation matrix.
//                // Use your favourite representation:
//
//                // Get the rotation from the current orientationProvider as rotation matrix
//                //gl.glMultMatrixf(orientationProvider.getRotationMatrix().getMatrix(), 0);
//
//                // Get the rotation from the current orientationProvider as quaternion
//                orientationProvider.getQuaternion(quaternion);
//                gl.glRotatef((float) (2.0f * Math.acos(quaternion.getW()) * 180.0f / Math.PI), quaternion.getX(), quaternion.getY(), quaternion.getZ());
//            }
//
//            // draw our object
//            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
//            gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
//
//            mCube.draw(gl);
//        } else {
//
//            if (orientationProvider != null) {
//                // All Orientation providers deliver Quaternion as well as rotation matrix.
//                // Use your favourite representation:
//
//                // Get the rotation from the current orientationProvider as rotation matrix
//                //gl.glMultMatrixf(orientationProvider.getRotationMatrix().getMatrix(), 0);
//
//                // Get the rotation from the current orientationProvider as quaternion
//                orientationProvider.getQuaternion(quaternion);
//                gl.glRotatef((float) (2.0f * Math.acos(quaternion.getW()) * 180.0f / Math.PI), quaternion.getX(), quaternion.getY(), quaternion.getZ());
//            }
//
//            float dist = 3;
//            drawTranslatedCube(gl, 0, 0, -dist);
//            drawTranslatedCube(gl, 0, 0, dist);
//            drawTranslatedCube(gl, 0, -dist, 0);
//            drawTranslatedCube(gl, 0, dist, 0);
//            drawTranslatedCube(gl, -dist, 0, 0);
//            drawTranslatedCube(gl, dist, 0, 0);
//        }
//
//        // draw our object
//        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
//        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
//
//        mCube.draw(gl);
    }

    /**
     * Draws a translated cube
     *
     * @param unused the surface
     * @param translateX x-translation
     * @param translateY y-translation
     * @param translateZ z-translation
     */
    private void drawTranslatedCube(GL10 unused, float translateX, float translateY, float translateZ) {
        glPushMatrix();
        glTranslatef(translateX, translateY, translateZ);

        // draw our object
        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_COLOR_ARRAY);

        mCube.draw(unused);
        glPopMatrix();
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

    /**
     * Flag indicating whether you want to view inside out, or outside in
     */
    private boolean showCubeInsideOut = true;

}
