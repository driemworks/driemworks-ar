package com.driemworks.ar.services.impl;

import com.driemworks.ar.services.DetectionService;
import com.driemworks.common.dto.SurfaceDataDTO;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of the DetectionService
 * Find the bounding box and contours of the largest object in the image
 */
public class GenericDetectionServiceImpl implements DetectionService {

    /**
     * {@inheritDoc}
     */
    @Override
    public SurfaceDataDTO detect(Mat image, boolean doDraw) {
        SurfaceDataDTO surfaceDataDTO = new SurfaceDataDTO();
        return surfaceDataDTO;
    }

    private List<MatOfPoint> findContours(Mat image) {
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(image, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_TC89_KCOS);
        return contours;
    }
}
