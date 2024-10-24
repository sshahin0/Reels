package com.sunfun.slideshow.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.daasuu.mp4compose.Rotation;
import com.daasuu.mp4compose.filter.GlFilter;
import com.daasuu.mp4compose.filter.GlTransformFilter;
import com.daasuu.mp4compose.player.GPUPlayerView;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SeekParameters;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.sunfun.slideshow.R;
import com.sunfun.slideshow.interfaces.OnAnimationListener;
import com.sunfun.slideshow.utils.ExoPlayerErrorHandler;
import com.sunfun.slideshow.utils.ExoPlayerHolder;
import com.sunfun.slideshow.utils.VideoInfo;
import com.sunfun.slideshow.view.scrubber.VideoScrubBar;
import com.sunfun.slideshow.viewmodel.MainViewModel;

import java.util.Objects;


public class FlipRotateFragment extends Fragment {

    private static final float ROTATION_360 = 360;
    private GPUPlayerView videoPreview;
    private ImageButton rotateLeftBtn;
    private ImageButton rotateRightBtn;
    private ImageButton flipVerticalBtn;
    private ImageButton flipHorizontalBtn;
    private ImageButton playBtn;
    private VideoScrubBar videoScrubBer;
    private MainViewModel mainViewModel;
    private Handler mHandler;
    private Runnable updateTask;
    private boolean isVideoPaused = false;
    private TextView backBtn;
    private TextView doneBtn;
    private boolean isRotated;
    private boolean isPreparing = true;
    private float rotation;
    private float initialRotation;
    private boolean initialFlipV;
    private boolean initialFlipH;
    private FrameLayout blackMask;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_flip_rotate, container, false);
    }

    @Override
    public Animation onCreateAnimation(int transit, final boolean enter, int nextAnim) {
        Animation anim = AnimationUtils.loadAnimation(getActivity(), nextAnim);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                if(enter){
                    findAllViews();
                    generalInitialization();
                    initButtons();
                    initPlayer();
                }else {
                    videoPreview = null;
                }
            }
        });
        return anim;
    }

    private void initButtons() {
        rotateLeftBtn.setOnClickListener(v -> {
            int offsetRotation = (VideoInfo.getInstance().isFlippedH || VideoInfo.getInstance().isFlippedH) ? Rotation.ROTATION_90.getRotation() : Rotation.ROTATION_270.getRotation();
            rotation = (offsetRotation + VideoInfo.getInstance().getRotation()) % ROTATION_360;
            isRotated = true;
            VideoInfo.getInstance().setRotated(true);
            VideoInfo.getInstance().setRotation(rotation);
            updateVideoPreview();

        });

        rotateRightBtn.setOnClickListener(v -> {
            int offsetRotation = (VideoInfo.getInstance().isFlippedH || VideoInfo.getInstance().isFlippedH) ? Rotation.ROTATION_270.getRotation() : Rotation.ROTATION_90.getRotation();
            rotation = (VideoInfo.getInstance().getRotation() + offsetRotation) % ROTATION_360;
            isRotated = true;
            VideoInfo.getInstance().setRotated(true);
            VideoInfo.getInstance().setRotation(rotation);
            updateVideoPreview();
        });

        flipVerticalBtn.setOnClickListener(v -> {
            if(VideoInfo.getInstance().getRotation() == 0 || VideoInfo.getInstance().getRotation() == 180){
                if (VideoInfo.getInstance().isFlippedH) {
                    VideoInfo.getInstance().isFlippedH = false;
                } else {
                    VideoInfo.getInstance().isFlippedH = true;
                }
            } else {
                if (VideoInfo.getInstance().isFlippedV) {
                    VideoInfo.getInstance().isFlippedV = false;
                } else {
                    VideoInfo.getInstance().isFlippedV = true;
                }
            }
            updateFilter();
        });

        flipHorizontalBtn.setOnClickListener(v -> {
            if(VideoInfo.getInstance().getRotation() == 0 || VideoInfo.getInstance().getRotation() == 180){
                if (VideoInfo.getInstance().isFlippedV) {
                    VideoInfo.getInstance().isFlippedV = false;
                } else {
                    VideoInfo.getInstance().isFlippedV = true;
                }
            } else {
                if (VideoInfo.getInstance().isFlippedH) {
                    VideoInfo.getInstance().isFlippedH = false;
                } else {
                    VideoInfo.getInstance().isFlippedH = true;
                }
            }
            updateFilter();
        });

        playBtn.setTag("0");
        playBtn.setOnClickListener(v -> {

            if(isPreparing){
                return;
            }

            if (playBtn.getTag().equals("1")) {
                pausePlayer();
            } else if (playBtn.getTag().equals("0")) {
                resumePlayer();
            }
        });

        backBtn.setOnClickListener(v -> {
            resetVideoInfo();
            resetPreview();
            finish();
        });

        doneBtn.setOnClickListener(v -> {
            resetPreview();
            finishWithResult();
        });
    }

    private void updateVideoPreview() {
        blackMask.setVisibility(View.VISIBLE);
        blackMask.post(() -> {
            videoPreview.post(()-> videoPreview.adjustWidthHeight(VideoInfo.getInstance().getCroppedWidth(), VideoInfo.getInstance().getCroppedHeight(), VideoInfo.getInstance().getRotation()));
            videoPreview.post(this::updateFilter);
            videoPreview.post(()-> blackMask.setVisibility(View.INVISIBLE));
        });
    }

    private void resetVideoInfo() {
        VideoInfo.getInstance().setRotation(initialRotation);
        VideoInfo.getInstance().setFlippedH(initialFlipH);
        VideoInfo.getInstance().setFlippedV(initialFlipV);
    }

    private void updateFilter() {
        GlTransformFilter glFiler = new GlTransformFilter();
        glFiler.setRotateInAngle(0);
        glFiler.setTranslateOffset( VideoInfo.getInstance().getLeft()/VideoInfo.getInstance().getWidth(),  (VideoInfo.getInstance().getTop())/VideoInfo.getInstance().getHeight());
        float scaleX = VideoInfo.getInstance().getCroppedWidth()/VideoInfo.getInstance().getWidth();
        float scaleY = VideoInfo.getInstance().getCroppedHeight()/VideoInfo.getInstance().getHeight();
        if(VideoInfo.getInstance().isFlippedV){ scaleX = scaleX * -1; }
        if(VideoInfo.getInstance().isFlippedH){ scaleY = scaleY * -1; }
        VideoInfo.getInstance().previousFlipH = VideoInfo.getInstance().isFlippedH;
        VideoInfo.getInstance().previousFlipV = VideoInfo.getInstance().isFlippedV;
        glFiler.setScaleUnit(scaleX, scaleY);
        glFiler.setRotateInAngle(VideoInfo.getInstance().getRotation());
        videoPreview.setGlFilter(glFiler);
    }

    private void finishWithResult() {
        pausePlayer();
        Objects.requireNonNull(getActivity()).getSupportFragmentManager();
        getActivity().getSupportFragmentManager().popBackStack();
        Intent intent = new Intent();
        VideoInfo videoInfo = VideoInfo.getInstance();

        if (isRotated) {
            videoInfo.setRotated(true);
            videoInfo.setRotation(rotation);
        }

        assert getTargetFragment() != null;
        getTargetFragment().onActivityResult(
                getTargetRequestCode(),
                Activity.RESULT_OK,
                intent
        );
    }

    private void finish() {
        pausePlayer();
        VideoInfo.getInstance().setRotated(false);

        Objects.requireNonNull(getActivity()).getSupportFragmentManager();
        getActivity().getSupportFragmentManager().popBackStack();
        Intent intent = new Intent();
        assert getTargetFragment() != null;
        getTargetFragment().onActivityResult(
                getTargetRequestCode(),
                Activity.RESULT_CANCELED,
                intent
        );
    }

    private void resetPreview() {
        GlFilter.resetFilter();
    }

    private void resumePlayer() {
        isVideoPaused = false;
        videoScrubBer.startAnimation();
        playBtn.setImageResource(R.drawable.ic_pause);
        playBtn.setTag("1");
        new Handler().post(updateTask);
    }

    private void generalInitialization() {
        isRotated = false;
        mainViewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
        initialRotation = VideoInfo.getInstance().getRotation();
        initialFlipV = VideoInfo.getInstance().isFlippedV;
        initialFlipH = VideoInfo.getInstance().isFlippedH;
    }

    private void findAllViews() {
        videoPreview = getView().findViewById(R.id.video_view);
        rotateLeftBtn = getView().findViewById(R.id.rotateLeftBtn);
        rotateRightBtn = getView().findViewById(R.id.rotateRightBtn);
        flipVerticalBtn = getView().findViewById(R.id.flipVerticalBtn);
        flipHorizontalBtn = getView().findViewById(R.id.flipHorizontalBtn);
        playBtn = getView().findViewById(R.id.playBtn);
        videoScrubBer = getView().findViewById(R.id.scrubBer);
        backBtn = getView().findViewById(R.id.backButton);
        doneBtn = getView().findViewById(R.id.doneButton);
        blackMask = getView().findViewById(R.id.black_mask);
    }

    private void initPlayer() {
        videoPreview.setSimpleExoPlayer(ExoPlayerHolder.getInstance(getContext()));
        videoPreview.adjustWidthHeight(VideoInfo.getInstance().getCroppedWidth(), VideoInfo.getInstance().getCroppedHeight(), VideoInfo.getInstance().getRotation());
        blackMask.setVisibility(View.VISIBLE);
        blackMask.post(() -> videoPreview.setVisibility(View.VISIBLE));
        videoPreview.getPlayer().setSeekParameters(SeekParameters.EXACT);
        initExoPlayerListener();
        setMediaSourceAndPrepare();
    }

    private void initExoPlayerListener() {
        videoPreview.getPlayer().addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (isPreparing && playbackState == ExoPlayer.STATE_READY) {
                    isPreparing = false;
                    updateFilter();
                    blackMask.setVisibility(View.INVISIBLE);
                    initScrubBar();
                }
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                new ExoPlayerErrorHandler(getContext(), iosDialog -> {
                    iosDialog.dismiss();
                    finish();
                }).handle(error);
            }
        });
    }

    private void setMediaSourceAndPrepare() {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getContext(), Util.getUserAgent(getContext(), "videoediting"));
        MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(VideoInfo.getInstance().getVideoPath()));
        isPreparing = true;
        videoPreview.getPlayer().prepare(videoSource);
    }


    private void initScrubBar() {
        videoScrubBer.setMediaPlayer(videoPreview.getPlayer());
        mainViewModel.progressBarImageList = null;


        if (VideoInfo.getInstance().isTrimmed) {
            videoPreview.getPlayer().seekTo((int) VideoInfo.getInstance().getStartMs());
            mainViewModel.getScrubBerBitmap("trim").observe(getActivity(), new Observer<Bitmap>() {
                @Override
                public void onChanged(@Nullable Bitmap bitmap) {
                    videoScrubBer.setBitmap(bitmap);
                }
            });
        } else if (VideoInfo.getInstance().isCut) {
            mainViewModel.getScrubBerBitmap("cut").observe(getActivity(), new Observer<Bitmap>() {
                @Override
                public void onChanged(@Nullable Bitmap bitmap) {
                    videoScrubBer.setBitmap(bitmap);
                }
            });
        } else {
            mainViewModel.getScrubBerBitmap("").observe(getActivity(), new Observer<Bitmap>() {
                @Override
                public void onChanged(@Nullable Bitmap bitmap) {
                    videoScrubBer.setBitmap(bitmap);
                }
            });
        }

        videoScrubBer.setOnSeekBarChangeListener(new VideoScrubBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(long progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch() {
                pausePlayer();
                playBtn.setTag("1");
            }

            @Override
            public void onStopTrackingTouch() {
                pausePlayer();
                playBtn.setTag("0");
            }
        });

        if (mHandler == null) mHandler = new Handler();
        updateTask = new Runnable() {
            @Override
            public void run() {
                try {
                    if (videoScrubBer.getProgress() < videoPreview.getPlayer().getDuration() && !isVideoPaused) {
                        mHandler.post(this);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };

        videoScrubBer.setOnAnimationListener(new OnAnimationListener() {
            @Override
            public void animationEnd() {
                playBtn.setImageResource(R.drawable.play);
                isVideoPaused = true;
                videoScrubBer.videoAnimator = null;
            }

            @Override
            public void animationStart() {
            }
        });

        videoScrubBer.removeWhiteBorder();
    }

    public void pausePlayer() {
        playBtn.setTag("0");
        playBtn.setImageResource(R.drawable.play);
        isVideoPaused = true;
        videoScrubBer.stopAnim();
    }

    public static FlipRotateFragment getInstance() {
        return new FlipRotateFragment();
    }


    @Override
    public void onStop() {
        super.onStop();
        if (videoPreview.getPlayer() != null) videoPreview.getPlayer().setPlayWhenReady(false);
    }


    @Override
    public void onPause() {
        super.onPause();
        pausePlayer();
    }

    @Override
    public void onResume() {
        super.onResume();
        resumePreview();
    }

    private void resumePreview() {
        try {
            if (videoPreview.getPlayer() != null) {
                videoPreview.getPlayer().setPlayWhenReady(false);
                videoScrubBer.thumbBar.setX(0);
                videoScrubBer.videoAnimator = null;
                videoPreview.getPlayer().seekTo(0);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }

    }


    public void onBackPressed() {
        finish();
    }
}
