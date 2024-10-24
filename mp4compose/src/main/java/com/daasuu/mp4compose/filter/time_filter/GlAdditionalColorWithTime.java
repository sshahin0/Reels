package com.daasuu.mp4compose.filter.time_filter;

import com.daasuu.mp4compose.filter.GlAdditionalColorFilter;
import com.daasuu.mp4compose.gl.GlFramebufferObject;

public class GlAdditionalColorWithTime extends GlFilterWithTime {

    private GlAdditionalColorFilter glAdditionalColorFilter;
    private float time;

    public GlAdditionalColorWithTime() {
        glAdditionalColorFilter = new GlAdditionalColorFilter();
    }

    public GlAdditionalColorWithTime(float[] bias) {
        glAdditionalColorFilter = new GlAdditionalColorFilter(bias);
    }

    @Override
    public void setup() {
        glAdditionalColorFilter.setup();
    }

    @Override
    public void release() {
        glAdditionalColorFilter.release();
    }

    @Override
    public void draw(int texName, GlFramebufferObject fbo) {
        updateBias();
        glAdditionalColorFilter.draw(texName, fbo);
    }

    @Override
    public void setFrameSize(int width, int height) {
        glAdditionalColorFilter.setFrameSize(width, height);
    }

    public void updateBias() {

        time = getInputTimePeriod();
        float[] bias = new float[]{0.0f, 0.0f, 0.0f, 0.0f};

        if (time < 0.1f) {
            bias[0] = 0.8f;//(float) Math.sin((time*3)*Math.PI);
        } else if (time < 0.2f) {
            bias[1] = 0.8f;//(float) Math.sin(((time-1f/3f)*3)*Math.PI);
        } else if (time < 0.3f) {
            bias[2] = 0.8f;//(float) Math.sin(((time-2f/3f)*3)*Math.PI);
        } else if (time < 0.4f) {
            bias[0] = 0.4f;
            bias[1] = 0.7f;//(float) Math.sin((time*3)*Math.PI);
        } else if (time < 0.5f) {
            bias[1] = 0.7f;
            bias[2] = 0.4f;//(float) Math.sin(((time-1f/3f)*3)*Math.PI);
        } else if (time < 0.6f) {
            bias[0] = 0.7f;
            bias[2] = 0.4f;//(float) Math.sin(((time-2f/3f)*3)*Math.PI);
        } else if (time < 0.7f) {
            bias[0] = 0.7f;
            bias[1] = 0.5f;//(float) Math.sin((time*3)*Math.PI);
        } else if (time < 0.8f) {
            bias[1] = 0.6f;
            bias[2] = 0.2f;//(float) Math.sin(((time-1f/3f)*3)*Math.PI);
        } else if (time < 0.9f) {
            bias[0] = 0.2f;
            bias[2] = 0.5f;//(float) Math.sin(((time-2f/3f)*3)*Math.PI);
        } else {

        }

        glAdditionalColorFilter.setBias(bias);
    }
}
