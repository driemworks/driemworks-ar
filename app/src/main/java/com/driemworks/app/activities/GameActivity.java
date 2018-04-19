package com.driemworks.app.activities;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import com.driemworks.app.R;
import com.driemworks.app.activities.base.AbstractVRActivity;
import com.driemworks.app.opengl.renderer.MyGLRenderer;
import com.driemworks.ar.services.SurfaceDetectionService;
import com.driemworks.common.dto.SurfaceDataDTO;
import com.driemworks.common.enums.Resolution;
import com.driemworks.common.utils.DisplayUtils;

import org.opencv.core.Core;
import org.opencv.core.Point;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * @author Tony
 */
public class GameActivity extends AbstractVRActivity implements View.OnTouchListener {

    /** Tbe width of the display */
    private static int screenWidth;

    /** The height of the display */
    private static int screenHeight;

    /** The surface detection service */
    private SurfaceDetectionService surfaceDetectionService;

    /** The color of the detected blob in HSV format */
    private Scalar mBlobColorHsv;

    private Mat mSpectrum;

    /** The size of the spectrum */
    private Size SPECTRUM_SIZE;

    /** The is color selected flag */
    private boolean isColorSelected;

    /** The surface data DTO */
    private SurfaceDataDTO surfaceDataDTO;

    /**
     * The renderer
     */
    private static GLSurfaceView.Renderer renderer;
    static {
//        renderer = new StaticCubeRenderer(Resolution.RES_STANDARD, false);
        renderer = new MyGLRenderer();
    }

    /**
     * The default constructor for the GameActivity
     */
    public GameActivity() {
        super(R.layout.game_layout, R.id.main_surface_view, R.id.gl_surface_view, renderer, Resolution.RES_STANDARD, true);
    }

    /**
     * The constructor for the OpenCV activity
     *
     * @param layoutResId         The layout id
     * @param openCVSurfaceViewId The open cv surface view
     * @param resolution          The resolution
     * @param implementOnTouch    The implement on touch activity flag
     */
    public GameActivity(int layoutResId, int openCVSurfaceViewId, int glSurfaceViewId, Resolution resolution, boolean implementOnTouch) {
        super(layoutResId, openCVSurfaceViewId, glSurfaceViewId, renderer, resolution, implementOnTouch);
        android.graphics.Point displaySize = DisplayUtils.getScreenSize(this);
        this.screenWidth = displaySize.x;
        this.screenHeight = displaySize.y;
        mSpectrum = new Mat();
        mBlobColorHsv = new Scalar(0, 0, 0);
        SPECTRUM_SIZE = new Size(200, 64);
        isColorSelected = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        surfaceDetectionService = new SurfaceDetectionService();
        android.graphics.Point displaySize = DisplayUtils.getScreenSize(this);
        this.screenWidth = displaySize.x;
        this.screenHeight = displaySize.y;
        mSpectrum = new Mat();
        mBlobColorHsv = new Scalar(0, 0, 0);
        SPECTRUM_SIZE = new Size(200, 64);
        isColorSelected = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame frame) {
        super.setmRgba(frame.rgba());
        if (isColorSelected) {
            surfaceDataDTO = surfaceDetectionService.detect(super.getmRgba(), 0, true);
            return surfaceDataDTO.getmRgba();
        }
        return super.getmRgba();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
//        Point correctedCoordinate = DisplayUtils.correctCoordinate(event, screenWidth, screenHeight);
//        isColorSelected = DetectorUtils.selectColor(correctedCoordinate, super.getmRgba());
//        surfaceDetectionService.getColorBlobDetector().setHsvColor(mBlobColorHsv);
//        Imgproc.resize(surfaceDetectionService.getColorBlobDetector().getSpectrum(), mSpectrum, SPECTRUM_SIZE);


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

        surfaceDetectionService.getColorBlobDetector().setHsvColor(mBlobColorHsv);
        Imgproc.resize(surfaceDetectionService.getColorBlobDetector().getSpectrum(), mSpectrum, SPECTRUM_SIZE);

        isColorSelected  = true;
//        Log.d(TAG, "color has been set");

        touchedRegionRgba.release();
        touchedRegionHsv.release();

        return false;
    }
}
