package com.daasuu.mp4compose.filter.BlendFilters;

import com.daasuu.mp4compose.filter.GlMirrorTileFilter;

public class GlRGBChannelCompositFilter extends GlBlendMultiInputSample {

    private static final String BLEND_FRAGMENT_SHADER = "" +
            "precision mediump float;\n" +
            "varying highp vec2 vTextureCoord;\n" +
            "uniform lowp sampler2D sTexture0;\n" +
            "uniform lowp sampler2D sTexture1;\n" +
            "uniform lowp sampler2D sTexture2;\n" +
            "void main() {\n" +
            "   vec4 colorR = texture2D(sTexture0, vTextureCoord);" +
            "   vec4 colorG = texture2D(sTexture1, vTextureCoord);" +
            "   vec4 colorB = texture2D(sTexture2, vTextureCoord);" +
            "   gl_FragColor = vec4(colorR.r,colorG.g,colorB.b,1.0);\n" +
            "}\n";


    private GlMirrorTileFilter redChannel;
    private GlMirrorTileFilter greenChannel;
    private GlMirrorTileFilter blueChannel;

    public GlRGBChannelCompositFilter() {
        super(BLEND_FRAGMENT_SHADER);
        init();
    }

    private void init() {

        redChannel = new GlMirrorTileFilter();
        redChannel.setTileScale(1);
        redChannel.setOffsetPosition(-0.05f, 0);

        greenChannel = new GlMirrorTileFilter();
        greenChannel.setTileScale(1);
        greenChannel.setOffsetPosition(0, .05f);

        blueChannel = new GlMirrorTileFilter();
        blueChannel.setTileScale(1);
        blueChannel.setOffsetPosition(0, -0.05f);

        addFilter(redChannel, greenChannel, blueChannel);

    }

    public void setRedChannelOffset(int offsetX, int offsetY) {
        redChannel.setOffsetPosition(offsetX, offsetY);
    }

    public void setGreenChannelOffset(int offsetX, int offsetY) {
        greenChannel.setOffsetPosition(offsetX, offsetY);
    }

    public void setBlueChannelOffset(int offsetX, int offsetY) {
        blueChannel.setOffsetPosition(offsetX, offsetY);
    }


}
