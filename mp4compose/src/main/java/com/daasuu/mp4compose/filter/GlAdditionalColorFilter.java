package com.daasuu.mp4compose.filter;

import android.opengl.GLES20;

import java.nio.FloatBuffer;

public class GlAdditionalColorFilter extends GlFilter{

    private static final String ADDITIONAL_COLOR_FRAGMENT_SHADER = "" +
            "precision highp float;\n" +
            "varying highp vec2 vTextureCoord;\n" +
            "uniform lowp sampler2D sTexture;\n" +
            "uniform vec4 bias;"+
            "void main() {\n" +
            "   vec4 color =  texture2D(sTexture, vTextureCoord);" +
            "   color.rgb = color.rgb + bias.rgb; "+
            "   color.a *= bias.a;" +
            "   gl_FragColor = color;\n" +
            "}\n";

    private float[] bias;

    public GlAdditionalColorFilter(){
        this(new float[]{
                0.0f,0.0f,0.0f,0.0f
        });
    }

    public GlAdditionalColorFilter(float[] bias) {
        super(DEFAULT_VERTEX_SHADER,ADDITIONAL_COLOR_FRAGMENT_SHADER);
        this.bias = bias;
    }

    @Override
    protected void onDraw() {
        GLES20.glUniform4fv(getHandle("bias"),1, FloatBuffer.wrap(bias));
    }


    public void setBias(float[] bias) {
        this.bias = bias;
    }

}
