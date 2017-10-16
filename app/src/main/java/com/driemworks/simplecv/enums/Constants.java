package com.driemworks.simplecv.enums;

/**
 * Created by Tony on 5/6/2017.
 */

public enum Constants {

    CONFIG_DTO("config");

    private String value;

    Constants(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
