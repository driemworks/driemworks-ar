package com.driemworks.driemworks_ar;

import com.driemworks.ar.utils.CvUtils;

import org.junit.Before;
import org.junit.Test;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import static org.junit.Assert.assertTrue;

/**
 * Test suite for the CameraPoseDTO
 */
public class CvUtilsTest {

    /** load the opencv lib */
    static {
        assertTrue(OpenCVLoader.initDebug(true));
    }

    @Before
    public void setup() {
//        OpenCvUtils.initOpenCV(true);
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
    }

}