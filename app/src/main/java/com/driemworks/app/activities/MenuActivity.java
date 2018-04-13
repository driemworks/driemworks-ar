package com.driemworks.app.activities;

import android.app.Activity;
import android.os.Bundle;

import com.driemworks.app.R;
import com.driemworks.app.layout.impl.MenuActivityLayoutManager;

/**
 * @author Tony
 */
public class MenuActivity extends Activity {

    /** The layout manager */
    private MenuActivityLayoutManager layoutManager;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        this.setContentView(R.layout.menu);
//
//        layoutManager = MenuActivityLayoutManager.getInstance(this);
//        layoutManager.setup(MenuActivityLayoutManager.PLAY_BTN, findViewById(R.id.play_btn));
    }

}
