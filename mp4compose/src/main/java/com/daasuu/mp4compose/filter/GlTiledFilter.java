package com.daasuu.mp4compose.filter;

import android.opengl.GLES20;

public class GlTiledFilter extends GlFilter{
    private static final String FRAGMENT_SHADER = "" +
            "precision mediump float;\n" +
            "varying highp vec2 vTextureCoord;\n" +
            "uniform lowp sampler2D sTexture;" +
            "uniform lowp float tileHeight;" +
            "uniform lowp float tileWidth;" +
            "void main(){" +
            "   float gridX = mod(vTextureCoord.x, tileWidth) *  (1.0 / tileWidth);" +
            "   float gridY = mod(vTextureCoord.y, tileHeight) * (1.0 / tileHeight);" +
            "   vec2 uv = vec2(gridX,gridY);" +
            "   gl_FragColor = texture2D(sTexture,uv) ;" +
            "}";

    private float numberOfTile;
    private float tileWidth;
    private float tileHeight;

    public GlTiledFilter()
    {
        this(2);
    }

    public GlTiledFilter(int numberOfTile)
    {
        super(DEFAULT_VERTEX_SHADER,FRAGMENT_SHADER);
        this.numberOfTile = (float) numberOfTile;
        updateTileUnit();
    }

    @Override
    protected void onDraw() {
        GLES20.glUniform1f(getHandle("tileHeight"), tileHeight);
        GLES20.glUniform1f(getHandle("tileWidth"), tileWidth);
    }

    public void setNumberOfTile(float numberOfTile) {
        this.numberOfTile = numberOfTile;
        updateTileUnit();
    }

    private void updateTileUnit() {

        tileHeight = 1.0f / numberOfTile ;
        tileWidth = 1.0f / numberOfTile ;
    }

}