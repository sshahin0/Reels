package com.daasuu.mp4compose.filter.time_filter;

import com.daasuu.mp4compose.filter.BlendFilters.GlWaveDistortionFilter;
import com.daasuu.mp4compose.gl.GlFramebufferObject;

public class GlWaveDistortionWithTime extends GlFilterWithTime {

    private GlWaveDistortionFilter glWaveDistortionFilter;
    private float maxDistortionInLength = 0.02f;
    private float maximumOffset = 0.015f;
    private float maxWaveLength = 0.020f;

    private float time;

    public GlWaveDistortionWithTime() {
        glWaveDistortionFilter = new GlWaveDistortionFilter();
        glWaveDistortionFilter.setWaveLengthforXY(maxWaveLength, maxWaveLength);
        glWaveDistortionFilter.setDistortionInX(0);
        glWaveDistortionFilter.setDistortionInY(0);
        glWaveDistortionFilter.setOffset(0);
    }


    @Override
    public void setup() {
        glWaveDistortionFilter.setup();
    }


    @Override
    public void release() {
        glWaveDistortionFilter.release();
    }

    @Override
    public void draw(int texName, GlFramebufferObject fbo) {

        updateOffsetAndDistortionLengthByTime();
        glWaveDistortionFilter.draw(texName, fbo);
    }

    private void updateOffsetAndDistortionLengthByTime() {

        time = getInputTimePeriod();

        float offset = (float) (Math.sin(2 * Math.PI * time) * this.maximumOffset);
        float distortionAmount = (float) (Math.sin(Math.PI * time) * maxDistortionInLength);

        glWaveDistortionFilter.setOffset(offset);
        glWaveDistortionFilter.setDistortionInY(distortionAmount);
        glWaveDistortionFilter.setDistortionInX(distortionAmount);
    }

    public void setMaximumOffset(float maximumOffset) {
        maximumOffset = maximumOffset;
    }

    public void setMaxDistortionInLength(float maxDistortionInLength) {
        this.maxDistortionInLength = maxDistortionInLength;
    }

    @Override
    public void setFrameSize(int width, int height) {
        glWaveDistortionFilter.setFrameSize(width, height);
    }
}

