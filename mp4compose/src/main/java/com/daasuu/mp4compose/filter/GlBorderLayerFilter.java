package com.daasuu.mp4compose.filter;

import android.opengl.GLES20;

import java.nio.FloatBuffer;

public class GlBorderLayerFilter extends GlFilter {

     private static final String FRAGMENT_SHADER =
             "precision highp float;\n" +
             "varying highp vec2 vTextureCoord;\n" +
             "uniform lowp sampler2D sTexture;\n" +
             "uniform vec2 texelSize;" +
             "uniform vec3 borderColor;" +
             "uniform float borderWidth;" +
             "uniform float cornerRadius;" +
             "\n"+
             "void main() {\n" +
             "  vec4 sourceColor = texture2D(sTexture, vTextureCoord);" +
             "  vec2 curCoord = vTextureCoord * texelSize;" +
             "  float x = (curCoord.x<borderWidth)? borderWidth: ((curCoord.x>(texelSize.x-borderWidth))? (texelSize.x-borderWidth): curCoord.x);" +
             "  float y = (curCoord.y<borderWidth)? borderWidth: ((curCoord.y>(texelSize.y-borderWidth))? (texelSize.y-borderWidth): curCoord.y);" +
             "  float d = distance(vec2(x,y),curCoord);" +
             "  float percent = smoothstep(0.0,cornerRadius,d);" +
             "gl_FragColor = vec4(mix(sourceColor.rgb,borderColor.rgb,percent),sourceColor.a);\n" +
             "}\n";

     private float borderWidth;
    private float[] borderColor;
    private float cornerRadius;

    public GlBorderLayerFilter(){
        super(DEFAULT_VERTEX_SHADER,FRAGMENT_SHADER);
        borderWidth = 0.1f;
        cornerRadius = 100f;
        borderColor = new float[]{0.0f,0.0f,1.0f};
    }

    @Override
    protected void onDraw() {

        GLES20.glUniform1f(getHandle("cornerRadius"), cornerRadius);
        GLES20.glUniform2f(getHandle("texelSize"), (float)getOutputWidth(),(float)getOutputHeight());
        GLES20.glUniform1f(getHandle("borderWidth"), borderWidth * getOutputWidth());
        GLES20.glUniform3fv(getHandle("borderColor"),1, FloatBuffer.wrap(borderColor));
    }


    public void setBorderColor(float[] borderColor) {
        this.borderColor = borderColor;
    }

    public void setBorderWidth(float borderWidth) {
        this.borderWidth = borderWidth;
    }

    public void setCornerRadius(float cornerRadius) {
        this.cornerRadius = cornerRadius;
    }
}
