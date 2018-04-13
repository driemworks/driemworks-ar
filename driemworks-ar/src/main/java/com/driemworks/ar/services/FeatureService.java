package com.driemworks.ar.services;

import com.driemworks.common.dto.FeatureDataDTO;

import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;

/**
 * Interface for feature services, which should be capable of detecting features and tracking them
 * @author Tony
 */
public interface FeatureService<T> {

    /**
     * Detect features in the input image
     * @param featureDataDTO The FeatureDataDTO
     */
    void featureDetection(FeatureDataDTO featureDataDTO);

    /**
     * Track features extracted from the previous image into the next image
     * @param referenceData The reference FeatureDataDTO
     * @param  currentFeatureData The current FeatureDataDTO
     * @return  T
     */
    T featureTracking(FeatureDataDTO referenceData, FeatureDataDTO currentFeatureData);

}
