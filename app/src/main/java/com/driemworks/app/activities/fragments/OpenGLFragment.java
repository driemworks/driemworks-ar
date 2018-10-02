package com.driemworks.app.activities.fragments;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.support.v4.app.Fragment;

import com.driemworks.app.activities.OpenARFragmentActivity;
import com.driemworks.app.builders.GLSurfaceViewBuilder;

/**
 *
 * @author Tony Riemer
 */
public class OpenGLFragment extends Fragment {

    /** The gl surface view */
    private GLSurfaceView glSurfaceView;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        if (context instanceof OpenARFragmentActivity) {
            OpenARFragmentActivity activity = (OpenARFragmentActivity) context;
            // build the gl surface view with i) translucent background
            //                                ii) Continuous rendering
            glSurfaceView = new GLSurfaceViewBuilder(activity, activity.getGLSurfaceViewId())
                    .setEGLConfigChooser(8, 8, 8, 8, 16, 0)
                    .setRenderer(activity.getRenderer())
                    .setFormat(PixelFormat.TRANSLUCENT)
                    .setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY)
                    .build();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();
        glSurfaceView.onResume();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause() {
        super.onPause();
        glSurfaceView.onPause();
    }

}
