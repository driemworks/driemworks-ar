package com.driemworks.ar.enums;

/**
 * Created by Tony on 6/18/2017.
 */

public enum HandEnum {

    RIGHT("Right"),
    LEFT("Left");

    private String value;

    HandEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
