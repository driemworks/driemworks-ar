package com.driemworks.simplecv.enums;

/**
 * Created by Tony on 5/16/2017.
 */

public enum Resolution {

    /** the default resolution */
    RES_STANDARD(800, 480);

    private int width;
    private int height;

    Resolution(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
