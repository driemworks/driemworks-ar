package com.driemworks.app.activities;

import android.os.Bundle;
import androidx.fragment.app.FragmentTransaction;
import android.util.Log;
import android.util.Pair;
import android.view.WindowManager;

import com.driemworks.app.R;
import com.driemworks.common.activities.AbstractARActivity;
import com.driemworks.common.fragments.OpenCVFragment;
import com.driemworks.common.fragments.OpenGLFragment;
import com.driemworks.ar.services.impl.FeatureDetectorServiceImpl;
import graphics.CubeRenderer;
import com.driemworks.common.utils.ImageConversionUtils;

import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

/**
 * The OpenAR Fragment Activity is an example of how to combine opencv and opengl in an activity
 *
 * @author Tony Riemer
 */
public class OpenARFragmentActivity extends AbstractARActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    /** The feature detector service */
    private FeatureDetectorServiceImpl featureDetectorService;

    /** The previous frame in grayscale */
    private Mat previousFrameGray;

    /** The previously detected key points */
    private MatOfKeyPoint previousKeyPoints = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // keep the screen on!
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // todo
        setRenderer(new CubeRenderer());
        setContentView(R.layout.opengl_opencv_layout);
        setGlSurfaceViewId(R.id.opengl_surface_view);
        setOpenCVSurfaceViewId(R.id.opencv_surface_view);
        // attach the fragments
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // add opengl fragment
        transaction.add(new OpenGLFragment(), "OpenGLFragment");
        // add opencv fragment
        transaction.add(new OpenCVFragment(), "OpenCVFragment");
        transaction.commit();
        featureDetectorService = new FeatureDetectorServiceImpl();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCameraViewStarted(int width, int height) {
        Log.i("OpenCvFragActiv: ", "called onCameraViewStarted.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCameraViewStopped() {
        Log.i("OpenCvFragActiv: ", "called onCameraViewStopped.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat mRgba = inputFrame.rgba();
        Mat mGray = inputFrame.gray();

        if (previousKeyPoints != null) {
            Log.d("previousKeyPoints", "are empty? " + previousKeyPoints.empty());
            Log.d("previousKeyPoints", "are empty? " + previousKeyPoints.empty());
        }

        if (previousKeyPoints == null || previousKeyPoints.toList().size() < 5) {
            previousKeyPoints = featureDetectorService.extractFeatureData(mRgba);
        } else {
            Pair<MatOfKeyPoint, MatOfKeyPoint> matchingKeypoints = featureDetectorService.trackKeyPoints(previousFrameGray, mGray, previousKeyPoints);
//            featureData.setKeyPoint(featureDetectorService.trackKeyPoints(previousFrameGray, mGray, featureData.getKeyPoint()).second);
            MatOfPoint2f previous2f = ImageConversionUtils.convertMatOfKeyPointsTo2f(matchingKeypoints.first);

            MatOfPoint2f next2f = ImageConversionUtils.convertMatOfKeyPointsTo2f(matchingKeypoints.second);
            for (Point p : next2f.toList()) {
                Imgproc.circle(mRgba, p, 5, new Scalar(0, 255, 0));
            }

            // if we haven't successfully tracked any keypoints, return the image
//            if (next2f.empty() || next2f.checkVector(2) < 0) {
//                return mRgba;
//            }

            previousKeyPoints = matchingKeypoints.second;
        }
        previousFrameGray = mGray;
        return mRgba;
    }

}
