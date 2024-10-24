package com.daasuu.mp4compose.filter;

import android.opengl.GLES20;

public class GlLaplacianFilter extends GlThreex3TextureSamplingFilter {

    private static final String LAPLACIAN_FRAGMENT_SHADER = "" +
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
            "    mediump vec3 bottomColor = texture2D(sTexture, bottomTextureCoordinate).rgb;\n" +
            "    mediump vec3 bottomLeftColor = texture2D(sTexture, bottomLeftTextureCoordinate).rgb;\n" +
            "    mediump vec3 bottomRightColor = texture2D(sTexture, bottomRightTextureCoordinate).rgb;\n" +
            "    mediump vec4 centerColor = texture2D(sTexture, textureCoordinate);\n" +
            "    mediump vec3 leftColor = texture2D(sTexture, leftTextureCoordinate).rgb;\n" +
            "    mediump vec3 rightColor = texture2D(sTexture, rightTextureCoordinate).rgb;\n" +
            "    mediump vec3 topColor = texture2D(sTexture, topTextureCoordinate).rgb;\n" +
            "    mediump vec3 topRightColor = texture2D(sTexture, topRightTextureCoordinate).rgb;\n" +
            "    mediump vec3 topLeftColor = texture2D(sTexture, topLeftTextureCoordinate).rgb;\n" +
            "\n" +
            "    mediump vec3 resultColor = topLeftColor * convolutionMatrix[0][0] + topColor * convolutionMatrix[0][1] + topRightColor * convolutionMatrix[0][2];\n" +
            "    resultColor += leftColor * convolutionMatrix[1][0] + centerColor.rgb * convolutionMatrix[1][1] + rightColor * convolutionMatrix[1][2];\n" +
            "    resultColor += bottomLeftColor * convolutionMatrix[2][0] + bottomColor * convolutionMatrix[2][1] + bottomRightColor * convolutionMatrix[2][2];\n" +
            "\n" + // Normalize the results to allow for negative gradients in the 0.0-1.0 colorspace.
            "   resultColor = resultColor + 0.5;" +
            "  gl_FragColor = vec4(resultColor, centerColor.a);\n" +
            "}";

    private float[] kernel;
    private float texelWidth;
    private float texelHeight;

    public GlLaplacianFilter()
    {
        this(new float[]{
           0.5f, 1.0f, 0.5f,
           1.0f, -6.0f, 1.0f,
           0.5f, 1.0f, 0.5f
        });
        texelWidth = 0;
        texelHeight = 0;
    }

    public GlLaplacianFilter(float texelWidth, float texelHeight)
    {
        this();
        this.texelHeight = 1f / texelHeight;
        this.texelWidth = 1f / texelWidth;
    }



    public GlLaplacianFilter(float[] kernel)
    {
        super(LAPLACIAN_FRAGMENT_SHADER);
        this.kernel = kernel;
    }

    public void setKernel(final float[] kernel)
    {
        this.kernel = kernel;
    }

    public void setFrameSize(float width,float height)
    {
        texelHeight = 1f/height;
        texelWidth = 1f/width;

    }

    @Override
    public void onDraw() {

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
}
