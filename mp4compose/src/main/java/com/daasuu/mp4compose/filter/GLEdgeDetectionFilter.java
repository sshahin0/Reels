package com.daasuu.mp4compose.filter;

public class GLEdgeDetectionFilter extends GlFilter {

    //Sobel Edge Detection Filter

    private static final String VERTEX_SHADER = "" +
            "attribute vec4 aPosition;\n" +
            "attribute vec4 aTextureCoord; \n " +
            "\n" +
            "varying vec2 vTextureCoord;" +
            "varying highp vec2 topLeftCoord;\n" +
            "varying highp vec2 topCoord;\n" +
            "varying highp vec2 topRightCoord;\n" +
            "varying highp vec2 leftCoord;\n" +
            "varying highp vec2 rightCoord;\n" +
            "varying highp vec2 bottomLeftCoord;" +
            "varying highp vec2 bottomCoord;" +
            "varying highp vec2 bottomRightCoord;" +
            "\n" +
            "void main(){\n" +
            "highp vec2 point = aTextureCoord.xy;" +
            "float offset = 0.002;" +
            "topLeftCoord = vec2(point.x - offset ,  point.y - offset);" +
            "topCoord = vec2(point.x ,  point.y - offset);" +
            "topRightCoord = vec2(point.x + offset,  point.y - offset);" +
            "leftCoord = vec2(point.x - offset, point.y);" +
            "rightCoord = vec2(point.x + offset, point.y);" +
            "bottomLeftCoord = vec2(point.x - offset , point.y + offset);" +
            "bottomCoord = vec2(point.x , point.y + offset);" +
            "bottomRightCoord = vec2(point.x + offset, point.y + offset);" +
            "vTextureCoord = point;" +
            "gl_Position = aPosition;" +
            "}\n" +
            "";

    private static final String FRAGMENT_SHADER = "" +
            "precision mediump float;" +
            " varying vec2 vTextureCoord;\n" +
            " uniform lowp sampler2D sTexture;\n" +
            "varying highp vec2 topLeftCoord;\n" +
            "varying highp vec2 topCoord;\n" +
            "varying highp vec2 topRightCoord;\n" +
            "varying highp vec2 leftCoord;\n" +
            "varying highp vec2 rightCoord;\n" +
            "varying highp vec2 bottomLeftCoord;" +
            "varying highp vec2 bottomCoord;" +
            "varying highp vec2 bottomRightCoord;" +
            "void main(){" +
            "\n" +
            "float dx = 0.0;" +
            "vec4 color = texture2D(sTexture, vTextureCoord);" +
            "float dy = dx;" +
            " dx += texture2D(sTexture, topLeftCoord).g * 0.039 ;" +
            " dx += texture2D(sTexture, leftCoord).g * 0.078 ;" +
            " dx += texture2D(sTexture, bottomLeftCoord).g * 0.039; " +
            " dx += texture2D(sTexture, topRightCoord).g * -0.039;" +
            " dx += texture2D(sTexture, rightCoord).g * -0.078;" +
            " dx += texture2D(sTexture, bottomRightCoord).g * -0.039;" +
            "\n" +
            " dy += texture2D(sTexture, topLeftCoord).g * 0.039;" +
            " dy += texture2D(sTexture, topCoord).g * 0.078;" +
            " dy += texture2D(sTexture, topRightCoord).g * 0.039; " +
            " dy += texture2D(sTexture, bottomLeftCoord).g * -0.039;" +
            " dy += texture2D(sTexture, bottomCoord).g * -0.078;" +
            " dy += texture2D(sTexture, bottomRightCoord).g * -0.039 ;" +
            "\n" +
            " float gradient = sqrt(( dx * dx ) + ( dy * dy )) ;" +
            "gradient = smoothstep(0.036, 0.041 , gradient);" +
            "vec3 edgeColor = vec3(1.0, 0.0, 0.2);" +
            "float grayValue = (color.r + color.g + color.b ) * 0.333 ;" +
            "vec3 grayColor = vec3(grayValue,grayValue,grayValue);" +
            "gl_FragColor = vec4( mix( (grayColor), edgeColor, gradient), 1.0); " +
            "}\n";


    public GLEdgeDetectionFilter() {
        super(VERTEX_SHADER, FRAGMENT_SHADER);
    }


    @Override
    protected void onDraw() {
    }
}
