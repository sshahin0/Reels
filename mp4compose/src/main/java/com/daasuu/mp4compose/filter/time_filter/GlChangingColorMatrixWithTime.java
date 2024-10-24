package com.daasuu.mp4compose.filter.time_filter;

import com.daasuu.mp4compose.filter.GlChangingColorMatrixFilter;
import com.daasuu.mp4compose.gl.GlFramebufferObject;

public class GlChangingColorMatrixWithTime extends GlFilterWithTime {

    GlChangingColorMatrixFilter glChangingColorMatrixFilter;
    private float time;
    private float intensity;


    public GlChangingColorMatrixWithTime() {
        glChangingColorMatrixFilter = new GlChangingColorMatrixFilter();
    }

    @Override
    public void setup() {
        glChangingColorMatrixFilter.setup();
    }

    @Override
    public void release() {
        glChangingColorMatrixFilter.release();
    }

    @Override
    public void draw(int texName, GlFramebufferObject fbo) {
        updateMatrixWithTime();
        glChangingColorMatrixFilter.draw(texName, fbo);
    }

    private void updateMatrixWithTime() {

        time = getInputTimePeriod();

        intensity = (float) (Math.sin(time + 0.3f));


        glChangingColorMatrixFilter.setIntensity(intensity);

        float tempTime = (time * 100) % 3f;

        float color = (float) Math.cos(time * 0.8);
        if (tempTime < 1f) {
            glChangingColorMatrixFilter.setColorMatrixForIndexValue(0, color);

        } else if (tempTime < 2f) {
            glChangingColorMatrixFilter.setColorMatrixForIndexValue(1, color);
        } else {
            glChangingColorMatrixFilter.setColorMatrixForIndexValue(2, color);
        }
    }

    @Override
    public void setFrameSize(int width, int height) {
        glChangingColorMatrixFilter.setFrameSize(width, height);
    }

}
