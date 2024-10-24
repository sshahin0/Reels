package com.daasuu.mp4compose.filter.Overlay;

import android.content.Context;

import com.daasuu.mp4compose.filter.Gl3x3ConvolutionFilter;
import com.daasuu.mp4compose.filter.GlFilterGroup;
import com.daasuu.mp4compose.filter.GlGrayScaleFilter;

public class GlRainDropsEffect extends GlVideoOverlayFilter {

    private static final String FRAGMENT_SHADER = OverlayShaderHelper.RAIN_DROPS_FRAGMENT;
    private GlFilterGroup maskProduce;
    private GlGrayScaleFilter grayScaleFilter;
    private Gl3x3ConvolutionFilter convFilter;

    public GlRainDropsEffect(String datasrc) {
        super(DEFAULT_VERTEX_SHADER, FRAGMENT_SHADER);
        this.context = null;
        this.datasrc = datasrc;

        additionalSetup();

    }

    public GlRainDropsEffect(Context context, int resId) {
        super(DEFAULT_VERTEX_SHADER, FRAGMENT_SHADER);
        this.context = context;
        this.dataSrcId = resId;
        this.datasrc = "";

        additionalSetup();

    }

    private void additionalSetup() {

        convFilter = new Gl3x3ConvolutionFilter();
        convFilter.setConvolutionKernel(new float[]{
                1f, 2f, -1f,
                -1.5f, 4f, 1.5f,
                -1f, 2f, 1f

        });
        convFilter.setRadius(1.5f);
        grayScaleFilter = new GlGrayScaleFilter();
        maskProduce = new GlFilterGroup(convFilter);
    }

    @Override
    public void setup() {
        maskProduce.setup();
        super.setup();
    }

    @Override
    public void release() {
        maskProduce.release();
        super.release();
    }

    @Override
    public void setFrameSize(int width, int height) {
        maskProduce.setFrameSize(width, height);
        super.setFrameSize(width, height);
    }

    @Override
    protected void onDraw() {

        overlayFrameBuffer.enable();
        maskProduce.draw(overlayFrameBuffer.getTexName(), overlayFrameBuffer);
        overlayFrameBuffer.disable();
        baseFbo.enable();

        super.onDraw();
    }
}
