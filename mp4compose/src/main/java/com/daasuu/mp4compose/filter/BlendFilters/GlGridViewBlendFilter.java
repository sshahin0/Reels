package com.daasuu.mp4compose.filter.BlendFilters;

import com.daasuu.mp4compose.filter.GlGridViewFilter;

public class GlGridViewBlendFilter extends GlBlendMultiInputSample {


    private static final String FRAGMENT_SHADER =
            "precision mediump float;\n" +
                    "varying highp vec2 vTextureCoord;\n" +
                    "uniform lowp sampler2D sTexture0;" +
                    "uniform lowp sampler2D sTexture1;" +
                    "void main(){" +
                    "   vec4 colorRed = texture2D(sTexture0,vTextureCoord);" +
                    "   vec4 colorBlue = texture2D(sTexture1,vTextureCoord);" +
                    "   gl_FragColor = vec4(colorRed.r, mix(colorRed.g,colorBlue.g,0.5), colorBlue.b, 1.0);" +
                    "}";

    private float maxBaseOffset;
    private GlGridViewFilter glGridViewFilterRed;
    private GlGridViewFilter getGlGridViewFilterCyan;

    public GlGridViewBlendFilter() {
        super(FRAGMENT_SHADER);
        glGridViewFilterRed = new GlGridViewFilter();
        getGlGridViewFilterCyan = new GlGridViewFilter();
        //maxBaseOffset = 0.25f;
        glGridViewFilterRed.setGridOffset(maxBaseOffset, 0);
        getGlGridViewFilterCyan.setGridOffset(-maxBaseOffset, 0);
        addFilter(glGridViewFilterRed, getGlGridViewFilterCyan);
    }


    public void setGridWithHeight(float width, float height) {
        glGridViewFilterRed.setGridWidthHeight(width, height);
        getGlGridViewFilterCyan.setGridWidthHeight(width, height);
        maxBaseOffset = width / 4;
    }

    public void setGridOffset(float BaseOffset) {

        glGridViewFilterRed.setGridOffset(BaseOffset, 0);
        getGlGridViewFilterCyan.setGridOffset(-BaseOffset, 0);
    }

    public float getMaxBaseOffset() {
        return maxBaseOffset;
    }
}
