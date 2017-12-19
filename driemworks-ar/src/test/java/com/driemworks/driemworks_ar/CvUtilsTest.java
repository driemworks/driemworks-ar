package com.driemworks.driemworks_ar;

import com.driemworks.ar.imageProcessing.ShapeDetector;
import com.driemworks.ar.utils.CvUtils;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Test suite for the CameraPoseDTO
 */
public class CvUtilsTest {

    /** load the opencv lib */
    static {
        System.loadLibrary("opencv_java3");
    }

    @Test
    public void testCanAdd() {
        // init a 3 x 1 mat (i.e. vector)
        // (0 1 1)
        Mat m1 = new Mat(3, 1, CvType.CV_64FC1);
        m1.put(0,0, 0);
        m1.put(1,0, 1);
        m1.put(2,0, 1);

        CvUtils.printMat(m1);

//        Mat m2 = m1.clone();
//        Mat m3 = CvUtils.add(m1, m2);
//        assertTrue(m3.empty());
    }

}