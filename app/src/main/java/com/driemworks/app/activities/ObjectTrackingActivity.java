package com.driemworks.app.activities;

import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.driemworks.app.activities.base.AbstractOpenCVActivity;
import com.driemworks.ar.services.SurfaceDetectionService;
import com.driemworks.ar.services.impl.FeatureServiceImpl;
import com.driemworks.common.dto.FeatureDataDTO;
import com.driemworks.common.dto.SurfaceDataDTO;
import com.driemworks.common.utils.ImageConversionUtils;
import com.driemworks.common.utils.TagUtils;
import com.driemworks.app.R;
import com.driemworks.common.enums.Resolution;
import com.driemworks.app.services.permission.impl.CameraPermissionServiceImpl;
import com.driemworks.common.utils.DisplayUtils;

import org.opencv.core.Size;

/**
 * @author Tony
 */
public class ObjectTrackingActivity extends AbstractOpenCVActivity implements CvCameraViewListener2 {

    /** The tag used for logging */
    private final String TAG = TagUtils.getTag(this.getClass());

    /** The color of the detected blob in HSV format */
    private Scalar mBlobColorHsv;

    /** The color spectrum */
    private Mat mSpectrum;

    /** Boolean flag to tell if the color is or isn't selected in the detector */
    private boolean mIsColorSelected = false;

    /** The size of the spectrum */
    private Size SPECTRUM_SIZE;

    /** The surface Detection service */
    private SurfaceDetectionService surfaceDetector;

    /** The feature service */
    private FeatureServiceImpl featureService;

    /** The service to request permission to use camera at runtime */
    private CameraPermissionServiceImpl cameraPermissionService;

    /** The width of the device screen */
    private int screenWidth;

    /** The height of the device screen */
    private int screenHeight;

    /** The resolution of the screen */
    private static final Resolution resolution = Resolution.RES_STANDARD;

    /** The mode button */
    private Button modeButton;

    /** The surface data */
    private SurfaceDataDTO surfaceDataDTO;

    /** The reference data */
    private FeatureDataDTO referenceData = null;

    /** The placeholder for the current frame's feature data */
    private FeatureDataDTO currentFeatureData;

    /** The matches features between images */
    private MatOfDMatch matches;

    /** The default constructor */
    public ObjectTrackingActivity() {
        super(R.layout.main_surface_view, R.id.main_surface_view, resolution, true);
        Log.i(TAG, "Instantiated new " + this.getClass());
        featureService = new FeatureServiceImpl();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);

        modeButton = (Button) findViewById(R.id.mode_btn);
        // on click, if surface data is detected, set the reference data
        modeButton.setOnClickListener(v -> {
            if (surfaceDataDTO != null && surfaceDataDTO.getBoundRect() != null) {
                Mat referenceImage = super.getmRgba().submat(surfaceDataDTO.getBoundRect());
                Imgproc.resize(referenceImage, referenceImage, new Size(resolution.getWidth(), resolution.getHeight()));
                referenceData = new FeatureDataDTO(referenceImage);
                featureService.featureDetection(referenceData);
                Log.d(TAG, "reference data kp are empty: " + (referenceData.getKeyPoints().empty()));
            }
        });

        // get screen dimensions
        android.graphics.Point size = DisplayUtils.getScreenSize(this);
        screenWidth = size.x;
        screenHeight = size.y;
        cameraPermissionService = new CameraPermissionServiceImpl(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause() {
        Log.d(TAG, "Called onPause");
        super.onPause();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCameraViewStarted(int width, int height) {
        Log.d(TAG, "camera view started");
        super.onCameraViewStarted(width, height);
        initFields();
    }

    /**
     * Initialize the detector and detector params
     */
    private void initFields() {
        surfaceDetector = new SurfaceDetectionService(new Scalar(255, 255, 255, 255),
                new Scalar(222, 040, 255), new Mat(), new Size(200, 64), null);
        mSpectrum = new Mat();
        mBlobColorHsv = new Scalar(255);
        SPECTRUM_SIZE = new Size(200, 64);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCameraViewStopped() {
        Log.d(TAG, "camera view stopped");
        super.onCameraViewStopped();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // the value used to bound the size of the area to be sampled
        int sizeThreshold = 8;

        Point correctedCoordinate = DisplayUtils.correctCoordinate(event, screenWidth, screenHeight);
        Rect touchedRect = new Rect((int)correctedCoordinate.x, (int)correctedCoordinate.y, sizeThreshold, sizeThreshold);
        if (null == touchedRect) {
            return false;
        }

        // get the rectangle around the point that was touched
        Mat touchedRegionRgba = super.getmRgba().submat(touchedRect);

        // format to hsv
        Mat touchedRegionHsv = new Mat();
        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

        // Calculate average color of touched region
        mBlobColorHsv = Core.sumElems(touchedRegionHsv);
        int pointCount = touchedRect.width * touchedRect.height;

        for (int i = 0; i < mBlobColorHsv.val.length; i++) {
            mBlobColorHsv.val[i] /= pointCount;
        }

        surfaceDetector.getColorBlobDetector().setHsvColor(mBlobColorHsv);
        Imgproc.resize(surfaceDetector.getColorBlobDetector().getSpectrum(), mSpectrum, SPECTRUM_SIZE);

        mIsColorSelected = true;
        Log.d(TAG, "color has been set");

        touchedRegionRgba.release();
        touchedRegionHsv.release();

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        // get the images from the input frame
        super.setmRgba(inputFrame.rgba());
        super.setOutput(super.getmRgba());
        Log.d(TAG, "size of input: " + super.getmRgba().size());
        Log.d(TAG, "Called onCameraFrame");
        // do nothing if no color has been selected
        if (!mIsColorSelected) {
            Log.d(TAG, "No color selected, return input image");
            return super.getmRgba();
        }

        if (referenceData == null) {
            surfaceDataDTO = surfaceDetector.detect(super.getmRgba(), 0, true);
            Log.d(TAG, "return mRgba from surface data - reference data is null.");
            return surfaceDataDTO.getmRgba();
        } else {
            currentFeatureData = new FeatureDataDTO(super.getmRgba());
            featureService.featureDetection(currentFeatureData);
            // find matches
            if (currentFeatureData.isEmpty()) {
                return super.getmRgba();
            }
            matches = featureService.featureTracking(referenceData, currentFeatureData);
            if (!referenceData.getKeyPoints().empty() && !currentFeatureData.getKeyPoints().empty()) {
                // draw rect around matches
                RotatedRect minBoundingBox = Imgproc.minAreaRect(ImageConversionUtils
                        .convertTrainMatOfDMatchToMatOfPoint2f(matches, currentFeatureData.getKeyPoints()));
                Rect rect = new Rect((int)minBoundingBox.center.x, (int)minBoundingBox.center.y, 100, 100);
                Imgproc.rectangle(super.getOutput(), rect.tl(), rect.br(), new Scalar(0, 255, 0), 8);
                Log.d(TAG, "return output");
                return super.getOutput();
            } else {
                Log.d(TAG, "return mRgba");
                return super.getmRgba();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == CameraPermissionServiceImpl.REQUEST_CODE) {
            cameraPermissionService.handleResponse(requestCode, permissions, grantResults);
        }
    }

}
