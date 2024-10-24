package com.daasuu.mp4compose.filter.BlendFilters;

import android.opengl.GLES20;
import android.util.Pair;

import com.daasuu.mp4compose.filter.GlFilter;
import com.daasuu.mp4compose.filter.GlFilterGroup;
import com.daasuu.mp4compose.filter.GlGaussianBlurFilter;
import com.daasuu.mp4compose.filter.GlGaussianBlurInVerticalDirection;
import com.daasuu.mp4compose.filter.GlTransformFilter;
import com.daasuu.mp4compose.gl.GlFramebufferObject;

import java.nio.FloatBuffer;
import java.util.ArrayList;

public class GlStoryViewFilter extends GlBlendMultiInputSample {


    private static final String STORYVIEW_FRAGMENT_SHADER = "" +
            "precision highp float;\n" +
            "varying highp vec2 vTextureCoord;\n" +
            "uniform lowp sampler2D sTexture0;\n" +
            "uniform lowp sampler2D sTexture1;\n" +
            "uniform vec2 offset;" +
            "void main() {\n" +
            "   vec2 coord = vTextureCoord;" +
            "   vec4 color1 = texture2D(sTexture0, vTextureCoord);" +
            "   float tileWidth = 1.0 - (offset.x * 2.0);" +
            "   float tileHeight = 1.0 - (offset.y * 2.0);" +
            "   vec2 uv = ((coord - offset) / vec2(tileWidth,tileHeight)); " +
            "   vec4 srcColor = (uv.x < 0.0 || uv.y < 0.0 || uv.x > 1.0 || uv.y > 1.0 )? color1 : texture2D(sTexture1, uv);" +
            "   gl_FragColor = srcColor ;\n" +
            "}\n";


    private GlTransformFilter scaleImg;
    private GlGaussianBlurFilter blurImg;
    private GlGaussianBlurInVerticalDirection blurImgV;
    private GlFilter sourceImg;
    private GlFilterGroup scaleWithBlur;

    private float offsetX;
    private float offsetY;
    private float aspectRatio;

    public GlStoryViewFilter() {

        super(STORYVIEW_FRAGMENT_SHADER);
        aspectRatio = 0f;

        scaleImg = new GlTransformFilter();
        blurImg = new GlGaussianBlurFilter();
        blurImgV = new GlGaussianBlurInVerticalDirection();


        scaleWithBlur = new GlFilterGroup(scaleImg, blurImg, blurImgV, blurImg, blurImgV, blurImg, blurImgV);

        sourceImg = new GlFilter();
        addFilter(scaleWithBlur, sourceImg);
        setScaleSize(0.95f, 0.95f);
        setBlurSize(0.3f);
        setOffsetForTile(120f);
    }


    @Override
    protected void onDraw(int texName, GlFramebufferObject fbo, ArrayList<Pair<GlFilter, GlFramebufferObject>> list) {

        super.onDraw(texName, fbo, list);

        GLES20.glUniform2fv(getHandle("offset"), 1, FloatBuffer.wrap(new float[]{offsetX / (float) getOutputWidth(), offsetY / (float) getOutputHeight()}));
    }

    public void setOffsetForTile(float offsetX) {

        if (aspectRatio != 0f) {
            setOffsetYwithAspect(offsetX);
        } else {

            this.offsetX = offsetX;
            this.offsetY = offsetX;
        }
    }

    private void setOffsetYwithAspect(float offsetX) {

        this.offsetX = offsetX;
        this.offsetY = (offsetX / aspectRatio);
    }


    public void setScaleSize(float x, float y) {
        if (scaleImg != null) {
            scaleImg.setScaleUnit(x, y);
        }
    }

    public void setBlurSize(float blur) {
        if (blurImg != null) {
            blurImg.setBlurSize(blur);
            blurImgV.setBlurSize(blur);
        }
    }

    @Override
    public void setFrameSize(int width, int height) {
        super.setFrameSize(width, height);
        aspectRatio = (float) width / (float) height;
        blurImg.setTexelHeightOffset(1f / getOutputHeight());
        blurImg.setTexelHeightOffset(1f / getOutputWidth());
        blurImgV.setTexelHeightOffset(1f / getOutputHeight());
        blurImgV.setTexelHeightOffset(1f / getOutputWidth());
        setOffsetYwithAspect(offsetX);
    }
}
