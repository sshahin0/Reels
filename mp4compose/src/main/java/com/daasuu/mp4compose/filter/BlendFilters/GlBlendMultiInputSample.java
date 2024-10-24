package com.daasuu.mp4compose.filter.BlendFilters;

import android.opengl.GLES20;
import android.opengl.GLES32;
import android.util.Pair;

import com.daasuu.mp4compose.filter.GlFilter;
import com.daasuu.mp4compose.gl.GlFramebufferObject;

import java.util.ArrayList;
import java.util.Arrays;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FLOAT;

public class GlBlendMultiInputSample extends GlFilter {

    private static final String BLEND_TO_INPUT_FRAGMENT_SHADER = "" +
            "precision mediump float;\n" +
            "varying highp vec2 vTextureCoord;\n" +
            "uniform lowp sampler2D sTexture0;\n" +
            "void main() {\n" +
            "   vec4 color1 = texture2D(sTexture0, vTextureCoord);" +
            "   gl_FragColor = color1;\n" +
            "}\n";

    private int prevTexName = -1;

    private ArrayList<GlFilter> groupOfFilters = new ArrayList<GlFilter>();
    private ArrayList<Pair<GlFilter, GlFramebufferObject>> list = new ArrayList<Pair<GlFilter, GlFramebufferObject>>();

    public GlBlendMultiInputSample() {
        this(DEFAULT_VERTEX_SHADER, BLEND_TO_INPUT_FRAGMENT_SHADER);
        addFilter(new GlFilter());
    }


    public GlBlendMultiInputSample(String defaultVertexShader, String blendToInputFragmentShader) {

        super(defaultVertexShader, blendToInputFragmentShader);
    }

    public GlBlendMultiInputSample(String fragmentShader) {
        this(DEFAULT_VERTEX_SHADER, fragmentShader);
        clearGroupFilters();

    }

    private void clearGroupFilters() {
        groupOfFilters.clear();
    }


    @Override
    public void setup() {
        super.setup();
        initializeFilters();
    }

    @Override
    public void handlerLoadForShader() {

        getHandle("aPosition");
        getHandle("aTextureCoord");
        getHandle("sTexture0");
    }

    //initialized all sub filters before drawing
    public void initializeFilters() {
        if (groupOfFilters != null) {
            final int max = groupOfFilters.size();
            int count = 0;

            for (final GlFilter shader : groupOfFilters) {
                shader.setup();
                final GlFramebufferObject fbo;
                if ((count + 1) <= max) {
                    fbo = new GlFramebufferObject();
                } else {
                    fbo = null;
                }
                list.add(Pair.create(shader, fbo));
                count++;
            }
        }
    }

    @Override
    public void draw(int texName, GlFramebufferObject fbo) {

        onDraw(texName, fbo, list);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(getHandle("aPosition"));
        GLES20.glDisableVertexAttribArray(getHandle("aTextureCoord"));
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

    }

    protected void onDraw(int texName, GlFramebufferObject fbo, ArrayList<Pair<GlFilter, GlFramebufferObject>> list) {

        //pre process of creating multiple inputs for current output
        prevTexName = texName;

        preProcessing(prevTexName, list);

        //enable current output module and execute shaders for final result
        fbo.enable();
        useProgram();

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, getVertexBufferName());
        GLES20.glEnableVertexAttribArray(getHandle("aPosition"));
        GLES20.glVertexAttribPointer(getHandle("aPosition"), VERTICES_DATA_POS_SIZE, GL_FLOAT, false, VERTICES_DATA_STRIDE_BYTES, VERTICES_DATA_POS_OFFSET);
        GLES20.glEnableVertexAttribArray(getHandle("aTextureCoord"));
        GLES20.glVertexAttribPointer(getHandle("aTextureCoord"), VERTICES_DATA_UV_SIZE, GL_FLOAT, false, VERTICES_DATA_STRIDE_BYTES, VERTICES_DATA_UV_OFFSET);

        for (int counter = 0; counter < list.size(); counter++) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + (counter + 1));
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, list.get(counter).second.getTexName());
            GLES20.glUniform1i(getHandle("sTexture" + counter), (counter + 1));
        }
//        //active the 0th unit for output
        GLES20.glActiveTexture(GLES32.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, prevTexName);

    }

    protected void preProcessing(int prevTexName, ArrayList<Pair<GlFilter, GlFramebufferObject>> list) {

        for (final Pair<GlFilter, GlFramebufferObject> pair : list) {

            if (pair.second != null) {
                if (pair.first != null) {
                    pair.second.enable();
                    GLES20.glClear(GL_COLOR_BUFFER_BIT);
                    pair.first.draw(prevTexName, pair.second);
                }
            }
        }
    }

    @Override
    public void release() {

        for (final Pair<GlFilter, GlFramebufferObject> pair : list) {
            if (pair.first != null) {
                pair.first.release();
            }
            if (pair.second != null) {
                pair.second.release();
            }
        }
        list.clear();
        super.release();
    }

    public void addFilter(GlFilter filter) {
        if (filter != null) {
            groupOfFilters.add(filter);
        }
    }

    public void addFilter(final GlFilter... glFilters) {
        if (groupOfFilters.size() > 0) {
            clearGroupFilters();
        }
        groupOfFilters.addAll(Arrays.asList(glFilters));
    }

    @Override
    public void setFrameSize(final int width, final int height) {
        super.setFrameSize(width, height);

        for (final Pair<GlFilter, GlFramebufferObject> pair : list) {
            if (pair.first != null) {
                pair.first.setFrameSize(width, height);
            }
            if (pair.second != null) {
                pair.second.setup(width, height);
            }
        }
    }

}
