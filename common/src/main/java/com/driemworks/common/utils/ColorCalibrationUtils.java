package com.driemworks.common.utils;

import android.util.Log;
import android.view.MotionEvent;

import com.driemworks.common.views.CustomSurfaceView;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

/**
 * Created by Tony on 4/23/2017.
 */

public class ColorCalibrationUtils {

    private static final String TAG = "ColorCalibrationUtils: ";

    public static Rect getTouchedRect(Mat mRgba, CustomSurfaceView mOpenCvCameraView, MotionEvent event, int sizeThreshold) {
        int cols = mRgba.cols();
        int rows = mRgba.rows();

        int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
        int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;

        int x = (int)event.getX() - xOffset;
        int y = (int)event.getY() - yOffset;

        Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");

        if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) {
            return null;
        }

        Rect touchedRect = new Rect();

        touchedRect.x = (x > sizeThreshold) ? x - sizeThreshold : 0;
        touchedRect.y = (y > sizeThreshold) ? y - sizeThreshold: 0;

        touchedRect.width = (x + sizeThreshold < cols) ? x + sizeThreshold - touchedRect.x : cols - touchedRect.x;
        touchedRect.height = (y + sizeThreshold < rows) ? y + sizeThreshold - touchedRect.y : rows - touchedRect.y;
        return touchedRect;
    }
}
