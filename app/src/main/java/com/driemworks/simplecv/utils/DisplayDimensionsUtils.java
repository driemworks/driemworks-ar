package com.driemworks.simplecv.utils;

import android.app.Activity;
import android.graphics.Point;
import android.view.Display;
import android.view.MotionEvent;

/**
 * Created by Tony on 10/15/2017.
 */

public class DisplayDimensionsUtils {

    /**
     * Calculates the coordinate on the screen corrected for resolution and screen size
     * @param event the touch event
     * @return the correct coordinates
     */
    public static org.opencv.core.Point correctCoordinate(MotionEvent event, int screenWidth, int screenHeight) {
        double x = (event.getX() * com.driemworks.common.enums.Resolution.RES_STANDARD.getWidth()) / screenWidth;
        double y = (event.getY() * com.driemworks.common.enums.Resolution.RES_STANDARD.getHeight()) / screenHeight;
        return new org.opencv.core.Point(x, y);
    }

    /**
     * Calculate the width of the display
     * @param activity
     * @return
     */
    public static int getScreenWidth(Activity activity) {
        return getScreenSize(activity).x;
    }

    /**
     * Calculate the height of the display
     * @param activity
     * @return
     */
    public static int getScreenHeight(Activity activity) {
        return getScreenSize(activity).y;
    }

    /**
     * Calculate the dimensions of the display
     * @param activity
     * @return
     */
    private static Point getScreenSize(Activity activity) {
        // get screen dimensions
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new android.graphics.Point();
        display.getSize(size);
        return size;
    }

}
