package com.daasuu.mp4compose.filter;

import android.opengl.GLES20;


import com.daasuu.mp4compose.gl.GlFramebufferObject;

import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_FLOAT;

public class GlConstantColorFilter extends GlFilter {

    private static final String VERTEX_SHADER =
            "attribute highp vec4 aPosition;\n" +
                    "void main() {\n" +
                    "gl_Position = aPosition;\n" +
                    "}\n";


    private static final String FRAGMENT_SHADER = "" +
            "precision mediump float;\n" +
            "uniform vec3 color;" +
            "void main() {\n" +
            "gl_FragColor = vec4(color,1.0);\n" +
            "}\n";

    private float[] color;

    public GlConstantColorFilter() {
        super(VERTEX_SHADER, FRAGMENT_SHADER);
        setColor(new float[]{0.0f, 0.0f, 0.0f});
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

        GLES20.glUniform3fv(getHandle("color"), 1, FloatBuffer.wrap(color));

    }


    public void setColor(float[] color) {
        this.color = color;
    }

}
