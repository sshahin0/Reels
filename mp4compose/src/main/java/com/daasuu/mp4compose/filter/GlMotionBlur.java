package com.daasuu.mp4compose.filter;

import android.opengl.GLES20;
import android.util.Log;

public class GlMotionBlur extends GlFilter {


    private static final String MOTION_BLUR_VERTEX_SHADER = "" +
            " attribute vec4 aPosition;" +
            " attribute vec4 aTextureCoord;" +
            " uniform vec2 directionalTexelStep;\n" +
            " \n" +
            " varying vec2 vTextureCoord;\n" +
            " varying vec2 oneStepBackTextureCoordinate;\n" +
            " varying vec2 twoStepsBackTextureCoordinate;\n" +
            " varying vec2 threeStepsBackTextureCoordinate;\n" +
            " varying vec2 fourStepsBackTextureCoordinate;\n" +
            " varying vec2 oneStepForwardTextureCoordinate;\n" +
            " varying vec2 twoStepsForwardTextureCoordinate;\n" +
            " varying vec2 threeStepsForwardTextureCoordinate;\n" +
            " varying vec2 fourStepsForwardTextureCoordinate;" +
            " void main(){" +
            "   gl_Position = aPosition;\n" +
            "     vTextureCoord = aTextureCoord.xy;\n" +
            "     oneStepBackTextureCoordinate = aTextureCoord.xy - directionalTexelStep;\n" +
            "     twoStepsBackTextureCoordinate = aTextureCoord.xy - (2.0 * directionalTexelStep);\n" +
            "     threeStepsBackTextureCoordinate = aTextureCoord.xy - (3.0 * directionalTexelStep);\n" +
            "     fourStepsBackTextureCoordinate = aTextureCoord.xy - (4.0 * directionalTexelStep);\n" +
            "     oneStepForwardTextureCoordinate = aTextureCoord.xy + directionalTexelStep;\n" +
            "     twoStepsForwardTextureCoordinate = aTextureCoord.xy + (2.0 * directionalTexelStep);\n" +
            "     threeStepsForwardTextureCoordinate = aTextureCoord.xy + (3.0 * directionalTexelStep);\n" +
            "     fourStepsForwardTextureCoordinate = aTextureCoord.xy + (4.0 * directionalTexelStep);"+
            "}";

    private static final String MOTION_BLUR_FRAGMENT_SHADER= "" +
            "precision mediump float;" +
            "varying vec2 vTextureCoord;\n" +
            "\n" +
            "uniform lowp sampler2D sTexture;\n" +
            "\n" +
            " varying vec2 oneStepBackTextureCoordinate;\n" +
            " varying vec2 twoStepsBackTextureCoordinate;\n" +
            " varying vec2 threeStepsBackTextureCoordinate;\n" +
            " varying vec2 fourStepsBackTextureCoordinate;\n" +
            " varying vec2 oneStepForwardTextureCoordinate;\n" +
            " varying vec2 twoStepsForwardTextureCoordinate;\n" +
            " varying vec2 threeStepsForwardTextureCoordinate;\n" +
            " varying vec2 fourStepsForwardTextureCoordinate;"+
            "void main(){" +
            "   vec4 fragmentColor = texture2D(sTexture, vTextureCoord) * 0.18;\n" +
            "   fragmentColor += texture2D(sTexture, oneStepBackTextureCoordinate) * 0.015;\n" +
            "   fragmentColor += texture2D(sTexture, twoStepsBackTextureCoordinate) *  0.12 ;\n" +
            "   fragmentColor += texture2D(sTexture, threeStepsBackTextureCoordinate) * 0.09;\n" +
            "   fragmentColor += texture2D(sTexture, fourStepsBackTextureCoordinate) * 0.05;\n" +
            "   fragmentColor += texture2D(sTexture, oneStepForwardTextureCoordinate) * 0.15;\n" +
            "   fragmentColor += texture2D(sTexture, twoStepsForwardTextureCoordinate) * 0.12;\n" +
            "   fragmentColor += texture2D(sTexture, threeStepsForwardTextureCoordinate) * 0.09;\n" +
            "   fragmentColor += texture2D(sTexture, fourStepsForwardTextureCoordinate) * 0.05;\n" +
            "   \n" +
            "   gl_FragColor = fragmentColor.ggga;" +
            "}";

    private float texelOffsetX, texelOffsetY;
    private float angle = 20.0f;
    private float radius = 15f;
    private float width = 1280f;
    private float height = 720f;

    public GlMotionBlur()
    {
        super( MOTION_BLUR_VERTEX_SHADER, MOTION_BLUR_FRAGMENT_SHADER );

        radius = 25f;
        angle = 45.0f;
        texelOffsetX = 0.0f;
        texelOffsetY = 0.0f;
    }



    @Override
    protected void onDraw() {

        Log.d("on_blur", "updateTexelPosition: "+ width+ " "+ height +" offsetX "+ texelOffsetX +" offsetY "+ texelOffsetY) ;

        GLES20.glUniform2f(getHandle("directionalTexelStep"), texelOffsetX, texelOffsetY);
    }

    @Override
    public void setFrameSize(int width, int height) {
        super.setFrameSize(width, height);
        this.width = width;
        this.height = height;
        updateTexelPosition();
    }

    private void updateTexelPosition() {

        float aspectRatio = 1.0f;
        Log.d("on_blur", "updateTexelPosition: "+ width+ " "+ height);


        if(height < width) {
            aspectRatio = height * (1f / width);

            texelOffsetX = radius * (float) Math.sin(Math.toRadians(angle)) / width;
            texelOffsetY = radius * (float) Math.cos(Math.toRadians(angle)) * aspectRatio / width;
        }
        else {

            aspectRatio = width * ( 1f / height);

            texelOffsetX = radius * (float) Math.sin(Math.toRadians(angle) ) / height;
            texelOffsetY = radius * (float) Math.cos(Math.toRadians(angle) ) * aspectRatio / height;

        }
    }

    public void setAngle(float angle) {
        this.angle = angle;
        updateTexelPosition();

    }

    public void setRadius(float radius) {
        this.radius = radius;
        updateTexelPosition();
    }
}
