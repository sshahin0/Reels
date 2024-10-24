package com.daasuu.mp4compose.filter.BlendFilters;

import android.opengl.GLES20;
import android.util.Pair;

import com.daasuu.mp4compose.filter.GlFilter;
import com.daasuu.mp4compose.filter.group_filters.GlAffineTranslateFilter;
import com.daasuu.mp4compose.gl.GlFramebufferObject;

import java.util.ArrayList;

import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_S;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_T;
import static android.opengl.GLES32.GL_CLAMP_TO_BORDER;

public class GlChromaticAbberationFilter extends GlBlendMultiInputSample {

    private static final String FRAGMENT_SHADER = "" +
            "precision mediump float;\n" +
            "varying highp vec2 vTextureCoord;\n" +
            "uniform lowp sampler2D sTexture0;\n" +
            "uniform lowp sampler2D sTexture1;\n" +
            "uniform lowp sampler2D sTexture2;\n" +
            "void main() {\n" +
            "   vec4 red = texture2D(sTexture0,vTextureCoord);" +
            "   vec4 green = texture2D(sTexture1,vTextureCoord);" +
            "   vec4 blue = texture2D(sTexture2,vTextureCoord);" +
            "   gl_FragColor = vec4(red.r, green.g, blue.b, 1.0);\n" +
            "}\n";

    private GlAffineTranslateFilter[] filters;
    private float uTime;
    private float texelWidthUnit;
    private float texelHeightUnit;
    private long time;
    boolean flag;
    int secCouunter = 0;
    private float inputRadius;
    private float inputAngle;
    private int width;

    public GlChromaticAbberationFilter() {
        super(FRAGMENT_SHADER);
        filters = new GlAffineTranslateFilter[3];

        inputAngle = 0.0f;
        //red channel
        filters[0] = new GlAffineTranslateFilter();
        //green channel
        filters[1] = new GlAffineTranslateFilter();
        //blue channel
        filters[2] = new GlAffineTranslateFilter();

        addFilter(filters[0], filters[1], filters[2]);
        //inputRadius = 0.05f;
        width = 1280;
        setInputRadius(30);

        //configureSubFilters(inputRadius,inputAngle);
        time = System.currentTimeMillis();
        flag = false;
        secCouunter = 0;

    }

    @Override
    protected void onDraw(int texName, GlFramebufferObject fbo, ArrayList<Pair<GlFilter, GlFramebufferObject>> list) {

//        if (System.currentTimeMillis() - time >= 50) {
//            time = System.currentTimeMillis();
//            if (!flag) {
//                uTime = uTime + 0.01f;
//                ++secCouunter;
//            }
//            if (flag) {
//                uTime = uTime - 0.01f;
//                secCouunter--;
//            }
//
//            if (secCouunter == 0) {
//                flag = false;
//            }
//            if (secCouunter == 10) {
//                flag = true;
//            }
//        }
//        //configureSubFilters(uTime* inputRadius,inputAngle);
//        configureSubFilters((float) (Math.sin(uTime) * inputRadius),inputAngle);


        super.onDraw(texName, fbo, list);

        //border enable
        GLES20.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);

        GLES20.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
    }


    public void configureSubFilters(float inputRadius, float inputAngle) {

        float pi = (float) Math.PI * 2f;
        float redAngle = inputAngle + pi;
        float greenAngle = inputAngle + (pi * 0.3333f);

        float blueAngle = inputAngle + (pi * 2.0f * 0.3333f);

        filters[0].setOffset((float) Math.sin(redAngle) * inputRadius, (float) Math.cos(redAngle) * inputRadius);

        filters[1].setOffset((float) Math.sin(greenAngle) * inputRadius, -(float) Math.cos(greenAngle) * inputRadius);
        filters[2].setOffset((float) Math.sin(blueAngle) * inputRadius, (float) Math.cos(blueAngle) * inputRadius);
    }

    @Override
    public void setFrameSize(int width, int height) {
        super.setFrameSize(width, height);
        this.width = width;
    }

    public void setInputRadius(float radius) {
        this.inputRadius = radius / width;
        configureSubFilters(inputRadius, inputAngle);
    }

    public void setInputAngle(float inputAngle) {
        this.inputAngle = inputAngle;
        configureSubFilters(inputRadius, inputAngle);
    }

    public float getInputAngle() {
        return inputAngle;
    }

    public float getInputRadius() {
        return inputRadius;
    }
}
