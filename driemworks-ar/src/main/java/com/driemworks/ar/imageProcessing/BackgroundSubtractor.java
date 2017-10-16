package com.driemworks.ar.imageProcessing;

import org.opencv.core.Mat;
import org.opencv.video.BackgroundSubtractorMOG2;
import org.opencv.video.Video;

/**
 * Created by Tony on 7/3/2017.
 */
public class BackgroundSubtractor {

    /** The background subtractor mog2 */
    private BackgroundSubtractorMOG2 mog2;

    /**
     * Constructor
     * instantiates the mog2
     */
    public BackgroundSubtractor() {
        mog2 = Video.createBackgroundSubtractorMOG2();
    }

    /**
     * applies a foreground mask to the input image
     * @param mRgba the input image (to which the mask will be applied)
     */
    public Mat applyForegroundMask(Mat mRgba) {
        Mat fgMask = new Mat();
        mog2.apply(mRgba, fgMask);

        Mat output = new Mat();
        mRgba.copyTo(output, fgMask);
        return output;
    }
}
