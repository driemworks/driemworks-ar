package com.driemworks.simplecv.enums;

/**
 * The Resolution enumeration
 * @author Tony
 */
public enum Resolution {

    /** the default resolution */
    RES_STANDARD(800, 480),
    THREE_TWENTY_BY_TWO_FORTY(320, 240);

    /**
     * The width
     */
    private int width;

    /**
     * The height
     */
    private int height;

    /**
     * Constructor for the Resolution
     * @param width The width
     * @param height The height
     */
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
