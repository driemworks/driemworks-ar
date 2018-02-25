package com.driemworks.app.utils;

import android.app.Activity;

import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;

import java.lang.reflect.Field;

import javax.microedition.khronos.opengles.GL10;

/**
 * Utility methods for rendering objects, making calculations needed to render objects
 * @author Tony
 */
public class RenderUtils {

    /**
     * Calculate the Euclidean distance along the x axis
     * @param vec1 The first vector
     * @param vec2 The second vector
     * @return {@link float} The Euclidean distance between the vectors along the x axis
     */
    public static float calcEuclideanDistanceX(SimpleVector vec1, SimpleVector vec2) {
        return (float)(Math.sqrt(Math.pow(Math.abs(vec1.x - vec2.x), 2)));
    }

    /**
     * Calculate the Euclidean distance (in 2 dimensions)
     * @param vec1 The first vector
     * @param vec2 The second vector
     * @return {@link float} The Euclidean distance between vec1 and vec2
     */
    public static float calcEuclideanDistance2D(SimpleVector vec1, SimpleVector vec2) {
        return (vec2.calcSub(new SimpleVector(0, 0, vec2.z))).calcSub(vec1.calcSub(
                new SimpleVector(0, 0, vec1.z))).length();
    }

    /**
     * Create a cube
     * @param color The RGBColor of the cube
     * @param scale The scale of the cube
     * @param x The x coordiante
     * @param y The y coordinate
     * @param z The z coordinate
     * @return {@link Object3D} The cube
     */
    public static Object3D createCube(RGBColor color, float scale, float x, float y, float z) {
        Object3D cube = Primitives.getCube(scale);
        cube.setAdditionalColor(color);
        cube.setOrigin(new SimpleVector(x, y, z));
        return cube;
    }

    /**
     * Create a sphere
     * @param color The RGBColor of the sphere
     * @param scale The scale of the sphere
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @return {@link Object3D} The sphere
     */
    public static Object3D createSphere(RGBColor color, float scale, float x, float y, float z) {
        Object3D sphere = Primitives.getSphere(scale);
        sphere.setAdditionalColor(color);
        sphere.setOrigin(new SimpleVector(x, y, z));
        return sphere;
    }

    /**
     * Update the camera rotation
     * @param cam The camera
     * @param deltaRotation The rotation
     */
    public static void updateRotation(Camera cam, float[] deltaRotation) {
        cam.rotateCameraY(-deltaRotation[0]);
        cam.rotateCameraX(deltaRotation[1]);
        cam.rotateCameraZ(deltaRotation[2]);
    }

    public static FrameBuffer updateFrameBuffer(FrameBuffer fb, GL10 gl, int width, int height) {
        if (fb != null) {
            fb.dispose();
        }

        return new FrameBuffer(gl, width, height);
    }

    /**
     *
     * @param src
     */
    public static void copy(Activity activity, Object src) {
        try {
            Field[] fs = src.getClass().getDeclaredFields();
            for (Field f : fs) {
                f.setAccessible(true);
                f.set(activity, f.get(src)) ;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
