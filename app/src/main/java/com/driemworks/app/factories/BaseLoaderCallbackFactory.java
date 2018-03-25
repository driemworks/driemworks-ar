package com.driemworks.app.factories;

import android.app.Activity;

import com.driemworks.app.views.OpenCVSurfaceView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;

/**
 * Factory for the BaseLoaderCallback
 * @author Tony
 */
public class BaseLoaderCallbackFactory {

    /**
     *
     * @param activity The {@link Activity}
     * @param customSurfaceView The {@link OpenCVSurfaceView}
     * @return the BaseLoaderCallback
     */
    public static BaseLoaderCallback getBaseLoaderCallback(final Activity activity, final OpenCVSurfaceView customSurfaceView) {
        return new BaseLoaderCallback(activity) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case LoaderCallbackInterface.SUCCESS: {
                        customSurfaceView.enableView();
                    }
                    break;
                    default: {
                        super.onManagerConnected(status);
                    }
                    break;
                }
            }
        };

    }
}
