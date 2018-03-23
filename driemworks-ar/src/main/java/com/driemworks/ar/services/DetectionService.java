package com.driemworks.ar.services;

import com.driemworks.common.dto.SurfaceDataDTO;

import org.opencv.core.Mat;

/**
 * @author Tony
 */
public interface DetectionService {

    /**
     * Detect the object within the mat
     * @param image The mat
     * @param doDraw The do draw flag
     * @return {@link SurfaceDataDTO}
     */
    SurfaceDataDTO detect(Mat image, boolean doDraw);

}
