package com.driemworks.common.views.Utils;

import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Tony on 7/15/2017.
 */

public class ViewReplacer implements Runnable {

    private View currentView;

    private View newView;

    public ViewReplacer(View currentView, View newView) {
        this.currentView = currentView;
        this.newView = newView;
    }

    /**
     * Get the parent of the view
     * @param view the view
     * @return the view's parent
     */
    public static ViewGroup getParent(View view) {
        return (ViewGroup)view.getParent();
    }

    /**
     * Replace a view with another view
     * @param currentView the current view
     * @param newView the new view (to replace current view)
     */
    public void replaceView() {
        ViewGroup parent = getParent(currentView);

        if (parent == null) {
            return;
        }

        final int index = parent.indexOfChild(currentView);
        parent.removeView(currentView);
        parent.removeView(newView);
        parent.addView(newView, index);

    }

    @Override
    public void run() {
        replaceView();
    }
}
