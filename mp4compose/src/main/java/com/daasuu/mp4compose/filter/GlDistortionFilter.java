package com.daasuu.mp4compose.filter;

import android.opengl.GLES20;

public class GlDistortionFilter extends GlFilter {

    private static final String FRAGMENT_SHADER = "" +
            "precision mediump float;\n" +
            "varying highp vec2 vTextureCoord;\n" +
            "uniform lowp sampler2D sTexture;" +
            "uniform float xWaveLength;" +
            "uniform float xAmount;" +
            "uniform float yWaveLength;" +
            "uniform float yAmount;" +

            "void main(){" +
            "   float y = vTextureCoord.y + sin((vTextureCoord.y / yWaveLength)) * yAmount;" +
            "   float x = vTextureCoord.x + sin((vTextureCoord.x / xWaveLength)) * xAmount;" +
            "   vec2 uv = vec2(x,y);" +
            "   gl_FragColor = texture2D(sTexture,uv);" +
            "}";

    private float xWaveLength;
    private float yWaveLength;
    private float xAmount;
    private float yAmount;



    public GlDistortionFilter()
    {
        super(DEFAULT_VERTEX_SHADER,FRAGMENT_SHADER);
        init();
    }

    private void init() {

        xWaveLength = 0.010f;
        yWaveLength = 0.010f;
        xAmount = 0.02f;
        yAmount = 0.02f;
    }

    @Override
    protected void onDraw() {

        GLES20.glUniform1f(getHandle("xWaveLength"), xWaveLength);
        GLES20.glUniform1f(getHandle("yWaveLength"), yWaveLength);
        GLES20.glUniform1f(getHandle("xAmount"), xAmount);
        GLES20.glUniform1f(getHandle("yAmount"), yAmount);
    }

    public void setWaveLength(float xWaveLength, float yWaveLength) {
        this.xWaveLength = xWaveLength;
        this.yWaveLength = yWaveLength;
    }

    public void setDistortionAmountInX(float xAmount)
    {
        this.xAmount = xAmount;
    }

    public void setDistortionAmountInY(float yAmount)
    {
        this.yAmount = yAmount;
    }

    public float getxAmount() {
        return xAmount;
    }

    public float getyAmount() {
        return yAmount;
    }
}
