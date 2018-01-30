package com.driemworks.simplecv.executors;

import android.util.Log;

import com.driemworks.ar.MonocularVisualOdometry.services.impl.MonocularVisualOdometryService;
import com.driemworks.ar.dto.CameraPoseDTO;
import com.driemworks.common.utils.TagUtils;

import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Asynchronous executor for the MonocularVisualOdodmetryService
 * @author Tony
 */
public class MonocularVisualOdometryExecutor {

    /**
     * The constant TAG for logging
     */
    private final String TAG = TagUtils.getTag(this);

    /**
     * The monocular visual odometry service
     */
    private MonocularVisualOdometryService monocularVisualOdometryService;

    /**
     * The executor service
     */
    private ExecutorService executorService;

    /**
     * The singleton instance
     */
    private static MonocularVisualOdometryExecutor monocularVisualOdometryExecutor = null;

    /**
     * Constructor for the {@link MonocularVisualOdometryExecutor}
     */
    private MonocularVisualOdometryExecutor() {
        monocularVisualOdometryService = new MonocularVisualOdometryService();
        executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Calculate the translation vector
     * @return the translation vector
     */
    public Future<CameraPoseDTO> calculateOdometry(
            Mat currentFrame,  Mat previousFrameGray, Mat currentFrameGray, MatOfKeyPoint previousPoints) {
        Log.d(TAG,"START - calculateOdometry");
        return executorService.submit(() ->
                monocularVisualOdometryService.monocularVisualOdometry(
                currentFrame, previousFrameGray, currentFrameGray, previousPoints)
        );
    }

    /**
     * Get the singleton instance of the executor
     * @return monocularVisualOdometryExecutor The monocularVisualOdometryExecutor
     */
    public static MonocularVisualOdometryExecutor getInstance() {
        if (monocularVisualOdometryExecutor == null) {
            monocularVisualOdometryExecutor = new MonocularVisualOdometryExecutor();
        }
        return monocularVisualOdometryExecutor;
    }

}
