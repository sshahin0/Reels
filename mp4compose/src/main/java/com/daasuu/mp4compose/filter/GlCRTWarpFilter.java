package com.daasuu.mp4compose.filter;

import android.opengl.GLES20;

import static android.opengl.GLES20.GL_TEXTURE_WRAP_S;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_T;
import static android.opengl.GLES32.GL_CLAMP_TO_BORDER;

public class GlCRTWarpFilter extends GlFilter {

    private static final String CRTWARP_FRAGMENT_SHADER = "" +
            "precision mediump float;\n" +
            "varying highp vec2 vTextureCoord;\n" +
            "uniform lowp sampler2D sTexture;" +
            "\n" +
            "void main() {\n" +
            "   vec2 dc =  ( vTextureCoord  - 0.5 ) * 2.0;" +
            "   dc.x *= 1.0 + pow(abs(dc.y) / 3.2 , 2.0);" +
            "   dc.y *= 1.0 + pow(abs(dc.x) / 3.2, 2.0);" +
            "   dc = ((dc / 2.0) + 0.5) ;\n" +
            "   gl_FragColor = texture2D(sTexture, dc);\n" +
            "}\n";


    public GlCRTWarpFilter() {
        super(DEFAULT_VERTEX_SHADER, CRTWARP_FRAGMENT_SHADER);
    }

    @Override
    protected void onDraw() {

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, getTextureName());
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
    }
}
