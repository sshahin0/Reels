package com.daasuu.mp4compose.filter.BlendFilters;

import android.opengl.GLES20;
import android.util.Log;
import android.util.Pair;

import com.daasuu.mp4compose.filter.GlFilter;
import com.daasuu.mp4compose.filter.GlStripesGeneratorFilter;
import com.daasuu.mp4compose.gl.GlFramebufferObject;

import java.util.ArrayList;

public class GlToggleInversionFilter extends GlBlendMultiInputSample {

    private static final String FRAGMENT_SHADER =
            "precision mediump float;\n" +
                    "varying highp vec2 vTextureCoord;\n" +
                    "uniform lowp sampler2D sTexture0;\n" +
                    "uniform lowp sampler2D sTexture;\n" +
                    "void main() {\n" +
                    "   vec4 color0 = texture2D(sTexture0, vTextureCoord);" +
                    "   vec4 color = texture2D(sTexture, vTextureCoord);" +
                    "   float active = (color0.r + color0.g + color0.b) / 3.0;" +
                    "   gl_FragColor = color * active + (1.0 - color) * (1.0 - active);\n" +
                    "}\n";
    private GlStripesGeneratorFilter maskImage;
    private GlFilter noFilter;
    private int counter;
    private float bandWidth;

    public GlToggleInversionFilter() {
        super(FRAGMENT_SHADER);

        maskImage = new GlStripesGeneratorFilter();
        maskImage.setOffset(0);
        maskImage.setAngle(0);
        setMaskBandSize(150);
        noFilter = new GlFilter();
        counter = 0;

        addFilter(maskImage);
    }

    @Override
    protected void onDraw(int texName, GlFramebufferObject fbo, ArrayList<Pair<GlFilter, GlFramebufferObject>> list) {

        Log.d("invert", "onDraw: counter" + counter);

        if (counter == bandWidth) {
            counter = 0;
        }

        setMaskImageOffset(counter);

        super.onDraw(texName, fbo, list);

        GLES20.glUniform1i(getHandle("sTexture"), 0);

        counter += 2;
    }

    public void setMaskImageOffset(float offset) {
        if (maskImage != null) {
            maskImage.setOffset(offset);
        }
    }

    public void setMaskBandSize(float bandWidth) {
        this.bandWidth = bandWidth;
        if (maskImage != null) {
            maskImage.setBendWidth((int) bandWidth / 2);
        }
    }

}
