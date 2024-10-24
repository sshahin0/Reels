package com.daasuu.mp4compose.filter.BlendFilters;

import android.util.Log;

import com.daasuu.mp4compose.filter.GlFilter;

import static com.daasuu.mp4compose.filter.BlendFilters.GlAdditionCompositingFilter.HELPER_FUNCTION;

public class GlMaskBlendTwoImage extends GlBlendMultiInputSample {

    private static final String FRAGMENT_SHADER = "" +
            "precision highp float;\n" +
            "varying highp vec2 vTextureCoord;\n" +
            "uniform  sampler2D sTexture0;" +
            "uniform  sampler2D sTexture1;" +
            "uniform  sampler2D sTexture2;\n" +
            "" + HELPER_FUNCTION +
            "void main() {\n" +
            "   vec4 baseColor = texture2D(sTexture0, vTextureCoord);" +
            "   vec4 overlayColor = texture2D(sTexture1, vTextureCoord);" +
            "   vec4 maskColor = texture2D(sTexture2, vTextureCoord);" +
            "   float val  = dot(maskColor.rgb,maskColor.rgb);" +
            "   gl_FragColor = vec4(mix(baseColor.rgb , overlayColor.rgb, val > 0.0 ? 1.0 : 0.0 ),1.0);\n" +
            "}\n";

    public GlMaskBlendTwoImage() {

        super(FRAGMENT_SHADER);
    }

    public void blendTwoImgWithMask(GlFilter inputImg, GlFilter backgroundImg, GlFilter maskImg) {
        Log.d("blend", "blendTwoImgWithMask: ");
        addFilter(inputImg, backgroundImg, maskImg);
    }
}
