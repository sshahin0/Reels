package com.daasuu.mp4compose.filter.time_filter;

import androidx.annotation.Nullable;

import com.daasuu.mp4compose.filter.BlendFilters.GlBlendMultiInputSample;
import com.daasuu.mp4compose.filter.BlendFilters.GlMaskBlendTwoImage;
import com.daasuu.mp4compose.filter.GlFilter;
import com.daasuu.mp4compose.gl.GlFramebufferObject;

public class GlMaskBlend2ImgByTime extends GlFilterWithTime {

    GlBlendMultiInputSample maskBlendTwoImage;
    GlFilter background;
    GlFilter foreground;
    GlFilter mask;

    public GlMaskBlend2ImgByTime(){

    }

    public void blendTwoImages(GlFilter backgroundImg, GlFilter foregroundImg, @Nullable GlFilter maskImg, GlBlendMultiInputSample blendType)
    {
        this.background = backgroundImg;
        this.foreground = foregroundImg;
        this.mask = maskImg;
        if(blendType instanceof GlMaskBlendTwoImage){

            if(mask!=null) {

                maskBlendTwoImage = blendType;
                ((GlMaskBlendTwoImage) maskBlendTwoImage).blendTwoImgWithMask(backgroundImg, foregroundImg, maskImg);
            }

        }
        else {
            maskBlendTwoImage = blendType;
            maskBlendTwoImage.addFilter(background, foreground);
        }
    }

    @Override
    public void setup() {
        maskBlendTwoImage.setup();
    }

    @Override
    public void release() {
        if(maskBlendTwoImage!= null) {
            maskBlendTwoImage.release();
        }
    }

    @Override
    public void draw(int texName, GlFramebufferObject fbo) {

        updatebyTime();

        maskBlendTwoImage.draw(texName, fbo);
    }

    private void updatebyTime() {

       // float time = (float) Math.sin(getInputTimePeriod() * Math.PI);

//        if(background instanceof GlFilterWithTime)
//        {
//            ((GlFilterWithTime) background).setInputTimePeriod(time);
//        }
//        if(foreground instanceof GlFilterWithTime)
//        {
//            ((GlFilterWithTime) foreground).setInputTimePeriod(time);
//        }

        if(mask instanceof GlFilterWithTime)
        {
            ((GlFilterWithTime) mask).setInputTimePeriod(getInputTimePeriod());
        }


    }

    @Override
    public void setFrameSize(int width, int height) {
        maskBlendTwoImage.setFrameSize(width, height);
    }
}