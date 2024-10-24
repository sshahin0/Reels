package com.daasuu.mp4compose.filter;

import android.opengl.GLES20;

import java.nio.FloatBuffer;

public class GlFalseColorFilter extends GlFilter {

    private static final String FRAGMENT_SHADER =
                    "precision highp float;\n" +
                    "varying highp vec2 vTextureCoord;\n" +
                    "uniform lowp sampler2D sTexture;\n" +
                    "uniform vec3 firstColor;" +
                    "uniform vec3 secondColor;" +
                    "void main() {\n" +
                    "   vec4 baseColor = texture2D(sTexture, vTextureCoord);" +
                    "   float luma = dot(baseColor.rgb, vec3(0.3,0.59,0.11)); \n"+
                    "   vec3 finalColor = mix(firstColor,secondColor, luma);" +
                    "gl_FragColor = vec4(finalColor,baseColor.a);\n" +
                    "}\n";

    private float[] firstColor;
    private float[] secondColor;

    public GlFalseColorFilter(){

        super(DEFAULT_VERTEX_SHADER,FRAGMENT_SHADER);
        firstColor = new float[]{0f,0f,0f};
        secondColor = new float[]{1f,1f,1f};
    }


    @Override
    protected void onDraw() {

        GLES20.glUniform3fv(getHandle("firstColor"),1, FloatBuffer.wrap(firstColor));
        GLES20.glUniform3fv(getHandle("secondColor"),1, FloatBuffer.wrap(secondColor));

    }

    public void setFirstColor(float[] firstColor) {

        this.firstColor = firstColor;
    }

    public void setSecondColor(float[] secondColor) {

        this.secondColor = secondColor;
    }

}
