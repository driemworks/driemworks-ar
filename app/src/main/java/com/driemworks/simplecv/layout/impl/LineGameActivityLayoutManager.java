package com.driemworks.simplecv.layout.impl;

import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.driemworks.simplecv.activities.LineGameActivity;
import com.driemworks.simplecv.activities.MenuActivity;
import com.driemworks.simplecv.layout.LayoutManager;

/**
 *
 */
public class LineGameActivityLayoutManager implements LayoutManager {

    public static Button backButton = null;
    public static final String BACK_BTN = "BackButton";

    /** The main activity */
    private static LineGameActivity lineGameActivity = null;
    private static LineGameActivityLayoutManager configurationLayoutManager = null;

    /**
     *
     */
    private LineGameActivityLayoutManager() {}

    @Override
    public <T extends View> void setup(String name, T layoutElement) {
        if (null != lineGameActivity) {
            if (name.equals(BACK_BTN)) {
                backButton = (Button) layoutElement;
//                setConfigurationOnTouchListener();
            }
        }
    }

    /**
     * The onTouchListener for the backButton button
     */
    private void setConfigurationOnTouchListener() {
        if (backButton != null) {
            backButton.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (null != lineGameActivity) {
                        Intent intent = new Intent(lineGameActivity, MenuActivity.class);
                        lineGameActivity.startActivity(intent);
                        lineGameActivity.finish();
                    }
                    return false;
                }
            });
        }
    }

    /**
     * Get the instance of the ConfigurationLayoutManager
     * @return
     */
    public static LineGameActivityLayoutManager getInstance(LineGameActivity activity) {
        if (configurationLayoutManager == null) {
            configurationLayoutManager = new LineGameActivityLayoutManager();
            configurationLayoutManager.setActivity(activity);
        }
        return configurationLayoutManager;
    }

    /**
     *
     * @param lineGameActivity
     */
    public void setActivity(LineGameActivity lineGameActivity) {
        this.lineGameActivity = lineGameActivity;
    }

}