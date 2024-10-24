package com.daasuu.mp4compose.filter.group_filters;

import android.opengl.GLES20;

import com.daasuu.mp4compose.filter.GlFilter;
import com.daasuu.mp4compose.gl.GlFramebufferObject;

public class GlAffineTranslateFilter extends GlFilter {
    private static final String TRANSLATE_FRAGMENT_SHADER =
            "precision highp float;\n" +
                    "varying highp vec2 vTextureCoord;\n" +
                    "uniform lowp sampler2D sTexture;" +
                    "uniform mediump float offsetX;" +
                    "uniform mediump float offsetY;\n" +
                    "void main() {\n" +
                    "   float u = (vTextureCoord.x - offsetX);" +
                    "   float v = (vTextureCoord.y - offsetY);" +
                    "   vec4 color = (vTextureCoord.x < offsetX || vTextureCoord.y < offsetY" +
                    "|| u > 1.0 || v >1.0 )? vec4(vec3(0),1) : texture2D(sTexture, vec2(u,v)); " +
                    "   gl_FragColor = color ;\n" +
                    "}\n";

    private float offsetX;
    private float offsetY;


    public GlAffineTranslateFilter() {
        super(DEFAULT_VERTEX_SHADER, TRANSLATE_FRAGMENT_SHADER);
        this.offsetX = 0.05f;
        this.offsetY = 0.05f;
    }

    @Override
    public void draw(int texName, GlFramebufferObject fbo) {
        super.draw(texName, fbo);
    }

    @Override
    protected void onDraw() {

//        GLES20.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
//
//        GLES20.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        GLES20.glUniform1f(getHandle("offsetX"), offsetX);
        GLES20.glUniform1f(getHandle("offsetY"), offsetY);

    }

    public void setOffset(float offsetX, float offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public float getOffsetX() {
        return offsetX;
    }

    public float getOffsetY() {
        return offsetY;
    }

    @Override
    public void setFrameSize(int width, int height) {
        super.setFrameSize(width, height);
    }
}
