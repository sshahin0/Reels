package com.daasuu.mp4compose.filter.BlendFilters;

import android.opengl.GLES20;
import android.util.Pair;

import com.daasuu.mp4compose.filter.GlFilter;
import com.daasuu.mp4compose.filter.GlTransformFilter;
import com.daasuu.mp4compose.gl.GlFramebufferObject;

import java.util.ArrayList;

public class GlTileZoomFilter extends GlBlendMultiInputSample {

    private static final String FRAGMENT_SHADER = "precision highp float;\n" +
            "varying highp vec2 vTextureCoord;\n" +
            "uniform lowp sampler2D sTexture0;\n" +
            "uniform lowp sampler2D sTexture1;" +
            "uniform highp float offset;\n" +
            "void main() {\n" +
            "   vec2 uv = vTextureCoord;" +
            "   float thresh = step(0.5,uv.y);" +
            "   vec4 color1 = texture2D(sTexture0, vTextureCoord);" +
            "   vec4 color2 = texture2D(sTexture1, vTextureCoord);" +
            "   float isBlank = (uv.x < offset || (1.0 - uv.x)<offset ||" +
            "                       uv.y < offset || (1.0 - uv.y)<offset) ? 1.0 : 0.0;" +
            "   gl_FragColor = color1 * (1.0 - isBlank) + color2 * isBlank;\n" +
            "}\n";


    private GlTransformFilter scaledFilter;
    private GlFilter srcFilter;

    private float maximumOffset = 1 / 4f;
    private float maximumScaleUnit = 0.7f;
    private float minimumOffset = 0;
    private float offsetShifted = 0;

    public GlTileZoomFilter() {
        super(FRAGMENT_SHADER);

        scaledFilter = new GlTransformFilter();
        srcFilter = new GlFilter();

        setOffsetShifted(0.0f);

        addFilter(srcFilter, scaledFilter);
    }

    @Override
    protected void onDraw(int texName, GlFramebufferObject fbo, ArrayList<Pair<GlFilter, GlFramebufferObject>> list) {
        super.onDraw(texName, fbo, list);
        GLES20.glUniform1f(getHandle("offset"), offsetShifted);
    }

    //accepted range between 0-1
    public void setOffsetShifted(float offsetShifted) {

        if (offsetShifted >= 1f) {
            this.offsetShifted = maximumOffset;
            scaledFilter.setScaleUnit(maximumScaleUnit, maximumScaleUnit);
        } else {
            this.offsetShifted = minimumOffset + (maximumOffset - minimumOffset) * offsetShifted;
            float scaleUnit = maximumScaleUnit - (offsetShifted * maximumScaleUnit);
            scaledFilter.resetTrasformation();
            scaledFilter.setScaleUnit(1f - scaleUnit, 1f - scaleUnit);
        }
    }

    public void setMaximumOffset(float maximumOffset) {
        this.maximumOffset = maximumOffset;
    }

    public void setMinimumOffset(float minimumOffset) {
        this.minimumOffset = minimumOffset;
    }

    public void setMaximumScaleUnit(float maximumScaleUnit) {
        this.maximumScaleUnit = maximumScaleUnit;
    }
}
