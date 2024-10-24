package com.daasuu.mp4compose.filter.time_filter;

import com.daasuu.mp4compose.filter.GlFilter;
import com.daasuu.mp4compose.filter.GlFilterGroup;
import com.daasuu.mp4compose.gl.GlFramebufferObject;

import java.util.Collection;

public class GlFilterGroupWithTime extends GlFilterGroup {

    private float inputTimePeriod;
    private float activationTime;

    public GlFilterGroupWithTime(final GlFilter... glFilters) {
        super(glFilters);
    }

    public GlFilterGroupWithTime(final Collection<GlFilter> glFilters) {
        super(glFilters);
    }

    public void setInputTimePeriod(float inputTimePeriod) {

        this.inputTimePeriod = inputTimePeriod;
    }

    public float getInputTimePeriod() {

        return inputTimePeriod;
    }

    @Override
    public void draw(int texName, GlFramebufferObject fbo) {
        timePropageteToGroup();

        super.draw(texName, fbo);
    }

    private void timePropageteToGroup() {

        Collection<GlFilter> filters = getFilters();

        for (GlFilter filter : filters) {
            if (filter instanceof GlFilterWithTime) {
                ((GlFilterWithTime) filter).setActivationTime(getInputTimePeriod());
            }
        }
    }

    public void addFilter(GlFilter filter) {
        if (filter == null)
            return;
        super.addFilter(filter);
    }


    public void setActivationTime(float activationTime) {
        this.activationTime = activationTime;
    }

    public float getActivationTime() {
        return activationTime;
    }

}
