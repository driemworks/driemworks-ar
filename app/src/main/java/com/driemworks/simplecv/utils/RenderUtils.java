package com.driemworks.simplecv.utils;

import android.app.Activity;
import android.util.Log;

import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.World;

import java.lang.reflect.Field;

import javax.microedition.khronos.opengles.GL10;

/**
 * Utility methods for rendering objects, making calculations needed to render objects
 * @author Tony
 */
public class RenderUtils {

    public static float calcEuclideanDistanceX(SimpleVector vec1, SimpleVector vec2) {
        return (float)(Math.sqrt(Math.pow(Math.abs(vec1.x - vec2.x), 2)));
    }

    public static float calcEuclideanDistance2D(SimpleVector vec1, SimpleVector vec2) {
        return (vec2.calcSub(new SimpleVector(0, 0, vec2.z))).calcSub(vec1.calcSub(
                new SimpleVector(0, 0, vec1.z))).length();
    }

    public static Object3D createCube(RGBColor color, float scale, float y, float x, float z) {
        Object3D cube = Primitives.getCube(scale);
        cube.setAdditionalColor(color);
        cube.setOrigin(new SimpleVector(x, y, z));
        return cube;
    }

    /**
     *
     * @param cam
     * @param deltaRotation
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
