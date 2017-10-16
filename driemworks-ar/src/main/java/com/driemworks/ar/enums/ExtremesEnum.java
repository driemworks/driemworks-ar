package com.driemworks.ar.enums;

/**
 * Created by Tony on 6/18/2017.
 */

public enum ExtremesEnum {

    MIN("min"),
    MAX("max");

    private String value;

    ExtremesEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
