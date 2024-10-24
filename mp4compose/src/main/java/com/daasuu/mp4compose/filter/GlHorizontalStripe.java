package com.daasuu.mp4compose.filter;

import android.opengl.GLES20;

public class GlHorizontalStripe extends GlFilter {


    private static final String HSTRIPE_FRAGMENT_SHADER =
            "precision mediump float;\n" +
            "varying highp vec2 vTextureCoord;\n" +
            "uniform lowp sampler2D sTexture;" +
            "uniform lowp float startIndx;" +
            "uniform lowp float endIndx;\n" +
            "void main() {\n" +
                    "vec4 otherColor = texture2D(sTexture, vTextureCoord);" +
            "   vec4 color = (vTextureCoord.y > startIndx && vTextureCoord.y < endIndx) ? vec4(1.0,1.0,1.0,1.0) : otherColor;"+
            "gl_FragColor = color;\n" +
            "}";

    private float startingIndex;
    private float endIndex;


    @Override
    public void setup() {
        super.setup();
    }

    public GlHorizontalStripe()
    {
        super(DEFAULT_VERTEX_SHADER,HSTRIPE_FRAGMENT_SHADER);
        setStartingIndex(100);
        setEndIndex(100);
    }

    @Override
    protected void onDraw() {

        GLES20.glUniform1f(getHandle("startIndx"), (startingIndex / (float) getOutputHeight()) );
        GLES20.glUniform1f(getHandle("endIndx"), (endIndex / (float) getOutputHeight()) );

    }

    public void setStartingIndex(float startingIndex) {
        this.startingIndex = startingIndex;
    }

    public void setEndIndex(float height) {
        this.endIndex =  (startingIndex + height);
    }

    @Override
    public void setFrameSize(int width, int height) {
        super.setFrameSize(width, height);
    }
}
