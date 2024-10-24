package com.daasuu.mp4compose.filter.time_filter;

import com.daasuu.mp4compose.filter.BlendFilters.GlMaskBlendTwoImage;
import com.daasuu.mp4compose.filter.BlendFilters.GlMaskingCompositFilter;
import com.daasuu.mp4compose.filter.BlendFilters.GlNormalBlendWithAlpha;
import com.daasuu.mp4compose.filter.GlAdditionalColorFilter;
import com.daasuu.mp4compose.filter.GlFalseColorFilter;
import com.daasuu.mp4compose.filter.GlFilter;
import com.daasuu.mp4compose.filter.GlFilterGroup;
import com.daasuu.mp4compose.filter.GlLineGenerator;
import com.daasuu.mp4compose.filter.GlMirrorTileFilter;
import com.daasuu.mp4compose.filter.GlTransformFilter;
import com.daasuu.mp4compose.gl.GlFramebufferObject;

public class GlGlitchFourWithTime extends GlFilterWithTime {

    //PastMonotony

    private GlMaskBlendTwoImage makeFilter1;
    private GlMaskBlendTwoImage makeFilter2;
    private GlMaskBlendTwoImage makeFilter3;
    private GlNormalBlendWithAlpha makeBaseFilter;
    private GlMaskBlendTwoImage output;

    private GlAdditionalColorFilter blueFilter;
    private GlMirrorTileFilter shiftedForMasking1;
    private GlMirrorTileFilter shiftedForMasking3;
    private GlTransformFilter rotateForMasking2;
    private GlMirrorTileFilter shiftInputImage;

    private GlMaskingCompositFilter maskFilter1;
    private GlMaskingCompositFilter maskFilter2;
    private GlMaskingCompositFilter maskFilter3;
    private GlLineGenerator bend1;
    private GlLineGenerator bend2;
    private GlLineGenerator bend3;
    private GlFalseColorFilter falseColorFilter2;
    private GlFalseColorFilter falseColorFilter3;

    private boolean isDrawable;
    private float duration;
    private float interval;
    private float timestamp;
    private float trigger;

    public GlGlitchFourWithTime() {


        shiftInputImage = new GlMirrorTileFilter();
        shiftInputImage.setTileScale(1);
        shiftInputImage.setOffsetPosition(0, 0f);

        makeBaseFilter = new GlNormalBlendWithAlpha();
        makeBaseFilter.setAlpha(0.5f);
        makeBaseFilter.addTwoFilters(new GlFilter(), new GlFilterGroup(new GlFilter(), shiftInputImage));

        shiftedForMasking1 = new GlMirrorTileFilter();
        shiftedForMasking1.setTileScale(1);
        shiftedForMasking1.setOffsetPosition(-0.015f, 0);

        blueFilter = new GlAdditionalColorFilter();
        blueFilter.setBias(new float[]{-0.5f, -0.2f, 0.2f, 1f});


        bend1 = new GlLineGenerator();
        bend1.setLineGeneratorType(0);


        falseColorFilter2 = new GlFalseColorFilter();
        falseColorFilter2.setFirstColor(new float[]{241f / 255f, 155f / 255f, 82f / 255f});
        falseColorFilter2.setSecondColor(new float[]{1f, 1f, 1f});

        bend2 = new GlLineGenerator();
        bend2.setLineGeneratorType(2);

        rotateForMasking2 = new GlTransformFilter();
        rotateForMasking2.resetTrasformation();
        rotateForMasking2.setRotateInAngle(180f);


        shiftedForMasking3 = new GlMirrorTileFilter();
        shiftedForMasking3.setTileScale(1);
        shiftedForMasking3.setOffsetPosition(0.015f, 0);

        falseColorFilter3 = new GlFalseColorFilter();
        falseColorFilter3.setFirstColor(new float[]{82f / 255f, 241f / 255f, 205f / 255f});
        falseColorFilter3.setSecondColor(new float[]{1f, 0f, 0f});

        bend3 = new GlLineGenerator();
        bend3.setLineGeneratorType(1);


        makeFilter1 = new GlMaskBlendTwoImage();
        makeFilter1.blendTwoImgWithMask(makeBaseFilter, new GlFilterGroup(makeBaseFilter, blueFilter, shiftedForMasking1), bend1);

        makeFilter2 = new GlMaskBlendTwoImage();
        makeFilter2.blendTwoImgWithMask(makeFilter1, new GlFilterGroup(makeBaseFilter, rotateForMasking2, falseColorFilter2), bend2);
//
        makeFilter3 = new GlMaskBlendTwoImage();
        makeFilter3.blendTwoImgWithMask(makeFilter2, new GlFilterGroup(makeBaseFilter, falseColorFilter3, shiftedForMasking3), bend3);

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

                shiftInputImage.setOffsetPosition(0.02f, -0.02f);

            } else if (time <= trigger && trigger < 1.0) {

                shiftInputImage.setOffsetPosition(0f, 0f);

            } else if (time > trigger && (time + interval) >= 1.0) {

                trigger = 1.0f;
                timestamp = 0.0f;
            }


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
