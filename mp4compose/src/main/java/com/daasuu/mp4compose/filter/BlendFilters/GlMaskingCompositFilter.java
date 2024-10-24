package com.daasuu.mp4compose.filter.BlendFilters;

public class GlMaskingCompositFilter extends GlAdditionCompositingFilter {

    private static final String FRAGMENT_SHADER = "" +
            "precision highp float;\n" +
            "varying highp vec2 vTextureCoord;\n" +
            "uniform lowp sampler2D sTexture0;" +
            "uniform lowp sampler2D sTexture1;\n" +
            "void main() {\n" +
            "   vec4 color0 = texture2D(sTexture0, vTextureCoord);" +
            "   vec4 color1 = texture2D(sTexture1, vTextureCoord);" +
            "   vec3 colorF = dot(color1.rgb,color1.rgb) > 0.0 ? color0.rgb : vec3(0);" +
            "   gl_FragColor = vec4(colorF,1.0);\n" +
            "}\n";

    public GlMaskingCompositFilter(){

        super(FRAGMENT_SHADER);
    }
}
