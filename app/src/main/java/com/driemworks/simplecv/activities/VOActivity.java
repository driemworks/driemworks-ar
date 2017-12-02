package com.driemworks.simplecv.activities;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.OpenCVLoader;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.CvType;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.WindowManager;

import com.driemworks.ar.MonocularVisualOdometry.FeatureService;
import com.driemworks.ar.dto.FeatureWrapper;
import com.driemworks.ar.dto.SequentialFrameFeatures;
import com.driemworks.common.factories.BaseLoaderCallbackFactory;
import com.driemworks.common.utils.ImageConversionUtils;
import com.driemworks.simplecv.R;
import com.driemworks.simplecv.enums.Resolution;
import com.driemworks.simplecv.services.permission.impl.CameraPermissionServiceImpl;
import com.driemworks.common.views.CustomSurfaceView;
import com.driemworks.simplecv.utils.DisplayUtils;

/**
 * @author Tony
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

    /** The previous feature wrapper */
    private FeatureWrapper previousWrapper = null;

    /** The current feature wrapper */
    private FeatureWrapper wrapper = null;

    /** The points detected in the "previous" image */
    private MatOfPoint2f currentPoints;

    /** The points detected in the "current" image */
    private MatOfPoint2f previousPoints;

    // TODO calculate intrinsic params of camera (camera calibration)
    private float focal = 718.8560f;
    private Point pp = new Point(607.1928, 185.2157);

    /** The current sequential frame features */
    private SequentialFrameFeatures sequentialFrameFeatures;

    private Mat gray;
    private Mat intermediateMat;
    private Mat output;

    /** The default constructor */
    public VOActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    private Mat r;
    private Mat t;

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

        r = new Mat();
        t = new Mat();

        gray = new Mat();
        intermediateMat = new Mat();
        output = new Mat();

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

    @Override
    public void onDestroy() {
        super.onDestroy();
        customSurfaceView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        Log.d(TAG, "camera view started");
        mRgba = new Mat(height, width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
        Log.d(TAG, "camera view stopped");
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        Log.d(TAG, "START - onCameraFrame");
        long startTime = System.currentTimeMillis();
        // get the image from the input frame
        mRgba = inputFrame.rgba();
        gray = inputFrame.gray();
        intermediateMat = new Mat();
        Imgproc.cvtColor(mRgba, intermediateMat, Imgproc.COLOR_RGBA2RGB);
        output = mRgba.clone();

        // detect and (optionally) draw key points
        wrapper = featureService.featureDetection(mRgba);
        if (false) {
            Features2d.drawKeypoints(intermediateMat, wrapper.getKeyPoints(), intermediateMat);
            Imgproc.cvtColor(intermediateMat, output, Imgproc.COLOR_RGB2RGBA);
        }

        // track into next image
        if (previousWrapper != null && previousWrapper.getFrame() != null) {
            MatOfByte status = new MatOfByte();
            MatOfFloat err = new MatOfFloat();
            Mat mask = new Mat();
            // previous image, current image, previous keypoints
            sequentialFrameFeatures = featureService.featureTracking(
                    /* already grayscale */previousWrapper.getFrameAsGrayscale(), gray,
                    previousWrapper.getKeyPoints(),
                    status, err);

            currentPoints = ImageConversionUtils.convertListToMatOfPoint2f(sequentialFrameFeatures.getCurrentFrameFeaturePoints());
            previousPoints = ImageConversionUtils.convertListToMatOfPoint2f(sequentialFrameFeatures.getPreviousFrameFeaturePoints());

            // calculate the essential matrix
            Mat e = Calib3d.findEssentialMat(currentPoints, previousPoints,
                    focal, pp, Calib3d.RANSAC, 0.99, 1.0, mask);

            // calculate rotation and translation matrices
            Calib3d.recoverPose(e, currentPoints, previousPoints, r, t, focal, pp, mask);
//            Log.d(TAG, "Calculated rotation matrix: " + R.toString());
//            Log.d(TAG, "Calculated translation matrix " + t.get(0,0).length);
        }

        previousWrapper = FeatureWrapper.clone(wrapper);
        Log.d(TAG, "END - onCameraFrame - time elapsed: " + (System.currentTimeMillis() - startTime) + " ms");
        return output;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == CameraPermissionServiceImpl.REQUEST_CODE) {
            cameraPermissionService.handleResponse(requestCode, permissions, grantResults);
        }
    }
}
