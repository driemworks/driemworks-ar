package graphics;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

import com.driemworks.common.graphics.AbstractOrientationRenderer;
import com.driemworks.common.sensor.orientationProvider.OrientationProvider;
import com.driemworks.common.sensor.representation.Quaternion;

import java.util.Arrays;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES10.glClear;
import static android.opengl.GLES10.glClearColor;
import static android.opengl.GLES10.glDisable;
import static android.opengl.GLES10.glEnableClientState;
import static android.opengl.GLES10.glFrustumf;
import static android.opengl.GLES10.glLoadIdentity;
import static android.opengl.GLES10.glMatrixMode;
import static android.opengl.GLES10.glPopMatrix;
import static android.opengl.GLES10.glPushMatrix;
import static android.opengl.GLES10.glRotatef;
import static android.opengl.GLES10.glTranslatef;
import static android.opengl.GLES10.glViewport;
import static javax.microedition.khronos.opengles.GL10.GL_COLOR_ARRAY;
import static javax.microedition.khronos.opengles.GL10.GL_COLOR_BUFFER_BIT;
import static javax.microedition.khronos.opengles.GL10.GL_DITHER;
import static javax.microedition.khronos.opengles.GL10.GL_MODELVIEW;
import static javax.microedition.khronos.opengles.GL10.GL_PROJECTION;
import static javax.microedition.khronos.opengles.GL10.GL_VERTEX_ARRAY;

/**
 * Class that implements the rendering of a cube with the current rotation of the device that is provided by an
 * OrientationProvider
 *
 * @author Alexander Pacha
 */
public class CubeRenderer extends AbstractOrientationRenderer implements GLSurfaceView.Renderer {
	/**
	 * The colour-cube that is drawn repeatedly
	 */
//	private Cube mCube;
	private Cube2 mCube2;

	private Quaternion quaternion = new Quaternion();

	Context context;
	/**
	 * Initialises a new CubeRenderer
	 * @param context
	 */
	public CubeRenderer(Context context) {
//		mCube = new Cube();
//-----------------------------------
		this.context = context;
	}

	/**
	 * Flag indicating whether you want to view inside out, or outside in
	 */
	private boolean showCubeInsideOut = true;

	/**
	 * Sets the orientationProvider of this renderer. Use this method to change which sensor fusion should be currently
	 * used for rendering the cube. Simply exchange it with another orientationProvider and the cube will be rendered
	 * with another approach.
	 *
	 * @param orientationProvider The new orientation provider that delivers the current orientation of the device
	 */
	@Override
	public void setOrientationProvider(OrientationProvider orientationProvider) {
		this.orientationProvider = orientationProvider;
	}

	// vPMatrix is an abbreviation for "Model View Projection Matrix"
	private final float[] vPMatrix = new float[16];
	private final float[] projectionMatrix = new float[16];
	private final float[] viewMatrix = new float[16];
	private float[] rotationMatrix = new float[16];
	private float angle = 0f;
	/**
	 * Perform the actual rendering of the cube for each frame
	 *
	 * @param gl10 The surface on which the cube should be rendered
	 */
	@Override
	public void onDrawFrame(GL10 gl10) {
//		// clear screen
//		glClear(GL_COLOR_BUFFER_BIT);
//
//		// set-up modelview matrix
//		glMatrixMode(GL_MODELVIEW);
//		glLoadIdentity();
//
//		if (showCubeInsideOut) {
//			float dist = 3;
//			glTranslatef(0, 0, -dist);
//
//			if (orientationProvider != null) {
//				// All Orientation providers deliver Quaternion as well as rotation matrix.
//				// Use your favourite representation:
//
//				// Get the rotation from the current orientationProvider as rotation matrix
//				//gl.glMultMatrixf(orientationProvider.getRotationMatrix().getMatrix(), 0);
//
//				// Get the rotation from the current orientationProvider as quaternion
//				orientationProvider.getQuaternion(quaternion);
//				glRotatef((float) (2.0f * Math.acos(quaternion.getW()) * 180.0f / Math.PI), quaternion.getX(), quaternion.getY(), quaternion.getZ());
//			}
//
//			// draw our object
//			glEnableClientState(GL_VERTEX_ARRAY);
//			glEnableClientState(GL_COLOR_ARRAY);
//
//			mCube.draw(gl10);
//		} else {
//
//			if (orientationProvider != null) {
//				// All Orientation providers deliver Quaternion as well as rotation matrix.
//				// Use your favourite representation:
//
//				// Get the rotation from the current orientationProvider as rotation matrix
//				//gl.glMultMatrixf(orientationProvider.getRotationMatrix().getMatrix(), 0);
//
//				// Get the rotation from the current orientationProvider as quaternion
//				orientationProvider.getQuaternion(quaternion);
//				glRotatef((float) (2.0f * Math.acos(quaternion.getW()) * 180.0f / Math.PI), quaternion.getX(), quaternion.getY(), quaternion.getZ());
//			}
//
//			float dist = 3;
//			drawTranslatedCube(gl10, 0, 0, -dist);
//			drawTranslatedCube(gl10, 0, 0, dist);
//			drawTranslatedCube(gl10, 0, -dist, 0);
//			drawTranslatedCube(gl10, 0, dist, 0);
//			drawTranslatedCube(gl10, -dist, 0, 0);
//			drawTranslatedCube(gl10, dist, 0, 0);
//		}
//
//		// draw our object
//		glEnableClientState(GL_VERTEX_ARRAY);
//		glEnableClientState(GL_COLOR_ARRAY);
//
//		mCube.draw(gl10);
//---------------------------------
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		//Camera position
		// Set the camera position (View matrix)
		long time = SystemClock.uptimeMillis() % 4000L;
		angle = ((float) (2*Math.PI)/ (float) 4000) * ((int) time);
		Matrix.setLookAtM(viewMatrix, 0, (float) (3*Math.sin(angle)), 0, (float) (3.0f*Math.cos(angle)),     0 ,0, 0,     0f, 1.0f, 0.0f);
// 		Without fake angle
//		Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, -4f, 0f, 0f, 0f, 0f, 1f, 0f);
		// projection x view = modelView
		Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

		//Creating rotation matrix
		Matrix.setRotateM(rotationMatrix, 0, angle, 0f, 0f, -1f);
		//rotation x camera = modelView
		float[] duplicateMatrix = Arrays.copyOf(vPMatrix, 16);

		//rotation x camera = modelView
		Matrix.multiplyMM(vPMatrix, 0, duplicateMatrix, 0, rotationMatrix, 0);
		Matrix.setRotateM(rotationMatrix, 0, angle, 0f, -1f, 0f);
		duplicateMatrix = Arrays.copyOf(vPMatrix, 16);
		Matrix.multiplyMM(vPMatrix, 0, duplicateMatrix, 0, rotationMatrix, 0);
		Matrix.setRotateM(rotationMatrix, 0, angle, -1f, 0f, 0f);
		duplicateMatrix = Arrays.copyOf(vPMatrix, 16);
		Matrix.multiplyMM(vPMatrix, 0, duplicateMatrix, 0, rotationMatrix, 0);

		// Draw shape
		mCube2.draw(vPMatrix);
	}

	/**
	 * Draws a translated cube
	 *
	 * @param gl10
	 * @param translateX x-translation
	 * @param translateY y-translation
	 * @param translateZ z-translation
	 */
	private void drawTranslatedCube(GL10 gl10, float translateX, float translateY, float translateZ) {
		glPushMatrix();
		glTranslatef(translateX, translateY, translateZ);

		// draw our object
		glEnableClientState(GL_VERTEX_ARRAY);
		glEnableClientState(GL_COLOR_ARRAY);

//		mCube.draw(gl10);
		glPopMatrix();
	}

	/**
	 * Update view-port with the new surface
	 *
	 * @param unused the surface
	 * @param width  new width
	 * @param height new height
	 */
	@Override
	public void onSurfaceChanged(GL10 unused, int width, int height) {
//		// set view-port
//		glViewport(0, 0, width, height);
//		// set projection matrix
//		float ratio = (float) width / height;
//		glMatrixMode(GL_PROJECTION);
//		glLoadIdentity();
//		glFrustumf(-ratio, ratio, -1, 1, 1, 10);
//----------------------
		GLES20.glViewport(0, 0, width, height);
		float ratio = (float) width / height;
		// this projection matrix is applied to object coordinates
		// in the onDrawFrame() method
		Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
	}

	@Override
	public void onSurfaceCreated(GL10 unused, EGLConfig config) {
		glDisable(GL_DITHER);
		glClearColor(0, 0, 0, 0);
		mCube2 = new Cube2(context);

	}
}


