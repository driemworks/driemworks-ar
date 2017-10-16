package com.driemworks.sensor;

/**
 * Created by Tony on 7/4/2017.
 */

public enum Tags {

    LocationService("LocationService: ");

    private String tag;

    Tags(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }
}
