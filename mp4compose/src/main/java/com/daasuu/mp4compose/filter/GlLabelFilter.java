package com.daasuu.mp4compose.filter;

import android.opengl.GLES20;

import java.nio.FloatBuffer;

public class GlLabelFilter extends GlFilter {

    private static final String LEVELS_FRAGMET_SHADER =

            "precision mediump float;\n" +
                    "\n" +
                    "varying highp vec2 vTextureCoord;\n" +
                    "uniform sampler2D sTexture;\n" +
                    " uniform mediump vec3 levelMinimum;\n" +
                    " uniform mediump vec3 levelMiddle;\n" +
                    " uniform mediump vec3 levelMaximum;\n" +
                    " uniform mediump vec3 minOutput;\n" +
                    " uniform mediump vec3 maxOutput;\n" +
                    " \n" +
                    " void main()\n" +
                    " {\n" +
                    "     mediump vec4 textureColor = texture2D(sTexture,vTextureCoord);\n" +
                    "     \n" +
                    "     gl_FragColor = vec4( mix(minOutput, maxOutput, pow(min(max(textureColor.rgb -levelMinimum, vec3(0.0)) / (levelMaximum - levelMinimum  ), vec3(1.0)), 1.0 /levelMiddle)) , textureColor.a);\n" +
                    " }\n";


    private float[] min;
    private float[] mid;
    private float[] max;
    private float[] minOutput;
    private float[] maxOutput;

    public GlLabelFilter(){

        this(new float[]{0.0f, 0.0f, 0.0f}, new float[]{1.f, 1f, 1f}, new float[]{1.0f, 1.0f, 1.0f}, new float[]{0.0f, 0.0f, 0.0f}, new float[]{1.0f, 1.0f, 1.0f});

    }


    public GlLabelFilter(float[] min, float[] mid, float[] max, float[] minOut, float[] maxOut) {

        super(DEFAULT_VERTEX_SHADER,LEVELS_FRAGMET_SHADER);
        this.min = min;
        this.mid = mid;
        this.max = max;
        minOutput = minOut;
        maxOutput = maxOut;
    }

    public void setMin(float min, float mid, float max, float minOut, float maxOut) {
        setRedMin(min, mid, max, minOut, maxOut);
        setGreenMin(min, mid, max, minOut, maxOut);
        setBlueMin(min, mid, max, minOut, maxOut);
    }

    public void setMin(float min, float mid, float max) {
        setMin(min, mid, max, 0.0f, 1.0f);
    }

    public void setRedMin(float min, float mid, float max, float minOut, float maxOut) {
        this.min[0] = min;
        this.mid[0] = mid;
        this.max[0] = max;
        minOutput[0] = minOut;
        maxOutput[0] = maxOut;

    }

    public void setRedMin(float min, float mid, float max) {
        setRedMin(min, mid, max, 0, 1);
    }

    public void setGreenMin(float min, float mid, float max, float minOut, float maxOut) {
        this.min[1] = min;
        this.mid[1] = mid;
        this.max[1] = max;
        minOutput[1] = minOut;
        maxOutput[1] = maxOut;
    }

    public void setGreenMin(float min, float mid, float max) {
        setGreenMin(min, mid, max, 0, 1);
    }

    public void setBlueMin(float min, float mid, float max, float minOut, float maxOut) {
        this.min[2] = min;
        this.mid[2] = mid;
        this.max[2] = max;
        minOutput[2] = minOut;
        maxOutput[2] = maxOut;
    }


    public void setBlueMin(float min, float mid, float max) {
        setBlueMin(min, mid, max, 0, 1);
    }

    @Override
    protected void onDraw() {

        GLES20.glUniform3fv(getHandle("levelMinimum"),1, FloatBuffer.wrap(min));
        GLES20.glUniform3fv(getHandle("levelMiddle"),1,FloatBuffer.wrap( mid));
        GLES20.glUniform3fv(getHandle("levelMaximum"), 1,FloatBuffer.wrap(max));
        GLES20.glUniform3fv(getHandle("minOutput"), 1, FloatBuffer.wrap(minOutput));
        GLES20.glUniform3fv(getHandle("maxOutput"), 1,FloatBuffer.wrap(maxOutput));
    }
}