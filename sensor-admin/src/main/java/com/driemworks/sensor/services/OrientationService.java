package com.driemworks.sensor.services;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 *
 * @author Tony
 *
 * Service for calculating the rotation and translation of the device
 * relative to the device's starting point.
 *
 * This service may only be used provided the device is equipped with
 * an accelerometer and a geomagnetic sensor.
 *
 * The class calling this service must implement SensorEventListener.
 *
 */
public class OrientationService {

    private final String TAG = "OrientationService: ";

    /** The sensor manager */
    private SensorManager sensorManager;

    /** The rotation vector sensor
     * Represents the orientation ofthe deviceas a combination of an angle and an axis,
     * in which the device has rotated through an angle theta around an axis (x, y, or z)
     */
    private Sensor rotationVectorSensor;

    /** The accelerometer */
    private Sensor accelerationSensor;

    /** The magnetic field sensor */
    private Sensor magnetSensor;

    public static float[] mRotationMatrix = new float[16];

    private float[] accelerationData = new float[3];
    private float[] magnetData = new float[3];
    private float[] rotationData = new float[3];

    private float[] bufferedAccelGData = new float[3];
    private float[] bufferedMagnetData = new float[3];


    /**
     *
     * @param sensorManager
     */
    public OrientationService(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        accelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        magnetSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        initRotationMatrix();
    }

    private void initRotationMatrix() {
        mRotationMatrix[ 0] = 1;
        mRotationMatrix[ 4] = 1;
        mRotationMatrix[ 8] = 1;
        mRotationMatrix[12] = 1;
    }

    /**
     * For now, we will use average acceleration.
     *
     * If the observer's position at time t is r(t) = (x(t), y(t), z(t)),
     * and the acceleration from time t to t + dT is given by a(t + dT),
     * then the observer's position at time t + dT is:
     *
     *              r(t + dT) = r(t) + (1/2)a*dT^2
     *
     *  We are assuming that acceleration is constant. This is going to be
     *  fairly unreliable...
     *
     */
    public float[] calcPositionFromAcceleration(SensorEvent event, float[] previousPosition, double dTime) {
        float[] position = new float[3];

        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            accelerationData = event.values;
            for (int index = 0; index < 3; index++) {
                position[index] = previousPosition[index] + (float) (accelerationData[index] * (Math.pow(dTime, 2) / 2));
            }
        }

        return position;
    }

    /**
     * Must be called from within onResume();
     * @param eventListener
     */
    public void registerListeners(SensorEventListener eventListener) {
        sensorManager.registerListener(eventListener, accelerationSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(eventListener, rotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(eventListener, magnetSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * Helper method for calculating our current position based on accelerometer readings.
     *
     * Uses the acceleration vector to calculated ((1/2)a_{axis}*t^2 for some time t
     * where axis =x, y, or z
     *
     */
    public float[] calcTranslationVector(SensorEvent event, double dTime) {
        float[] position = new float[3];

        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            accelerationData = event.values;
            for (int index = 0; index < 3; index++) {
                position[index] = (float)(accelerationData[index] * (Math.pow(dTime, 2) / 2));
            }
        }

        return position;
    }

    public float[] calcDeviceOrientationVector(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            rotationData = event.values.clone();
        }
        return rotationData;
    }

    public float[] calcDeviceOrientationMatrix(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(
                    mRotationMatrix, event.values);
            rotationData = event.values;
            Log.d("****", "rotation data size: " + rotationData.length);
            for (float f : rotationData) {
                Log.d("****", "value is: " + f);
            }
        }
            return rotationData;
    }

    /**
     *
     * @param event
     */
    public float[] updateRotationMatrix(SensorEvent event) {
        Log.d(TAG, "called updateRotationMatrix");
        final int type = event.sensor.getType();
        if (type == Sensor.TYPE_LINEAR_ACCELERATION) {
            accelerationData = event.values.clone();
            Log.d("*****", "accelerometer data: " + logArray(accelerationData));
        } else if (type == Sensor.TYPE_MAGNETIC_FIELD) {
            magnetData = event.values.clone();
            Log.d("*****", "magnet data: " + logArray(magnetData));
        }


        rootMeanSquareBuffer(bufferedAccelGData, accelerationData);
        rootMeanSquareBuffer(bufferedMagnetData, magnetData);
        SensorManager.getRotationMatrix(mRotationMatrix, null,
                bufferedAccelGData, bufferedMagnetData);
        Log.d("*****", "calculated rotation matrix: " + logArray(mRotationMatrix));
        return mRotationMatrix;
    }

    private String logArray(float[] in) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (float f : in) {
            sb.append(f);
            sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }


    private void rootMeanSquareBuffer(float[] target, float[] values) {
        final float amplification = 200.0f;
        float buffer = 20.0f;

        target[0] += amplification;
        target[1] += amplification;
        target[2] += amplification;
        values[0] += amplification;
        values[1] += amplification;
        values[2] += amplification;

        target[0] = (float) (Math
                .sqrt((target[0] * target[0] * buffer + values[0] * values[0])
                        / (1 + buffer)));
        target[1] = (float) (Math
                .sqrt((target[1] * target[1] * buffer + values[1] * values[1])
                        / (1 + buffer)));
        target[2] = (float) (Math
                .sqrt((target[2] * target[2] * buffer + values[2] * values[2])
                        / (1 + buffer)));

        target[0] -= amplification;
        target[1] -= amplification;
        target[2] -= amplification;
        values[0] -= amplification;
        values[1] -= amplification;
        values[2] -= amplification;
    }

    public static float[] getmRotationMatrix() {
        return mRotationMatrix;
    }

    public static void setmRotationMatrix(float[] mRotationMatrix) {
        OrientationService.mRotationMatrix = mRotationMatrix;
    }
}

















