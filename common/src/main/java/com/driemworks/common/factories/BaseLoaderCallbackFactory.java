package com.driemworks.common.factories;

import android.app.Activity;

import com.driemworks.common.enums.Resolution;
import com.driemworks.common.views.CustomSurfaceView;

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
     * @param customSurfaceView The {@link CustomSurfaceView}
     * @return the BaseLoaderCallback
     */
    public static BaseLoaderCallback getBaseLoaderCallback(final Activity activity, final CustomSurfaceView customSurfaceView, final Resolution resolution) {
        return new BaseLoaderCallback(activity) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case LoaderCallbackInterface.SUCCESS: {
                        customSurfaceView.enableView();
                        customSurfaceView.setMaxFrameSize(resolution.getWidth(), resolution.getHeight());
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
