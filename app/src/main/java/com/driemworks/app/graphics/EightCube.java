package com.driemworks.app.graphics;

import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.World;

/**
 * Created by Tony on 7/7/2017.
 */

public class EightCube {

    /** Front, Bottom Left */
    private Object3D cube1;
    /** Front, Top Left */
    private Object3D cube2;
    /** Front, Bottom Right */
    private Object3D cube3;
    /** Front, Top Right */
    private Object3D cube4;
    /** Back, Bottom Left */
    private Object3D cube5;
    /** Back, Top Left */
    private Object3D cube6;
    /** Back, Bottom Right */
    private Object3D cube7;
    /** Back, Top Right */
    private Object3D cube8;

    private Object3D centerSphere;

    /** The center (common) point for the cube */
    private SimpleVector center;
    /** The scale of each cube */
    private float scale;
    /** The color of the cubes */
    private RGBColor color;
    /** The texture of the sphere/cubes */

    /**
     *
     */
    public EightCube(float scale, SimpleVector center, RGBColor color) {
        this.center = center;
        this.scale = scale;
        this.color = color;
        init();
    }

    public void addToWorld(World world) {
        if (world != null) {
            world.addObject(cube1);
            world.addObject(cube2);
            world.addObject(cube3);
            world.addObject(cube4);
            world.addObject(cube5);
            world.addObject(cube6);
            world.addObject(cube7);
            world.addObject(cube8);
            world.addObject(centerSphere);
        }
    }

    public void updateCenter(SimpleVector center) {
        this.center = center;
    }

    /**
     * Initializes the cubes
     * sets the color, scale, and origin of the cubes
     */
    private void init() {
        initCubes();
        centerSphere = Primitives.getSphere(scale);
        centerSphere.setAdditionalColor(RGBColor.GREEN);
        updateOrigins(center, scale);
        centerSphere.setOrigin(center);
        updateColor(color);
        centerSphere.setAdditionalColor(RGBColor.BLACK);
    }

    public void updateScale(float scale) {
        this.scale = scale;
        cube1.setScale(scale);
        cube2.setScale(scale);
        cube3.setScale(scale);
        cube4.setScale(scale);
        cube5.setScale(scale);
        cube6.setScale(scale);
        cube7.setScale(scale);
        cube8.setScale(scale);

    }

    public Object3D[] getCubes() {
        Object3D[] objArr = new Object3D[8];
        objArr[0] = cube1;
        objArr[1] = cube2;
        objArr[2] = cube3;
        objArr[3] = cube4;
        objArr[4] = cube5;
        objArr[5] = cube6;
        objArr[6] = cube7;
        objArr[7] = cube8;
        return objArr;
    }

    /**
     * Instantiates the cubes
     */
    private void initCubes() {
        cube1 = Primitives.getCube(scale);
        cube2 = Primitives.getCube(scale);
        cube3 = Primitives.getCube(scale);
        cube4 = Primitives.getCube(scale);
        cube5 = Primitives.getCube(scale);
        cube6 = Primitives.getCube(scale);
        cube7 = Primitives.getCube(scale);
        cube8 = Primitives.getCube(scale);
    }


    /**
     * Updates the color of the cubes
     * @param color
     */
    public void updateColor(RGBColor color) {
        this.color = color;
        cube1.setAdditionalColor(color);
        cube2.setAdditionalColor(color);
        cube3.setAdditionalColor(color);
        cube4.setAdditionalColor(color);
        cube5.setAdditionalColor(color);
        cube6.setAdditionalColor(color);
        cube7.setAdditionalColor(color);
        cube8.setAdditionalColor(color);
    }

    /**
     * Update the center (common) point
     */
    public void updateOrigins(SimpleVector center, float scale) {
        this.center = center;
        this.scale = scale;
        float s = scale;
        cube1.setOrigin(center.calcAdd(new SimpleVector(-s, -s, -s)));
        cube2.setOrigin(center.calcAdd(new SimpleVector( s, -s, -s)));
        cube3.setOrigin(center.calcAdd(new SimpleVector( s, s, -s)));
        cube4.setOrigin(center.calcAdd(new SimpleVector(-s, s,  -s)));
        cube5.setOrigin(center.calcAdd(new SimpleVector(-s,  s, s)));
        cube6.setOrigin(center.calcAdd(new SimpleVector(-s,  s,  s)));
        cube7.setOrigin(center.calcAdd(new SimpleVector( s,  s, s)));
        cube8.setOrigin(center.calcAdd(new SimpleVector( s,  s,  s)));
        centerSphere.setOrigin(center);
    }

    public Object3D getCube1() {
        return cube1;
    }

    public void setCube1(Object3D cube1) {
        this.cube1 = cube1;
    }

    public Object3D getCube2() {
        return cube2;
    }

    public void setCube2(Object3D cube2) {
        this.cube2 = cube2;
    }

    public Object3D getCube3() {
        return cube3;
    }

    public void setCube3(Object3D cube3) {
        this.cube3 = cube3;
    }

    public Object3D getCube4() {
        return cube4;
    }

    public void setCube4(Object3D cube4) {
        this.cube4 = cube4;
    }

    public Object3D getCube5() {
        return cube5;
    }

    public void setCube5(Object3D cube5) {
        this.cube5 = cube5;
    }

    public Object3D getCube6() {
        return cube6;
    }

    public void setCube6(Object3D cube6) {
        this.cube6 = cube6;
    }

    public Object3D getCube7() {
        return cube7;
    }

    public void setCube7(Object3D cube7) {
        this.cube7 = cube7;
    }

    public Object3D getCube8() {
        return cube8;
    }

    public void setCube8(Object3D cube8) {
        this.cube8 = cube8;
    }

    public SimpleVector getCenter() {
        return center;
    }

    public float getScale() {
        return scale;
    }

    public RGBColor getColor() {
        return color;
    }

    public Object3D getCenterSphere() {
        return centerSphere;
    }

    public void setCenterSphere(Object3D centerSphere) {
        this.centerSphere = centerSphere;
    }
}
