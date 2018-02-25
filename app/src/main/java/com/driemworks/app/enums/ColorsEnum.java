package com.driemworks.app.enums;

import com.threed.jpct.RGBColor;

/**
 * The colors enumeration
 * @author Tony
 */
public enum ColorsEnum {

    YELLOW_ACTIVE(new RGBColor(255, 187, 0)),
    RED(new RGBColor(255, 0, 0));

    /**
     * The rgb color
     */
    private RGBColor color;

    /**
     * Constructor for the ColorsEnum
     * @param color The RGBColor
     */
    ColorsEnum(RGBColor color) {
        this.color = color;
    }

    /**
     * Getter for the color
     * @return color The RGBColor
     */
    public RGBColor getColor() {
        return color;
    }
}
