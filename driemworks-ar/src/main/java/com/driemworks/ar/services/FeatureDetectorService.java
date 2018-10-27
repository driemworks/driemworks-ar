package com.driemworks.ar.services;

import android.util.Pair;

import com.driemworks.ar.dto.FeatureData;

import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;

/**
 * Interface for feature services, which should be capable of detecting features and tracking them
 * @author Tony
 */
public interface FeatureDetectorService {

    /**
     * Detect features in the input image
     * @param frame The input image
     * @return {@link MatOfKeyPoint}
     */
    MatOfKeyPoint extractFeatureData(Mat frame);

    /**
     * Track features extracted from the previous image into the next image
     * @param previousFrameGray The previous image in grayscale
     * @param currentFrameGray The current image in grayscale
     * @param previousKeyPoints The list of previously detected key points
     * @return {@link Pair<MatOfKeyPoint, MatOfKeyPoint>} The pair of tracked
     *          and matching key points from the previous and the current image,
     *          with the previous image's data being the first item in the pair
     */
    Pair<MatOfKeyPoint,  MatOfKeyPoint> trackKeyPoints(Mat previousFrameGray, Mat currentFrameGray,
                                                       MatOfKeyPoint previousKeyPoints);

}
