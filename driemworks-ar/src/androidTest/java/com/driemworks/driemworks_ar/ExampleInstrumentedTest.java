package com.driemworks.driemworks_ar;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.driemworks.ar.utils.CvUtils;
import com.driemworks.common.utils.OpenCvUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    static {
//        System.loadLibrary("opencv_java3");
    }

    @Before
    public void setup() {
        OpenCvUtils.initOpenCV(true);
    }

    @Test
    public void testCanAdd() {
        // init a 3 x 1 mat (i.e. vector)
        // (0 1 1)
        Mat m1 = new Mat(3, 1, CvType.CV_64FC1);
        m1.put(0,0, 0);
        m1.put(1,0, 1);
        m1.put(2,0, 1);
        Mat m2 = m1.clone();
        Mat m3 = CvUtils.add(m1, m2);
        assertEquals(0, m3.get(0,0)[0], 0.0001);
    }


}
