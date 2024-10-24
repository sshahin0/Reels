package com.daasuu.mp4compose.filter;

public class GlEmbossFilter extends Gl3x3ConvolutionFilter {

    private float intensity;

    public GlEmbossFilter()
    {
        this(1.0f);
    }

    @Override
    public void setup() {
        super.setup();
    }

    public GlEmbossFilter(float intensity) {

        super( new StringBuilder()
                .append(THREE_X_THREE_TEXTURE_SAMPLING_FRAGMENT_SHADER.replace("gl_FragColor = resultColor", "float gray = (resultColor.r + resultColor.g +resultColor.b) / 3.0; \n" +
                        " gl_FragColor = vec4( vec3(gray),1.0)"))
                .toString());
        this.intensity = intensity;
        setIntensity(intensity);


    }

    public void setIntensity(final float intensity) {
        this.intensity = intensity;

        setConvolutionKernel(new float[]{
                (intensity * - 2.0f), -intensity  , 0.0f,
                -intensity, 1,  intensity,
                0.0f,  intensity , (intensity * 2.0f)
        });
    }

    public float getIntensity() {
        return intensity;
    }
}
