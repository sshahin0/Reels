package com.daasuu.mp4compose.filter.time_filter;

import com.daasuu.mp4compose.filter.BlendFilters.GlChromaticAbberationFilter;
import com.daasuu.mp4compose.gl.GlFramebufferObject;

public class GlChromaticAbberationWithTime extends GlFilterWithTime {

    GlChromaticAbberationFilter glChroAbbFilter;
    private float inputRadius;
    private float inputAngle;
    private float time;

    public GlChromaticAbberationWithTime() {

        glChroAbbFilter = new GlChromaticAbberationFilter();
        setInputRadius(25);
        setInputAngle(0.0f);

    }

    @Override
    public void setup() {
        glChroAbbFilter.setup();
    }

    @Override
    public void release() {
        glChroAbbFilter.release();
    }

    @Override
    public void draw(int texName, GlFramebufferObject fbo) {
        updateRadiusAndAngleByTime();
        glChroAbbFilter.draw(texName, fbo);
    }

    private void updateRadiusAndAngleByTime() {

        time = getInputTimePeriod();
        glChroAbbFilter.configureSubFilters((float) (Math.sin(time * Math.PI) * inputRadius), inputAngle);

    }

    public void setInputRadius(float inputRadius) {
        glChroAbbFilter.setInputRadius(inputRadius);
        this.inputRadius = glChroAbbFilter.getInputRadius();
    }

    public void setInputAngle(float inputAngle) {
        glChroAbbFilter.setInputAngle(inputAngle);
        this.inputAngle = inputAngle;
    }


    @Override
    public void setFrameSize(int width, int height) {
        glChroAbbFilter.setFrameSize(width, height);
    }
}
