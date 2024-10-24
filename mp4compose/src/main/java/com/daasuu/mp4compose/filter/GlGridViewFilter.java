package com.daasuu.mp4compose.filter;

import android.opengl.GLES20;

public class GlGridViewFilter extends GlFilter {

    private static final String FRAGMENT_SHADER =
            "precision mediump float;\n" +
            "varying highp vec2 vTextureCoord;\n" +
            "uniform lowp sampler2D sTexture;" +
            "uniform float tileWidth;" +
            "uniform float tileHeight;" +
            "uniform float offsetX;" +
            "uniform float offsetY;\n" +
            "void main() {\n" +
            "   float baseOffsetX = (1.0 / 2.0) + offsetX;" +
            "   float baseOffsetY = (1.0 / 2.0) + offsetY;" +
            //translate (0,0) to bottom-left corner of tile
            "   float translateX = vTextureCoord.x - baseOffsetX + (tileWidth / 2.0);" +
            "   float translateY = vTextureCoord.y - baseOffsetY + (tileHeight / 2.0);" +
            //Grid number identified from tile size
            "   float numGridX = floor(translateX / tileWidth);" +
            "   float numGridY = floor(translateY / tileHeight);" +
            //scale full texture into tile
            "   float u = (translateX - ( numGridX * tileWidth)); " +
            "   float v = (translateY - ( numGridY * tileHeight)) ;" +
            "   u += (u < 0.0) ?  tileWidth : 0.0 ;" +
            "   v += (v < 0.0) ?  tileHeight : 0.0 ;" +
            "gl_FragColor = texture2D(sTexture, vec2(u/tileWidth,v/tileHeight));\n" +
            "}\n";


    private float gridWidth;
    private float gridHeight;
    private float gridOffsetX;
    private float gridOffsetY;

    public GlGridViewFilter(){
        super(DEFAULT_VERTEX_SHADER,FRAGMENT_SHADER);
        gridWidth = 0.5f;
        gridHeight = 0.5f;
        gridOffsetX = 0;
        gridOffsetY = 0;
    }

    @Override
    protected void onDraw() {

        GLES20.glUniform1f(getHandle("tileWidth"), gridWidth);
        GLES20.glUniform1f(getHandle("tileHeight"), gridHeight);
        GLES20.glUniform1f(getHandle("offsetX"), gridOffsetX);
        GLES20.glUniform1f(getHandle("offsetY"), gridOffsetY);
    }

    public void setGridWidthHeight(float width,float height)
    {
        // width height normalized; 0.0 - 1.0
        this.gridWidth = width;
        this.gridHeight = height;
    }

    public void setGridOffset(float offsetX, float offsetY)
    {
        this.gridOffsetX = offsetX;
        this.gridOffsetY = offsetY;
    }

    public float getGridHeight() {
        return gridHeight;
    }

    public float getGridWidth() {
        return gridWidth;
    }
}
