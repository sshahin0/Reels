package com.daasuu.mp4compose.filter;

public class MonoEffectFilter extends GlFilter {

    private static final String FRAGMENT_SHADER =
            "precision mediump float;\n" +
                    "varying highp vec2 vTextureCoord;\n" +
                    "uniform lowp sampler2D sTexture;\n" +
                    "void main() {\n" +
                    " vec4 color = texture2D(sTexture, vTextureCoord);" +
                    " float mean = (color.r + color.g + color.b) / 3.0;" +
                    "gl_FragColor = vec4 (vec3(1.0 * log(1.0 + vec3(mean))),1.0) ;\n" +
                    "}\n";

    public MonoEffectFilter(){
        super(DEFAULT_VERTEX_SHADER,FRAGMENT_SHADER);
    }
}
