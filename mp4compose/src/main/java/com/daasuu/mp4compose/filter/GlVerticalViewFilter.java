package com.daasuu.mp4compose.filter;

import android.opengl.GLES20;


public class GlVerticalViewFilter extends GlFilter {

    private static final String FRAGMENT_SHADER =  "precision highp float;\n" +
            "\n" +
            "varying highp vec2 vTextureCoord;\n" +
            "uniform sampler2D sTexture;\n" +
            " uniform highp float offsetY;\n" +
            " \n" +
            " void main()\n" +
            " {\n" +
            "     vec2 uv = vTextureCoord;" +
            "     float yPos = uv.y + offsetY;" +
            "     yPos = yPos > 1.0 ? yPos - 1.0 : yPos;" +
            "     gl_FragColor = texture2D(sTexture, vec2(uv.x,yPos));\n" +
            " }\n";

    private float offsetY = 0.0f;
    public GlVerticalViewFilter(){

        super(DEFAULT_VERTEX_SHADER,FRAGMENT_SHADER);
        offsetY = 0.0f;
    }

    @Override
    protected void onDraw() {

       GLES20.glUniform1f(getHandle("offsetY"),this.offsetY);

    }

    public void setOffsetY(float offsetY) {
        this.offsetY = offsetY;
    }
}
