package com.driemworks.driemworks_ar;

import com.driemworks.common.utils.OpenCvUtils;

import org.junit.Before;
import org.junit.Test;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 * Created by Tony on 3/25/2018.
 */

public class FeatureServiceImplTest {

    private static final String DOLLAR_BILL_REFERENCE = "src/test/resources/images/dollar_bill_ref.jpg";

    @Before
    public void setup() {
        OpenCvUtils.initOpenCV(true);
    }

    @Test
    public void testCanMatchFeatures() {
        Mat dollarBillRef = Imgcodecs.imread(DOLLAR_BILL_REFERENCE);
        Imgcodecs.imwrite("src/test/resources/images/dollar_bill_save.jpg", dollarBillRef);
    }

}
