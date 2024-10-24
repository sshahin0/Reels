package com.daasuu.mp4compose.filter;

import android.util.Log;

import com.daasuu.mp4compose.filter.time_filter.GlFilterWithTime;
import com.daasuu.mp4compose.gl.GlFramebufferObject;

public class GlFilterController extends GlFilter {

    protected GlFilter inputFilter;
    protected GlFilterWithTime inputFilterWithTime;
    private boolean isTimeFilter;
    private float timeInterval;

    public GlFilterController(GlFilter inputFilter) {
        try {

            inputFilterWithTime = (GlFilterWithTime) inputFilter;
            isTimeFilter = true;
            timeInterval = 5000f;

        } catch (ClassCastException ex) {
            isTimeFilter = false;
            this.inputFilter = inputFilter;
        }
    }

    @Override
    public void setup() {

        if (inputFilter != null) {
            inputFilter.setup();
        }

        if (inputFilterWithTime != null) {
            inputFilterWithTime.setup();
        }
    }

    @Override
    public void release() {
        if (inputFilter != null) {
            inputFilter.release();
        }

        if (inputFilterWithTime != null) {
            inputFilterWithTime.release();
        }

    }

    @Override
    public void draw(int texName, GlFramebufferObject fbo) {
        if (isTimeFilter) {
            float time = (System.currentTimeMillis() % (long) this.timeInterval) / this.timeInterval;

            inputFilterWithTime.setInputTimePeriod(time);
            inputFilterWithTime.draw(texName, fbo);
        } else {
            inputFilter.draw(texName, fbo);
        }
    }

    @Override
    public void setFrameSize(int width, int height) {

        if (inputFilter != null) {
            inputFilter.setFrameSize(width, height);
        }
        if (inputFilterWithTime != null) {
            inputFilterWithTime.setFrameSize(width, height);
        }
    }

    public void setTimeIntervel(float timeInterval) {

        this.timeInterval = timeInterval;
        Log.d("controller", "setTimeIntervel: " + this.timeInterval);
    }

}