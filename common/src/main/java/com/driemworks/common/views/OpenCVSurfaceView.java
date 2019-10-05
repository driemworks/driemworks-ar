package com.driemworks.common.views;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.AttributeSet;

import com.driemworks.common.utils.TagUtils;

import org.opencv.android.JavaCameraView;

import java.util.List;

/**
 *
 * This class is a possible bottleneck
 * @author Tony
 */
public class OpenCVSurfaceView extends JavaCameraView {
    /**
     * The tag used for logging
     */
    private final String TAG = TagUtils.getTag(this);
    
    public OpenCVSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public List<String> getEffectList() {
        return mCamera.getParameters().getSupportedColorEffects();
    }

    public boolean isEffectSupported() {
        return (mCamera.getParameters().getColorEffect() != null);
    }

    public String getEffect() {

        return mCamera.getParameters().getColorEffect();

    }

    public void setEffect(String effect) {
        Camera.Parameters params = mCamera.getParameters();
        params.setColorEffect(effect);
        mCamera.setParameters(params);
    }

    public Camera.Parameters getParameters(){
        Camera.Parameters params = mCamera.getParameters();
        return params;
    }

    public void setParameters(Camera.Parameters params){
        mCamera.setParameters(params);
    }

    public List<Size> getResolutionList() {
        return mCamera.getParameters().getSupportedPreviewSizes();
    }

    public void setResolution(Size resolution) {
        disconnectCamera();
        mMaxHeight = resolution.height;
        mMaxWidth = resolution.width;
        connectCamera(getWidth(), getHeight());
    }

    public Size getResolution() {
        return mCamera.getParameters().getPreviewSize();
    }
}