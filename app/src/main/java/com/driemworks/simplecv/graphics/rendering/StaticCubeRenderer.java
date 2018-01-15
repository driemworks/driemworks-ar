package com.driemworks.simplecv.graphics.rendering;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.driemworks.common.utils.TagUtils;
import com.driemworks.sensor.utils.OrientationUtils;
import com.driemworks.simplecv.activities.CubeActivity;
import com.driemworks.common.enums.Resolution;
import com.driemworks.simplecv.enums.Tags;
import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.World;
import com.threed.jpct.util.MemoryHelper;

import org.opencv.core.Rect;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
     * The current system time (in ms)
     */
    private long time = System.currentTimeMillis();

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

    /** The point to which the cube will be moved */
    private float x = 0;
    private float y = 0;
    private float z = 100;

    private Rect surface;

    private int width = Resolution.RES_STANDARD.getWidth();
    private int height = Resolution.RES_STANDARD.getHeight();

    public void setPreviousRotationVector(float[] previousRotationVector) {
        this.previousRotationVector = previousRotationVector;
    }

    private Object3D cube;

    /**
     * The default constructor
     */
    public StaticCubeRenderer() {
        super();
    }

    /**
     * Instantiates the world and objects in it
     *
     * @param g1
     * @param w
     * @param h
     */
    public void onSurfaceChanged(GL10 g1, int w, int h) {
        Log.d(TAG, "surface changed");

        if (fb != null) {
            fb.dispose();
        }

        fb = new FrameBuffer(g1, w, h);
//        if (master == null) {
            initWorld();
//        }
    }

    /**
     * Initialize the world
     */
    private void initWorld() {
        world = new World();
        world.setAmbientLight(20, 30, 40);

        cube = createCube(RGBColor.WHITE, 20, 0, 0, 300);
        cube.setVisibility(true);
        world.addObject(cube);

        // setup the camera
        cam = world.getCamera();
        // the camera should be positioned at the "center" of the world
        cam.setPosition(0,0,0);
//        cam.lookAt(new SimpleVector(width / 2, height / 2, 300));
        cam.lookAt(new SimpleVector(0,0,100));
        sun = new Light(world);
        sun.setIntensity(255, 255, 255);
        // setup the sun
        SimpleVector sv = new SimpleVector();
        sv.set(cube.getTransformedCenter());
        sv.y = 0;
        sv.x = width / 2;
        sv.z += 300;
        sun.setPosition(sv);
        MemoryHelper.compact();
    }

    public void onSurfaceCreated(GL10 g1, EGLConfig config) {
        g1.glDisable(GL10.GL_DITHER);
    }

    private AtomicInteger frameCounter = new AtomicInteger(0);

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDrawFrame(GL10 g1) {
        frameCounter.incrementAndGet();
        if (fb != null) {
            fb.clear();
            world.renderScene(fb);
            // for the moment, we will assume that camera motion occurs within a plane
            cam.setPosition(new SimpleVector(x, y, z));
            // offset with the cube

            cam.lookAt(new SimpleVector(x, y, 300 + z));
            //            cam.setPosition(new SimpleVector(cam.getPosition().x, cam.getPosition().y,
//                    getSurfaceDataDTO().getzCoordinate()));
//            updateOriginSurface();
//            updateVectors();
//
//            if (super.getRotationVector() != null) {
//                OrientationUtils.calcDeltaRotation(MULTIPLIER, super.getRotationVector(), previousRotationVector, deltaRotation);
//                Log.i(TAG, "Calculated rotation delta rotation: " + deltaRotation);
//                if (deltaRotation != null) {
//                    updateRotation(deltaRotation);
//                }
//            }


            world.draw(fb);
            fb.display();
        }

        Log.d("Origin of cube: ","" + cube.getOrigin());
        Log.d("Position of camera: ","" + cam.getPosition());
        Log.d("Direction of camera: ", "" + cam.getDirection());
    }

    private void updateRotation(float[] rotation) {
        cam.rotateCameraY(-rotation[0]);
        cam.rotateCameraX(rotation[1]);
        cam.rotateCameraZ(rotation[2]);
    }

    private float calcEuclideanDistanceX(SimpleVector vec1, SimpleVector vec2) {
        return (float)(Math.sqrt(Math.pow(Math.abs(vec1.x - vec2.x), 2)));
    }

    private float calcEuclideanDistance(SimpleVector vec1, SimpleVector vec2) {
        return (float)(Math.sqrt(
                Math.pow(Math.abs(vec1.x - vec2.x), 2)
                        + Math.pow(Math.abs(vec1.y - vec2.y), 2)
                        + Math.pow(Math.abs(vec1.z - vec2.z), 2)));
    }

    private Object3D createCube(RGBColor color, float scale, float y, float x, float z) {
        Object3D cube = Primitives.getCube(scale);
        cube.setAdditionalColor(color);
        cube.setOrigin(new SimpleVector(x, y, z));
        return cube;
    }


    private void updateVectors() {
        if (super.getRotationVector() != null) {
            System.arraycopy(super.getRotationVector(), 0, previousRotationVector, 0, 3);
        }
    }

    /** The rate at which spheres are generated */
    private static final int objectCreationRate = 67;

    /**
     *
     */
    private void updateOriginSurface() {
        if (null != super.getSurfaceDataDTO()) {
            Log.d(TAG, "Attempting to find points to update currentCube position");
            // get the detected surface
            if (null != super.getSurfaceDataDTO().getBoundRect()) {
                Log.d(TAG, "bounding rect found. getting points");
                surface = super.getSurfaceDataDTO().getBoundRect();
                // set the currentCube position
                if (surface.area() > 5) {
                    x = (float) surface.tl().x + 90;
                    y = (float) surface.tl().y + 90;
                    z = super.getSurfaceDataDTO().getzCoordinate();
                    Log.d("*****", "calculated z: "+ z);

                    cube.setOrigin(new SimpleVector(x, y, z));
                    Log.d(TAG, "Updating currentCube position with coordinate x = " + x + ", y = " + y + ", z = " + z);
                }
            }
        }
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }
}