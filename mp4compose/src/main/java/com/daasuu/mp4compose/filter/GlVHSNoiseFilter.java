package com.daasuu.mp4compose.filter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.util.Log;

import com.daasuu.mp4compose.R;

public class GlVHSNoiseFilter extends GlFilter {

    private static final String FRAGMENT_SHADER = "" +
            "precision mediump float;\n" +
            "varying highp vec2 vTextureCoord;\n" +
            "uniform lowp sampler2D sTexture;" +
            "uniform float u_time;" +

            "void main() {\n" +
            " vec4 color1 = texture2D(sTexture, vTextureCoord);" +
            "float stripe = smoothstep( (1.0 - 0.5), 1.0, cos( (  (u_time ) + vTextureCoord.y) / 0.04));" +
            "float wave = fract((10000.* (u_time + 0.02)) * sin( 360. * vTextureCoord.x + (640. * vTextureCoord.y + 0.03)));\n" +
            "gl_FragColor =  color1 + (wave * wave * stripe) + (wave * 0.05);\n" +
            "}\n";

    private Bitmap noise;
    private Context context;
    private float noiseWidth;
    private float noiseHeight;
    int textureID;
    long time;
    private float timeStamp = 0f;
    private int secCounter = 0;
    private int sign = 1;
    private boolean flag = false;

    public GlVHSNoiseFilter(Context context) {
        super(DEFAULT_VERTEX_SHADER, FRAGMENT_SHADER);
        this.context = context;


    }

    @Override
    public void setup() {
        super.setup();
        // loadNoiseSample();
    }


    private void loadNoiseSample() {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;

        noise = BitmapFactory.decodeResource(context.getResources(), R.drawable.hello, options);
        noiseWidth = 1 / noiseWidth;
        noiseHeight = 1 / noiseHeight;

        Log.v("noise", " " + noise.getWidth() + "  " + noise.getHeight());

        textureID = com.daasuu.gpuv.egl.EglUtil.loadTexture(noise, -1, false);

        time = System.currentTimeMillis();
        timeStamp = 0f;
        secCounter = 0;
        flag = false;


    }

    @Override
    protected void onDraw() {

        if (System.currentTimeMillis() - time >= 100) {

            time = System.currentTimeMillis();

            if (!flag) {
                timeStamp = timeStamp + 0.01f;
                ++secCounter;
            }
            if (flag) {
                timeStamp = timeStamp - 0.01f;
                secCounter--;
            }

            if (secCounter == 0) {
                flag = false;
            }

            if (secCounter == 5) {
                flag = true;
            }

            Log.d("ShowTIme", "onDraw: timeStamp " + timeStamp);
        }


        GLES20.glUniform1f(getHandle("u_time"), timeStamp);
//
//        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
//
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textureID);
//
//        GLES20.glUniform1i(getHandle("nTexture"), 1);
//
//        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
//
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,getTextureName());
    }
}
