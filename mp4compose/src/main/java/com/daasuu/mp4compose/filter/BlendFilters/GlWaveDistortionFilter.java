package com.daasuu.mp4compose.filter.BlendFilters;

import android.util.Pair;

import com.daasuu.mp4compose.filter.GlDistortionFilter;
import com.daasuu.mp4compose.filter.GlFilter;
import com.daasuu.mp4compose.filter.GlFilterGroup;
import com.daasuu.mp4compose.filter.GlTransformFilter;
import com.daasuu.mp4compose.gl.GlFramebufferObject;

import java.util.ArrayList;

public class GlWaveDistortionFilter extends GlBlendMultiInputSample {

    private static final String FRAGMENT_SHADER = "" +
            "precision mediump float;\n" +
            "varying highp vec2 vTextureCoord;\n" +
            "uniform lowp sampler2D sTexture0;\n" +
            "uniform lowp sampler2D sTexture1;\n" +
            "uniform lowp sampler2D sTexture2;\n" +
            "void main() {\n" +
            "   vec4 color = texture2D(sTexture0, vTextureCoord);" +
            "   vec4 red = texture2D(sTexture1, vTextureCoord);" +
            "   vec4 blue = texture2D(sTexture2, vTextureCoord);" +
            "   gl_FragColor = vec4(color.r,red.g,blue.b,color.a);\n" +
            "}\n";

    private GlDistortionFilter glDistortionFilter;
    private GlTransformFilter redTransform;
    private GlTransformFilter blueTransform;
    private float offset = 0;

    public GlWaveDistortionFilter() {
        super(FRAGMENT_SHADER);
        glDistortionFilter = new GlDistortionFilter();
        redTransform = new GlTransformFilter();
        blueTransform = new GlTransformFilter();
        setOffset(0.025f);
        addFilter(glDistortionFilter, new GlFilterGroup(glDistortionFilter, redTransform), new GlFilterGroup(glDistortionFilter, blueTransform));

    }

    @Override
    protected void preProcessing(int prevTexName, ArrayList<Pair<GlFilter, GlFramebufferObject>> list) {
        super.preProcessing(prevTexName, list);
    }

    public void setWaveLengthforXY(float xWaveLength, float yWaveLength) {
        if (glDistortionFilter != null) {
            glDistortionFilter.setWaveLength(xWaveLength, yWaveLength);
        }
    }

    public void setDistortionInX(float xAmount) {
        if (glDistortionFilter != null) {
            glDistortionFilter.setDistortionAmountInX(xAmount);
        }
    }


    public void setDistortionInY(float yAmount) {
        if (glDistortionFilter != null) {
            glDistortionFilter.setDistortionAmountInY(yAmount);
        }
    }

    public float getDistortionInX() {
        if (glDistortionFilter != null) {
            return glDistortionFilter.getxAmount();
        } else
            return 0;
    }


    public float getDistortionInY() {
        if (glDistortionFilter != null) {
            return glDistortionFilter.getyAmount();
        } else
            return 0;
    }

    public void setOffset(final float offset) {
        if (redTransform != null && blueTransform != null) {
            this.offset = offset;
            redTransform.resetTrasformation();
            redTransform.setTranslateOffset(offset, 0);
            blueTransform.resetTrasformation();
            blueTransform.setTranslateOffset(-offset, 0);
        }
    }


}
