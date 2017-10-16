package com.driemworks.common.factories;

import android.app.Activity;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import com.driemworks.common.views.CustomSurfaceView;
/**
 * Created by Tony on 7/7/2017.
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
