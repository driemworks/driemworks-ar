package com.driemworks.app;

import org.junit.Test;

/**
 * Unit tests for the orientation utils
 */
public class OrientationUtilsTest {

    /**
     * Verify that we can correctly calculate the delta orientation
     * @throws Exception
     */
    @Test
    public void canCalculateDeltaOrientation() throws Exception {
        float[] dest = new float[3];
        float[] src1 = new float[3];
        src1[0]= 1;
        src1[1] = 0;
        src1[2] = 0;

        float[] src2 = new float[3];
        src2[0] = 1;
        src2[1] = 1;
        src2[2] = 0;
    }

}