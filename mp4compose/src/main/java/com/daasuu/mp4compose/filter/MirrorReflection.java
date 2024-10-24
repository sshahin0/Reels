package com.daasuu.mp4compose.filter;

public class MirrorReflection extends GlFilter {

    private static final String FRAGMENT_SHADER = "" +
            "precision mediump float;\n" +
            "varying highp vec2 vTextureCoord;\n" +
            "uniform lowp sampler2D sTexture;" +
            "\n" +
            "void main() {\n" +
            " float check = float(vTextureCoord.x > 0.5);" +
            " vec4 mColor = texture2D(sTexture, vec2(1.0 - vTextureCoord.x , vTextureCoord.y));" +
            " vec4 oColor = texture2D(sTexture,vTextureCoord);" +
            "gl_FragColor = oColor * check + mColor * (1.0 - check);\n" +
            "}\n";




    public MirrorReflection()
    {
        super(DEFAULT_VERTEX_SHADER,FRAGMENT_SHADER);
    }

    @Override
    protected void onDraw() {

    }
}
