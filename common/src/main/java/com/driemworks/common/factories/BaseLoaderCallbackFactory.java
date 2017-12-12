package com.driemworks.common.factories;

import android.app.Activity;

import com.driemworks.common.views.CustomSurfaceView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
/**
 * @author Tony
 */
public class BaseLoaderCallbackFactory {

    /**
     *
     * @param activity
     * @param customSurfaceView
     * @return
     */
    public static BaseLoaderCallback getBaseLoaderCallback(final Activity activity, final CustomSurfaceView customSurfaceView) {
        return new BaseLoaderCallback(activity) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case LoaderCallbackInterface.SUCCESS: {
                        customSurfaceView.enableView();
                        customSurfaceView.setMaxFrameSize(800, 480);
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
