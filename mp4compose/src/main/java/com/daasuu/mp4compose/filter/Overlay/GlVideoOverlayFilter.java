package com.daasuu.mp4compose.filter.Overlay;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.SurfaceTexture;
import android.media.MediaMetadataRetriever;
import android.opengl.GLES20;
import android.util.Log;
import android.view.Surface;

import com.daasuu.mp4compose.composer.DecoderSurface;
import com.daasuu.mp4compose.composer.MoviePlayer;
import com.daasuu.mp4compose.composer.SpeedControlCallback;
import com.daasuu.mp4compose.filter.GlFilter;
import com.daasuu.mp4compose.gl.GlFramebufferObject;
import com.daasuu.mp4compose.gl.GlPreviewRenderer;
import com.daasuu.mp4compose.gl.TextureRenderer;
import com.daasuu.mp4compose.player.GPUPlayerRenderer;

import java.io.IOException;

public class GlVideoOverlayFilter extends GlFilter implements SurfaceTexture.OnFrameAvailableListener {

    private static String FRAGMENT_SHADER = OverlayShaderHelper.VIDEO_OVERLAY_FRAGMENT;

    protected String datasrc = null;
    protected int dataSrcId;
    protected Context context;
    private SurfaceTexture mSurfaceTexture = null;
    private Surface mSurface = null;

    private PlayMovieThread playMovieThread;
    private SpeedControlCallback mCallback;
    protected GlFramebufferObject overlayFrameBuffer;
    protected GlFramebufferObject baseFbo;
    private TextureRenderer textureRenderer;
    private int overlayTexId = -1;

    private boolean isSetup;
    private int updateTexImageCompare = 0;
    private int updateTexImageCounter = 0;
    private OverlayInfo overlayInfo;
    private int texName = -1;


    public GlVideoOverlayFilter(String datasrc) {
        this(DEFAULT_VERTEX_SHADER, FRAGMENT_SHADER);
        this.datasrc = datasrc;
    }

    public GlVideoOverlayFilter(Context context, int resId) {
        this(DEFAULT_VERTEX_SHADER, FRAGMENT_SHADER);
        this.dataSrcId = resId;
        this.context = context;
        this.datasrc = "";

    }

    public GlVideoOverlayFilter(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
        updateTexImageCompare = 0;
        updateTexImageCounter = 0;
        playMovieThread = null;
        isSetup = false;

    }

    @Override
    public void setup() {
        super.setup();
        //register new surface for

        if (overlayTexId == -1) {
            overlayTexId = com.daasuu.gpuv.egl.EglUtil.createOESTexture();
            Log.d("overlay", "setup: OES textureId " + overlayTexId);
        }

        mCallback = new SpeedControlCallback();
        overlayFrameBuffer = new GlFramebufferObject();
        textureRenderer = new TextureRenderer();

        mSurfaceTexture = new SurfaceTexture(overlayTexId);
        mSurfaceTexture.setOnFrameAvailableListener(this);
        mSurface = new Surface(mSurfaceTexture);

        if (datasrc.isEmpty())
            playMovieThread = new PlayMovieThread(context, dataSrcId, mSurface, mSurfaceTexture, mCallback);
        else
            playMovieThread = new PlayMovieThread(datasrc, mSurface, mSurfaceTexture, mCallback);

        initWithHeightOfVideo();

    }

    @Override
    public void setFrameSize(int width, int height) {
        super.setFrameSize(width, height);

        if (overlayFrameBuffer != null) {
            overlayFrameBuffer.setup(width, height);
        }

    }

    private void initWithHeightOfVideo() {

        overlayInfo = new OverlayInfo();

        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();

            if (context != null) {

                final AssetFileDescriptor afd = context.getResources().openRawResourceFd(dataSrcId);

                retriever.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());

            } else {
                retriever.setDataSource(datasrc);
            }
            overlayInfo.width = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
            overlayInfo.height = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
            overlayInfo.rotation = Float.parseFloat(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));
            overlayInfo.duration = Float.parseFloat(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            Log.d("Overlay", "initWithHeightOfVideo: \n Width = " + overlayInfo.width + " Height " + overlayInfo.height + " rotation " + overlayInfo.rotation + " Duration " + overlayInfo.getDuration());
            retriever.release();
        } catch (Exception ex) {
            Log.d("ErrorItem", " : \n Width = " + overlayInfo.width + " Height " + overlayInfo.height + " rotation " + overlayInfo.rotation + " Duration " + overlayInfo.getDuration());

        }

        textureRenderer.setFrameBufferObject(overlayFrameBuffer, overlayInfo);
    }

    @Override
    public void draw(int texName, GlFramebufferObject fbo) {
        this.baseFbo = fbo;
        this.texName = texName;
        if (mSurfaceTexture != null) {
            textureRenderer.drawFrame(mSurfaceTexture, overlayTexId, fbo);
        }
        super.draw(texName, fbo);

        //using GLThread to update the SurfaceTexture
        if (GPUPlayerRenderer.glContexThreadID == Thread.currentThread().getId()) {
            synchronized (this) {
                if (updateTexImageCompare != updateTexImageCounter) {

                    // loop and call updateTexImage() for each time the onFrameAvailable() method was called below.
                    while (updateTexImageCompare != updateTexImageCounter) {

                        mSurfaceTexture.updateTexImage();
                        updateTexImageCompare++;
                        // increment the compare value until it's the same as _updateTexImageCounter
                        isSetup = true;
                    }
                }
            }
        }

        if (DecoderSurface.glContexThreadID == Thread.currentThread().getId()) {
            synchronized (this) {
                if (updateTexImageCompare != updateTexImageCounter) {

                    // loop and call updateTexImage() for each time the onFrameAvailable() method was called below.
                    while (updateTexImageCompare != updateTexImageCounter) {

                        mSurfaceTexture.updateTexImage();
                        updateTexImageCompare++;
                        // increment the compare value until it's the same as _updateTexImageCounter
                        isSetup = true;
                    }
                }
            }
        }
    }

    @Override
    protected void onDraw() {

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);

        if (isSetup) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, overlayFrameBuffer.getTexName());
        } else {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texName);
        }
        GLES20.glUniform1i(getHandle("mTexture"), 1);

    }

    @Override
    public void release() {
        super.release();

        if (overlayFrameBuffer != null) {
            overlayFrameBuffer.release();
            textureRenderer = null;
        }

        if (playMovieThread != null) {
            playMovieThread.requestStop();
        }

        if (overlayTexId != -1 && mSurfaceTexture != null) {
            try {
                mSurfaceTexture.detachFromGLContext();
            } catch (Exception ex) {

            } finally {
                mSurfaceTexture = null;
            }
        }

    }

    public GlFramebufferObject getOverlayFrameBuffer() {
        return overlayFrameBuffer;
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {

        if (surfaceTexture != null) {
            updateTexImageCounter++;
        }
    }


    /**
     * Thread object that plays a movie from a file to a surface.
     * <p>
     * Currently loops until told to stop.
     */
    private static class PlayMovieThread extends Thread {
        private String mFile;
        private int mResId;
        private int flag = 0;
        private Context context;
        private final Surface mSurface;
        private final SpeedControlCallback mCallback;
        private MoviePlayer mMoviePlayer;

        /**
         * Creates thread and starts execution.
         * <p>
         * The object takes ownership of the Surface, and will access it from the new thread.
         * When playback completes, the Surface will be released.
         */
        public PlayMovieThread(String file, Surface surface, SurfaceTexture st, SpeedControlCallback callback) {
            mFile = file;
            flag = 1;
            mSurface = surface;
            mCallback = callback;
            context = null;
            Log.d("tapos", "Player: " + Thread.currentThread());
            start();
        }

        public PlayMovieThread(Context context, int resId, Surface surface, SurfaceTexture st, SpeedControlCallback callback) {
            this.mResId = resId;
            flag = 2;
            this.context = context;
            mSurface = surface;
            mCallback = callback;
            start();
        }

        /**
         * Asks MoviePlayer to halt playback.  Returns without waiting for playback to halt.
         * <p>
         * Call from UI thread.
         */
        public void requestStop() {
            mMoviePlayer.requestStop();
        }

        @Override
        public void run() {
            try {
                Thread.sleep(5);
                if (flag == 1) {
                    mMoviePlayer = new MoviePlayer(mFile, mSurface, mCallback);
                } else if (flag == 2) {
                    mMoviePlayer = new MoviePlayer(context, mResId, mSurface, mCallback);
                }
                mMoviePlayer.setLoopMode(true);
                mMoviePlayer.play();
            } catch (IOException ioe) {
                Log.e("overlay", "movie playback failed", ioe);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                mSurface.release();
                Log.d("overlay", "PlayMovieThread stopping");
            }
        }
    }


}
