package com.driemworks.app.layout.impl;

import android.app.Activity;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.driemworks.app.activities.ConfigurationActivity;
import com.driemworks.app.activities.CubeActivity;
import com.driemworks.app.graphics.rendering.GraphicsRenderer;
import com.driemworks.app.layout.LayoutManager;

/**
 * Created by Tony on 5/13/2017.
 */
public class CubeActivityLayoutManager implements LayoutManager {

    public static Button reconfigureButton = null;
    public static final String RECONFIG = "Reconfig";

    public static Button showRectButton = null;
    public static final String SHOW_RECT = "ShowRect";

    public static Button newObjectButton = null;
    public static final String NEW_OBJ_BTN = "newObjBtn";

    private static CubeActivity activity = null;

    private static GraphicsRenderer graphicsRenderer = null;

    public <T extends Activity> void setActivity(T activity) {
        if (activity instanceof CubeActivity) {
            this.activity = (CubeActivity) activity;
        }
    }

    public <T extends GLSurfaceView.Renderer> void setRenderer(T renderer) {
        if (renderer instanceof  GraphicsRenderer) {
            this.graphicsRenderer = (GraphicsRenderer) renderer;
        }
    }

    private static CubeActivityLayoutManager layoutManager;

    @Override
    public <T extends View> void setup(String name, T layoutElement) {
        if (name.equals(RECONFIG)) {
            reconfigureButton = (Button) layoutElement;
            setReconfigOnTouchListener();
        } else if (name.equals(SHOW_RECT)) {
            showRectButton = (Button) layoutElement;
            setShowRectOnTouchListener();
        } else if (name.equals(NEW_OBJ_BTN)) {
            newObjectButton = (Button) layoutElement;
            setNewObjOnTouchListener();
        }
    }

    private void setNewObjOnTouchListener() {
        if (newObjectButton != null && graphicsRenderer != null) {
            newObjectButton.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    graphicsRenderer.setAddNewCube(!graphicsRenderer.isAddNewCube());
                    return false;
                }
            });
        }
    }


    /**
     * Setup the onTouchListener for the reconfigureButton button
     */
    private void setReconfigOnTouchListener() {
        if (reconfigureButton != null) {
            reconfigureButton.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Intent intent = new Intent(activity.getApplicationContext(), ConfigurationActivity.class);
                    activity.startActivity(intent);
                    activity.finish();
                    return false;
                }
            });
        }
    }

    /**
     *
     */
    private void setShowRectOnTouchListener() {
        if (showRectButton != null) {
            showRectButton.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    activity.setDoDraw(!activity.isDoDraw());
                    return false;
                }
            });
        }
    }

    /**
     *
     * @return
     */
    public static CubeActivityLayoutManager getInstance() {
        if (activity == null) {
            layoutManager = new CubeActivityLayoutManager();
        }

        return layoutManager;
    }

}
