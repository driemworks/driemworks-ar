package com.driemworks.ar.MonocularVisualOdometry.services;

import com.driemworks.ar.dto.FeatureWrapper;
import com.driemworks.ar.dto.SequentialFrameFeatures;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfKeyPoint;

/**
 * Interface for feature services, which should be capable of detecting features and tracking them
 * @author Tony
 */
public interface FeatureService {

    /**
     * Detect features in the input image
     * @param frame The input image
     * @return {@link FeatureWrapper}
     */
    FeatureWrapper featureDetection(Mat frame);

    /**
     * Track features extracted from the previous image into the next image
     * @param previousFrameGray The previous image in grayscale
     * @param currentFrameGray The current image in grayscale
     * @param previousKeyPoints The list of previously detected key points
     * @return {@link SequentialFrameFeatures}
     */
    SequentialFrameFeatures featureTracking(Mat previousFrameGray, Mat currentFrameGray,
                                                   MatOfKeyPoint previousKeyPoints);

}
