package com.sunfun.slideshow.view.scrubber;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.core.content.ContextCompat;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.sunfun.slideshow.R;
import com.sunfun.slideshow.interfaces.OnProgressChangeListenerAdvance;
import com.sunfun.slideshow.utils.CustomScrollView;
import com.sunfun.slideshow.utils.VideoInfo;

public class AudioScrubBarAdvance extends CustomScrollView {
    public OnProgressChangeListenerAdvance onProgressChangeListener;
    private int rootWidth;
    private SimpleExoPlayer mediaPlayer;
    public ObjectAnimator advanceAnimator;
    private boolean userTouch = false;
    private long lastPosition = 0;

    public AudioScrubBarAdvance(Context context) {
        super(context);
    }

    public AudioScrubBarAdvance(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public AudioScrubBarAdvance(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {
        super.onSizeChanged(width, height, oldw, oldh);

        rootWidth = width;
        int scrubberMargin = width / 2;

        FrameLayout frameLayout = new FrameLayout(getContext());
        this.addView(frameLayout);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(rootWidth * 2, height);
        RelativeLayout relativeLayout = new RelativeLayout(getContext());
        relativeLayout.setLayoutParams(params);
        relativeLayout.setClickable(true);
        relativeLayout.setFocusable(true);
        frameLayout.addView(relativeLayout);

        RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(rootWidth * 2, height);
        relativeParams.setMargins(scrubberMargin, 0, scrubberMargin, 0);
        LinearLayout videoFrameContainer = new LinearLayout(getContext());
        videoFrameContainer.setOrientation(LinearLayout.HORIZONTAL);
        videoFrameContainer.setLayoutParams(relativeParams);
        relativeLayout.addView(videoFrameContainer, relativeParams);

        ImageView imageView = new ImageView(getContext());
        imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_audio_visual));
        videoFrameContainer.addView(imageView);
        relativeLayout.requestLayout();

        setOnFlingListener(new OnFlingListener() {
            @Override
            public void onFlingStarted() {

            }

            @Override
            public void onFlingStopped() {
                if (userTouch) {
                    onProgressChangeListener.onScrollPositionChange((mediaPlayer.getDuration() * getScrollX()) / (long) rootWidth);
                }
            }
        });
    }


    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        long currentPlayProgress = (mediaPlayer.getDuration() * l) / (long) rootWidth;
        if (userTouch) {
            onProgressChangeListener.onScrollPositionChange(currentPlayProgress);
        }
    }

    public void setOnProgressChangeListener(OnProgressChangeListenerAdvance onProgressChangeListener) {
        this.onProgressChangeListener = onProgressChangeListener;
    }

    public void setMediaPlayer(SimpleExoPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            userTouch = true;
            onProgressChangeListener.onStartScrolling();
        } else if (ev.getAction() == MotionEvent.ACTION_UP) {
            onProgressChangeListener.onStopScrolling();
            advanceAnimator = null;
            userTouch = false;
        }
        return super.onTouchEvent(ev);
    }

    public void moveScrollBar() {
        scrollTo((int) getCurrentVideoPosition(), 0);
    }

    private long getCurrentVideoPosition() {
        try {
            long currentVideoPosition = rootWidth * (mediaPlayer.getCurrentPosition() - VideoInfo.getInstance().getStartMs()) / VideoInfo.getInstance().getDuration();
            if (VideoInfo.getInstance().isCut) {
                if (mediaPlayer.getCurrentPosition() >= VideoInfo.getInstance().getEndMs()) {
                    currentVideoPosition = rootWidth * (mediaPlayer.getCurrentPosition() - VideoInfo.getInstance().getEndMs()) / VideoInfo.getInstance().getDuration() + lastPosition;
                } else {
                    currentVideoPosition = rootWidth * (mediaPlayer.getCurrentPosition()) / VideoInfo.getInstance().getDuration();
                    lastPosition = currentVideoPosition;
                }
            }
            return currentVideoPosition;
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }
    }
}
