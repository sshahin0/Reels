package com.daasuu.mp4compose.filter.time_filter;

import com.daasuu.mp4compose.filter.BlendFilters.GlGridViewBlendFilter;
import com.daasuu.mp4compose.gl.GlFramebufferObject;

public class GlGridShiftWithTime extends GlFilterWithTime {

    private GlGridViewBlendFilter gridViewBlendFilter;

    private float maximumOffset;

    private float time;
    private int lap;

    public GlGridShiftWithTime() {
        gridViewBlendFilter = new GlGridViewBlendFilter();
        maximumOffset = 0.25f;
    }

    @Override
    public void setup() {
        gridViewBlendFilter.setup();
    }

    @Override
    public void release() {
        gridViewBlendFilter.release();
    }

    @Override
    public void draw(int texName, GlFramebufferObject fbo) {

        updateOffsetByTime();
        gridViewBlendFilter.draw(texName, fbo);
    }

    private void updateOffsetByTime() {

        time = getInputTimePeriod();

        float offset = (float) ((Math.cos(Math.PI * time)) * maximumOffset / 2f);

        gridViewBlendFilter.setGridOffset(offset);

    }

    public void setGridWithHeight(float width, float height) {
        gridViewBlendFilter.setGridWithHeight(width, height);
        maximumOffset = gridViewBlendFilter.getMaxBaseOffset();
    }

    @Override
    public void setFrameSize(int width, int height) {
        gridViewBlendFilter.setFrameSize(width, height);
    }
}
