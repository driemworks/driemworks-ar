package com.driemworks.common.interfaces;

import org.opencv.android.CameraBridgeViewBase;

public interface  OpenARActivityInterface extends CameraBridgeViewBase.CvCameraViewListener2 {

    int getOpenCVSurfaceViewId();

    int getGLSurfaceViewId();

}
