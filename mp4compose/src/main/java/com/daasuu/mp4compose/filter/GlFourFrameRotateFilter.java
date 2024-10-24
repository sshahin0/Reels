package com.daasuu.mp4compose.filter;

import android.util.Pair;

import com.daasuu.mp4compose.filter.BlendFilters.GlBlendMultiInputSample;
import com.daasuu.mp4compose.gl.GlFramebufferObject;

import java.util.ArrayList;

public class GlFourFrameRotateFilter extends GlBlendMultiInputSample {
    private static final String FRAGMENT_SHADER =
            "precision highp float;\n" +
                    "varying highp vec2 vTextureCoord;\n" +
                    "uniform lowp sampler2D sTexture0;\n" +
                    "uniform lowp sampler2D sTexture1;\n" +
                    "uniform lowp sampler2D sTexture2;\n" +
                    "uniform lowp sampler2D sTexture3;\n" +
                    "const float tileWidth = 0.5;" +
                    "const float tileHeight = 0.5;" +
                    "void main() {\n" +
                    "   float gridX = mod(vTextureCoord.x, tileWidth) *  (1.0 / tileWidth);" +
                    "   float gridY = mod(vTextureCoord.y, tileHeight) * (1.0 / tileHeight);" +
                    "   vec2 uv = vec2(gridX,gridY);" +
                    "   float rId = step(tileWidth,  vTextureCoord.x);" +
                    "   float cId = step(tileHeight, vTextureCoord.y);" +
                    "   vec4 color1 = texture2D(sTexture2, uv);" +
                    "   vec4 color2 = texture2D(sTexture3, uv);" +
                    "   vec4 color3 = texture2D(sTexture1, uv);" +
                    "   vec4 color4 = texture2D(sTexture0, uv);" +
                    "   " +
                    "   gl_FragColor =  color1 * (1.0 - rId) * (1.0 - cId) + " +
                    "                   color2 * rId * (1.0 - cId) +" +
                    "                   color3 * rId * cId + " +
                    "                   color4 * (1.0 - rId) * cId;" +
                    "}\n";

    GlTransformFilter firstTransform;
    GlTransformFilter secondTransform;
    GlTransformFilter thirdTransform;
    GlTransformFilter fourthTransform;

    GlFilterGroup firstTransformWithMirror;
    GlFilterGroup secondTransformWithMirror;
    GlFilterGroup thirdTransformWithMirror;
    GlFilterGroup fourthTransformWithMirror;


    GlFilter firstSub;
    GlFilter secondSub;
    GlFilter thirdSub;
    GlFilter fourthSub;

    GlFilterGroup firstGroup;
    GlFilterGroup secondGroup;
    GlFilterGroup thirdGroup;
    GlFilterGroup fourthGroup;
    private float angleIn;
    private long time;
    private float baseScale;


    public GlFourFrameRotateFilter(final GlFilter... glFilters) {
        super(FRAGMENT_SHADER);

        this.firstTransform = new GlTransformFilter();
        this.secondTransform = new GlTransformFilter();
        this.thirdTransform = new GlTransformFilter();
        this.fourthTransform = new GlTransformFilter();
        baseScale = 1.0f;
        GlMirrorTileFilter mirror = new GlMirrorTileFilter();
        mirror.setTileScale(1.3f);

        firstTransformWithMirror = new GlFilterGroup(mirror, firstTransform);
        secondTransformWithMirror = new GlFilterGroup(mirror, secondTransform);
        thirdTransformWithMirror = new GlFilterGroup(mirror, thirdTransform);
        fourthTransformWithMirror = new GlFilterGroup(mirror, fourthTransform);


        addFourSubFilterBeforeRotate(glFilters);

    }

    private void makeGroup() {
        if (firstSub != null) {
            firstGroup = new GlFilterGroup(firstSub, firstTransformWithMirror);
        } else {
            firstGroup = new GlFilterGroup(firstTransformWithMirror);
        }

        if (secondSub != null) {
            secondGroup = new GlFilterGroup(secondSub, secondTransformWithMirror);
        } else {
            secondGroup = new GlFilterGroup(secondTransformWithMirror);
        }

        if (thirdSub != null) {
            thirdGroup = new GlFilterGroup(thirdSub, thirdTransformWithMirror);
        } else {
            thirdGroup = new GlFilterGroup(thirdTransformWithMirror);
        }


        if (fourthSub != null) {
            fourthGroup = new GlFilterGroup(fourthSub, fourthTransformWithMirror);
        } else {
            fourthGroup = new GlFilterGroup(fourthTransformWithMirror);
        }

        angleIn = (float) (45 * Math.PI) / 180f;

        //updateAngleWithTime();


    }

    @Override
    public void setup() {
        super.setup();
    }

    @Override
    public void draw(int texName, GlFramebufferObject fbo) {

        baseScale = (float) (float) getOutputWidth() / (float) getOutputHeight() - 0.08f; // 0.06 value for some offset from aspect ratio to zoom in
        super.draw(texName, fbo);
    }

    @Override
    protected void onDraw(int texName, GlFramebufferObject fbo, ArrayList<Pair<GlFilter, GlFramebufferObject>> list) {

        super.onDraw(texName, fbo, list);
    }

//    private void updateAngleWithTime() {
//
//        float time = (System.currentTimeMillis() % 6000)/ 6000f;
//
//
//        setRotationAngleFor1stQuadrant(angleIn,2.0f);
//        setRotationAngleFor2ndQuadrant(angleIn,3.0f);
//        setRotationAngleFor3rdQuadrant(angleIn,1.0f);
//        setRotationAngleFor4thQuadrant(angleIn,0.0f);
//
//    }

    public void setRotationAngleFor1stQuadrant(float angleStep, float rotation) {

        firstTransform.resetTrasformation();
        firstTransform.setScaleUnit(baseScale, baseScale);
        firstTransform.setRotateInAngle((float) Math.toDegrees(setAngle(angleStep, rotation)));
    }

    public void setRotationAngleFor2ndQuadrant(float angleStep, float rotation) {

        secondTransform.resetTrasformation();
        secondTransform.setScaleUnit(baseScale, baseScale);
        secondTransform.setRotateInAngle((float) Math.toDegrees(setAngle(angleStep, rotation)));
    }

    public void setRotationAngleFor3rdQuadrant(float angleStep, float rotation) {

        thirdTransform.resetTrasformation();
        thirdTransform.setScaleUnit(baseScale, baseScale);
        thirdTransform.setRotateInAngle((float) Math.toDegrees(setAngle(angleStep, rotation)));
    }

    public void setRotationAngleFor4thQuadrant(float angleStep, float rotation) {

        fourthTransform.resetTrasformation();
        fourthTransform.setScaleUnit(baseScale, baseScale);
        fourthTransform.setRotateInAngle((float) Math.toDegrees(setAngle(angleStep, rotation)));
    }

    public float setAngle(float angleStep, float rotation) {

        return (float) (rotation * Math.PI / 2.0) + angleStep;

    }

    public void addFourSubFilterBeforeRotate(final GlFilter... glFilters) {
        if (glFilters.length < 4) {
            return;
        }

        firstSub = glFilters[0];
        secondSub = glFilters[1];
        thirdSub = glFilters[2];
        fourthSub = glFilters[3];

        makeGroup();
        addFilter(firstGroup, secondGroup, thirdGroup, fourthGroup);

    }

    public void setBaseScale(float baseScale) {
        this.baseScale = baseScale;
    }
}