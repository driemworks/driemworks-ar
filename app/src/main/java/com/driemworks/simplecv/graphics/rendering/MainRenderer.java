package com.driemworks.simplecv.graphics.rendering;

import android.opengl.GLSurfaceView;
import android.util.Log;

import com.driemworks.ar.utils.HandDetectionUtils;
import com.driemworks.common.dto.ConfigurationDTO;
import com.driemworks.common.dto.SurfaceDataDTO;
import com.driemworks.sensor.utils.OrientationUtils;
import com.driemworks.simplecv.activities.MainActivity;
import com.driemworks.simplecv.enums.ColorsEnum;
import com.driemworks.simplecv.enums.IntentIdentifer;
import com.driemworks.common.enums.Resolution;
import com.driemworks.simplecv.graphics.EightCube;
import com.driemworks.simplecv.utils.RandomUtils;
import com.driemworks.simplecv.utils.RenderUtils;
import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.World;
import com.threed.jpct.util.MemoryHelper;

import org.opencv.core.Point;
import org.opencv.core.Rect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Tony on 7/11/2017.
 */
public class MainRenderer extends AbstractRenderer implements GLSurfaceView.Renderer {

    private World world;
    private Light sun;
    private Camera cam;
    private FrameBuffer fb = null;

    private SurfaceDataDTO surfaceDataDTO;

    private final float width = Resolution.RES_STANDARD.getWidth()/2;
    private final float height = Resolution.RES_STANDARD.getHeight()/2;

    private Object3D cube1;
    private static Object3D cube2;

    private static Object3D laserBeam;

    private Object3D cube3;

    private Object3D trackingSphere;

    private Object3D[] objects = new Object3D[3];

    private float[] deltaRotation = new float[3];
    private float[] previousRotationVector = new float[3];

    private static final float MULTIPLIER = 2.7f;

    private float detectedX;
    private float detectedY;
    private float detectedZ;
    private float assumedZ = 100;

    private boolean isInit = true;

    private long startTime;
    private long elapsedTime = 0L;

    private Map<Object3D, Long> touchTimeMap;

    private MainActivity activity;

    private boolean isTranslated = false;
    private boolean isLocked = false;
    private boolean updatedStartTime = false;

    private boolean doMoveCube2 = false;

    private boolean isMenuInitialized = true;

    private long prevFrameTime = 0L;

    private boolean cubeGameMode = false;
    private boolean isCubeGameInit = false;

    private EightCube superCube;

    /**
     * Constructor for the menu renderer
     * @param activity the activity to which the renderer is associated
     */
    public MainRenderer(MainActivity activity) {
        this.activity = activity;
        init();
        startTime = System.currentTimeMillis();
        touchTimeMap = new HashMap<>();
        touchTimeMap.put(cube1, 0L);
        touchTimeMap.put(cube2, 0L);
        touchTimeMap.put(cube3, 0L);
    }

    /**
     * Init the world for the menu
     */
    private void init() {
        world = new World();
        world.setAmbientLight(20, -30, 40);

        cube1 = Primitives.getCube(20);
        cube1.setAdditionalColor(RGBColor.RED);
        cube1.setOrigin(new SimpleVector(width/2 - 130, height/2, 200));
        world.addObject(cube1);

        cube2 = Primitives.getCube(20);
        cube2.setAdditionalColor(RGBColor.BLUE);
        cube2.setOrigin(new SimpleVector(width/2, height/2, 200));
        world.addObject(cube2);

        laserBeam = cube2;

        cube3 = Primitives.getCube(20);
        cube3.setAdditionalColor(RGBColor.RED);
        cube3.setOrigin(new SimpleVector(width/2 + 130, height/2, 200));
        world.addObject(cube3);

        trackingSphere = Primitives.getSphere(10);
        trackingSphere.setAdditionalColor(RGBColor.WHITE);
        world.addObject(trackingSphere);

        objects[0] = cube1;
        objects[1] = cube2;
        objects[2] = cube3;

        cam = world.getCamera();
        cam.setPosition(width / 2, height / 2, -200);
        //cam.lookAt(new SimpleVector(width/2 + 20, height/2 + 20, 300));
        cam.lookAt(cube2.getTransformedCenter());

        sun = new Light(world);
        sun.setIntensity(255, 255, 255);
        // setup the sun
        SimpleVector sv = new SimpleVector();
        sv.set(cube2.getTransformedCenter());
        sv.y = 0;
        sv.x = width / 2;
        sv.z += 300;
        sun.setPosition(sv);
        MemoryHelper.compact();

        doRotateCam = false;
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
            if (isInit) {
                isInit = false;
                deltaRotation = new float[]{0,0,0};
            } else if (doRotateCam) {
                if (super.getRotationVector() != null) {
                    OrientationUtils.calcDeltaRotation(MULTIPLIER, super.getRotationVector(), previousRotationVector, deltaRotation);
                    if (deltaRotation != null) {
                        RenderUtils.updateRotation(cam, deltaRotation);
                    }
                }
            }
//            updateVectors();
            updateObjects();
            world.draw(fb);
            fb.display();
        }
    }

    /**
     *
     */
    private void updateVectors() {
        if (super.getRotationVector() != null && previousRotationVector != null) {
            System.arraycopy(super.getRotationVector(), 0, previousRotationVector, 0, 3);
        }
    }

    /**
     *
     */
    private void initCubeGame() {
        doRotateCam = false;
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

        startTime = System.currentTimeMillis();
    }

    /**
     * Update objects displayed on screen
     */
    private void updateObjects() {
        if (cubeGameMode) {
            if (!isCubeGameInit && super.isTouch()) {
                super.setTouch(false);
                isMenuInitialized = false;
                worldInitializer.run();
                activity.getLayoutManager().setMenu(false);
            } else {
                updateCubeGame();
            }
        } else {
            if (!isMenuInitialized) {
                isCubeGameInit = false;
                menuInitializer.run();
                activity.getLayoutManager().setMenu(true);
            } else {
                updateMenu();
            }
        }
    }

    /**
     * Thread to initialize the world (for the cube game)
     */
    private Runnable worldInitializer = new Runnable() {
        @Override
        public void run() {
            initCubeGame();
            isCubeGameInit = true;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * Thread to initialize the menu
     */
    private Runnable menuInitializer = new Runnable() {
        @Override
        public void run() {
            init();
            isMenuInitialized = true;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    private AtomicInteger multiplier = new AtomicInteger(0);
    /**
     *
     */
    private boolean updateLaser(Object3D laserBeam) {
        laserBeam.setAdditionalColor(RGBColor.RED);
        if (isTouch()) {
            SimpleVector translationVector = cam.getDirection().normalize();
            translationVector.scalarMul(10);
            laserBeam.translate(translationVector);
            if (multiplier.incrementAndGet() == 10) {
                multiplier.set(0);
            } else {
                return true;
            }
        } else {
            resetLaser(surfaceDataDTO.getBoundRect(), cube2);
        }
        return false;
    }

    private Runnable laserAnimationThread = new Runnable() {
        @Override
        public void run() {
            synchronized (laserBeam) {
                long startTime = System.currentTimeMillis();
                long elapsedTime = 0;
                SimpleVector dir;
                while (elapsedTime < 5000) {
                    dir = cam.getDirection().normalize();
                    dir.scalarMul(5);
                    laserBeam.translate(dir);
                    elapsedTime = System.currentTimeMillis() - startTime;
                }
                laserBeam.notify();
            }
        }
    };

    private Runnable updateLaserBeam = new Runnable() {
        @Override
        public void run() {
            synchronized (laserBeam) {

            }
        }
    };

    /**
     *
     * @param laserBeam
     * @param doShoot
     */
    private boolean updateLaserBeam(Object3D laserBeam, boolean doShoot) {
        if (doShoot) {
            laserAnimationThread.run();
            return true;
        }
        return false;
    }

    private void resetLaser(Rect boundRect, Object3D cube2) {
        updateDetectedPointGunTip(boundRect);
        cube2.setOrigin(new SimpleVector(detectedX, detectedY, detectedZ));

    }

    private static List<Object3D> bullets = new ArrayList<>();
    static {
        bullets.add(cube2);
    }


    /**
     * Update the main menu
     */
    private void updateMenu() {
        if (surfaceDataDTO != null) {
            if (surfaceDataDTO.getBoundRect() != null) {
                updateDetectedPoints(surfaceDataDTO.getHullPoints(), surfaceDataDTO.getBoundRect(), assumedZ);
                trackingSphere.setOrigin(new SimpleVector(detectedX, detectedY, 200));
                elapsedTime = System.currentTimeMillis() - startTime;
                if (doMoveCube2) {
                    // switch to the laser activity
                    Map<String, ConfigurationDTO> configMap = new HashMap<>();
                    configMap.put(IntentIdentifer.CONFIG_DTO.getValue(), activity.getConfigurationDTO());
                } else {
                    if (!cube1.getVisibility() && !cube3.getVisibility()) {
                        updateVisibility(true, cube1, cube3);
                        doRotateCam = true;
                    }

                    for (Object3D obj : objects) {
                        if (RenderUtils.calcEuclideanDistanceX(new SimpleVector(
                                detectedX, detectedY, detectedZ), obj.getOrigin()) < 18) {
                            if (obj.equals(cube1) && isTouch()) {
                                // next frame will init cube game
                                cubeGameMode = true;
                                return;
                            } else if (obj.equals(cube2)) {
                                if (isTouch()) {
                                    doMoveCube2 = true;
                                    doRotateCam = false;
                                    setTouch(false);
                                    updateVisibility(false, cube1, cube3);
                                }
                            }
                            obj.setAdditionalColor(ColorsEnum.YELLOW_ACTIVE.getColor());
                        } else {
                            if (obj.equals(cube2)) {
                                obj.setAdditionalColor(RGBColor.BLUE);
                            } else {
                                obj.setAdditionalColor(RGBColor.RED);
                            }
                        }
                    }
                }
                prevFrameTime = elapsedTime;
            }
        }
    }

    /**
     *
     * @param visible
     * @param cubes
     */
    private void updateVisibility(boolean visible, Object3D... cubes) {
       for (Object3D cube : cubes) {
           cube.setVisibility(visible);
       }
    }

    /**
     *
     * @param world
     * @param sun
     */
    private void randomizeAmbientLight(World world, Light sun) {
        // try to add some indication that SOMETHING is going to occur
        world.setAmbientLight(world.getAmbientLight()[0] + RandomUtils.decideIntegerInRange(-10, 10),
                world.getAmbientLight()[1] + RandomUtils.decideIntegerInRange(-10, 10),
                world.getAmbientLight()[2] + RandomUtils.decideIntegerInRange(-10, 10));
        sun.setPosition(sun.getPosition().calcAdd(new SimpleVector(1, 1, 1)));
    }

    /**
     * Updates the renderer for the cube game. (i.e. the super cube)
     */
    private void updateCubeGame() {
        Log.d("current time (s): ", "" + (System.currentTimeMillis() - startTime)/1000);
        if (surfaceDataDTO != null) {
            if (surfaceDataDTO.getHullPoints() != null && surfaceDataDTO.getBoundRect() != null) {
                updateDetectedPoints(surfaceDataDTO.getHullPoints(), surfaceDataDTO.getBoundRect(), 275);
                detectedZ = 500;
                // if less than the specified number of seconds has passed
                // this will be the behavior
                if (!isTranslated) {
                    if ((System.currentTimeMillis() - startTime) < 10000) {
                        superCube.updateOrigins(new SimpleVector(detectedX, detectedY, detectedZ),
                                superCube.getScale());
                        if ((System.currentTimeMillis() - startTime >= 8000)) {
                            randomizeAmbientLight(world, sun);
                        }
                    } else if ((System.currentTimeMillis() - startTime) >= 10000 && (System.currentTimeMillis() - startTime) < 10500) {
                        superCube.getCube1().translate(RandomUtils.decideFloatInRange(-3, 3), RandomUtils.decideFloatInRange(-3, 3), 0);
                    } else {
                        superCube.getCube1().setAdditionalColor(RGBColor.BLUE);
                        isTranslated = true;
                    }
                } else {
                    if (!isLocked) {
                       // superCube.getCube1().rotateAxis(superCube.getCenter(), 1);
                        superCube.getCube1().setOrigin(new SimpleVector(detectedX, detectedY, detectedZ));
                        // if the moved cube is near enough the center
                        // then it snaps back into position
                        if (RenderUtils.calcEuclideanDistanceX(superCube.getCenter(), superCube.getCube1().getOrigin()) < 2) {
                            isLocked = true;
                            superCube.getCube1().setOrigin(superCube.getCenter().calcAdd(new SimpleVector(-superCube.getScale(), -2*superCube.getScale(), -superCube.getScale())));
                            superCube.updateColor(RGBColor.BLUE);
                        }
                    } else {
                        if (!updatedStartTime) {
                            startTime = System.currentTimeMillis();
                            updatedStartTime = true;
                        } else {
                            elapsedTime = System.currentTimeMillis() - startTime;
                            if (System.currentTimeMillis() - startTime < 10000) {
                                superCube.updateOrigins(new SimpleVector(detectedX, detectedY, detectedZ), superCube.getScale());
                            } else if (elapsedTime >= 10000 && elapsedTime < 12000) {
                                // scatter the cubes (but not the center)
                                for (Object3D obj : superCube.getCubes()) {
                                    obj.translate(RandomUtils.decideFloatInRange(-5, 5),
                                            RandomUtils.decideFloatInRange(-5, 5), 0);
                                }
                            } else {
                                superCube.getCenterSphere().setOrigin(new SimpleVector(detectedX, detectedY, detectedZ));
                                if (elapsedTime > 12000) {
                                    for (Object3D obj : superCube.getCubes()) {
                                        if (RenderUtils.calcEuclideanDistance2D(superCube.getCenterSphere().getOrigin(), obj.getOrigin()) < 10) {
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

    private boolean doRotateCam = true;

    private void updateDetectedPointGunTip(Rect rect) {
        detectedX = (float)(rect.tl().x - (Math.abs(rect.tl().x - rect.br().x)/2));
        detectedY = (float)(rect.tl().y);
        detectedZ = 375;

    }

    private void updateDetectedPoints(List<Point> points, Rect rect, float baseZ) {
        detectedX = (float)HandDetectionUtils.getGunTip(points).x - 30;
        detectedY = (float) HandDetectionUtils.getGunTip(points).y;
        detectedZ = baseZ - (float) rect.area()/600;
    }

    public SurfaceDataDTO getSurfaceDataDTO() {
        return surfaceDataDTO;
    }

    public void setSurfaceDataDTO(SurfaceDataDTO surfaceDataDTO) {
        this.surfaceDataDTO = surfaceDataDTO;
    }

    public void setCubeGameMode(boolean cubeGameMode) {
        this.cubeGameMode = cubeGameMode;
    }
}
