package com.daasuu.mp4compose.filter.time_filter;

import com.daasuu.mp4compose.filter.BlendFilters.GlVerticalViewBlendFilter;
import com.daasuu.mp4compose.gl.GlFramebufferObject;

public class GlVerticalSlideWithTime extends GlFilterWithTime {

    private GlVerticalViewBlendFilter glVerticalViewBlendFilter;

    public GlVerticalSlideWithTime() {

        glVerticalViewBlendFilter = new GlVerticalViewBlendFilter();
    }

    public GlVerticalSlideWithTime(GlVerticalViewBlendFilter glFilter) {

        if (glFilter != null) {

            glVerticalViewBlendFilter = glFilter;
        } else {

            glVerticalViewBlendFilter = new GlVerticalViewBlendFilter();
        }
    }

    @Override
    public void setup() {
        glVerticalViewBlendFilter.setup();
    }

    @Override
    public void setFrameSize(int width, int height) {
        glVerticalViewBlendFilter.setFrameSize(width, height);
    }

    @Override
    public void release() {
        glVerticalViewBlendFilter.release();
    }

    @Override
    public void draw(int texName, GlFramebufferObject fbo) {

        updateOffsetWithTime();

        glVerticalViewBlendFilter.draw(texName, fbo);
    }

    private void updateOffsetWithTime() {

        float time = getInputTimePeriod();

        glVerticalViewBlendFilter.setOffset(time);
    }

}
