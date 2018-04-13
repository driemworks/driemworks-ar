package com.driemworks.app.layout.impl;

import android.content.Intent;
import android.view.View;
import android.widget.Button;

import com.driemworks.app.activities.MenuActivity;
import com.driemworks.app.activities.ObjectTrackingActivity;
import com.driemworks.app.layout.LayoutManager;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Tony
 */
public class MenuActivityLayoutManager implements LayoutManager {

    /** The play button */
    private Button playButton = null;

    /** The play button name */
    public static final String PLAY_BTN = "play_btn";

    /** The menu activity */
    private MenuActivity menuActivity;

    /** The layout manager instance */
    private static MenuActivityLayoutManager instance = null;


    /**
     * Get the instance of the ConfigurationLayoutManager
     * @return The instance of the layout manager
     */
    public static MenuActivityLayoutManager getInstance(MenuActivity activity) {
        if (instance == null) {
            instance = new MenuActivityLayoutManager();
            instance.setActivity(activity);
        }
        return instance;
    }

    /**
     * Setter for the activity
     * @param activity The activity to set
     */
    private void setActivity(MenuActivity activity) {
        this.menuActivity = activity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends View> void setup(String name, T layoutElement) {
        if (StringUtils.equals(name, PLAY_BTN)) {
            playButton = (Button) layoutElement;
            setPlayButtonOnClickListener();
        }
    }

    /**
     * Set the play button on click listener
     */
    private void setPlayButtonOnClickListener() {
        if (playButton != null) {
            playButton.setOnClickListener(v -> {
                Intent intent = new Intent(menuActivity, ObjectTrackingActivity.class);
                menuActivity.startActivity(intent);
                menuActivity.finish();
            });
        }
    }

}
