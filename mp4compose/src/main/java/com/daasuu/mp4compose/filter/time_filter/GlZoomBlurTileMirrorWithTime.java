package com.daasuu.mp4compose.filter.time_filter;

import android.graphics.PointF;

import com.daasuu.mp4compose.filter.GlFilterGroup;
import com.daasuu.mp4compose.filter.GlMirrorTileFilter;
import com.daasuu.mp4compose.filter.GlZoomBlurFilter;
import com.daasuu.mp4compose.gl.GlFramebufferObject;

public class GlZoomBlurTileMirrorWithTime extends GlFilterWithTime {


    private GlMirrorTileFilter mirrorTile;
    private GlZoomBlurFilter zoomBlur;
    private GlFilterGroup group;
    float[] blurRange = new float[2];
    float[] zoomRange = new float[2];


    public GlZoomBlurTileMirrorWithTime() {
        mirrorTile = new GlMirrorTileFilter();
        zoomBlur = new GlZoomBlurFilter();
        group = new GlFilterGroup(mirrorTile, zoomBlur);
        setZoomRange(1.0f, 2.0f);
        setBlurRange(2.0f, 10.0f);
    }

    @Override
    public void setup() {
        group.setup();
    }

    @Override
    public void release() {
        group.release();
    }

    @Override
    public void setFrameSize(int width, int height) {
        group.setFrameSize(width, height);
    }

    @Override
    public void draw(int texName, GlFramebufferObject fbo) {

        updateScaleAndZoomLevelbyTime();

        group.draw(texName, fbo);
    }

    private void updateScaleAndZoomLevelbyTime() {

        float time = (float) Math.sin(getInputTimePeriod() * Math.PI);

        float currentScale = time * (zoomRange[1] - zoomRange[0]) + zoomRange[0];
        mirrorTile.setOffsetPosition(0, 0);
        mirrorTile.setTileScale(currentScale);

        float currentBlurRadius = time * (blurRange[1] - blurRange[0]) + blurRange[0];
        zoomBlur.setBlurCenter(new PointF(0.5f, 0.5f));
        zoomBlur.setBlurSize(currentBlurRadius);
    }

    public void setZoomRange(float min, float max) {
        this.zoomRange[0] = min;
        this.zoomRange[1] = max;
    }

    public void setBlurRange(float min, float max) {
        this.blurRange[0] = min;
        this.blurRange[1] = max;
    }
}
