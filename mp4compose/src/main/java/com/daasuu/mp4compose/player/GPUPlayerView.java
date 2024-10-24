package com.daasuu.mp4compose.player;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLException;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;

import com.daasuu.mp4compose.filter.GlFilter;
import com.daasuu.mp4compose.gl.GlConfigChooser;
import com.daasuu.mp4compose.gl.GlContextFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.video.VideoListener;

import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.opengles.GL10;

public class GPUPlayerView extends GLSurfaceView implements VideoListener {

    private final static String TAG = GPUPlayerView.class.getSimpleName();

    private GPUPlayerRenderer renderer;
    private SimpleExoPlayer player;

    public float videoAspect = 1f;
    public PlayerScaleType playerScaleType = PlayerScaleType.RESIZE_FIT_WIDTH;

    public GPUPlayerView(Context context) {
        this(context, null);
    }

    public GPUPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setEGLContextFactory(new GlContextFactory());
        setEGLConfigChooser(new GlConfigChooser(false));

        this.renderer = new GPUPlayerRenderer(this);
        setRenderer(renderer);
    }

    public GPUPlayerView setSimpleExoPlayer(SimpleExoPlayer player) {
        if (this.player != null) {
            this.player.release();
            this.player = null;
        }

        this.player = player;
        this.player.addVideoListener(this);
        this.renderer.setSimpleExoPlayer(player);
        return this;
    }

    public void setGlFilter(GlFilter glFilter) {
        renderer.setGlFilter(glFilter);
    }

    public void setPlayerScaleType(PlayerScaleType playerScaleType) {
        this.playerScaleType = playerScaleType;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();

        int viewWidth = measuredWidth;
        int viewHeight = measuredHeight;

        switch (playerScaleType) {
            case RESIZE_FIT_WIDTH:
                viewHeight = (int) (measuredWidth / videoAspect);
                if (viewHeight > measuredHeight) {
                    viewHeight = measuredHeight;
                }

                viewWidth = (int) (measuredHeight * videoAspect);
                if (viewWidth > measuredWidth) {
                    viewWidth = measuredWidth;
                }
                break;
            case RESIZE_FIT_HEIGHT:
                viewWidth = (int) (measuredHeight * videoAspect);
                if (viewWidth > measuredWidth) {
                    viewWidth = measuredWidth;
                }

                viewHeight = (int) (measuredWidth / videoAspect);
                if (viewHeight > measuredHeight) {
                    viewHeight = measuredHeight;
                }
                break;
        }

        setMeasuredDimension(viewWidth, viewHeight);
    }

    @Override
    public void onPause() {
        super.onPause();
        renderer.release();
    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
        videoAspect = ((float) width / height) * pixelWidthHeightRatio;
    }

    @Override
    public void onRenderedFirstFrame() {
        // do nothing
    }

    public void adjustWidthHeight(float width, float height, float rotation) {
        if (width > height) {
            if (rotation == 0 || rotation == 180) {
                playerScaleType = PlayerScaleType.RESIZE_FIT_WIDTH;
                videoAspect = width / height;
            } else {
                playerScaleType = PlayerScaleType.RESIZE_FIT_HEIGHT;
                videoAspect = height / width;
            }
        } else if (width < height) {
            if (rotation == 0 || rotation == 180) {
                playerScaleType = PlayerScaleType.RESIZE_FIT_HEIGHT;
                videoAspect = width / height;
            } else {
                playerScaleType = PlayerScaleType.RESIZE_FIT_WIDTH;
                videoAspect = height / width;
            }
        } else {
            playerScaleType = PlayerScaleType.RESIZE_FIT_WIDTH;
            videoAspect = 1;
        }

        requestLayout();

    }

    // supporting methods
    private Bitmap snapshotBitmap;

    public void captureBitmap(final Activity activity, final BitmapReadyCallbacks bitmapReadyCallbacks) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                EGL10 egl = (EGL10) EGLContext.getEGL();
                GL10 gl = (GL10) egl.eglGetCurrentContext().getGL();
                snapshotBitmap = createBitmapFromGLSurface(0, 0, getWidth(), getHeight(), gl);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bitmapReadyCallbacks.onBitmapReady(snapshotBitmap);
                    }
                });
            }
        });
    }

    // from other answer in this question
    private Bitmap createBitmapFromGLSurface(int x, int y, int w, int h, GL10 gl) {
        int[] bitmapBuffer = new int[w * h];
        int[] bitmapSource = new int[w * h];
        IntBuffer intBuffer = IntBuffer.wrap(bitmapBuffer);
        intBuffer.position(0);

        try {
            gl.glReadPixels(x, y, w, h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, intBuffer);
            int offset1, offset2;
            for (int i = 0; i < h; i++) {
                offset1 = i * w;
                offset2 = (h - i - 1) * w;
                for (int j = 0; j < w; j++) {
                    int texturePixel = bitmapBuffer[offset1 + j];
                    int blue = (texturePixel >> 16) & 0xff;
                    int red = (texturePixel << 16) & 0x00ff0000;
                    int pixel = (texturePixel & 0xff00ff00) | red | blue;
                    bitmapSource[offset2 + j] = pixel;
                }
            }
        } catch (GLException e) {
            Log.e(TAG, "createBitmapFromGLSurface: " + e.getMessage(), e);
            return null;
        }

        return Bitmap.createBitmap(bitmapSource, w, h, Bitmap.Config.ARGB_8888);
    }

    public SimpleExoPlayer getPlayer() {
        return player;
    }

    public interface BitmapReadyCallbacks {
        void onBitmapReady(Bitmap bitmap);
    }

}

