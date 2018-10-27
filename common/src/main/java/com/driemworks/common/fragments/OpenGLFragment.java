package com.driemworks.common.fragments;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.driemworks.common.activities.AbstractARActivity;
import com.driemworks.common.builders.GLSurfaceViewBuilder;
import com.driemworks.common.graphics.AbstractOrientationRenderer;
import com.driemworks.common.sensor.orientationProvider.ImprovedOrientationSensorProvider;
import com.driemworks.common.sensor.orientationProvider.OrientationProvider;

import static android.content.Context.SENSOR_SERVICE;

/**
 * This fragment may only be implemented in a class that extends AbstractARActivity
 * @author Tony Riemer
 */
public class OpenGLFragment extends Fragment {

    /** The gl surface view */
    private GLSurfaceView glSurfaceView;

    /** The cube renderer */
    private AbstractOrientationRenderer renderer;

    /** The orientation provider */
    private OrientationProvider orientationProvider;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        if (context instanceof AbstractARActivity) {
            AbstractARActivity activity = (AbstractARActivity) context;
            orientationProvider = new ImprovedOrientationSensorProvider((SensorManager)activity.getSystemService(SENSOR_SERVICE));
            renderer = activity.getRenderer();
            renderer.setOrientationProvider(orientationProvider);
            // build the gl surface view with i) translucent background
            //                                ii) Continuous rendering
            int id = activity.getGlSurfaceViewId();
            glSurfaceView = new GLSurfaceViewBuilder(activity, activity.getGlSurfaceViewId())
                    .setEGLConfigChooser(8, 8, 8, 8, 16, 0)
                    .setRenderer(renderer)
                    .setFormat(PixelFormat.TRANSLUCENT)
                    .setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY)
                    .build();
            glSurfaceView.setZOrderOnTop(true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        glSurfaceView.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                return true;
//            }
//        });
        return glSurfaceView;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();
        orientationProvider.start();
        glSurfaceView.onResume();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause() {
        super.onPause();
        orientationProvider.stop();
        glSurfaceView.onPause();
    }

}
