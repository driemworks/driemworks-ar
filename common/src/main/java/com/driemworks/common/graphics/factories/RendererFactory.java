package com.driemworks.common.graphics.factories;

import android.opengl.GLSurfaceView;

import com.driemworks.common.graphics.AbstractOrientationRenderer;
import com.driemworks.common.graphics.CubeRenderer;

public class RendererFactory {

    public static AbstractOrientationRenderer getRenderer(Class renderer) {
        if (CubeRenderer.class.equals(renderer)) {
            return new CubeRenderer();
        }
        return null;
    }

}
