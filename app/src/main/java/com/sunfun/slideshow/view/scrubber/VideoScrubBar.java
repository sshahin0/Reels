package com.sunfun.slideshow.view.scrubber;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.sunfun.slideshow.R;
import com.sunfun.slideshow.interfaces.OnAnimationListener;
import com.sunfun.slideshow.utils.VideoInfo;
import com.sunfun.slideshow.utils.VideoUtils;


public class VideoScrubBar extends RelativeLayout {

    private View selectedView;
    public View thumbBar;
    public LinearLayout videoFrameContainer;
    private OnSeekBarChangeListener onSeekBarChangeListener;
    public boolean isMoved = false;
    public ObjectAnimator videoAnimator;
    private float startX;
    private float endX;
    private long animDuration;
    private OnAnimationListener onAnimationListener;
    private boolean isPartOne = true;
    private long progress;
    private SimpleExoPlayer mediaPlayer;
    public long thumbPosition;
    private float thumbBarX;
    private LayoutParams imageParams;
    private LinearLayout extraLayout;
    private int rootWidth;

    public VideoScrubBar(Context context) {
        super(context);
    }

    public VideoScrubBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public VideoScrubBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public synchronized void setBitmap(Bitmap bitmap)  {
        if(bitmap.isRecycled()) return;
        ImageView imageView = new ImageView(getContext());
        imageView.setLayoutParams(imageParams);
        imageView.setImageBitmap(bitmap);
        imageView.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.black));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        videoFrameContainer.addView(imageView, imageParams);
        videoFrameContainer.requestLayout();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onSizeChanged(final int width, int height, int oldw, int oldh) {
        super.onSizeChanged(width, height, oldw, oldh);

        LayoutInflater mInflater = LayoutInflater.from(getContext());
        float leftMargin = ((ConstraintLayout.LayoutParams)getLayoutParams()).leftMargin;
        float rightMargin = ((ConstraintLayout.LayoutParams)getLayoutParams()).rightMargin;

        if(leftMargin == 0){
            leftMargin = VideoUtils.convertDpToPx(getContext(), 6);
        }

        if(rightMargin == 0){
            rightMargin = VideoUtils.convertDpToPx(getContext(), 4);
        }

        rootWidth = (int) (width - leftMargin - rightMargin);

        LayoutTransition layoutTransition = new LayoutTransition();
        LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(width, height);
        linearParams.setMarginStart((int) leftMargin);
        linearParams.setMarginEnd((int) rightMargin);
        videoFrameContainer = new LinearLayout(getContext());
        videoFrameContainer.setOrientation(LinearLayout.HORIZONTAL);
        videoFrameContainer.setBackground(getResources().getDrawable(R.drawable.imageview_border_round));
        videoFrameContainer.setLayoutTransition(layoutTransition);
        addView(videoFrameContainer, linearParams);

        extraLayout = new LinearLayout(getContext());
        extraLayout.setOrientation(LinearLayout.HORIZONTAL);
        extraLayout.setBackground(getResources().getDrawable(R.drawable.imageview_border_round));
        addView(extraLayout, linearParams);

        thumbBar = mInflater.inflate(R.layout.thumb_marker, this, false);
        thumbBar.setX(0);
        thumbBar.setY(0);
        addView(thumbBar);

        int imageWidth = (rootWidth / VideoUtils.SCRUBBER_BER_BACKGROUND_IMAGE_COUNT);
        imageParams = new RelativeLayout.LayoutParams(imageWidth, height);
        imageParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        setOnTouchListener((v, event) -> {

            float x = event.getX();
            float thumbBarPosition = VideoScrubBar.this.getChildAt(2).getTranslationX();

            if (event.getAction() == MotionEvent.ACTION_DOWN && selectedView == null) {
                if (Math.abs(thumbBarPosition - x) < thumbBar.getWidth() * 2) {
                    selectedView = VideoScrubBar.this.getChildAt(2);
                    onSeekBarChangeListener.onStartTrackingTouch();
                }
            }

            if (event.getAction() == MotionEvent.ACTION_MOVE && selectedView != null) {
                if (selectedView == VideoScrubBar.this.getChildAt(2)) {
                    isMoved = true;

                    float currentPosition = x - 8;

                    if(currentPosition<0) currentPosition = 0;
                    else if(currentPosition> rootWidth) currentPosition = rootWidth;
                    selectedView.setTranslationX(currentPosition);

                    if (VideoInfo.getInstance().isCut) {
                        progress = (long) ((long) ((mediaPlayer.getDuration() * currentPosition) / (float) rootWidth) / (float) (VideoInfo.getInstance().totalVideoDuration) * VideoInfo.getInstance().getDuration());
                        if (progress >= VideoInfo.getInstance().startMs) {
                            progress = progress + (VideoInfo.getInstance().endMs - VideoInfo.getInstance().startMs);
                        }
                        seekTo(progress);
                        onSeekBarChangeListener.onProgressChanged(progress, true);
                    } else {
                        seekTo((long) ((VideoInfo.getInstance().getDuration() * currentPosition) / (float) rootWidth + VideoInfo.getInstance().startMs));
                        onSeekBarChangeListener.onProgressChanged((long) ((VideoInfo.getInstance().getDuration() * currentPosition) / (float) rootWidth), true);
                    }

                }
            }

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if(selectedView != null){
                    onSeekBarChangeListener.onStopTrackingTouch();
                    selectedView = null;
                    videoAnimator = null;
                }
            }
            return false;
        });
    }

    public void removeWhiteBorder(){
        videoFrameContainer.setBackground(getResources().getDrawable(R.drawable.imageview_round));
        extraLayout.setBackground(getResources().getDrawable(R.drawable.imageview_round));
    }

    public long getProgress() {
        return (long) ((mediaPlayer.getDuration() * thumbBar.getX()) / videoFrameContainer.getWidth());
    }

    public void setOnSeekBarChangeListener(OnSeekBarChangeListener onSeekBarChangeListener) {
        this.onSeekBarChangeListener = onSeekBarChangeListener;
    }


    public void stopAnim() throws IllegalStateException {
        if (videoAnimator != null && videoAnimator.isRunning()) {
            videoAnimator.pause();
            mediaPlayer.setPlayWhenReady(false);
        } else if (videoAnimator != null) {
            videoAnimator.cancel();
            videoAnimator = null;
        }
    }

    public void startAnimation() {

        if (mediaPlayer == null) {
            return;
        }

        if (VideoInfo.getInstance().isCut) {
            startCutAnimation();
            return;
        }

        if (videoAnimator != null) {
            if (videoAnimator.isPaused()) {
                videoAnimator.resume();
                mediaPlayer.setPlayWhenReady(true);
            }
        } else {
            if (isMoved) {
                startX = thumbBarX;
                endX = videoFrameContainer.getWidth();
                animDuration = (int) (VideoInfo.getInstance().endMs - thumbPosition);
                mediaPlayer.seekTo(thumbPosition);
                isMoved = false;
            } else {
                startX = 0;
                endX = videoFrameContainer.getWidth();
                animDuration = (VideoInfo.getInstance().endMs - VideoInfo.getInstance().startMs);
                mediaPlayer.seekTo(VideoInfo.getInstance().startMs);
            }


           videoAnimator = ObjectAnimator.ofFloat(thumbBar, "x", startX, endX);
            videoAnimator.setDuration(animDuration);

            videoAnimator.setInterpolator(new LinearInterpolator());
            videoAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    onAnimationListener.animationEnd();
                    mediaPlayer.setPlayWhenReady(false);
                    mediaPlayer.seekTo((int) VideoInfo.getInstance().endMs);

                    mediaPlayer.seekTo((int) VideoInfo.getInstance().startMs);
                    thumbPosition = (int) VideoInfo.getInstance().startMs;
                    thumbBar.setX(0);

                    super.onAnimationEnd(animation);
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    onAnimationListener.animationStart();
                    mediaPlayer.setPlayWhenReady(true);
                    super.onAnimationStart(animation);
                }

                @Override
                public void onAnimationPause(Animator animation) {
                    super.onAnimationPause(animation);
                }
            });

            videoAnimator.start();
        }
    }

    private void startCutAnimation() {
        if (videoAnimator != null) {
            if (videoAnimator.isPaused()) {
                videoAnimator.resume();
                mediaPlayer.setPlayWhenReady(true);
            }
        } else {
            if (isMoved) {

                startX = thumbBarX;
                endX = videoFrameContainer.getWidth();

                mediaPlayer.seekTo(thumbPosition);
                if (thumbPosition <= VideoInfo.getInstance().startMs) {
                    isPartOne = true;
                    animDuration = (int) ((int) VideoInfo.getInstance().totalVideoDuration - (VideoInfo.getInstance().endMs - VideoInfo.getInstance().startMs) - thumbPosition);
                } else {
                    animDuration = (int) VideoInfo.getInstance().totalVideoDuration - thumbPosition;
                }
                isMoved = false;

            } else {
                isPartOne = true;
                startX = 0;
                endX = videoFrameContainer.getWidth();
                animDuration = (int) ((int) VideoInfo.getInstance().totalVideoDuration - (VideoInfo.getInstance().endMs - VideoInfo.getInstance().startMs));
                mediaPlayer.seekTo(0);
            }

            videoAnimator = ObjectAnimator.ofFloat(thumbBar, "x", startX, endX);

            if (animDuration < 0) {
                animDuration = 50;
            }

            videoAnimator.setDuration(animDuration);
            videoAnimator.setInterpolator(new LinearInterpolator());
            videoAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    onAnimationListener.animationEnd();
                    mediaPlayer.setPlayWhenReady(false);

                    mediaPlayer.seekTo(0);
                    thumbPosition = 0;
                    thumbBar.setX(0);

                    super.onAnimationEnd(animation);
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    onAnimationListener.animationStart();
                    mediaPlayer.setPlayWhenReady(true);
                    super.onAnimationStart(animation);
                }

                @Override
                public void onAnimationPause(Animator animation) {
                    super.onAnimationPause(animation);
                }
            });

            videoAnimator.addUpdateListener(animation -> {
                if (mediaPlayer.getCurrentPosition() >= VideoInfo.getInstance().startMs && isPartOne) {
                    mediaPlayer.seekTo((int) VideoInfo.getInstance().endMs);
                    isPartOne = false;
                }
            });

            videoAnimator.start();
        }
    }


    public void setOnAnimationListener(OnAnimationListener onAnimationListener) {
        this.onAnimationListener = onAnimationListener;
    }


    public void setMediaPlayer(SimpleExoPlayer mediaplayer) {
        this.mediaPlayer = mediaplayer;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public void setThumbPosition(long currentVideoPosition) {
        int x = (int) (videoFrameContainer.getWidth() * currentVideoPosition/(float)mediaPlayer.getDuration());

        isMoved = true;
        float currentPosition = x - 8;
        if(currentPosition<0) currentPosition = 0;
        else if(currentPosition> rootWidth) currentPosition = rootWidth;
        thumbBar.setX(currentPosition);


        if (VideoInfo.getInstance().isCut) {
            progress = (long) ((long) ((mediaPlayer.getDuration() * currentPosition) / (float) rootWidth) / (float) (VideoInfo.getInstance().totalVideoDuration) * VideoInfo.getInstance().getDuration());
            if (progress >= VideoInfo.getInstance().startMs) {
                progress = progress + (VideoInfo.getInstance().endMs - VideoInfo.getInstance().startMs);
            }
            seekTo(progress);
            onSeekBarChangeListener.onProgressChanged(progress, true);
        } else {
            seekTo((long) ((VideoInfo.getInstance().getDuration() * currentPosition) / (float) rootWidth + VideoInfo.getInstance().startMs));
            onSeekBarChangeListener.onProgressChanged((long) ((VideoInfo.getInstance().getDuration() * currentPosition) / (float) rootWidth), true);
        }

        if(videoAnimator!=null) videoAnimator = null;

    }

    public interface OnSeekBarChangeListener {
        void onProgressChanged(final long progress, boolean fromUser);

        void onStartTrackingTouch();

        void onStopTrackingTouch();
    }


    private void seekTo(final long progress) {
        mediaPlayer.seekTo(progress);
        mediaPlayer.addListener(new Player.EventListener() {
            @Override
            public void onSeekProcessed() {
                thumbPosition = (int) mediaPlayer.getCurrentPosition();
                thumbBarX = thumbBar.getX();
                mediaPlayer.removeListener(this);
            }
        });
    }

}