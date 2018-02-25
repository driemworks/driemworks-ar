package com.driemworks.app.graphics.rendering;

import android.opengl.GLSurfaceView;

import com.driemworks.common.dto.SurfaceDataDTO;

import org.opencv.core.Mat;

/**
 * @author Tony
 */
public abstract class AbstractRenderer implements GLSurfaceView.Renderer {

    private SurfaceDataDTO surfaceDataDTO;
    private Mat mRgba;
    private boolean isTouch;

    private float[] rotationVector;

    private float[] previousRotationVector;

    public float[] getPreviousRotationVector() {
        return previousRotationVector;
    }

    public void setPreviousRotationVector(float[] previousRotationVector) {
        this.previousRotationVector = previousRotationVector;
    }

    public SurfaceDataDTO getSurfaceDataDTO() {
        return surfaceDataDTO;
    }

    public void setSurfaceDataDTO(SurfaceDataDTO surfaceDataDTO) {
        this.surfaceDataDTO = surfaceDataDTO;
    }

    public Mat getmRgba() {
        return mRgba;
    }

    public void setmRgba(Mat mRgba) {
        this.mRgba = mRgba;
    }

    public boolean isTouch() {
        return isTouch;
    }

    public void setTouch(boolean touch) {
        isTouch = touch;
    }

    public float[] getRotationVector() {
        return rotationVector;
    }

    public void setRotationVector(float[] rotationVector) {
        this.rotationVector = rotationVector;
    }
}
