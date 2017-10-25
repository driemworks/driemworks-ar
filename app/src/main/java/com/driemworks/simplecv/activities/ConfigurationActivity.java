package com.driemworks.simplecv.activities;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;

import com.driemworks.ar.imageProcessing.ColorBlobDetector;
import com.driemworks.ar.services.SurfaceDetectionService;
import com.driemworks.ar.utils.ImageProcessingUtils;
import com.driemworks.common.dto.SurfaceDataDTO;
import com.driemworks.common.factories.BaseLoaderCallbackFactory;
import com.driemworks.simplecv.R;
import com.driemworks.simplecv.enums.Resolution;
import com.driemworks.simplecv.enums.Tags;
import com.driemworks.simplecv.layout.impl.ConfigurationLayoutManager;
import com.driemworks.simplecv.services.permission.impl.LocationPermissionServiceImpl;
import com.driemworks.simplecv.services.permission.impl.CameraPermissionServiceImpl;
import com.driemworks.common.views.CustomSurfaceView;
import com.threed.jpct.World;

import org.opencv.core.Size;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 *
 */
public class ConfigurationActivity extends Activity implements OnTouchListener, CvCameraViewListener2 {

    /** load the opencv lib */
    static {
        System.loadLibrary("opencv_java3");
    }

    /** The service to request permission to use camera at runtime */
    private CameraPermissionServiceImpl cameraPermissionService;

    /** The service to request permission to get location data at runtime */
    private LocationPermissionServiceImpl locationPermissionService;

    /** The layout manager for this activity */
    private ConfigurationLayoutManager layoutManager;

    /** The tag used for logging */
    private static final String TAG = Tags.ConfigurationActivity.getTag();

    /** The input camera frame in RGBA format */
    private Mat mRgba;

    /** The customSurfaceView surface view */
    private CustomSurfaceView customSurfaceView;

    /** The threshold when detecting defects */
    double iThreshold = 0;

    /** The color of the detected blob in HSV format */
    private Scalar mBlobColorHsv;

    /** The color blob detector */
    private ColorBlobDetector mDetector;

    /** The color spectrum */
    private Mat mSpectrum;

    /** Boolean flag to tell if the color is or isn't selected in the detector */
    private boolean mIsColorSelected = false;

    /** The size of the spectrum */
    private Size SPECTRUM_SIZE;

    /** The surface Detection service */
    private SurfaceDetectionService surfaceDetector;

    /** The width of the device screen */
    private int screenWidth;

    /** The height of the device screen */
    private int screenHeight;

    /** The base loader callback */
    private BaseLoaderCallback mLoaderCallback;

    private int currentPositionX = 100;
    private int currentPositionY = 100;

    private double touchedX;
    private double touchedY;

    private Point refPt = new Point(Resolution.RES_STANDARD.getWidth() - 100, Resolution.RES_STANDARD.getHeight()/2);

    /** The default constructor */
    public ConfigurationActivity() {
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
        Display display = getWindowManager().getDefaultDisplay();
        android.graphics.Point size = new android.graphics.Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;

        if (!OpenCVLoader.initDebug()) {
            Log.e("OpvenCVLoader", "OvenCVLoader successful: false");
        } else {
            Log.d("OpenCVLoader", "OpenCVLoader successful");
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        cameraPermissionService = new CameraPermissionServiceImpl(this);

        locationPermissionService = new LocationPermissionServiceImpl(this);

        setContentView(R.layout.main_surface_view);

        customSurfaceView = (CustomSurfaceView) findViewById(R.id.main_surface_view);
        customSurfaceView.setCvCameraViewListener(this);
        customSurfaceView.setOnTouchListener(ConfigurationActivity.this);
        customSurfaceView.setMaxFrameSize(800, 480);

        layoutManager = ConfigurationLayoutManager.getInstance();
        layoutManager.setActivity(this);
        layoutManager.setup(ConfigurationLayoutManager.CONFIG_BUTTON, findViewById(R.id.mode_btn));

        mLoaderCallback = BaseLoaderCallbackFactory.getBaseLoaderCallback(this, customSurfaceView);
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
        mRgba = new Mat();
        initFields(width, height);
    }

    /**
     * @param width
     * @param height
     */
    private void initFields(int width, int height) {
        surfaceDetector = new SurfaceDetectionService(new Scalar(255, 255, 255, 255),
                new Scalar(222, 040, 255), new Mat(), new Size(200, 64), null);
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mDetector = new ColorBlobDetector();
        mSpectrum = new Mat();
        mBlobColorHsv = new Scalar(255);
        SPECTRUM_SIZE = new Size(200, 64); // TODO make a constant
    }

    public void onCameraViewStopped() {
        Log.d(TAG, "camera view stopped");
        mRgba.release();
    }

    /**
     * Calculates the coordinate on the screen corrected for resolution and screen size
     * @param event the touch event
     * @return the correct coordinates
     */
    private Point correctCoordinate(MotionEvent event) {
        double x = (event.getX() * com.driemworks.common.enums.Resolution.RES_STANDARD.getWidth()) / screenWidth;
        double y = (event.getY() * com.driemworks.common.enums.Resolution.RES_STANDARD.getHeight()) / screenHeight;
        return new Point(x, y);
    }

    /**
     * The onTouch method -> samples the color of the touch region
     * @param v
     * @param event
     * @return
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (true) {
            touchedX = correctCoordinate(event).x;
            touchedY = correctCoordinate(event).y;
        } else {
            // the value used to bound the size of the area to be sampled
            int sizeThreshold = 10;

            Point correctedCoordinate = correctCoordinate(event);
            Rect touchedRect = new Rect((int) correctedCoordinate.x, (int) correctedCoordinate.y, sizeThreshold, sizeThreshold);
            if (null == touchedRect) {
                return false;
            }

            // get the rectangle around the point that was touched
            Mat touchedRegionRgba = mRgba.submat(touchedRect);

            // format to hsv
            Mat touchedRegionHsv = new Mat();
            Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

            // Calculate average color of touched region
            mBlobColorHsv = Core.sumElems(touchedRegionHsv);
            int pointCount = touchedRect.width * touchedRect.height;

            for (int i = 0; i < mBlobColorHsv.val.length; i++) {
                mBlobColorHsv.val[i] /= pointCount;
            }

            mDetector = surfaceDetector.getColorBlobDetector();
            mDetector.setHsvColor(mBlobColorHsv);
            surfaceDetector.setColorBlobDetector(mDetector);
            Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);

            mIsColorSelected = true;
            Log.d(TAG, "color has been set");

            touchedRegionRgba.release();
            touchedRegionHsv.release();
        }
        return true;
    }

    /**
     *
     * @param inputFrame
     * @return
     */
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        // get the images from the input frame
        mRgba = inputFrame.rgba();
        // circle to serve as a reference point for moving the piece
        Imgproc.circle(mRgba, refPt, 50, new Scalar(0, 0, 255));
        // TODO - this all needs to be rewritten later
        if (touchedX > 0 || touchedY > 0) {
            Imgproc.circle(mRgba, new Point(touchedX, touchedY), 10, new Scalar(255, 255, 255));
            // set the position of the concentric circles
            if (touchedY == 0) {
                if (touchedX > refPt.x) {
                    currentPositionX += 5;
                } else if (touchedX < refPt.x) {
                    currentPositionX -= 5;
                }
            } else if (touchedX == 0) {
                if (touchedY > refPt.y) {
                    currentPositionX += 5;
                } else if (touchedY < refPt.y) {
                    currentPositionY -= 5;
                }
            } else if (touchedX > refPt.x) {
                currentPositionX += 2;
                if (touchedY > refPt.y) {
                    currentPositionY += 2;
                } else if (touchedY < refPt.y) {
                    currentPositionY -= 2;
                }
            } else if (touchedX < refPt.x) {
                currentPositionX -= 2;
                if (touchedY > refPt.y) {
                    currentPositionY += 2;
                } else if (touchedY < refPt.y) {
                    currentPositionY -= 2;
                }
            }
        }
        // do nothing if no color has been selected
//      if (!mIsColorSelected) {
        if (Boolean.TRUE) {
            Log.d(TAG, "No color selected, return input image");
//            return mRgba;
            return detectCannyEdges(mRgba);
        }
        SurfaceDataDTO surfaceData = surfaceDetector.detect(mRgba, iThreshold, true);
//        return surfaceData.getmRgba();
        return detectCannyEdges(surfaceData.getmRgba());

    }

    Mat solidRect;

    int velocity = 10;

    /**
     *
     * @param image
     * @return
     */
    private Mat
    detectCannyEdges(Mat image) {
        if (image == null) {
            return image;
        }

        Mat mHierarchy = new Mat();

        Mat detectedEdges = new Mat(image.size(), CvType.CV_8UC1);
        Imgproc.cvtColor(image, detectedEdges, Imgproc.COLOR_BGR2GRAY, 40);
        Imgproc.Canny(detectedEdges, detectedEdges, 80, 100);
        Imgproc.threshold(detectedEdges, detectedEdges, 100, 255, Imgproc.THRESH_BINARY);

        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(detectedEdges, contours, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        if (contours.isEmpty()) {
            return image;
        }

        RotatedRect rect = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(0).toArray()));

        Map<Integer, Rect> rectMap = ImageProcessingUtils.getBoundedRect(rect, contours);
        int boundPos = (int) rectMap.keySet().toArray()[0];
        Rect boundRect = rectMap.get(boundPos);
        if (boundRect != null) {

            solidRect = new Mat(boundRect.size(), CvType.CV_8UC4);
        }

        MatOfPoint2f pointMat = new MatOfPoint2f();
        Imgproc.approxPolyDP(new MatOfPoint2f(contours.get(boundPos).toArray()), pointMat, 1.7, true);
        contours.set(boundPos, new MatOfPoint(pointMat.toArray()));

        MatOfInt hull = new MatOfInt();
//        MatOfInt4 convexDefect = new MatOfInt4();
        Imgproc.convexHull(new MatOfPoint(contours.get(boundPos).toArray()), hull);

        // ensure that there are at least three points, since we must have a convex polygon
        if (hull.toArray().length <= 3) {
            return image;
        }

        ///////////////////////

        if (contours.size() >= 2) {
            RotatedRect rect1 = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(1).toArray()));

            Map<Integer, Rect> rectMap1 = ImageProcessingUtils.getBoundedRect(rect1, contours);
            int boundPos1 = (int) rectMap.keySet().toArray()[0];
            Rect boundRect1 = rectMap.get(boundPos1);
            if (boundRect1 != null) {
//            solidRect = new Mat(boundRect1.size(), CvType.CV_8UC4);
            }

            MatOfPoint2f pointMat1 = new MatOfPoint2f();
            Imgproc.approxPolyDP(new MatOfPoint2f(contours.get(boundPos).toArray()), pointMat1, 1.7, true);
            contours.set(boundPos1, new MatOfPoint(pointMat1.toArray()));

            MatOfInt hull1 = new MatOfInt();
//        MatOfInt4 convexDefect = new MatOfInt4();
            Imgproc.convexHull(new MatOfPoint(contours.get(boundPos1).toArray()), hull1);

            // ensure that there are at least three points, since we must have a convex polygon
            if (hull1.toArray().length <= 3) {
                return image;
            }
        }


        ///////////////////////////
        // TODO
        if (currentPositionX < 0) {
            currentPositionX = 0;}
        if (currentPositionY < 0) {
            currentPositionY = 0;}
        else if (currentPositionX - boundRect.br().x > 0 && currentPositionX - boundRect.br().x <= velocity) {
            if (boundRect.tl().y - currentPositionY > 0 && boundRect.tl().y - currentPositionY <= velocity) {
                currentPositionY -= velocity;
            } else if (currentPositionY - boundRect.br().y > 0 && currentPositionY - boundRect.br().y <= velocity) {
                currentPositionY += velocity;
            } else {
                currentPositionX += velocity;
            }
        } else if (boundRect.tl().x - currentPositionX > 0 && boundRect.tl().x - currentPositionX <= velocity) {
            if (boundRect.tl().y - currentPositionY > 0 && boundRect.tl().y - currentPositionY <= velocity) {
                currentPositionY -= velocity;
            } else if (currentPositionY - boundRect.br().y > 0 && currentPositionY - boundRect.br().y <= velocity) {
                currentPositionY += velocity;
            } else {
                currentPositionX -= velocity;
                touchedX = refPt.x + 1;
                touchedY = refPt.y + 1;
            }
        } else if (boundRect.tl().y - currentPositionY > 0 && boundRect.tl().y - currentPositionY <= velocity) {
            if (currentPositionX - boundRect.br().x > 0 && currentPositionX - boundRect.br().x <= velocity) {
                currentPositionX += velocity;
            } else if (boundRect.tl().x - currentPositionX > 0 && boundRect.tl().x - currentPositionX <= velocity) {
                currentPositionX -= velocity;
                touchedX = refPt.x + 1;
                touchedY = refPt.y + 1;
            } else {
                currentPositionY -= velocity;
                touchedX = refPt.x + 1;
                touchedY = refPt.y + 1;
            }
            } else if (currentPositionY - boundRect.br().y > 0 && currentPositionY - boundRect.br().y <= velocity) {
                if (currentPositionX - boundRect.br().x > 0 && currentPositionX - boundRect.br().x <= velocity) {
                    currentPositionX += velocity;
                } else if (boundRect.tl().x - currentPositionX > 0 && boundRect.tl().x - currentPositionX <= velocity) {
                    currentPositionX -= velocity;
                    touchedX = refPt.x + 50;
                    touchedY = refPt.y - 50;
                } else {
                    currentPositionY += velocity;
                }
            }

        Imgproc.circle(image, new Point(currentPositionX, currentPositionY), 15, new Scalar(255, 0, 0));
        Imgproc.circle(image, new Point(currentPositionX, currentPositionY), 10, new Scalar(0, 0, 255));
        Imgproc.circle(image, new Point(currentPositionX, currentPositionY), 5, new Scalar(0, 255, 0));

//        Imgproc.convexityDefects(new MatOfPoint(contours.get(boundPos).toArray()), hull, convexDefect);

//        List<Point> listPo = ImageProcessingUtils.getListOfPoints(contours, hull, boundPos);
//
//        for (Point p : listPo) {
//            Imgproc.circle(image, p, 6, new Scalar(255, 200, 255));
//        }

        Log.w(TAG, "bound rect null? = " + (boundRect == null));
//        Imgproc.rectangle(image, boundRect.tl(), boundRect.br(), new Scalar(0, 255, 0), 2, 8, 0);


//        solidRect.setTo(new Scalar(0, 255, 0));
//        Mat submat = image.submat(boundRect);
//        solidRect.copyTo(submat);

        return image ;
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
        } else if (requestCode == LocationPermissionServiceImpl.REQUEST_CODE) {
            locationPermissionService.handleResponse(requestCode, permissions, grantResults);
        }
    }

    public double getiThreshold() {
        return iThreshold;
    }

    public Scalar getmBlobColorHsv()  {
        return mBlobColorHsv;
    }

}

