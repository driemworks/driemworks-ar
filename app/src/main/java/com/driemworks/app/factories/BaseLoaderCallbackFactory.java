package com.driemworks.app.factories;

import android.app.Activity;

import com.driemworks.common.enums.Resolution;
import com.driemworks.app.views.CustomSurfaceView;

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
    public static BaseLoaderCallback getBaseLoaderCallback(final Activity activity, final CustomSurfaceView customSurfaceView) {
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
