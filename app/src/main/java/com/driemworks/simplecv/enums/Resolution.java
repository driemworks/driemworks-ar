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

    /**
     * Getter for the width
     * @return width The width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Setter for the width
     * @param width The width to set
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Getter for the height
     * @return The height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Setter for the height
     * @param height The height to set
     */
    public void setHeight(int height) {
        this.height = height;
    }
}
