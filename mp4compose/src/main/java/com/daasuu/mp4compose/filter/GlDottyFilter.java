package com.daasuu.mp4compose.filter;

import android.opengl.GLES20;

public class GlDottyFilter extends GlFilter {

    static final String FRAGMENT_SHADER= "" +
            "precision highp float;\n" +
            "varying highp vec2 vTextureCoord;\n" +
            "uniform lowp sampler2D sTexture;\n" +
            "uniform float intensity;" +
            "uniform vec2 texelSize;" +
            "\n"+
            "void main() {\n" +
            "gl_FragColor = texture2D(sTexture, vTextureCoord) * 2. - length(fract( (vTextureCoord * texelSize) * (intensity/10.)) - 0.5);\n" +
            "}\n";
    private float intensity;

    public GlDottyFilter(){
        super(DEFAULT_VERTEX_SHADER,FRAGMENT_SHADER);

        setIntensity(0.3f);
    }


    @Override
    protected void onDraw() {

        GLES20.glUniform1f(getHandle("intensity"), intensity);
        GLES20.glUniform2f(getHandle("texelSize"), (float)getOutputWidth(),(float)getOutputHeight());
    }

    public void setIntensity(float intensity) {
        this.intensity = 1.0f - intensity;
    }
}
