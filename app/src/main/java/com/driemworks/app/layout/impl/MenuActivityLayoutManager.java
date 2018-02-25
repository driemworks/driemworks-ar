package com.driemworks.app.layout.impl;

import android.content.Intent;
import android.view.View;
import android.widget.Button;

import com.driemworks.app.activities.ConfigurationActivity;
import com.driemworks.app.activities.LineGameActivity;
import com.driemworks.app.activities.MenuActivity;
import com.driemworks.app.layout.LayoutManager;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by Tony on 10/29/2017.
 */

public class MenuActivityLayoutManager implements LayoutManager {

    private Button lineGameActivityButton = null;
    public static final String LINE_ACT_BTN = "LineGameActivityButton";

    private Button confgiActivityButton = null;
    public static final String CONFIG_ACT_BTN = "ConfigurationActivityButton";

    private MenuActivity menuActivity;
    private static MenuActivityLayoutManager instance = null;


    /**
     * Get the instance of the ConfigurationLayoutManager
     * @return
     */
    public static MenuActivityLayoutManager getInstance(MenuActivity activity) {
        if (instance == null) {
            instance = new MenuActivityLayoutManager();
            instance.setActivity(activity);
        }
        return instance;
    }

    private void setActivity(MenuActivity activity) {
        this.menuActivity = activity;
    }


    @Override
    public <T extends View> void setup(String name, T layoutElement) {
        if (StringUtils.equals(name, LINE_ACT_BTN)) {
            lineGameActivityButton = (Button) layoutElement;
            setLineGameActivityButtonOnClickListener();
        } else if (StringUtils.equals(name, CONFIG_ACT_BTN)) {
            confgiActivityButton = (Button) layoutElement;
            setConfigurationActivityOnClickListener();
        }
    }

    /**
     * OnClick - start the LineGameActivity
     */
    private void setLineGameActivityButtonOnClickListener() {
        if (lineGameActivityButton != null) {
            lineGameActivityButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // start line game activity
                    Intent intent = new Intent(menuActivity, LineGameActivity.class);
                    menuActivity.startActivity(intent);
                    menuActivity.finish();
                }
            });
        }
    }

    /**
     * OnClick -> start the ConfigurationActivity
     */
    private void setConfigurationActivityOnClickListener() {
        if (confgiActivityButton != null) {
            confgiActivityButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // start config activity
                    Intent intent = new Intent(menuActivity, ConfigurationActivity.class);
                    menuActivity.startActivity(intent);
                    menuActivity.finish();
                }
            });
        }
    }

}
