package com.daasuu.mp4compose.filter.time_filter;


import com.daasuu.mp4compose.filter.GlMirrorTileFilter;
import com.daasuu.mp4compose.gl.GlFramebufferObject;

public class GlMirrorTileWithTime extends GlFilterWithTime {

    private GlMirrorTileFilter mirrorTileFilter;
    private float time;
    private float offsetValue;
    private float offsetX;
    private float offsetY;
    boolean toggle;

    public GlMirrorTileWithTime() {
        mirrorTileFilter = new GlMirrorTileFilter();
        offsetValue = 0;
        offsetX = 0;
        offsetY = 0;
        toggle = false;
    }

    public void setTileScale(int tile) {
        mirrorTileFilter.setTileScale((float) tile);
    }

    public void setOffsetPosition(float offsetX, float offsetY) {
        mirrorTileFilter.setOffsetPosition(offsetX, offsetY);
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }


    @Override
    public void setup() {
        mirrorTileFilter.setup();
    }

    @Override
    public void release() {
        mirrorTileFilter.release();
    }

    @Override
    public void setFrameSize(int width, int height) {
        mirrorTileFilter.setFrameSize(width, height);
    }

    @Override
    public void draw(int texName, GlFramebufferObject fbo) {

        updateOffsetOrTileByTime();
        mirrorTileFilter.draw(texName, fbo);
    }

    private void updateOffsetOrTileByTime() {

        time = getInputTimePeriod();
//        if(time < 0.01){
//            toggle = !toggle;
//        }

        offsetValue = (float) Math.sin(time * 2 * Math.PI) * (1f / 4f);

        //Log.d("Mirror", "updateOffsetOrTileByTime: " + offsetValue);
        if (!toggle) {
            mirrorTileFilter.setOffsetPosition(offsetX + offsetValue, offsetY + offsetValue);
        }
//        else if(toggle){
//            mirrorTileFilter.setOffsetPosition(offsetX - offsetValue, offsetY - offsetValue);
//        }
    }


}
