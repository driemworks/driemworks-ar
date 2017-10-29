package com.driemworks.simplecv.activities;

import android.app.Activity;
import android.os.Bundle;

import com.driemworks.simplecv.R;
import com.driemworks.simplecv.layout.impl.MenuActivityLayoutManager;

/**
 * Created by Tony on 10/29/2017.
 */

public class MenuActivity extends Activity {

    private MenuActivityLayoutManager layoutManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.menu);

        layoutManager = MenuActivityLayoutManager.getInstance(this);
        layoutManager.setup(MenuActivityLayoutManager.CONFIG_ACT_BTN, findViewById(R.id.config_activity_btn));
        layoutManager.setup(MenuActivityLayoutManager.LINE_ACT_BTN, findViewById(R.id.line_activity_btn));

    }

}
