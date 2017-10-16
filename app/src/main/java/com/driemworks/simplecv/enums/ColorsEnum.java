package com.driemworks.simplecv.enums;

import com.threed.jpct.RGBColor;

/**
 * Created by Tony on 7/14/2017.
 */

public enum ColorsEnum {

    YELLOW_ACTIVE(new RGBColor(255, 187, 0)),
    RED(new RGBColor(255, 0, 0));

    private RGBColor color;

    ColorsEnum(RGBColor color) {
        this.color = color;
    }

    public RGBColor getColor() {
        return color;
    }
}
