package com.daasuu.mp4compose.filter.BlendFilters;

import android.opengl.GLES20;
import android.util.Pair;

import com.daasuu.mp4compose.filter.GlFilter;
import com.daasuu.mp4compose.filter.GlFilterGroup;
import com.daasuu.mp4compose.filter.GlGrayScaleFilter;
import com.daasuu.mp4compose.filter.GlRandomStripesFilter;
import com.daasuu.mp4compose.filter.group_filters.GlAffineTranslateFilter;
import com.daasuu.mp4compose.gl.GlFramebufferObject;

import java.util.ArrayList;
import java.util.Random;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;

public class GlGlitchFilter extends GlBlendMultiInputSample {

    private static final String FRAGMENT_SHADER =
            "precision mediump float;\n" +
                    "varying highp vec2 vTextureCoord;\n" +
                    "uniform lowp sampler2D sTexture0;\n" +
                    "uniform lowp sampler2D sTexture1;\n" +
                    "uniform lowp sampler2D sTexture2;\n" +
                    "uniform lowp sampler2D sTexture3;\n" +
                    "uniform lowp sampler2D sTexture4;\n" +
                    "void main() {\n" +
                    "   vec4 color0 = texture2D(sTexture0, vTextureCoord);" +
                    "   vec4 color1 = texture2D(sTexture1, vTextureCoord);" +
                    "   vec4 color2 = texture2D(sTexture2, vTextureCoord);" +
                    "   vec4 color3 = texture2D(sTexture3, vTextureCoord);" +
                    "   vec4 color4 = texture2D(sTexture4, vTextureCoord);" +
                    "   float maskValue = (color0.r + color0.g + color0.b)/  3.0;" +
                    "   vec4 newColor = vec4(color2.r, color3.g, color4.b,1.0);" +
                    "   gl_FragColor = mix(color1,newColor, maskValue);\n" +
                    "}\n";

    private int minShift = 10;
    private int maxShift = 70;
    private int shiftDifference = maxShift - minShift;
    private GlRandomStripesFilter hStripeMask;
    private GlFilterGroup sourceImage;
    private GlAffineTranslateFilter leftShift;
    private GlAffineTranslateFilter rightShift;
    private GlAffineTranslateFilter topRightShift;
    private boolean isStripeGenerated;
    private Random rand;
    private int val;
    private float offsetX;

    public GlGlitchFilter() {

        super(FRAGMENT_SHADER);

        hStripeMask = new GlRandomStripesFilter();
        sourceImage = new GlFilterGroup(new GlGrayScaleFilter());
        leftShift = new GlAffineTranslateFilter();
        rightShift = new GlAffineTranslateFilter();
        topRightShift = new GlAffineTranslateFilter();

        isStripeGenerated = false;

        addFilter(hStripeMask, sourceImage, leftShift, rightShift, topRightShift);

        rand = new Random();
    }

    @Override
    protected void preProcessing(int prevTexName, ArrayList<Pair<GlFilter, GlFramebufferObject>> list) {

        offsetX = randomOffsetGen();
        leftShift.setOffset((-offsetX + 30) / getOutputWidth(), 0);

        rightShift.setOffset((offsetX - 20) / getOutputWidth(), 0);

        topRightShift.setOffset(offsetX / getOutputWidth(), 0);


        for (int counter = 0; counter < list.size(); counter++) {
            Pair<GlFilter, GlFramebufferObject> pair = list.get(counter);
            if (isStripeGenerated && counter == 0) {
                if (pair.second != null) {
                    if (pair.first != null) {
                        pair.second.enable();
                        pair.first.draw(pair.second.getTexName(), pair.second);
                    }
                }
            } else {

                isStripeGenerated = true;

                if (pair.second != null) {
                    if (pair.first != null) {
                        pair.second.enable();
                        GLES20.glClear(GL_COLOR_BUFFER_BIT);
                        pair.first.draw(prevTexName, pair.second);
                    }
                }
            }


        }
    }

    private float randomOffsetGen() {
        val = rand.nextInt(1000);
        return minShift + (val % shiftDifference);
    }
}
