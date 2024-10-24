package com.daasuu.mp4compose.filter;

import android.opengl.GLES20;

public class Gl3x3ConvolutionFilter extends GlThreex3TextureSamplingFilter{

    protected static final String THREE_X_THREE_TEXTURE_SAMPLING_FRAGMENT_SHADER = "" +
            "precision mediump float;\n" +
            "\n" +
            "uniform sampler2D sTexture;\n" +
            "\n" +
            "uniform mediump mat3 convolutionMatrix;\n" +
            "\n" +
            "varying highp vec2 textureCoordinate;" +
            "varying highp vec2 leftTextureCoordinate;" +
            "varying highp vec2 rightTextureCoordinate;" +

            "varying highp vec2 topTextureCoordinate;" +
            "varying highp vec2 topLeftTextureCoordinate;" +
            "varying highp vec2 topRightTextureCoordinate;" +

            "varying highp vec2 bottomTextureCoordinate;" +
            "varying highp vec2 bottomLeftTextureCoordinate;" +
            "varying highp vec2 bottomRightTextureCoordinate;" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "    mediump vec4 bottomColor = texture2D(sTexture, bottomTextureCoordinate);\n" +
            "    mediump vec4 bottomLeftColor = texture2D(sTexture, bottomLeftTextureCoordinate);\n" +
            "    mediump vec4 bottomRightColor = texture2D(sTexture, bottomRightTextureCoordinate);\n" +
            "    mediump vec4 centerColor = texture2D(sTexture, textureCoordinate);\n" +
            "    mediump vec4 leftColor = texture2D(sTexture, leftTextureCoordinate);\n" +
            "    mediump vec4 rightColor = texture2D(sTexture, rightTextureCoordinate);\n" +
            "    mediump vec4 topColor = texture2D(sTexture, topTextureCoordinate);\n" +
            "    mediump vec4 topRightColor = texture2D(sTexture, topRightTextureCoordinate);\n" +
            "    mediump vec4 topLeftColor = texture2D(sTexture, topLeftTextureCoordinate);\n" +
            "\n" +
            "    mediump vec4 resultColor = topLeftColor * convolutionMatrix[0][0] + topColor * convolutionMatrix[0][1] + topRightColor * convolutionMatrix[0][2];\n" +
            "    resultColor += leftColor * convolutionMatrix[1][0] + centerColor * convolutionMatrix[1][1] + rightColor * convolutionMatrix[1][2];\n" +
            "    resultColor += bottomLeftColor * convolutionMatrix[2][0] + bottomColor * convolutionMatrix[2][1] + bottomRightColor * convolutionMatrix[2][2];\n" +
            "\n" +
            "    gl_FragColor = resultColor;\n" +
            "}";


    private float[] kernel;
    private float texelWidth;
    private float texelHeight;
    private float radius;

    public Gl3x3ConvolutionFilter() {
        this(new float[]{
                0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 0.0f
        });
    }


    public Gl3x3ConvolutionFilter(float[] kernel) {

        super(THREE_X_THREE_TEXTURE_SAMPLING_FRAGMENT_SHADER);
        this.kernel = kernel;
        texelWidth = 0f;
        texelHeight = 0f;
        this.radius = 1f;
    }

    @Override
    public void setup() {
        super.setup();
        setConvolutionKernel(kernel);
    }

    public Gl3x3ConvolutionFilter(String fragmentShader)
    {
        super(fragmentShader);
    }

    @Override
    public void onDraw()
    {
        if(texelHeight == 0 || texelWidth == 0 )
        {
            super.onDraw();
        }
        else {

            GLES20.glUniform1f(getHandle("texelWidth"), texelWidth);
            GLES20.glUniform1f(getHandle("texelHeight"), texelHeight);
        }
        GLES20.glUniformMatrix3fv(getHandle("convolutionMatrix"), 1, false, kernel, 0);
    }

    public void setConvolutionKernel(float[] kernel) {
        this.kernel = kernel;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public void setFrameSize(final float width, final float height)
    {
        texelWidth = radius/ width;
        texelHeight = radius/ height;
    }

}
