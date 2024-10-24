package com.daasuu.mp4compose.filter;

import android.opengl.GLES20;

public class GlLineScreenFilter extends GlFilter {

    static final String FRAGMENT_SHADER = "" +
            "precision highp float;\n" +
            "varying highp vec2 vTextureCoord;\n" +
            "uniform lowp sampler2D sTexture;" +
            "uniform vec2 texelSize;\n" +
            "uniform float scale;" +
            "uniform float saturation;" +
            "uniform float isGrayScale;" +
            "uniform float angle;" +
            "uniform float amount;" +
            "" +ShaderHelper.GRAY_SCALE_FUNC+
            "" +ShaderHelper.ROTATION_FUNC+
            "\n"+
            "void main() {\n" +
            "   vec3 color = texture2D(sTexture,vTextureCoord).rgb ;" +
            "   color = (isGrayScale) == 1.0 ?  grayScale(color) : color; " +
            "   vec2 p = rotation(angle) * vTextureCoord * texelSize * scale;" +
            "   float pattern = sin(p.x) * amount;" +
            "   color = color * 10.0 * saturation - 5.0 + pattern ;" +
            "gl_FragColor = vec4(color,1.0);\n" +
            "}\n";

    private float status;
    private float scale;
    private boolean isGrayScale;
    private float saturation;
    private float angle;
    private float inputAmount;


    public GlLineScreenFilter()
    {
        super(DEFAULT_VERTEX_SHADER,FRAGMENT_SHADER);
        isGrayScale = false;
        scale = 0.5f;
        saturation = 1.2f;
        inputAmount = 1.0f;
        angle = (float) (Math.PI/2f);

    }

    @Override
    protected void onDraw() {
        GLES20.glUniform1f(getHandle("scale"), scale);
        GLES20.glUniform2f(getHandle("texelSize"), (float)getOutputWidth(),(float)getOutputHeight());
        GLES20.glUniform1f(getHandle("angle"), angle);
        GLES20.glUniform1f(getHandle("amount"),inputAmount);
        GLES20.glUniform1f(getHandle("saturation"), saturation);
        status = 0.0f;
        if(isGrayScale){
            status = 1.0f;
        }
        GLES20.glUniform1f(getHandle("isGrayScale"), status);
    }

    public void setInputAmount(float inputAmount) {
        this.inputAmount = inputAmount;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    public void setGrayScale(boolean grayScale) {
        isGrayScale = grayScale;
    }

    public void setSaturation(float saturation) {
        this.saturation = saturation;
    }
}
