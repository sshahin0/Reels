package com.daasuu.mp4compose.filter;

import java.util.Random;

public class GlChangingColorMatrixFilter extends GlColorMatrixFilter {

    private float[] green;
    private float[] red;
    private float[] blue;
    private Random rand;
    private float inputTime;
    private long time;
    private int index;

    private int colorLevel = 253;

    public GlChangingColorMatrixFilter(){
        super();
        
        rand = new Random();
        time = System.currentTimeMillis();
        index = 0;
    }

    @Override
    protected void onDraw() {
        super.onDraw();
    }


    private float[] setGreenValue(float green) {

        return new float[]{
                1.0f - green, 0.0f, 0.0f, 0.0f,
                green * 0.2f, green, green * 0.05f,0.0f,
                0.0f, 0.0f, green * 0.09f, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f,
        };
    }

    private float[] setRedValue(float red) {

        return new float[]{
                red, red * 0.10f , red * 0.01f , 0.0f,
                0.0f, 1.0f - red, 0.0f, 0.0f,
                0.0f, 0.0f, red * 0.8f, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f

        };
    }

    private float[] setBlueValue(float blue) {

        return new float[]{
                1.0f, blue * 0.5f, 0.0f, 0.0f,
                blue * 0.3f, 1.0f, 0.0f, 0.0f,
                blue * 0.1f, blue * 0.03f , blue, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f
        };
    }

    public void setColorMatrixForIndexValue(int index,float value) {
        if(index == 0)
        {
            setColorMatrix(setRedValue(value));
        }

        if(index == 1)
        {
            setColorMatrix(setGreenValue(value));
        }

        if(index == 2)
        {
            setColorMatrix(setBlueValue(value));
        }
    }

    @Override
    public void setIntensity(float intensity) {
        super.setIntensity(intensity);
    }
}
