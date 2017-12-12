package com.driemworks.driemworks_ar;

import com.driemworks.ar.imageProcessing.ShapeDetector;

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
public class CameraPoseDTOTest {

    /** load the opencv lib */
    static {
        System.loadLibrary("opencv_java3");
    }

    @Test
    public void testCanAdd() {

    }

}