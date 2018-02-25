package com.driemworks.app.layout;

import android.view.View;

/**
 * Created by Tony on 5/6/2017.
 */
public interface LayoutManager {

    /**
     *
     * @param name
     * @param layoutElement
     * @param <T>
     */
    <T extends View> void setup(String name, T layoutElement);

}
