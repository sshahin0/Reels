package com.daasuu.mp4compose.filter;

import android.opengl.GLES20;
import android.util.Log;

import com.daasuu.mp4compose.gl.GlFramebufferObject;

import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_FLOAT;

public class GlStripesGeneratorFilter extends GlFilter {

    private static final String VERTEX_SHADER =
            "attribute highp vec4 aPosition;\n" +
                    "varying highp vec2 vCoord;\n" +
                    "void main() {\n" +
                    "gl_Position = aPosition;\n" +
                    "vCoord = aPosition.xy;\n" +
                    "}\n";


    private static final String FRAGMENT_SHADER = "" +
            "precision mediump float;\n" +
            "varying vec2 vCoord;" +
            "uniform float texelHeight;" +
            "uniform float texelWidth;" +
            "uniform float bandWidth;" +
            "uniform float angle;" +
            "uniform vec3 color1;" +
            "uniform vec3 color2;" +
            "uniform float offset;" +
            "mat2 rotm(float r) {\n" +
            "    float cr = cos(r);\n" +
            "    float sr = sin(r);\n" +
            "    return mat2(cr,-sr,sr,cr);\n" +
            "}" +
            "" +
            "void main() {\n" +
            "   float u = (vCoord.x + 1.0) * texelWidth/ 2.0;  " +
            "   float v = (vCoord.y + 1.0) * texelHeight/ 2.0;  " +
            "   mat2 rotatedM = rotm(angle);" +
            "   vec2 rotatedUV =  rotatedM * vec2(u - offset,v); " +
            "   float bandUnit = bandWidth / texelWidth; " +
            "   float r = floor(mod(rotatedUV.x / bandWidth, 2.0));" +
            "gl_FragColor = (r == 0.0) ? vec4(color1,1.0) : vec4(color2,1.0);\n" +
            "}\n";

    private float angle;

    private int bandWidth;

    private float[] color1;
    private float[] color2;
    private float offset;

    public GlStripesGeneratorFilter() {
        super(VERTEX_SHADER, FRAGMENT_SHADER);
        setColor1(new float[]{1.0f, 1.0f, 1.0f});
        setColor2(new float[]{0.0f, 0.0f, 0.0f});
        setBendWidth(40);
        setAngle(45);
    }

    @Override
    public void handlerLoadForShader() {
    }

    @Override
    public void draw(final int texName, final GlFramebufferObject fbo) {

        useProgram();

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, getVertexBufferName());
        GLES20.glEnableVertexAttribArray(getHandle("aPosition"));
        GLES20.glVertexAttribPointer(getHandle("aPosition"), VERTICES_DATA_POS_SIZE, GL_FLOAT, false, VERTICES_DATA_STRIDE_BYTES, VERTICES_DATA_POS_OFFSET);

        onDraw();

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(getHandle("aPosition"));
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }


    @Override
    protected void onDraw() {

        Log.d("stripes", "onDraw: angle" + angle);

        GLES20.glUniform1f(getHandle("texelHeight"), (float) getOutputHeight());
        GLES20.glUniform1f(getHandle("texelWidth"), (float) getOutputWidth());
        GLES20.glUniform1f(getHandle("bandWidth"), bandWidth);
        GLES20.glUniform1f(getHandle("angle"), angle);
        GLES20.glUniform1f(getHandle("offset"), offset);
        GLES20.glUniform3fv(getHandle("color1"), 1, FloatBuffer.wrap(color1));
        GLES20.glUniform3fv(getHandle("color2"), 1, FloatBuffer.wrap(color2));

    }

    public void setBendWidth(int bandWidth) {
        this.bandWidth = bandWidth;
    }

    public void setAngle(float angleInDegree) {

        this.angle = (float) Math.toRadians(angleInDegree);
    }

    public void setColor1(float[] color1) {
        this.color1 = color1;
    }

    public void setColor2(float[] color2) {
        this.color2 = color2;
    }

    public void setOffset(float offset) {
        this.offset = offset;
    }
}
