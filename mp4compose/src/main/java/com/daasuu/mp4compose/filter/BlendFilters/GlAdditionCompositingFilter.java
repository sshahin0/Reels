package com.daasuu.mp4compose.filter.BlendFilters;

import android.util.Log;

import com.daasuu.mp4compose.filter.GlFilter;

public class GlAdditionCompositingFilter extends GlBlendMultiInputSample {


    protected static final String HELPER_FUNCTION = "" +
            "" +
            "vec4 unPreMultiply(vec4 s){" +
            "   return vec4(s.rgb / max(s.a, 0.00001),s.a);" +
            "}" +
            "" +
            "vec4 preMultiply(vec4 s){" +
            "   return vec4(s.rgb * s.a, s.a);" +
            "}" +
            "" +
            "vec4 normalBlend(vec4 cB, vec4 cS){" +
            "   vec4 dst = preMultiply(cB);" +
            "   vec4 src = preMultiply(cS);" +
            "   return unPreMultiply(src + dst * (1.0 - src.a));" +
            "}" +
            "" +
            "vec4 blendBaseAlpha(vec4 cB, vec4 cS,vec4 B){" +
            "   vec4 cR = vec4((1.0 - cB.a) * cS.rgb + cB.a * clamp(B.rgb,0.0,0.5), cS.a);" +
            "   return normalBlend(cB,cR);" +
            "}" +
            "vec4 multiplyBlend(vec4 cB,vec4 cS){" +
            "   vec4 B = clamp(vec4(cB.rgb * cS.rgb,cS.a),0.0,1.0);" +
            "   return blendBaseAlpha(cB,cS,B);" +
            "}" +
            "";

    protected static final String COMPOSIT_FRAGMENT_SHADER =
            "precision highp float;\n" +
                    "varying highp vec2 vTextureCoord;\n" +
                    "uniform lowp sampler2D sTexture0;" +
                    "uniform lowp sampler2D sTexture1;\n" +
                    "void main() {\n" +
                    "   vec4 color0 = texture2D(sTexture0, vTextureCoord);" +
                    "   vec4 color1 = texture2D(sTexture1, vTextureCoord);" +
                    "   gl_FragColor = color0 + color1 ;\n" +
                    "}\n";


    public GlAdditionCompositingFilter() {
        this(COMPOSIT_FRAGMENT_SHADER);
    }

    public GlAdditionCompositingFilter(String fragmentShader) {
        super(fragmentShader);
    }

    public void addTwoFilters(GlFilter background, GlFilter foreground) {
        addFilter(background, foreground);
    }

    @Override
    public void setFrameSize(int width, int height) {
        super.setFrameSize(width, height);
    }

    @Override
    public void addFilter(GlFilter... glFilters) {

        int len = glFilters.length;
        if (len == 2) {
            super.addFilter(glFilters);
        } else {
            throw new StackOverflowError();
        }
    }

    @Override
    public void addFilter(GlFilter filter) {

        Log.d("AdditionCompositing", "addFilter: Not applicable here");
    }
}
