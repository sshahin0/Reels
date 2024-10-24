package com.daasuu.mp4compose.filter.group_filters;

import com.daasuu.mp4compose.filter.BlendFilters.GlAdditionCompositingFilter;
import com.daasuu.mp4compose.filter.Gl3x3ConvolutionFilter;
import com.daasuu.mp4compose.filter.GlFilter;
import com.daasuu.mp4compose.filter.GlFilterGroup;
import com.daasuu.mp4compose.filter.GlGaussianBlurFilter;
import com.daasuu.mp4compose.filter.GlGaussianBlurInVerticalDirection;
import com.daasuu.mp4compose.filter.GlMotionBlur;
import com.daasuu.mp4compose.filter.GlThresholdFilter;

public class GlStarBurstFilter extends GlAdditionCompositingFilter {

    private GlThresholdFilter thresholdFilter1, thresholdFilter2;
    private GlMotionBlur motionBlur1;
    private GlMotionBlur motionBlur2;

    private GlFilterGroup blurImage1;
    private GlFilterGroup blurImage2;
    private GlGaussianBlurInVerticalDirection blurGaussianV;
    private GlGaussianBlurFilter blurGaussianH;
    private Gl3x3ConvolutionFilter conv;

    GlAdditionCompositingFilter additionCompositingFilter;

    public GlStarBurstFilter() {

        super();

        blurImage1 = new GlFilterGroup();
        blurImage2 = new GlFilterGroup();

        additionCompositingFilter = new GlAdditionCompositingFilter();

        thresholdFilter1 = new GlThresholdFilter();
        thresholdFilter1.setThreshold(0.9f);

        conv = new Gl3x3ConvolutionFilter();
        conv.setConvolutionKernel(new float[]{
                0.15f, 0.0f, 0.1f,
                0.0f, 0.5f, 0.0f,
                0.1f, 0.0f, 0.15f
        });

        blurGaussianV = new GlGaussianBlurInVerticalDirection();
        blurGaussianH = new GlGaussianBlurFilter();
        blurGaussianV.setBlurSize(0.2f);
        blurGaussianH.setBlurSize(0.2f);

        motionBlur1 = new GlMotionBlur();
        motionBlur1.setRadius(30f);
        motionBlur1.setAngle(45);

        blurImage1.setFilters(conv, thresholdFilter1, blurGaussianH, motionBlur1, blurGaussianV);

        motionBlur2 = new GlMotionBlur();
        motionBlur2.setRadius(30f);
        motionBlur2.setAngle(135);

        blurImage2.setFilters(conv, thresholdFilter1, blurGaussianH, motionBlur2, blurGaussianV);

        additionCompositingFilter.addTwoFilters(blurImage1, blurImage2);

        addFilter(new GlFilter(), additionCompositingFilter);
    }

}
