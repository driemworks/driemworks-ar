package com.driemworks.simplecv.graphics.rendering;

import android.opengl.GLSurfaceView;
import android.util.Log;

import com.driemworks.common.utils.TagUtils;
import com.driemworks.sensor.services.OrientationService;
import com.driemworks.sensor.utils.OrientationUtils;
import com.driemworks.common.enums.Resolution;
import com.driemworks.simplecv.utils.RenderUtils;
import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.Object3D;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.World;
import com.threed.jpct.util.MemoryHelper;

import org.opencv.core.Rect;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @author Tony
 */
public class StaticCubeRenderer extends AbstractRenderer implements GLSurfaceView.Renderer {

    /**
     * The tag used for logging
     */
    private final String TAG = TagUtils.getTag(this.getClass());

    /**
     * The frame buffer
     */
    private FrameBuffer fb = null;

    /**
     * The world
     */
    private World world = null;

    /**
     * The light
     */
    private Light sun = null;

    private Camera cam;

    private float[] previousRotationVector = new float[3];
    private float[] deltaRotation = new float[3];

    /** The x camera coordinate */
    private float x = 0;

    /**
     * The y camera coordinate
     */
    private float y = 0;

    /**
     * The z camera coordinate.
     */
    private float z = 100;

    private int width;
    private int height;

    private Object3D cube;

    private boolean isRotationEnabled;

    /**
     * The default constructor
     */
    public StaticCubeRenderer(Resolution resolution, boolean isRotationEnabled) {
        super();
        this.width = resolution.getWidth();
        this.height = resolution.getHeight();
        this.isRotationEnabled = isRotationEnabled;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSurfaceChanged(GL10 g1, int w, int h) {
        Log.d(TAG, "surface changed");

        if (fb != null) {
            fb.dispose();
        }

        fb = new FrameBuffer(g1, w, h);
        initWorld();
    }

    /**
     * Initialize the world
     */
    private void initWorld() {
        world = new World();
        world.setAmbientLight(20, 30, 40);

        cube = RenderUtils.createCube(RGBColor.WHITE, 20, 0, 0, 300);
        cube.setVisibility(true);
        world.addObject(cube);

        // setup the camera
        cam = world.getCamera();
        cam.setPosition(0,0,0);
        cam.lookAt(new SimpleVector(0,0,100));

        sun = new Light(world);
        sun.setIntensity(255, 255, 255);
        SimpleVector sv = new SimpleVector();
        sv.set(cube.getTransformedCenter());
        sv.y = 0;
        sv.x = width / 2;
        sv.z += 300;
        sun.setPosition(sv);
        MemoryHelper.compact();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSurfaceCreated(GL10 g1, EGLConfig config) {
        g1.glDisable(GL10.GL_DITHER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDrawFrame(GL10 g1) {
        if (fb != null) {
            fb.clear();
            world.renderScene(fb);
            cam.setPosition(new SimpleVector(x, y, z));

            if (isRotationEnabled && super.getRotationVector() != null) {
                OrientationUtils.calcDeltaRotation(
                        1.0f, super.getRotationVector(), previousRotationVector, deltaRotation);
                updateRotation();
                OrientationUtils.copyVectors(super.getRotationVector(), previousRotationVector);
            }

            world.draw(fb);
            fb.display();
        }

        Log.d("Origin of cube: ","" + cube.getOrigin());
        Log.d("Position of camera: ","" + cam.getPosition());

    }

    /**
     * Update the camera rotation based on the calculated delta rotation vector
     */
    private void updateRotation() {
        cam.rotateCameraY(-deltaRotation[0]);
        cam.rotateCameraX(deltaRotation[1]);
        cam.rotateCameraZ(deltaRotation[2]);
    }

    /**
     * Getter for the x coordinate
     * @return x The x coordinate
     */
    public float getX() {
        return x;
    }

    /**
     * Setter for the x coordinate
     * @param x The x coordinate to set
     */
    public void setX(float x) {
        this.x = x;
    }

    /**
     * Getter for the y coordinate
     * @return y The y coordinate
     */
    public float getY() {
        return y;
    }

    /**
     * Setter for the y coordinate
     * @param y The y coordinate to set
     */
    public void setY(float y) {
        this.y = y;
    }

    /**
     * Getter for the z coordinate
     * @return z The z coordinate
     */
    public float getZ() {
        return z;
    }

    /**
     * Setter for the z coordinate
     * @param z The z coordinate to set
     */
    public void setZ(float z) {
        this.z = z;
    }


}