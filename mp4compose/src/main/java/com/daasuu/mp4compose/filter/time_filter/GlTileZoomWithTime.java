package com.daasuu.mp4compose.filter.time_filter;

import com.daasuu.mp4compose.filter.BlendFilters.GlTileZoomFilter;
import com.daasuu.mp4compose.gl.GlFramebufferObject;

public class GlTileZoomWithTime extends GlFilterWithTime {

    private GlTileZoomFilter tileZoom;

    public GlTileZoomWithTime() {

        tileZoom = new GlTileZoomFilter();
    }

    @Override
    public void setup() {
        tileZoom.setup();
    }

    @Override
    public void release() {
        tileZoom.release();
    }

    @Override
    public void setFrameSize(int width, int height) {
        tileZoom.setFrameSize(width, height);
    }

    @Override
    public void draw(int texName, GlFramebufferObject fbo) {
        UpdateTileOffsetWithTime();
        tileZoom.draw(texName, fbo);
    }

    private void UpdateTileOffsetWithTime() {

        float time = getInputTimePeriod();

        tileZoom.setOffsetShifted((float) Math.sin(2 * Math.PI * time));
    }

}
