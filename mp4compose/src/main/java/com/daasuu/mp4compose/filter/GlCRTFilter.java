package com.daasuu.mp4compose.filter;

import android.opengl.GLES20;

public class GlCRTFilter extends GlFilter {

    private static final String FRAGMENT_SHADER =
            "precision mediump float;\n" +
                    "varying highp vec2 vTextureCoord;\n" +
                    "uniform lowp sampler2D sTexture;" +
                    "uniform float height;" +
                    "uniform float widthRatio;\n" +
                    "void main() {\n" +
                    " highp vec2 point = vTextureCoord;" +
                    " float bandSize = 20.0;" +
                    " vec4 color = texture2D(sTexture, vTextureCoord);" +
                    " lowp int columnIndex = int( mod( point.x / height , 3.0));" +
                    " lowp int rowIndex = int( mod( point.y / widthRatio , bandSize));" +
                    " float scanLineMult = ( rowIndex == 0 || rowIndex == 1 ) ? 0.3 : 1.0 ;" +
                    " float red = (columnIndex == 0) ? color.r : color.r * 0.1;" +
                    " float green = (columnIndex == 1 )? color.g : color.g * 0.1;" +
                    " float blue = (columnIndex == 2 ) ? color.b : color.b * 0.1;" +
                    " gl_FragColor = vec4(vec3(red,green,blue) * scanLineMult , 1.0);\n" +
                    "}\n";

    private  float height =  0.02f;
    private float width = 1/1280f;

    public GlCRTFilter()
    {
        super(DEFAULT_VERTEX_SHADER,FRAGMENT_SHADER);
    }

    @Override
    protected void onDraw() {
        GLES20.glUniform1f(getHandle("height"), height);
        GLES20.glUniform1f(getHandle("widthRatio"), width);
    }
}
