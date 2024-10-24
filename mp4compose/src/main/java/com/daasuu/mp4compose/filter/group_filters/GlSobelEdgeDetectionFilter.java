package com.daasuu.mp4compose.filter.group_filters;

import com.daasuu.mp4compose.filter.GlFilterGroup;
import com.daasuu.mp4compose.filter.GlGrayScaleFilter;
import com.daasuu.mp4compose.filter.GlThreex3TextureSamplingFilter;

public class GlSobelEdgeDetectionFilter extends GlFilterGroup {

    private static final String SOBEL_EDGE_DETECTION = "" +
            "precision mediump float;\n" +
            "\n" +
            "varying vec2 textureCoordinate;\n" +
            "varying vec2 leftTextureCoordinate;\n" +
            "varying vec2 rightTextureCoordinate;\n" +
            "\n" +
            "varying vec2 topTextureCoordinate;\n" +
            "varying vec2 topLeftTextureCoordinate;\n" +
            "varying vec2 topRightTextureCoordinate;\n" +
            "\n" +
            "varying vec2 bottomTextureCoordinate;\n" +
            "varying vec2 bottomLeftTextureCoordinate;\n" +
            "varying vec2 bottomRightTextureCoordinate;\n" +
            "\n" +
            "uniform sampler2D sTexture;\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "    float bottomLeftIntensity = texture2D(sTexture, bottomLeftTextureCoordinate).r;\n" +
            "    float topRightIntensity = texture2D(sTexture, topRightTextureCoordinate).r;\n" +
            "    float topLeftIntensity = texture2D(sTexture, topLeftTextureCoordinate).r;\n" +
            "    float bottomRightIntensity = texture2D(sTexture, bottomRightTextureCoordinate).r;\n" +
            "    float leftIntensity = texture2D(sTexture, leftTextureCoordinate).r;\n" +
            "    float rightIntensity = texture2D(sTexture, rightTextureCoordinate).r;\n" +
            "    float bottomIntensity = texture2D(sTexture, bottomTextureCoordinate).r;\n" +
            "    float topIntensity = texture2D(sTexture, topTextureCoordinate).r;\n" +
            "    float h = -topLeftIntensity - 2.0 * topIntensity - topRightIntensity + bottomLeftIntensity + 2.0 * bottomIntensity + bottomRightIntensity;\n" +
            "    float v = -bottomLeftIntensity - 2.0 * leftIntensity - topLeftIntensity + bottomRightIntensity + 2.0 * rightIntensity + topRightIntensity;\n" +
            "\n" +
            "    float mag = length(vec2(h, v));\n" +
            "\n" +
            "    gl_FragColor = vec4(vec3(mag), 1.0);\n" +
            "}";

    public GlSobelEdgeDetectionFilter() {
        super();
        GlGrayScaleFilter gray = new GlGrayScaleFilter();
        GlThreex3TextureSamplingFilter sample = new GlThreex3TextureSamplingFilter(SOBEL_EDGE_DETECTION);
        setFilters(gray, sample);

    }

}
