package com.driemworks.simplecv.graphics.rendering;

import android.opengl.GLSurfaceView;
import android.util.Log;

import com.driemworks.simplecv.enums.Resolution;
import com.driemworks.simplecv.graphics.EightCube;
import com.driemworks.simplecv.utils.RandomUtils;
import com.driemworks.simplecv.utils.RenderUtils;
import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.Object3D;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.World;
import com.threed.jpct.util.MemoryHelper;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * The renderer used for a simple cube-based game
 */
public class CubeGameRenderer extends AbstractRenderer implements GLSurfaceView.Renderer {

    private World world;
    private Light sun;
    private Camera cam;
    private EightCube superCube;

    private FrameBuffer fb = null;

    private final float width = Resolution.RES_STANDARD.getWidth()/2;
    private final float height = Resolution.RES_STANDARD.getHeight()/2;

    private Mat mRgba;

    private float detectedX;
    private float detectedY;
    private float detectedZ;
    private static final float assumedZ = 275;

    private boolean isTranslated = false;
    private boolean isLocked;

    private static long startTime;


    public CubeGameRenderer() {
        startTime = System.currentTimeMillis();
        init();
    }

    private void init() {
        world = new World();
        world.setAmbientLight(20, -30, 40);

        SimpleVector center = new SimpleVector(width/2, height/2, 200);
        superCube = new EightCube(20, center, RGBColor.RED);
        superCube.addToWorld(world);

        // setup the camera
        cam = world.getCamera();
        // the camera should be positioned at the "center" of the world
        cam.setPosition(width / 2, height / 2, -200);
        cam.lookAt(new SimpleVector(width / 2, height / 2, 300));

        sun = new Light(world);
        sun.setIntensity(255, 255, 255);
        // setup the sun
        SimpleVector sv = new SimpleVector();
        sv.set(center);
        sv.y = 0;
        sv.x = width / 2;
        sv.z += 300;
        sun.setPosition(sv);
        MemoryHelper.compact();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {}

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        fb = RenderUtils.updateFrameBuffer(fb, gl, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (fb != null) {
            fb.clear();
            world.renderScene(fb);
            updateObjects();
            world.draw(fb);
            fb.display();
        }
    }

    private boolean updatedStartTime = false;

    private float elapsedTime;

    /**
     *
     */
    private void updateObjects() {
        if (super.getSurfaceDataDTO() != null) {
            if (super.getSurfaceDataDTO().getHullPoints() != null) {
                updateDetectedPoints(super.getSurfaceDataDTO().getBoundRect());
                //updateDetectedPoints(surfaceDataDTO.getHullPoints());
                // if less than the specified number of seconds has passed
                // this will be the behavior
                if (!isTranslated) {
                    if ((System.currentTimeMillis() - startTime) < 10000) {
                        superCube.updateOrigins(new SimpleVector(detectedX, detectedY, detectedZ),
                                superCube.getScale());
                    } else if ((System.currentTimeMillis() - startTime) >= 10000
                            && (System.currentTimeMillis() - startTime) < 10500) {
                        superCube.getCube1().translate(RandomUtils.decideFloatInRange(-3, 3),
                                                       RandomUtils.decideFloatInRange(-3, 3),
                                                       RandomUtils.decideFloatInRange(-3, 3));
                    } else {
                        superCube.getCube1().setAdditionalColor(RGBColor.BLUE);
                        isTranslated = true;
                    }
                } else {
                    if (!isLocked) {
                        superCube.getCube1().setOrigin(new SimpleVector(
                                detectedX, detectedY, detectedZ
                        ));
                        // if the moved cube is near enough the center
                        // then it snaps back into position
                        if (RenderUtils.calcEuclideanDistanceX(superCube.getCenter(),
                                superCube.getCube1().getOrigin()) < 2) {
                            isLocked = true;
                            superCube.getCube1().setOrigin(
                                    superCube.getCenter().calcAdd(new SimpleVector(
                                            -superCube.getScale(),
                                            -2*superCube.getScale(),
                                            -superCube.getScale())));
                            superCube.updateColor(RGBColor.BLUE);
                        }
                    } else {
                        if (!updatedStartTime) {
                            startTime = System.currentTimeMillis();
                            updatedStartTime = true;
                        } else {
                            elapsedTime = System.currentTimeMillis() - startTime;
                            if (System.currentTimeMillis() - startTime < 10000) {
                                // move super cube around
                                superCube.updateOrigins(
                                        new SimpleVector(detectedX, detectedY, detectedZ),
                                        superCube.getScale());
                            } else if (elapsedTime >= 10000 && elapsedTime < 12000) {
                                // scatter the cubes (but not the center)
                                for (Object3D obj : superCube.getCubes()) {
                                    obj.translate(RandomUtils.decideFloatInRange(-5, 5),
                                            RandomUtils.decideFloatInRange(-5, 5),
                                            RandomUtils.decideFloatInRange(-5, 5));
                                }
                            } else {
                                superCube.getCenterSphere().setOrigin(
                                        new SimpleVector(detectedX, detectedY, detectedZ));
                                if (elapsedTime > 12000) {
                                    for (Object3D obj : superCube.getCubes()) {
                                        Log.d("calcEucDistance2D: ", RenderUtils
                                                .calcEuclideanDistance2D(superCube.getCenterSphere()
                                                        .getOrigin(), obj.getOrigin()) + "");
                                        if (RenderUtils.calcEuclideanDistance2D(superCube
                                            .getCenterSphere().getOrigin(), obj.getOrigin()) < 10) {
                                            obj.setAdditionalColor(RGBColor.BLACK);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Update the detected points based on the rect
     * @param rect the Rect
     */
    private void updateDetectedPoints(Rect rect) {
        detectedX = (float)rect.tl().x - 50;
        detectedY = (float) rect.tl().y;
        detectedZ = assumedZ - (float) rect.area()/600;
        Log.d("detected z: ", detectedZ + "");
    }

    public void setmRgba(Mat mRgba) {
        this.mRgba = mRgba;
    }

    public EightCube getSuperCube() {
        return superCube;
    }

    public void setSuperCube(EightCube superCube) {
        this.superCube = superCube;
    }
}
