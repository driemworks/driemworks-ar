package com.driemworks.common.fragments;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.driemworks.common.activities.AbstractARActivity;
import com.driemworks.common.builders.OpenCVSurfaceViewBuilder;
import com.driemworks.common.factories.BaseLoaderCallbackFactory;
import com.driemworks.common.views.OpenCVSurfaceView;
import com.driemworks.common.enums.Resolution;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;

/**
 * @author Tony Riemer
 */
public class OpenCVFragment extends Fragment {

    /** load the opencv lib */
    static {
        System.loadLibrary("opencv_java3");
    }

    /** The CvCameraViewListener2 */
    private CameraBridgeViewBase.CvCameraViewListener2 cvCameraViewListener;

    /** The open cv surface view */
    private OpenCVSurfaceView openCVSurfaceView;

    /** The base loader callback */
    private BaseLoaderCallback baseLoaderCallback;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof AbstractARActivity) {
            AbstractARActivity activity = (AbstractARActivity) context;
            try {
                cvCameraViewListener = activity;
                // build the views and init callbacks
                openCVSurfaceView = new OpenCVSurfaceViewBuilder(activity, activity.getOpenCVSurfaceViewId())
                        .setCvCameraViewListener(cvCameraViewListener)
                        .setMaxFrameSize(Resolution.RES_STANDARD)
                        .build();
                // init base loader callback
                baseLoaderCallback = BaseLoaderCallbackFactory.getBaseLoaderCallback(
                        activity, openCVSurfaceView);
                // init opencv loader
                OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, activity, baseLoaderCallback);
                // require CAMERA
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.CAMERA}, 1);
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString() + " must implement CvCameraViewListener2");
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause() {
        super.onPause();
        if (openCVSurfaceView != null) {
            openCVSurfaceView.disableView();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        openCVSurfaceView.disableView();
    }
}


