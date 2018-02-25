package com.driemworks.common.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.MotionEvent;

import com.driemworks.common.enums.Resolution;

import org.opencv.core.Rect;

/**
 * Utility class for correcting coordinates for display purposes
 * @author Tony
 */
public class DisplayUtils {

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
     * Calculate the dimensions of the display
     * @param activity The activity
     * @return {@link Point} A point with the x value the width and y value the height
     */
    public static Point getScreenSize(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new android.graphics.Point();
        display.getSize(size);
        return size;
    }
}
