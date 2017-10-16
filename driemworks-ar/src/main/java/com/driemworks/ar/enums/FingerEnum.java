package com.driemworks.ar.enums;

/**
 * Created by Tony on 6/18/2017.
 */

public enum FingerEnum {

    PINKY("Pinky", 0, 4),
    INDEX("Index", 1, 3),
    MIDDLE("Middle", 2, 2),
    RING("Ring", 3, 1),
    THUMB("Thumb", 4, 0);

    private String name;

    private int indexLeft;

    private int indexRight;

    FingerEnum(String name, int indexLeft, int indexRight) {
        this.name = name;
        this.indexLeft = indexLeft;
        this.indexRight = indexRight;
    }

    public String getName() {
        return name;
    }

    public int getIndexLeft() {
        return indexLeft;
    }

    public int getIndexRight() {
        return indexRight;
    }
}
