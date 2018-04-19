package com.driemworks.ar.services.impl;

import android.util.Log;

import com.driemworks.ar.services.FeatureService;
import com.driemworks.common.dto.FeatureDataDTO;
import com.driemworks.common.utils.OpenCvUtils;

import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Tony
 */
public class FeatureServiceImpl implements FeatureService<MatOfDMatch> {

    /** The feature detector */
    private FeatureDetector detector;

    /** The descriptor extractor */
    private DescriptorExtractor extractor;

    /** The descriptor matcher */
    private DescriptorMatcher matcher;

    /** The multiplier used to find good matches */
    private static final float MULTIPLIER = 4.25f;

    /**
     * The constructor for the FeatureServiceImpl
     * ORB Detector/ORB extractor, Bruteforce Hamming matcher
     */
    public FeatureServiceImpl() {
        detector = FeatureDetector.create(FeatureDetector.ORB);
        extractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void featureDetection(FeatureDataDTO dto) {
        featureDetection(dto.getImage(), dto.getKeyPoints(), dto.getDescriptors());
    }

    /**
     * Detect the features
     * @param frame The frame
     * @param keyPoints The key points
     * @param descriptors The descriptors
     */
    private void featureDetection(Mat frame, MatOfKeyPoint keyPoints, Mat descriptors) {
        // convert input image to gray
        Mat gray = new Mat();
        Mat mDilatedMask = new Mat();
        Imgproc.cvtColor(frame, gray, Imgproc.COLOR_RGBA2GRAY);
        OpenCvUtils.sharpenImage(gray);
        detector.detect(gray, keyPoints);
//        Imgproc.dilate(gray, mDilatedMask, new Mat());
//        OpenCvUtils.sharpenImage(mDilatedMask);
//        // detect keypoints
//        detector.detect(mDilatedMask, keyPoints);
        // extract  descriptors
//        extractor.compute(mDilatedMask, keyPoints, descriptors);
        extractor.compute(gray, keyPoints, descriptors);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MatOfDMatch featureTracking(FeatureDataDTO referenceData, FeatureDataDTO currentFeatureData) {
        Log.d("featureTracking: ", "current kp empty? = " + currentFeatureData.getKeyPoints().empty());
        MatOfDMatch matches = new MatOfDMatch();
        Mat referenceImage = referenceData.getImage();
        if (referenceImage.type() == currentFeatureData.getImage().type()) {
            matcher.match(referenceData.getDescriptors(), currentFeatureData.getDescriptors(), matches);
        } else {
            return null;
        }

        // where do these values come from??
        double minDistance = 50.0;
        double maxDistance = 20.0;

        List<DMatch> matchesList = matches.toList();
        double dist;
        for (DMatch match : matchesList) {
            dist = match.distance;
            if (dist < minDistance) {
                minDistance = dist;
            } else if (dist > maxDistance) {
                maxDistance = dist;
            }
        }

        LinkedList<DMatch> good_matches = new LinkedList<>();
        for (DMatch match : matchesList) {
            if (match.distance <= (MULTIPLIER * minDistance)) {
                good_matches.addLast(match);
            }
        }

        MatOfDMatch goodMatches = new MatOfDMatch();
        goodMatches.fromList(good_matches);
        return goodMatches;
    }
}
