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
 *
 */
public class ShapeDetectorTest {

    ShapeDetector shapeDetector;

    Mat image;

    /** load the opencv lib */
    static {
        System.loadLibrary("opencv_java3");
    }

    @Before
    public void setup() {
        shapeDetector = new ShapeDetector();
        image = new Mat(400, 400, CvType.CV_32F);
        image.setTo(new Scalar(255, 255, 255));
        Imgproc.rectangle(image, new Point(100, 100), new Point(200, 200), new Scalar(0,0,0), 2);
        Imgcodecs.imwrite("D:/work/rect.jpg", image);
    }

    @Test
    public void can_locate_vertices() throws Exception {
        List<Pair> vertices = shapeDetector.findVertices(image);
        Assert.assertNotNull(vertices);
    }
}