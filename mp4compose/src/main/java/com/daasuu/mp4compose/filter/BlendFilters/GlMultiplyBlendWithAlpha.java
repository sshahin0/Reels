package com.daasuu.mp4compose.filter.BlendFilters;

import android.opengl.GLES20;
import android.util.Pair;

import com.daasuu.mp4compose.filter.GlFilter;
import com.daasuu.mp4compose.gl.GlFramebufferObject;

import java.util.ArrayList;

public class GlMultiplyBlendWithAlpha extends GlAdditionCompositingFilter {

    private static final String MULTIPLY_BLEND_FRAGMENT_SHADER = "" +
            "precision highp float;\n" +
            "varying highp vec2 vTextureCoord;\n" +
            "uniform lowp sampler2D sTexture0;" +
            "uniform lowp sampler2D sTexture1;" +
            "uniform float alpha;" +
            "" +
            HELPER_FUNCTION +
            "" +
            "void main() {\n" +
            "   vec4 color0 = texture2D(sTexture0, vTextureCoord);" +
            "   vec4 color1 = texture2D(sTexture1, vTextureCoord);" +
            "   vec4 blendedColor = multiplyBlend(color1,color0);" +
            "   gl_FragColor = mix(color1,blendedColor, alpha);\n" +
            "}";


    private float alpha = 1f;

    public GlMultiplyBlendWithAlpha() {

        super(MULTIPLY_BLEND_FRAGMENT_SHADER);

        alpha = 1f;

    }


    @Override
    protected void onDraw(int texName, GlFramebufferObject fbo, ArrayList<Pair<GlFilter, GlFramebufferObject>> list) {
        super.onDraw(texName, fbo, list);

        GLES20.glUniform1f(getHandle("alpha"), this.alpha);
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }
}
