package com.daasuu.mp4compose.filter.time_filter;

import android.util.Log;

import com.daasuu.mp4compose.filter.BlendFilters.GlMaskBlendTwoImage;
import com.daasuu.mp4compose.filter.BlendFilters.GlNormalBlendWithAlpha;
import com.daasuu.mp4compose.filter.GlFalseColorFilter;
import com.daasuu.mp4compose.filter.GlFilter;
import com.daasuu.mp4compose.filter.GlFilterGroup;
import com.daasuu.mp4compose.filter.GlLineGenerator;
import com.daasuu.mp4compose.filter.GlMirrorTileFilter;
import com.daasuu.mp4compose.filter.GlTransformFilter;
import com.daasuu.mp4compose.gl.GlFramebufferObject;

public class GlGlitchThreeWithTime extends GlFilterWithTime {


    //Gifted Friendliness

    private GlMaskBlendTwoImage makeFilter1;
    private GlMaskBlendTwoImage makeFilter2;
    private GlMaskBlendTwoImage makeFilter3;
    private GlNormalBlendWithAlpha makeBaseFilter;
    private GlMaskBlendTwoImage output;
    private GlMirrorTileFilter shiftedForMasking1;
    private GlTransformFilter mirrorForMasking3;
    private GlMirrorTileFilter shiftInputImage;

    private GlLineGenerator bend1;
    private GlLineGenerator bend2;
    private GlLineGenerator bend3;
    private GlFalseColorFilter falseColorFilter2;
    private boolean isDrawable;
    private float duration;
    private float interval;
    private float timestamp;
    private float trigger;


    public GlGlitchThreeWithTime() {


        shiftInputImage = new GlMirrorTileFilter();
        shiftInputImage.setTileScale(1);
        shiftInputImage.setOffsetPosition(0, 0f);

        makeBaseFilter = new GlNormalBlendWithAlpha();
        makeBaseFilter.setAlpha(0.7f);
        makeBaseFilter.addTwoFilters(new GlFilter(), new GlFilterGroup(new GlFilter(), shiftInputImage));


        shiftedForMasking1 = new GlMirrorTileFilter();
        shiftedForMasking1.setTileScale(1);
        shiftedForMasking1.setOffsetPosition(-0.02f, 0);

        bend1 = new GlLineGenerator();
        bend1.setLineGeneratorType(4);

        falseColorFilter2 = new GlFalseColorFilter();
        falseColorFilter2.setFirstColor(new float[]{0f, 0f, 0f});
        falseColorFilter2.setSecondColor(new float[]{175f / 255f, 69 / 255f, 99f / 255f});

        bend2 = new GlLineGenerator();
        bend2.setLineGeneratorType(3);

        mirrorForMasking3 = new GlTransformFilter();
        mirrorForMasking3.setScaleUnit(1f, -1f);

        bend3 = new GlLineGenerator();
        bend3.setLineGeneratorType(5);

        makeFilter1 = new GlMaskBlendTwoImage();
        makeFilter1.blendTwoImgWithMask(makeBaseFilter, new GlFilterGroup(makeBaseFilter, shiftedForMasking1), bend1);

        makeFilter2 = new GlMaskBlendTwoImage();
        makeFilter2.blendTwoImgWithMask(makeFilter1, new GlFilterGroup(makeBaseFilter, falseColorFilter2), bend2);
//
        makeFilter3 = new GlMaskBlendTwoImage();
        makeFilter3.blendTwoImgWithMask(makeFilter2, new GlFilterGroup(makeBaseFilter, mirrorForMasking3), bend3);


        output = makeFilter3;

        //constraints
        isDrawable = false;
        duration = 0.0f;
        interval = 0.0f;
        timestamp = 0.0f;
        trigger = 0.0f;

    }

    @Override
    public void setup() {
        output.setup();
    }

    @Override
    public void release() {
        output.release();
    }

    @Override
    public void setFrameSize(int width, int height) {
        output.setFrameSize(width, height);
    }

    @Override
    public void draw(int texName, GlFramebufferObject fbo) {

        UpdateGlitchPositionWithTime();
        if (isDrawable) {
            output.draw(texName, fbo);
        }
    }

    private void UpdateGlitchPositionWithTime() {

        float time = getInputTimePeriod();

        if (time >= getActivationTime() && (trigger <= 0f || trigger >= 1.0f)) {
            isDrawable = true;
            timestamp = time + duration;
            trigger = time + interval;
        }

        if (isDrawable) {

            if (time <= timestamp && time <= trigger && trigger < 1.0) {

                shiftInputImage.setOffsetPosition(0.035f, 0f);

            } else if (time <= trigger && trigger < 1.0) {

                shiftInputImage.setOffsetPosition(0f, 0f);

            } else if (time > trigger && (time + interval) >= 1.0) {

                trigger = 1.0f;
                timestamp = 0.0f;
            }

            Log.d("mask", "UpdateGlitchPositionWithTime: " + timestamp + "  " + trigger + "  " + time);

            float tempTime1 = (time + 0.2f);
            float temTime = tempTime1 > 1.0f ? (tempTime1 - 1.0f) : tempTime1;
            bend1.setInputTimePeriod(temTime);
            bend2.setInputTimePeriod(1.0f - time);
            bend3.setInputTimePeriod(time);
        }
    }


    public void setOverlayDuration(float duration) {
        this.duration = duration;
    }

    public void setOverlayInterval(float interval) {
        this.interval = interval;
    }


}
