package com.daasuu.mp4compose.filter;

import android.opengl.GLES20;

public class ShadedTileFilter extends GlFilter {

    private static final String FRAGMENT_SHADER =
            "precision mediump float;\n" +
                    "varying highp vec2 vTextureCoord;\n" +
                    "uniform lowp sampler2D sTexture;" +
                    "uniform float _tileSize;\n" +
                    "void main() {\n" +
                    "vec2 coord = vTextureCoord;" +
                    "float brightness = 1.0 - (mod(coord.x, _tileSize) / _tileSize) ;" +
                    "brightness *= 1.0 - (mod(coord.y, (_tileSize - 0.08)) / (_tileSize - 0.08)) ; " +
                    "vec4 pixelValue = texture2D(sTexture, vTextureCoord);" +
                    "gl_FragColor = vec4( (brightness + 0.4) * pixelValue.rgb, pixelValue.a);\n" +
                    "}\n";

    private float tileSize = 0.15f;

    public ShadedTileFilter()
    {
        super(DEFAULT_VERTEX_SHADER,FRAGMENT_SHADER);
    }



    @Override
    protected void onDraw() {

        GLES20.glUniform1f(getHandle("_tileSize"), tileSize);
    }
}
