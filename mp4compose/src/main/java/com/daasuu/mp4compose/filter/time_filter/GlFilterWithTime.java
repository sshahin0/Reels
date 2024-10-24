package com.daasuu.mp4compose.filter.time_filter;

import android.content.res.Resources;

import com.daasuu.mp4compose.filter.GlFilter;

public class GlFilterWithTime extends GlFilter {

    private float inputTimePeriod;
    private float activationTime;


    public GlFilterWithTime() {
        super();
    }

    public GlFilterWithTime(final Resources res, final int vertexShaderSourceResId, final int fragmentShaderSourceResId) {
        this(res.getString(vertexShaderSourceResId), res.getString(fragmentShaderSourceResId));

    }

    public GlFilterWithTime(final String vertexShaderSource, final String fragmentShaderSource) {
        super(vertexShaderSource, fragmentShaderSource);
    }


    public void setInputTimePeriod(float inputTimePeriod) {

        this.inputTimePeriod = inputTimePeriod;
    }

    public float getInputTimePeriod() {

        return inputTimePeriod;
    }

    public void setActivationTime(float activationTime) {
        this.activationTime = activationTime;
    }

    public float getActivationTime() {
        return activationTime;
    }
}
