package com.driemworks.app.activities;

import android.support.v4.app.FragmentActivity;

import com.driemworks.app.activities.interfaces.OpenARActivityInterface;

import org.opencv.android.CameraBridgeViewBase;

public abstract class AbstractARActivity extends FragmentActivity implements CameraBridgeViewBase.CvCameraViewListener2, OpenARActivityInterface {
}
