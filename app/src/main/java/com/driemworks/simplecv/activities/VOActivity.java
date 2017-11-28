package com.driemworks.simplecv.activities;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.OpenCVLoader;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;

import com.driemworks.ar.MonocularVisualOdometry.FeatureService;
import com.driemworks.ar.dto.FeatureWrapper;
import com.driemworks.ar.imageProcessing.ColorBlobDetector;
import com.driemworks.ar.services.SurfaceDetectionService;
import com.driemworks.common.dto.SurfaceDataDTO;
import com.driemworks.common.factories.BaseLoaderCallbackFactory;
import com.driemworks.common.utils.ImageConversionUtils;
import com.driemworks.simplecv.R;
import com.driemworks.simplecv.enums.Resolution;
import com.driemworks.simplecv.enums.Tags;
import com.driemworks.simplecv.layout.impl.ConfigurationLayoutManager;
import com.driemworks.simplecv.services.permission.impl.LocationPermissionServiceImpl;
import com.driemworks.simplecv.services.permission.impl.CameraPermissionServiceImpl;
import com.driemworks.common.views.CustomSurfaceView;
import com.driemworks.simplecv.utils.DisplayUtils;

import org.opencv.core.Size;
import org.opencv.video.Video;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class VOActivity extends Activity implements CvCameraViewListener2 {

    /** load the opencv lib */
    static {
        System.loadLibrary("opencv_java3");
    }

    /** The service to request permission to use camera at runtime */
    private CameraPermissionServiceImpl cameraPermissionService;

    /** The tag used for logging */
    private static final String TAG = "VOActivity: ";

    /** The input camera frame in RGBA format */
    private Mat mRgba;

    /** The customSurfaceView surface view */
    private CustomSurfaceView customSurfaceView;
    /** The width of the device screen */
    private int screenWidth;

    /** The height of the device screen */
    private int screenHeight;

    /** The base loader callback */
    private BaseLoaderCallback mLoaderCallback;

    /** The feature service */
    private FeatureService featureService;

    /** The default constructor */
    public VOActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);

        // get screen dimensions
        android.graphics.Point size = DisplayUtils.getScreenSize(this);
        screenWidth = size.x;
        screenHeight = size.y;

        if (!OpenCVLoader.initDebug()) {
            Log.e("OpvenCVLoader", "OvenCVLoader successful: false");
        } else {
            Log.d("OpenCVLoader", "OpenCVLoader successful");
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        cameraPermissionService = new CameraPermissionServiceImpl(this);

        setContentView(R.layout.main_surface_view);

        customSurfaceView = (CustomSurfaceView) findViewById(R.id.main_surface_view);
        customSurfaceView.setCvCameraViewListener(this);
        customSurfaceView.setMaxFrameSize(800, 480);

        mLoaderCallback = BaseLoaderCallbackFactory.getBaseLoaderCallback(this, customSurfaceView);
        featureService = new FeatureService();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "Called onPause");
        super.onPause();
        if (customSurfaceView != null) {
            customSurfaceView.disableView();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
        customSurfaceView.setMaxFrameSize(Resolution.RES_STANDARD.getWidth(), Resolution.RES_STANDARD.getHeight());
    }

    public void onDestroy() {
        super.onDestroy();
        customSurfaceView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        Log.d(TAG, "camera view started");
        mRgba = new Mat(height, width, CvType.CV_8UC4);
    }

    public void onCameraViewStopped() {
        Log.d(TAG, "camera view stopped");
        mRgba.release();
    }

    private Mat previousFrame = null;
    private FeatureWrapper previousWrapper = null;

    private float focal = 718.8560f;
    private Point pp = new Point(607.1928, 185.2157);

    /**
     *
     * @param inputFrame
     * @return
     */
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        // get the images from the input frame
        mRgba = inputFrame.rgba();
        Mat rgb = new Mat();
        Mat output = mRgba.clone();

        // if it is the first frame, no second frame will exist
        // so after extracting the features, we need to break

        // 1) features detection
        Imgproc.cvtColor(mRgba, rgb, Imgproc.COLOR_RGBA2RGB);
        FeatureWrapper wrapper = featureService.featureDetection(mRgba);
        // draw detected keypoints features
        Features2d.drawKeypoints(rgb, wrapper.getKeyPoints(), rgb);

        // first time through (frame 0)
        // just set the previous wrapper to the wrapper
        if (previousWrapper == null) {
            previousWrapper = FeatureWrapper.clone(wrapper);
            return output;
        }

        MatOfByte status = new MatOfByte();
        MatOfFloat err = new MatOfFloat();
        Map<String, List<Point>> trackedFeatures = featureService.featureTracking(previousWrapper, wrapper, status, err);
        for (Map.Entry<String, List<Point>> e : trackedFeatures.entrySet()) {
            Log.d(TAG, "tracked feature: " + e.toString());
        }
//        output = trackFeatures(mRgba, true);
//        Imgproc.cvtColor(rgb, output, Imgproc.COLOR_RGB2RGBA);



//
//        if (wrapper != null && previousFrame != null && previousWrapper != null && previousWrapper.getKeyPoints() != null) {
//            Mat mask = new Mat();
//            Mat R = new Mat();
//            Mat t = new Mat();
//            Log.d(TAG, "previous wrapper key points: " + previousWrapper.getKeyPoints().toString());
//            Log.d(TAG, "previous wrapper check vector on keypoints " + previousWrapper.getKeyPoints().checkVector(2));
//            Log.d(TAG, "previous wrapper check vector on descriptors " + previousWrapper.getDescriptors().checkVector(2));
//            // should be a vector of length 2?
//            if (previousWrapper.getKeyPoints().checkVector(2) > 0) {
//                Mat E = Calib3d.findEssentialMat(ImageConversionUtils.convertMatOfKeyPointsTo2f(wrapper.getKeyPoints()),
//                        ImageConversionUtils.convertMatOfKeyPointsTo2f(previousWrapper.getKeyPoints()),
//                        focal, pp, Calib3d.RANSAC, 0.999, 1.0, mask);
//                Calib3d.recoverPose(E, previousWrapper.getKeyPoints(), wrapper.getKeyPoints(), R, t, focal, pp);
//                // use R and t to track camera position
//                Log.d("Recovered Pose: ", "Calculated R and t: R: " + R.toString() + "\n" + "\t\t t: " + t.toString());
//            }
//        }

        previousFrame = mRgba.clone();
        previousWrapper = FeatureWrapper.clone(wrapper);
        return output;
    }

    /**
     *
     * @param frame
     * @param drawMatches
     * @return
     */
    private Mat trackFeatures(Mat frame, boolean drawMatches) {
        FeatureWrapper currentWrapper = featureService.featureDetection(frame);
        MatOfDMatch goodMatches = featureService.featureMatching(previousWrapper, currentWrapper);
        Mat outputImage =  new Mat();
        MatOfByte drawnMatches = new MatOfByte();
        if (frame.empty() || frame.size() == new Size(0, 0)) {
            return frame;
        }

        if (drawMatches) {
            Features2d.drawMatches(previousWrapper.getFrame(), previousWrapper.getKeyPoints(),
                    currentWrapper.getFrame(), currentWrapper.getKeyPoints(),
                    goodMatches, outputImage);
        }

        previousWrapper = FeatureWrapper.clone(currentWrapper);
        return outputImage;
    }

    /**
     * Handle the results of a permissions request
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == CameraPermissionServiceImpl.REQUEST_CODE) {
            cameraPermissionService.handleResponse(requestCode, permissions, grantResults);
        }
    }
}
