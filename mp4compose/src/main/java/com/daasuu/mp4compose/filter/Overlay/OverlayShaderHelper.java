package com.daasuu.mp4compose.filter.Overlay;

public class OverlayShaderHelper {

    protected static final String RANDOM_NOISE =
            "float rand(vec2 co)\n" +
                    "{\n" +
                    "    highp float a = 12.9898;\n" +
                    "    highp float b = 78.233;\n" +
                    "    highp float c = 43758.5453;\n" +
                    "    highp float dt= dot(co.xy ,vec2(a,b));\n" +
                    "    highp float sn= mod(dt,3.14);\n" +
                    "    return fract(sin(sn) * c);\n" +
                    "}";

    protected static final String GLOBAL_VARIBALE = "" +
            "   precision highp float;\n" +
            "   varying highp vec2 vTextureCoord;\n" +
            "   uniform lowp sampler2D sTexture;" +
            "   uniform lowp sampler2D mTexture;" ;



    public static final String VIDEO_OVERLAY_FRAGMENT = "" +
            GLOBAL_VARIBALE+
            "\n" +
            "void main() {\n" +
            "   vec4 baseColor = texture2D(sTexture, vTextureCoord); " +
            "   vec4 overlay = texture2D(mTexture, vTextureCoord);" +
            "   vec3 luminanceWeighting = vec3(0.2125, 0.7154, 0.0721);" +
            "   vec3 mask =  1.0 - dot(overlay.rgb, luminanceWeighting) <=  0.94 ? vec3(1.0) : vec3(0.0); " +
            "   float thresh = smoothstep(mask.r, overlay.r, 1.0);" +
            "   gl_FragColor = vec4(mix(baseColor.rgb,overlay.rgb, 1.0 - thresh),1.0);\n" +
            "}\n";


    public static final String RAINY_GLASS_FRAGMENT = "" +
            GLOBAL_VARIBALE + RANDOM_NOISE +
            "" +
            "\n" +
            "void main() {\n" +
            "   vec2 uv = vTextureCoord;" +
            "   vec2 noise = vec2(rand(uv),rand(uv.yx))* 0.05;" +
            "   vec4 baseColor = texture2D(sTexture, uv);" +
            "   vec4 overlay = texture2D(mTexture, uv); " +
            "   overlay = (baseColor.r == overlay.r && baseColor.g == overlay.g && baseColor.b == overlay.b) ? vec4(0.0) : overlay;" +
            "   baseColor = texture2D(sTexture, uv - noise * 0.2 , 1.0 );" +
            "   vec3 luminanceWeighting = vec3(0.2125, 0.7154, 0.0721);" +
            "   float thresh = smoothstep(dot(overlay.rgb,luminanceWeighting),.75,1.0) < 0.05 ? 0.0 : 1.0;" +
            "   vec4 mask = vec4(texture2D(sTexture, uv * 0.995).rgb  , 1.5);" +
            "   gl_FragColor = vec4(mix(mask.rgb,baseColor.rgb,thresh),1.0) ;\n" +
            "}";


    public static final String RAIN_DROPS_FRAGMENT = "" +
            GLOBAL_VARIBALE +
            "" +
            "\n" +
            "void main() {\n" +
            "vec2 uv = vTextureCoord;" +
            "vec4 overlay = texture2D(mTexture, uv);" +
            "vec4 baseColor = texture2D(sTexture, uv);" +
            "overlay = (baseColor.r == overlay.r && baseColor.g == overlay.g && baseColor.b == overlay.b) ? vec4(0.0) : overlay;" +
            " uv = (overlay.r - .5) > 0.0 ? vec2(uv.x * (1.0 - (overlay.r - 0.5) * (20.0/720.)), uv.y * (1.0 - (overlay.r - 0.5) * (20.0/1280.))) : uv ;"+
            " gl_FragColor = texture2D(sTexture, uv);\n" +
            "}";

    public static final String DOTS_FRAGMENT_SHADER = "" +
            GLOBAL_VARIBALE +
            "\n" +
            "   uniform vec3 dotColor;" +
            "void main(){" +
            "   vec2 uv = vTextureCoord;" +
            "   vec4 overlay = texture2D(mTexture, uv);" +
            "   vec4 baseColor = texture2D(sTexture, uv);" +
            "   overlay = (baseColor.r == overlay.r && baseColor.g == overlay.g && baseColor.b == overlay.b) ? vec4(0.0) : overlay;" +
            "   overlay.rgb = overlay.rgb * dotColor;" +
            "   float thresh = dot(overlay.rgb,dotColor) > 1.5 ? 1.0 : 0.0 ;" +
            "   gl_FragColor = vec4(mix(baseColor.rgb, overlay.rgb, thresh), 1.0);" +
            "}";




}
