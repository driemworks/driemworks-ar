package com.driemworks.ar.MonocularVisualOdometry;

import android.media.Image;
import android.provider.Settings;
import android.util.Log;
import android.util.Pair;

import com.driemworks.ar.dto.FeatureWrapper;
import com.driemworks.ar.dto.SequentialFrameFeatures;
import com.driemworks.ar.utils.NativeUtils;
import com.driemworks.common.utils.ImageConversionUtils;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;

import java.util.ArrayList;
import java.util.List;

/**
 * Monocular visual odometry using FAST/ORB/Hamming distance
 *
 * @author Tony
 *
 */
public class FeatureService {

    /** The detector */
    private FeatureDetector detector;

    /** The descriptor descriptorExtractor */
    private DescriptorExtractor descriptorExtractor;

    /** The descriptorMatcher */
    private DescriptorMatcher descriptorMatcher;

    /** The term criteria */
    private static final TermCriteria termCriteria = new TermCriteria(TermCriteria.EPS | TermCriteria.MAX_ITER, 30, 0.01);

    /**
     * Constructor for the FeatureService
     */
    public FeatureService() {
        // FAST feature detector
        detector = FeatureDetector.create(FeatureDetector.FAST);
        // ORB descriptor extraction
        descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        // bruteforce hamming metric
        descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
    }

    /**
     * Detect features in the input frame
     * @param frame the frame in which we are detecting features
     * @return the feature wrapper, containing detected features and descriptors
     */
    public FeatureWrapper featureDetection(Mat frame) {
        Log.d(this.getClass().getCanonicalName(), "START - featureDetection");
        long startTime = System.currentTimeMillis();
        MatOfKeyPoint mKeyPoints = new MatOfKeyPoint();
        Mat mIntermediateMat = new Mat();

        detector.detect(frame, mKeyPoints);
        Log.d("keypoints: ", "" + mKeyPoints.checkVector(2));
        descriptorExtractor.compute(frame, mKeyPoints, mIntermediateMat);
        Log.d("FeatureService: ", "keypoints: " + mKeyPoints);
        Log.d(this.getClass().getCanonicalName(), "END - featureDetection - time elapsed: " + (System.currentTimeMillis() - startTime) + " ms");
        return new FeatureWrapper("fast", "orb", frame, mIntermediateMat, mKeyPoints, null);
    }

    /**
     * Track features from a previous frame into the current frame
     * @param previousFrameGray the previous frame in grayscale
     * @param currentFrameGray the current frame in grayscale
     * @param previousKeyPoints the previously detected key points
     * @param status the status mat
     * @param err the error mat
     * @return the original features, as well as the features tracked into the current frame
     */
    public SequentialFrameFeatures featureTracking(Mat previousFrameGray, Mat currentFrameGray,
                                                   MatOfKeyPoint previousKeyPoints,
                                                   MatOfByte status, MatOfFloat err) {
        Log.d(this.getClass().getCanonicalName(), "START - featureTracking");
        long startTime = System.currentTimeMillis();

        // filter out points not tracked in current frame
        MatOfPoint2f previousKeyPoints2f = ImageConversionUtils.convertMatOfKeyPointsTo2f(previousKeyPoints);
        MatOfPoint2f previousKPConverted = new MatOfPoint2f();
        previousKeyPoints2f.convertTo(previousKPConverted, CvType.CV_32FC2);
        Log.d("previousKeyPoints2f ", "checkVector: " + previousKPConverted.checkVector(2));

        MatOfPoint2f currentKeyPoints2f = new MatOfPoint2f();

        Video.calcOpticalFlowPyrLK(previousFrameGray, currentFrameGray,
                previousKeyPoints2f, currentKeyPoints2f,
                status, err, new Size(21, 21), 0, termCriteria, /** flags */0, 0.001);

        byte[] statusArray = status.toArray();
        int indexCorrection = 0;

        List<Point> previousKeyPointsList = previousKeyPoints2f.toList();
        Log.d("previousKeyPointsList", "size: " + previousKeyPointsList.size());

        List<Point> currentKeyPointsList = currentKeyPoints2f.toList();
        Log.d("currentKeyPointsList", "size: " + currentKeyPointsList.size());

//        for (int i = 0; i < currentKeyPointsList.size(); i++) {
//            Point pt = currentKeyPointsList.get(i - indexCorrection);
//
//            if (statusArray[i] == 0 || (pt.x == 0 || pt.y == 0)) {
//                // removes points which are tracked off screen
//                if (pt.x == 0 || pt.y == 0) {
//                    statusArray[i] = 0;
//                }
//                // remove points for which tracking has failed
//                previousKeyPointsList.remove(i - indexCorrection);
//                currentKeyPointsList.remove(i - indexCorrection);
//                indexCorrection++;
//            }
//        }

        Log.d(this.getClass().getCanonicalName(), "END - featureTracking - time elapsed: " + (System.currentTimeMillis() - startTime) + " ms");
        return new SequentialFrameFeatures(previousKeyPointsList, currentKeyPointsList);
    }

    /**
     * @return Pair of new, FILTERED, last and current POINTS, or null if it hasn't managed to track anything.
     */
    public Pair<Point[], Point[]> track(final Mat lastImg, final Mat currentImg, Point[] lastPoints){
        final int size = lastPoints.length;
        final MatOfPoint2f currentPointsMat = new MatOfPoint2f();
        final MatOfPoint2f pointsFBMat = new MatOfPoint2f();
        final MatOfByte statusMat = new MatOfByte();
        final MatOfFloat errSimilarityMat = new MatOfFloat();
        final MatOfByte statusFBMat = new MatOfByte();
        final MatOfFloat errSimilarityFBMat = new MatOfFloat();
//        MatOfPoint2f test = new MatOfPoint2f();
//        Imgproc.cvtColor(new MatOfPoint(lastPoints), test, CvType.CV_32F);
//        Log.d(FeatureService.class.getCanonicalName(), "check vector of test: " + test.checkVector(2, CvType.CV_32F, true));
        Log.d(FeatureService.class.getCanonicalName(), "check vector of new MatOfPoint2f from last points: " + new MatOfPoint2f(lastPoints).checkVector(2, CvType.CV_32F, true));
        Log.d(FeatureService.class.getCanonicalName(), "check vector of new MatOfPoint2f from currentPointsMat: " + currentPointsMat.checkVector(2, CvType.CV_32F, true));

        MatOfPoint2f prevPointsTest = new MatOfPoint2f();
        MatOfPoint2f prevPoints = new MatOfPoint2f(lastPoints);
        prevPoints.convertTo(prevPointsTest, CvType.CV_32F);
        Log.d(FeatureService.class.getCanonicalName(), "check vector of new MatOfPoint2f from prevPointsTest: " + prevPointsTest.checkVector(2, CvType.CV_32F, true));

        //Forward-Backward tracking
        Video.calcOpticalFlowPyrLK(lastImg, currentImg, new MatOfPoint2f(lastPoints), currentPointsMat,
                statusMat, errSimilarityMat, WINDOW_SIZE, MAX_LEVEL, termCriteria, 0, LAMBDA);
        Video.calcOpticalFlowPyrLK(currentImg, lastImg, currentPointsMat, pointsFBMat,
                statusFBMat, errSimilarityFBMat, WINDOW_SIZE, MAX_LEVEL, termCriteria, 0, LAMBDA);

        final byte[] status = statusMat.toArray();
        float[] errSimilarity = new float[lastPoints.length];
        //final byte[] statusFB = statusFBMat.toArray();
        final float[] errSimilarityFB = errSimilarityFBMat.toArray();

        // compute the real FB error (relative to LAST points not the current ones...
        final Point[] pointsFB = pointsFBMat.toArray();
        for(int i = 0; i < size; i++){
            errSimilarityFB[i] = NativeUtils.norm(pointsFB[i], lastPoints[i]);
        }

        final Point[] currPoints = currentPointsMat.toArray();
        // compute real similarity error
        errSimilarity = normCrossCorrelation(lastImg, currentImg, lastPoints, currPoints, status);


        //TODO  errSimilarityFB has problem != from C++
        // filter out points with fwd-back error > the median AND points with similarity error > median
        return filterPts(lastPoints, currPoints, errSimilarity, errSimilarityFB, status);
    }

    private static final int MAX_COUNT = 20;
    private static final double EPSILON = 0.03;
    private static final Size WINDOW_SIZE = new Size(4, 4);
    private static final int MAX_LEVEL = 5;
    private static final float LAMBDA = 0f; // minEigenThreshold
    private static final Size CROSS_CORR_PATCH_SIZE = new Size(10, 10);

    /**
     * @return real similarities errors
     */
    private float[] normCrossCorrelation(final Mat lastImg, final Mat currentImg, final Point[] lastPoints, final Point[] currentPoints, final byte[] status){
        final float[] similarity = new float[lastPoints.length];

        final Mat lastPatch = new Mat(CROSS_CORR_PATCH_SIZE, CvType.CV_8U);
        final Mat currentPatch = new Mat(CROSS_CORR_PATCH_SIZE, CvType.CV_8U);
        final Mat res = new Mat(new Size(1, 1), CvType.CV_32F);

        for(int i = 0; i < lastPoints.length; i++){
            if(status[i] == 1){
                Imgproc.getRectSubPix(lastImg, CROSS_CORR_PATCH_SIZE, lastPoints[i], lastPatch);
                Imgproc.getRectSubPix(currentImg, CROSS_CORR_PATCH_SIZE, currentPoints[i], currentPatch);
                Imgproc.matchTemplate(lastPatch, currentPatch, res, Imgproc.TM_CCOEFF_NORMED);

                similarity[i] = NativeUtils.getFloat(0, 0, res);
            }else{
                similarity[i] = 0f;
            }
        }

        return similarity;
    }

    float errFBMed;

    /**
     * @return Pair of new, FILTERED, last and current POINTS. Null if none were valid (with similarity > median and FB error <= median)
     */
    private Pair<Point[], Point[]> filterPts(final Point[] lastPoints, final Point[] currentPoints, final float[] similarity, final float[] errFB, final byte[] status){
        final List<Point> filteredLastPoints = new ArrayList<>();
        final List<Point> filteredCurrentPoints = new ArrayList<>();
        final List<Float> filteredErrFB = new ArrayList<>();

        final float similarityMed = NativeUtils.median(similarity);
        Log.i(NativeUtils.TAG, "Filter points MED SIMILARITY: " + similarityMed);

        for(int i = 0; i < currentPoints.length; i++){
            if(status[i] == 1 && similarity[i] > similarityMed){
                filteredLastPoints.add(lastPoints[i]);
                filteredCurrentPoints.add(currentPoints[i]);
                filteredErrFB.add(errFB[i]);
            }
        }

        final List<Point> filteredLastPoints2 = new ArrayList<>();
        final List<Point> filteredCurrentPoints2 = new ArrayList<>();
        if(filteredErrFB.size() > 0){
            errFBMed = NativeUtils.median(filteredErrFB);

            for(int i = 0; i < filteredErrFB.size(); i++){
                // status has already been checked
                if(filteredErrFB.get(i) <= errFBMed){
                    filteredLastPoints2.add(filteredLastPoints.get(i));
                    filteredCurrentPoints2.add(filteredCurrentPoints.get(i));
                }
            }

            Log.i(NativeUtils.TAG, "Filter points MED ErrFB: " + errFBMed + " K count=" + filteredLastPoints2.size());
        }

        final int size = filteredLastPoints2.size();
        return size > 0 ? new Pair<Point[], Point[]>(filteredLastPoints2.toArray(new Point[size]), filteredCurrentPoints2.toArray(new Point[size])) : null;
    }

    public FeatureDetector getDetector() {
        return detector;
    }

    public void setDetector(FeatureDetector detector) {
        this.detector = detector;
    }

    public DescriptorExtractor getDescriptorExtractor() {
        return descriptorExtractor;
    }

    public void setDescriptorExtractor(DescriptorExtractor descriptorExtractor) {
        this.descriptorExtractor = descriptorExtractor;
    }

    public DescriptorMatcher getDescriptorMatcher() {
        return descriptorMatcher;
    }

    public void setDescriptorMatcher(DescriptorMatcher descriptorMatcher) {
        this.descriptorMatcher = descriptorMatcher;
    }
}
