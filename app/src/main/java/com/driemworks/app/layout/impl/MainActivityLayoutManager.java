package com.driemworks.app.layout.impl;

import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.driemworks.app.activities.ObjectTrackingActivity;
import com.driemworks.app.activities.MainActivity;
import com.driemworks.app.layout.LayoutManager;

/**
 * Created by Tony on 7/16/2017.
 */
public class MainActivityLayoutManager implements LayoutManager {

    public static final String BACK_BTN = "backbutton";
    private Button backButton = null;

    private MainActivity mainActivity;

    private boolean isMenu = true;

    /**
     *
     * @param activity
     */
    public MainActivityLayoutManager(MainActivity activity) {
        this.mainActivity = activity;
    }

    @Override
    public <T extends View> void setup(String name, T layoutElement) {
        if (name.equals(BACK_BTN)) {
            backButton = (Button) layoutElement;
            setBackButtonOnTouchListener();
        }
    }

    /**
     *
     */
    private void setBackButtonOnTouchListener() {
        if (backButton != null) {
            backButton.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (isMenu) {
                        Intent intent = new Intent(mainActivity, ObjectTrackingActivity.class);
                        mainActivity.startActivity(intent);
                        mainActivity.finish();
                        return false;
                    } else {
                        mainActivity.getMainRenderer().setCubeGameMode(false);
                    }
                    return false;
                }
            });
        }
    }

    public boolean isMenu() {
        return isMenu;
    }

    public void setMenu(boolean menu) {
        isMenu = menu;
    }
}
