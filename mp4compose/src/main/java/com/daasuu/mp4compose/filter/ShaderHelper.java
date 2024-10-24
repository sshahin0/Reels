package com.daasuu.mp4compose.filter;

public class ShaderHelper {

    public static final String GRAY_SCALE_FUNC = "" +
            "vec3 grayScale(vec3 pix){" +
            "   return vec3(dot(pix, vec3(0.2126, 0.7152, 0.0722)));" +
            "}" ;

    public static final String ROTATION_FUNC = "" +
            "mat2 rotation(float angle){" +
            "   float cr = cos(angle);" +
            "   float sr = sin(angle);" +
            " return mat2( vec2(cr,sr* -1.0), vec2(sr,cr) );" +
            "}" ;

}
