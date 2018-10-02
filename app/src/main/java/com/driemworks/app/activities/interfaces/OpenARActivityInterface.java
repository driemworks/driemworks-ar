package com.driemworks.app.activities.interfaces;

import android.opengl.GLSurfaceView;

import org.opencv.android.CameraBridgeViewBase;

public interface OpenARActivityInterface extends CameraBridgeViewBase.CvCameraViewListener2 {

    GLSurfaceView.Renderer renderer = null;

    int getOpenCVSurfaceViewId();

    GLSurfaceView.Renderer getRenderer();

    void setRenderer(GLSurfaceView.Renderer renderer);

}
