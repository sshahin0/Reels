package com.daasuu.mp4compose.filter;

import android.opengl.GLES20;
import android.util.Pair;

import com.daasuu.mp4compose.gl.GlFramebufferObject;

import java.util.Random;

public class GlRandomStripesFilter extends GlFilter {


    private Pair<GlHorizontalStripe, GlFramebufferObject> stripe;


    private float minHeight = 50f;
    private float maxHeight = 150f;
    private float difference = maxHeight - minHeight;
    private boolean stripeGenerated;
    private GlFramebufferObject fbo;

    public GlRandomStripesFilter() {
        super(DEFAULT_VERTEX_SHADER, DEFAULT_FRAGMENT_SHADER);

        stripeGenerated = false;

        GlHorizontalStripe hStripe = new GlHorizontalStripe();

        hStripe.setStartingIndex(0);
        hStripe.setEndIndex(0);
        GlFramebufferObject foo = new GlFramebufferObject();
        stripe = Pair.create(hStripe, foo);


    }

    @Override
    public void setup() {
        super.setup();

        if (stripe.first != null) {
            stripe.first.setup();
        }
    }

    @Override
    public void draw(int texName, GlFramebufferObject fbo) {
        this.fbo = fbo;
        super.draw(texName, fbo);
    }

    @Override
    protected void onDraw() {

        if (!stripeGenerated) {

            //random stripe generator
            float height = (float) getOutputHeight();
            float currentHeight = 0f;
            int curIndex = 0;
            Random rand = new Random();


            while (currentHeight < height) {
                float rendHeight = minHeight + rand.nextInt(1000) % (int) difference;

                if (curIndex % 2 == 1) {
                    currentHeight += rendHeight;
                } else {
                    stripe.second.enable();
                    stripe.first.setStartingIndex(currentHeight);
                    stripe.first.setEndIndex(rendHeight);

                    stripe.first.draw(stripe.second.getTexName(), stripe.second);
                    stripe.second.disable();
                    currentHeight += rendHeight;
                }

                curIndex++;
            }
            stripeGenerated = true;
            fbo.enable();
        }

        //latest bind texture in active unit TEXTURE_0.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, stripe.second.getTexName());
    }


    @Override
    public void setFrameSize(int width, int height) {
        super.setFrameSize(width, height);

        if (stripe != null) {
            stripe.second.setup(width, height);
            stripe.first.setFrameSize(width, height);
        }
    }

    public boolean isStripeGenerated() {
        return stripeGenerated;
    }

    @Override
    public void release() {

        super.release();

        if (stripe.first != null)
            stripe.first.release();
        if (stripe.second != null)
            stripe.second.release();
    }
}
