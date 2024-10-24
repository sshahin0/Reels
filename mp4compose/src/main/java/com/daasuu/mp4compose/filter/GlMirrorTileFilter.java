package com.daasuu.mp4compose.filter;

import android.opengl.GLES20;

import java.nio.FloatBuffer;

public class GlMirrorTileFilter extends GlFilter{

    private static final String FRAGMENT_SHADER = "" +
            "precision highp float;\n" +
            "varying highp vec2 vTextureCoord;\n" +
            "uniform lowp sampler2D sTexture;" +
            "uniform vec2 offset;" +
            "uniform vec2 tileSize;" +
            "void main(){" +
            "   float tileWidth = 1.0 / tileSize.x;" +
            "   float tileHeight = 1.0 / tileSize.y;" +
            "   float offsetX = (1.0 / 2.0) + offset.x;" +
            "   float offsetY = (1.0 / 2.0) + offset.y;" +
            //translate (0,0) to bottom-left corner of tile
            "   float translateX = vTextureCoord.x - offsetX + (tileWidth / 2.0);" +
            "   float translateY = vTextureCoord.y - offsetY + (tileHeight / 2.0);" +
            //Grid number identified from tile size
            "   float numGridX = floor(translateX / tileWidth);" +
            "   float numGridY = floor(translateY / tileHeight);" +
            //scale full texture into tile
            "   float u = (translateX - ( numGridX * tileWidth)); " +
            "   float v = (translateY - ( numGridY * tileHeight)) ;" +
            "   u += (u < 0.0) ?  tileWidth : 0.0 ;" +
            "   v += (v < 0.0) ?  tileHeight : 0.0 ;" +
            "   u = (int(mod(numGridX, 2.0)) == 0) ? u / tileWidth  : (tileWidth - u ) / tileWidth ;" +
            "   v = (int(mod(numGridY, 2.0)) == 0) ? v / tileHeight : (tileHeight - v) / tileHeight;" +
            "   vec2 pixelPosition = vec2(u,v);" +
            "   gl_FragColor = texture2D(sTexture,pixelPosition);" +
            "}";

    private float tileScale;
    private float offsetX;
    private float offsetY;

    public GlMirrorTileFilter()
    {
        super(DEFAULT_VERTEX_SHADER,FRAGMENT_SHADER);
        //default parameter for shader
        tileScale = 3.0f;
        offsetX = 0.0f;
        offsetY = 0.0f;
    }

    @Override
    protected void onDraw() {

        GLES20.glUniform2fv(getHandle("tileSize"), 1, FloatBuffer.wrap(new float[]{tileScale,tileScale}));
        GLES20.glUniform2fv(getHandle("offset"), 1, FloatBuffer.wrap(new float[]{offsetX,offsetY}));

    }

    public void setTileScale(float tileScale) {
        this.tileScale = tileScale;
    }

    public void setOffsetPosition(final float offsetX, final float offsetY)
    {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }
}
