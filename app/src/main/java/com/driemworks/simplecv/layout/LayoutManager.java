package com.driemworks.simplecv.layout;

import android.view.View;

import java.util.Map;

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
