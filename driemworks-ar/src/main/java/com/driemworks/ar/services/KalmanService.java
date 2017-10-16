package com.driemworks.ar.services;

import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.video.KalmanFilter;

/**
 * Created by Tony on 5/14/2017.
 */

public class KalmanService {

    /** The Kalman Filter */
    private KalmanFilter kalmanFilter;

    /** The initial state of the detected surface */
    private Point initialState;
    /** The point predicted by the kalman filter */
    private Point predictPt;
    /** The state estimated by the kalman filter */
    private Point newState;

    /** Placeholders for the predictions, measurements, and estimations
     * made by the kalman filter
     */
    private Mat prediction;
    private Mat measurement;
    private Mat estimation;

    private double deltaTime;

    /**
     * Constructor for the kalman service
     */
    public KalmanService(Point initialState) {
        this.initialState = initialState;
        newState = new Point();
        predictPt  = new Point();

        prediction = new Mat(4, 1, CvType.CV_32F);
        estimation = new Mat(4, 1, CvType.CV_32F);

        initKalmanFilter();
    }

    public KalmanService(Point pt, double dt, double Accel_noise_mag){
        newState = new Point();
        predictPt  = new Point();

        prediction = new Mat(4, 1, CvType.CV_32F);
        estimation = new Mat(4, 1, CvType.CV_32F);

        kalmanFilter = new KalmanFilter(4,2,0,CvType.CV_32F);
        deltaTime = dt;

        Mat transitionMatrix = new Mat(4,4,CvType.CV_32F,new Scalar(0));
        float[] tM = {1,0,1,0,0,1,0,1,0,0,1,0,0,0,0,1};
        transitionMatrix.put(0,0,tM);
        kalmanFilter.set_transitionMatrix(transitionMatrix);
        initialState = pt;

        // init measurement matrix
        measurement = Mat.eye(2, 4, CvType.CV_32F);
        measurement.setTo(new Scalar(0));
        kalmanFilter.set_measurementMatrix(measurement);

        Mat statePre = new Mat(4,1,CvType.CV_32F,new Scalar(0));
        statePre.put(0,0,pt.x);
        statePre.put(1,0,pt.y);
        statePre.put(2,0,0);
        statePre.put(3,0,0);
        kalmanFilter.set_statePre(statePre);

        Mat statePost = new Mat(4,1,CvType.CV_32F,new Scalar(0));
        statePost.put(0,0,pt.x);
        statePost.put(1,0,pt.y);
        statePost.put(2,0,0);
        statePost.put(3,0,0);
        kalmanFilter.set_statePost(statePost);
        kalmanFilter.set_measurementMatrix(Mat.eye(2,4,CvType.CV_32F));

        Mat processNoiseCov = new Mat(4,4,CvType.CV_32F,new Scalar(0));
        float[] dTime = {(float)(Math.pow(deltaTime,4.0) / 4.0),0,
                        (float)(Math.pow(deltaTime,3.0) / 2.0),0,0,
                        (float)(Math.pow(deltaTime,4.0) / 4.0),0,
                        (float)(Math.pow(deltaTime,3.0) / 2.0),
                        (float)(Math.pow(deltaTime,3.0) / 2.0),0,
                        (float)Math.pow(deltaTime,2.0),0,0,
                        (float)(Math.pow(deltaTime,3.0) / 2.0),0,
                        (float)Math.pow(deltaTime,2.0)};
        processNoiseCov.put(0,0,dTime);
        processNoiseCov = processNoiseCov.mul(processNoiseCov,Accel_noise_mag);
        kalmanFilter.set_processNoiseCov(processNoiseCov);

        Mat id1 = Mat.eye(2,2,CvType.CV_32F);
        id1=id1.mul(id1,1e-1);
        kalmanFilter.set_measurementNoiseCov(id1);

        Mat id2 = Mat.eye(4,4,CvType.CV_32F);
        id2=id2.mul(id2,.1);
        kalmanFilter.set_errorCovPost(id2);
    }


    /**
     *
     * @param measuredPoint
     */
    public void update(Point measuredPoint) {
        predict();
        try {
            correct(measuredPoint);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("HEY!", e.getMessage());
        }
    }

    /**
     * Predicts the next point
     */
    private void predict() {
        prediction = kalmanFilter.predict();
        predictPt.x = prediction.get(0,0)[0]; //predictPt.x = prediction.get(1,1)[0];
        predictPt.y = prediction.get(1,0)[0]; //predictPt.y = prediction.get(2,1)[0];
    }

    /**
     * Corrects the predicted value based on the measurement
     * @param measuredPoint
     */
    private void correct(Point measuredPoint) throws Exception {
        updateMeasurement(measuredPoint);
        estimation = kalmanFilter.correct(measurement);
        newState.x = estimation.get(0, 0)[0];
        newState.y = estimation.get(1, 0)[0];
    }

    /**
     * Updates the measurement matrix based on the input measured point
     * @param measuredPoint The measured point
     */
    private void updateMeasurement(Point measuredPoint) {
        measurement.put(0, 0, measuredPoint.x);
        measurement.put(1, 0, measuredPoint.y);
    }

    /**
     * Initializes the Kalman Filter
     */
    private void initKalmanFilter() {
        kalmanFilter = new KalmanFilter(4, 2, 0, CvType.CV_32F);

        // setup transition matrix
        Mat transitionMatrix = new Mat(4, 4, CvType.CV_32F, new Scalar(0));
        float[] tM = {1, 0, 1, 0,
                      0, 1, 0, 1,
                      0, 0, 1, 0,
                      0, 0, 0, 1};
        transitionMatrix.put(0, 0, tM);
        kalmanFilter.set_transitionMatrix(transitionMatrix);

        // init measurement matrix
        measurement = Mat.eye(2, 4, CvType.CV_32F);
        measurement.setTo(new Scalar(0));
        kalmanFilter.set_measurementMatrix(measurement);

        // init state pre matrix
        Mat statePre = new Mat(4,1, CvType.CV_32F);
        statePre.put(0, 0, initialState.x);
        statePre.put(1, 0, initialState.y);
        statePre.put(2, 0, 0);
        statePre.put(3, 0, 0);
        kalmanFilter.set_statePre(statePre);

        // set noise covariance matrix
        Mat processNoiseCov = Mat.eye(4,4,CvType.CV_32F);
        processNoiseCov = processNoiseCov.mul(processNoiseCov,1e-1);
        kalmanFilter.set_processNoiseCov(processNoiseCov);

        // measurement noise covariance matrix
        Mat measurementNoiseCov = Mat.eye(2, 2, CvType.CV_32F);
        measurementNoiseCov = measurementNoiseCov.mul(measurementNoiseCov,1e-1);
        kalmanFilter.set_measurementNoiseCov(measurementNoiseCov);

        // set error covariance post
        Mat id2 = Mat.eye(4, 4 ,CvType.CV_32F);
        id2=id2.mul(id2,0.1);
        kalmanFilter.set_errorCovPost(id2);
    }

    public KalmanFilter getKalmanFilter() {
        return kalmanFilter;
    }

    public void setKalmanFilter(KalmanFilter kalmanFilter) {
        this.kalmanFilter = kalmanFilter;
    }

    public Point getInitialState() {
        return initialState;
    }

    public void setInitialState(Point initialState) {
        this.initialState = initialState;
    }

    public Point getPredictPt() {
        return predictPt;
    }

    public void setPredictPt(Point predictPt) {
        this.predictPt = predictPt;
    }

    public Point getNewState() {
        return newState;
    }

    public void setNewState(Point newState) {
        this.newState = newState;
    }

    public Mat getPrediction() {
        return prediction;
    }

    public void setPrediction(Mat prediction) {
        this.prediction = prediction;
    }

    public Mat getMeasurement() {
        return measurement;
    }

    public void setMeasurement(Mat measurement) {
        this.measurement = measurement;
    }

    public Mat getEstimation() {
        return estimation;
    }

    public void setEstimation(Mat estimation) {
        this.estimation = estimation;
    }
}
