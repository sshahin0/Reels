package com.daasuu.mp4compose.filter.BlendFilters;

import com.daasuu.mp4compose.filter.GlVerticalViewFilter;

public class GlVerticalViewBlendFilter extends GlBlendMultiInputSample {

    private static final String FRAGMENT_SHADER = "" +
            "precision highp float;\n" +
            "varying highp vec2 vTextureCoord;\n" +
            "uniform lowp sampler2D sTexture0;\n" +
            "uniform lowp sampler2D sTexture1;\n" +
            "void main() {\n" +
            "   vec2 uv = vTextureCoord;" +
            "   float thresh = step(0.5,uv.y);" +
            "   vec4 color1 = texture2D(sTexture0, vTextureCoord);" +
            "   vec4 color2 = texture2D(sTexture1, vTextureCoord);" +
            "" +
            "   gl_FragColor = color1 * thresh +  color2 * (1.0 - thresh);\n" +
            "}\n";

    private GlVerticalViewFilter vFilter1;
    private GlVerticalViewFilter vFilter2;
    private float offsetYvFilter1;
    private float offsetYvFilter2;


    public GlVerticalViewBlendFilter() {

        super(FRAGMENT_SHADER);

        vFilter1 = new GlVerticalViewFilter();
        vFilter2 = new GlVerticalViewFilter();
        offsetYvFilter1 = 0.35f;
        offsetYvFilter2 = 0.65f;

        vFilter1.setOffsetY(offsetYvFilter1);
        vFilter2.setOffsetY(offsetYvFilter2);

        addFilter(vFilter1, vFilter2);
    }

    public void setOffset(float offset) {

        float offsetY1 = (offset + offsetYvFilter1) > 1.0f ? (offset + offsetYvFilter1) - 1.0f : (offset + offsetYvFilter1);
        float offsetY2 = (offsetYvFilter2 - offset) < 0.0f ? 1f + (offsetYvFilter2 - offset) : (offsetYvFilter2 - offset);
        vFilter1.setOffsetY(offsetY1);
        vFilter2.setOffsetY(offsetY2);
    }

    public void setOffsetYvFilter1(float offsetYvFilter1) {
        this.offsetYvFilter1 = offsetYvFilter1;
    }

    public void setOffsetYvFilter2(float offsetYvFilter2) {
        this.offsetYvFilter2 = offsetYvFilter2;
    }
}
