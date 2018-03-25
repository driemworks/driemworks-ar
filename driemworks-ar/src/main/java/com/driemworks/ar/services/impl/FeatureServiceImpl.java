package com.driemworks.ar.services.impl;

import android.util.Log;

import com.driemworks.ar.services.FeatureService;
import com.driemworks.common.dto.FeatureDataDTO;

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
public class FeatureServiceImpl {

    /** The feature detector */
    private FeatureDetector detector;

    /** The descriptor extractor */
    private DescriptorExtractor extractor;

    /** The descriptor matcher */
    private DescriptorMatcher matcher;

    private MatOfKeyPoint keyPoint;

    private Mat descriptors;

    private double maxDistance;
    private double minDistance;


    /**
     * The constructor for the FeatureServiceImpl
     */
    public FeatureServiceImpl() {
        detector = FeatureDetector.create(FeatureDetector.ORB);
        extractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
        keyPoint = new MatOfKeyPoint();
        descriptors = new Mat();
    }

    public void featureDetection(FeatureDataDTO dto) {
        featureDetection(dto.getImage(), dto.getKeyPoints(), dto.getDescriptors());
    }

    public void featureDetection(Mat frame, MatOfKeyPoint keyPoints, Mat descriptors) {
        // convert input image to gray
        Mat gray = new Mat();
        Imgproc.cvtColor(frame, gray, Imgproc.COLOR_RGBA2GRAY);
        // detect keypoints
        detector.detect(gray, keyPoints);
        // extract  descriptors
        extractor.compute(gray, keyPoints, descriptors);
    }

    public MatOfDMatch featureTracking(FeatureDataDTO referenceData, FeatureDataDTO currentFeatureData) {
        Log.d("featureTracking: ", "current kp empty? = " + currentFeatureData.getKeyPoints().empty());
        MatOfDMatch matches = new MatOfDMatch();
        Mat referenceImage = referenceData.getImage();
        if (referenceImage.type() == currentFeatureData.getImage().type()) {
            matcher.match(referenceData.getDescriptors(), currentFeatureData.getDescriptors(), matches);
        } else {
            return null;
        }

        minDistance = 100.0;
        maxDistance = 0.0;

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
            // 1.5????
            if (match.distance <= (1.5 * minDistance)) {
                good_matches.addLast(match);
            }
        }

        MatOfDMatch goodMatches = new MatOfDMatch();
        goodMatches.fromList(good_matches);
        return goodMatches;
    }
}
