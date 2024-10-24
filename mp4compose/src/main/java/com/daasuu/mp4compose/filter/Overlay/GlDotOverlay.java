package com.daasuu.mp4compose.filter.Overlay;

import android.content.Context;
import android.opengl.GLES20;

import java.nio.FloatBuffer;

public class GlDotOverlay extends GlVideoOverlayFilter {

    static final String FRAGMENT_SHADER = OverlayShaderHelper.DOTS_FRAGMENT_SHADER;

    private float[] overlayColor;

    public GlDotOverlay(String datasrc)
    {
        super(DEFAULT_VERTEX_SHADER,FRAGMENT_SHADER);
        this.context = null;
        this.datasrc = datasrc;
        overlayColor = new float[]{1.0f,1.0f,1.0f};

    }
    public GlDotOverlay(Context context, int resId)
    {
        super(DEFAULT_VERTEX_SHADER,FRAGMENT_SHADER);
        this.context = context;
        this.dataSrcId = resId;
        this.datasrc = "";
        overlayColor = new float[]{1.0f,1.0f,1.0f};

    }


    @Override
    protected void onDraw() {

        GLES20.glUniform3fv(getHandle("dotColor"),1, FloatBuffer.wrap(overlayColor));
        super.onDraw();
    }

    public void setOverlayColor(float[] overlayColor) {
        this.overlayColor = overlayColor;
    }
}
