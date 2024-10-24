package com.daasuu.mp4compose.filter.time_filter;

import com.daasuu.mp4compose.filter.GlFilter;
import com.daasuu.mp4compose.filter.GlFourFrameRotateFilter;
import com.daasuu.mp4compose.filter.GlTransformFilter;
import com.daasuu.mp4compose.gl.GlFramebufferObject;

public class GlFourFrameRotateWithTime extends GlFilterWithTime {

    private GlFourFrameRotateFilter glFourFrameRotateWithTime;

    GlFilter firstSub;
    GlFilter secondSub;
    GlFilter thirdSub;
    GlFilter fourthSub;
    GlFilterWithTime timeFilter;
    GlTransformFilter transformFilter;

    private float time;

    public GlFourFrameRotateWithTime(GlFilter filter1, GlFilter filter2, GlFilter filter3, GlFilter filter4) {
        firstSub = filter1;
        secondSub = filter2;
        thirdSub = filter3;
        fourthSub = filter4;
        glFourFrameRotateWithTime = new GlFourFrameRotateFilter(filter1, filter2, filter3, filter4);
        transformFilter = new GlTransformFilter();
    }

    @Override
    public void setup() {
        glFourFrameRotateWithTime.setup();
        transformFilter.setup();
    }

    @Override
    public void handlerLoadForShader() {
        glFourFrameRotateWithTime.handlerLoadForShader();
    }

    @Override
    public void release() {
        glFourFrameRotateWithTime.release();
        transformFilter.release();
    }

    @Override
    public void draw(int texName, GlFramebufferObject fbo) {

        updateRotationColors();
        glFourFrameRotateWithTime.draw(texName, fbo);
//        transformFilter.resetTrasformation();
//        transformFilter.setScaleUnit(0.5f,0.5f);
//        fbo.disable();
//        fbo.enable();
//        transformFilter.draw(fbo.getTexName(),fbo);
    }

    public void updateRotationColors() {

        time = getInputTimePeriod();

        float angleIn = (float) ((45.0f / 180.0f) * Math.PI * Math.cos(2 * Math.PI * time));

        try {
            glFourFrameRotateWithTime.setRotationAngleFor1stQuadrant(angleIn, 2.0f);
            ((GlFilterWithTime) firstSub).setInputTimePeriod((time % 0.3333f) / 0.333f);

        } catch (ClassCastException ex) {
        }

        try {
            glFourFrameRotateWithTime.setRotationAngleFor2ndQuadrant(angleIn, 3.0f);
            ((GlFilterWithTime) secondSub).setInputTimePeriod((time % 0.6666f) / 0.666f);
        } catch (ClassCastException ex) {
        }
        try {
            glFourFrameRotateWithTime.setRotationAngleFor3rdQuadrant(angleIn, 1.0f);
            ((GlFilterWithTime) thirdSub).setInputTimePeriod((float) (1.0f - (time % 0.7) / 0.7));
        } catch (ClassCastException ex) {
        }
        try {
            glFourFrameRotateWithTime.setRotationAngleFor4thQuadrant(angleIn, 0.0f);
            ((GlFilterWithTime) fourthSub).setInputTimePeriod(1.0f - (time % 0.4f) / 0.4f);
        } catch (ClassCastException ex) {
        }


    }

    @Override
    public void setFrameSize(int width, int height) {
        super.setFrameSize(width, height);
        firstSub.setFrameSize(width, height);
        secondSub.setFrameSize(width, height);
        thirdSub.setFrameSize(width, height);
        fourthSub.setFrameSize(width, height);

        glFourFrameRotateWithTime.setFrameSize(width, height);
        transformFilter.setFrameSize(width, height);

    }
}