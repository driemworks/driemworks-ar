package com.driemworks.common.enums;

/**
 * Created by Tony on 6/18/2017.
 */

public enum Coordinates {

    X("X"),
    Y("X");

    private String value;

    Coordinates(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
