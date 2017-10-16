package com.driemworks.ar.enums;

/**
 * Created by Tony on 5/6/2017.
 */

public enum Tags {

    SurfaceDetectionService("Surface Detect Srvc: "),
    DetectorUtils("Detector Utils: ");

    private String tag;

    Tags(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

}
