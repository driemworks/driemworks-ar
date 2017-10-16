package com.driemworks.simplecv.utils;

import com.threed.jpct.SimpleVector;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Utility class for making random decisions
 */
public class RandomUtils {

    private RandomUtils() {}

    /**
     * Decides a random SimpleVector in the region
     * [0, width] x [0, height]
     * @param width the width of the frame
     * @param height the height of the frame
     * @return a random SimpleVector
     */
    public static SimpleVector decideSimpleVector(int width, int height, float z) {
       return new SimpleVector(decideFloatInRange(0, width),
               decideFloatInRange(0, height), z);
    }

    /**
     * Calculate a random float between the two bounds
     * @param lowerBound the lower bound
     * @param upperBound the upper bound
     * @return the randomly decided float
     */
    public static float decideFloatInRange(int lowerBound, int upperBound) {
        return (float)ThreadLocalRandom.current().nextInt(lowerBound, upperBound + 1);
    }

    /**
     * Calculate a random integer between the two bounds
     * @param lowerBound the lower bound
     * @param upperBound the upper bound
     * @return the random integer
     */
    public static int decideIntegerInRange(int lowerBound, int upperBound) {
        return ThreadLocalRandom.current().nextInt(lowerBound, upperBound + 1);
    }

    /**
     * Chooses a random object in the array
     * @param arr the array of objects
     * @return the randomly chosen object
     */
    public static <T extends Object> T decide(T[] arr) {
        return arr[(int)decideIntegerInRange(0, arr.length - 1)];
    }
}
