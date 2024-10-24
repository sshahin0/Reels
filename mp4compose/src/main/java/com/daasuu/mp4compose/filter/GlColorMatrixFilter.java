package com.daasuu.mp4compose.filter;

import android.opengl.GLES20;

public class GlColorMatrixFilter extends GlFilter{

    private static final String COLOR_MATRIX_FRAGMENT_SHADER = "" +
            "precision mediump float;\n" +
            "varying highp vec2 vTextureCoord;\n" +
            "uniform lowp sampler2D sTexture;" +
            "uniform lowp mat4 matrix;" +
            "uniform lowp float intensity;\n" +
            "\n" +
            "void main(){\n" +
            "    vec4 textureColor = texture2D(sTexture,vTextureCoord);\n" +
            "    vec4 outputColor = textureColor * matrix;\n" +
            "    gl_FragColor = outputColor * intensity + (1.0 - intensity) * textureColor;\n" +
            "}";


    private float intensity;

    private float[] colorMatrix;

    public GlColorMatrixFilter()
    {
        this(1.0f,new float[]{

                1.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f
        });
    }

    public GlColorMatrixFilter(float intensity, float[] colorMatrix) {

        super(DEFAULT_VERTEX_SHADER,COLOR_MATRIX_FRAGMENT_SHADER);
        this.intensity = intensity;
        this.colorMatrix = colorMatrix;
    }

    @Override
    protected void onDraw(){

        GLES20.glUniform1f(getHandle("intensity"), intensity);
        GLES20.glUniformMatrix4fv(getHandle("matrix"), 1, false, colorMatrix, 0);

    }

    public void setColorMatrix(float[] colorMatrix) {
        this.colorMatrix = colorMatrix;
    }

    public void setIntensity(float intensity)
    {
        this.intensity = intensity;
    }


}
