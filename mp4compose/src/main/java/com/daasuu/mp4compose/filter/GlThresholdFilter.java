package com.daasuu.mp4compose.filter;

import android.opengl.GLES20;

public class GlThresholdFilter extends GlFilter {
    private static final String FRAGMENT_SHADER = "" +
            "precision mediump float;\n" +
            "varying highp vec2 vTextureCoord;\n" +
            "uniform lowp sampler2D sTexture;" +
            "uniform float threshold;" +
            "void main(){" +
            "   float luma = dot(texture2D(sTexture,vTextureCoord).rgb, vec3(0.2126, 0.7152, 0.0722));" +
            "   gl_FragColor = vec4(step(threshold, luma));" +
            "}";


    private float threshold;

    public GlThresholdFilter()
    {
        this(0.9f);
    }

    public GlThresholdFilter(float thresh)
    {
        super(DEFAULT_VERTEX_SHADER,FRAGMENT_SHADER);
        this.threshold = thresh;
    }



    @Override
    protected void onDraw() {
        GLES20.glUniform1f(getHandle("threshold"), threshold);

    }

    public void setThreshold(final float threshold) {
        this.threshold = threshold;
    }
}
