package com.driemworks.simplecv.enums;

/**
 * Created by Tony on 5/6/2017.
 */

public enum Tags {

    GraphicsRenderer("Graphics Renderer: "),
    ConfigurationActivity("Configuration Activity: "),
    GameRenderer("Game Renderer: "),
    GameActivity("Game Activity: "),
    OpenCvActivity("OpenCvActivity: ");

    private String tag;

    Tags(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

}
