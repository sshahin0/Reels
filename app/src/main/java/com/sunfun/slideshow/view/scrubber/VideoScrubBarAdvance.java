package com.sunfun.slideshow.view.scrubber;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.sunfun.slideshow.R;
import com.sunfun.slideshow.interfaces.OnAnimationListener;
import com.sunfun.slideshow.interfaces.OnProgressChangeListenerAdvance;
import com.sunfun.slideshow.utils.CustomScrollView;
import com.sunfun.slideshow.utils.VideoInfo;
import com.sunfun.slideshow.utils.VideoUtils;

public class VideoScrubBarAdvance extends CustomScrollView {
    private View startBar;
    private View endBar;

    private View selectionView1;
    private View selectionView2;

    private String type = "trim";
    public OnProgressChangeListenerAdvance onProgressChangeListener;
    private RelativeLayout relativeLayout;
    private LinearLayout videoFrameContainer;
    private int markerWidth;
    private int screenWidth;
    private long thumbPosition;
    private FrameLayout frameLayout;
    private int rootWidth;
    private long startPosition;
    private long endPosition;
    private SimpleExoPlayer mediaPlayer;
    private OnAnimationListener onAnimationListener;
    public ObjectAnimator advanceAnimator;
    private long animDuration;
    private int scrubberMargin;
    public float startBarX;
    public float endBarX;
    private View seekView;
    private boolean userTouch = true;

    public VideoScrubBarAdvance(Context context) {
        super(context);
    }

    public VideoScrubBarAdvance(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public VideoScrubBarAdvance(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {
        super.onSizeChanged(width, height, oldw, oldh);

        LayoutInflater mInflater = LayoutInflater.from(getContext());
        screenWidth = width;
        scrubberMargin = width / 2;
        rootWidth = (int) ((screenWidth/VideoUtils.SCRUBBER_BER_BACKGROUND_IMAGE_COUNT) * VideoUtils.getMaxFrame());

        markerWidth = (int) getResources().getDimension(R.dimen.marker_width);

        frameLayout = new FrameLayout(getContext());
        this.addView(frameLayout);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(rootWidth + screenWidth, RelativeLayout.LayoutParams.MATCH_PARENT);
        relativeLayout = new RelativeLayout(getContext());
        relativeLayout.setLayoutParams(params);
        relativeLayout.setClickable(true);
        relativeLayout.setFocusable(true);
        frameLayout.addView(relativeLayout);

        RelativeLayout.LayoutParams linearParams = new RelativeLayout.LayoutParams(rootWidth + screenWidth, RelativeLayout.LayoutParams.WRAP_CONTENT);
        linearParams.setMargins(scrubberMargin, 0, scrubberMargin, 0);
        videoFrameContainer = new LinearLayout(getContext());
        videoFrameContainer.setOrientation(LinearLayout.HORIZONTAL);
        videoFrameContainer.setLayoutParams(linearParams);
        relativeLayout.addView(videoFrameContainer, linearParams);
        videoFrameContainer.requestLayout();
        relativeLayout.requestLayout();

        startBar = mInflater.inflate(R.layout.start_marker, relativeLayout, false);
        ((ConstraintLayout.LayoutParams) (startBar.findViewById(R.id.startMarker)).getLayoutParams()).setMargins(0, 0, 0, 0);
        startBar.setTranslationX(0);
        startBar.setTranslationY(0);
        relativeLayout.addView(startBar);

        endBar = mInflater.inflate(R.layout.end_marker, relativeLayout, false);
        ((ConstraintLayout.LayoutParams) (endBar.findViewById(R.id.endMarker)).getLayoutParams()).setMargins(0, 0, 0, 0);
        endBar.setTranslationX(0);
        endBar.setTranslationY(0);
        relativeLayout.addView(endBar);

        if (type.equals("trim")) {
            startBar.setRotation(180);
            endBar.setRotation(180);
        }

        LayoutParams viewParams1 = new LayoutParams(0, LayoutParams.MATCH_PARENT);
        selectionView1 = new View(getContext());
        selectionView1.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorBlackTrans));
        relativeLayout.addView(selectionView1, viewParams1);

        LayoutParams viewParams2 = new LayoutParams(0, LayoutParams.MATCH_PARENT);
        selectionView2 = new View(getContext());
        selectionView2.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorBlackTrans));
        relativeLayout.addView(selectionView2, viewParams2);

        this.setOnFlingListener(new OnFlingListener() {
            @Override
            public void onFlingStarted() {

            }

            @Override
            public void onFlingStopped() {
                mediaPlayer.addListener(new Player.EventListener() {
                    @Override
                    public void onSeekProcessed() {
                        onProgressChangeListener.onVideoPositionChanged();
                        mediaPlayer.removeListener(this);
                    }
                });
            }
        });
    }


    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        try {
            onProgressChangeListener.onScrollPositionChange();
            if (userTouch) {
                seekTo((mediaPlayer.getDuration() * l) / (long) rootWidth, getRootView());
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void seekTo(final long progress, final View view) {
        mediaPlayer.seekTo(progress);
        seekView = view;
        mediaPlayer.addListener(new Player.EventListener() {
            @Override
            public void onSeekProcessed() {
                if (seekView != null && seekView == startBar) {
                    startPosition = (int) mediaPlayer.getCurrentPosition();
                    startBarX = (int) (startBar.getX() + markerWidth);
                } else if (seekView != null && seekView == endBar) {
                    endPosition = (int) mediaPlayer.getCurrentPosition();
                    endBarX = (int) endBar.getX();
                } else if (seekView != null && seekView == getRootView()) {
                    thumbPosition = (int) mediaPlayer.getCurrentPosition();
                    onProgressChangeListener.onVideoPositionChanged();
                }
                seekView = null;
            }
        });
    }


    private void colorView() {
        if (type.equals("cut")) {
            selectionView1.setTranslationX(startBar.getTranslationX() + markerWidth);
            selectionView1.getLayoutParams().width = (int) (endBar.getTranslationX() - startBar.getTranslationX() - markerWidth);

            selectionView2.setTranslationX(endBar.getTranslationX());
            selectionView2.getLayoutParams().width = 0;
        } else {
            selectionView1.setTranslationX((float) scrubberMargin);
            selectionView1.getLayoutParams().width = (int) (startBar.getTranslationX() - scrubberMargin);

            selectionView2.setTranslationX(endBar.getTranslationX() + markerWidth);
            selectionView2.getLayoutParams().width = (int) (rootWidth - endBar.getTranslationX() + scrubberMargin - markerWidth);
        }

        frameLayout.requestLayout();
        relativeLayout.requestLayout();
        videoFrameContainer.requestLayout();
        requestLayout();
    }


    public void setBitmap(Bitmap bitmap) {
        if(bitmap.isRecycled()) return;
        RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(screenWidth/VideoUtils.SCRUBBER_BER_BACKGROUND_IMAGE_COUNT, getHeight());
        imageParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        ImageView imageView = new ImageView(getContext());
        imageView.setLayoutParams(imageParams);
        imageView.setImageBitmap(bitmap);
        imageView.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.black));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        videoFrameContainer.addView(imageView, imageParams);
        videoFrameContainer.requestLayout();
    }

    public void startAnim() {
        if (type.equals("trim")) {
            startTrimAnimation();
        } else {
            startCutAnimPart1();
        }
    }

    private void startTrimAnimation() {
        if (advanceAnimator != null && advanceAnimator.isPaused()) {
            advanceAnimator.resume();
        } else {
            float startX;
            float endX;
            if (thumbPosition > startPosition && thumbPosition < endPosition) {
                startX = getScrollX();
                endX = endBarX;
                animDuration = endPosition - thumbPosition;
                mediaPlayer.seekTo(thumbPosition);
            } else {
                startX = startBarX;
                endX = endBarX;
                animDuration = endPosition - startPosition;
            }

            advanceAnimator = ObjectAnimator.ofInt(this, "scrollX", (int) startX, (int) endX);

            if (animDuration > 0) {
                animDuration = animDuration + 150;
                advanceAnimator.setDuration(animDuration);
            } else {
                advanceAnimator.setDuration(50);
            }

            advanceAnimator.setInterpolator(new LinearInterpolator());
            advanceAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    onAnimationListener.animationEnd();
                    userTouch = true;
                    thumbPosition = startPosition;
                    smoothScrollTo((int) startBarX, 0);
                    super.onAnimationEnd(animation);
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    onAnimationListener.animationStart();
                    userTouch = false;
                    super.onAnimationStart(animation);
                }

                @Override
                public void onAnimationPause(Animator animation) {
                    super.onAnimationPause(animation);
                }
            });

            advanceAnimator.start();
        }
    }

    private void startCutAnimPart1() {
        if (advanceAnimator != null && advanceAnimator.isPaused()) {
            advanceAnimator.resume();
        } else {
            int startX;
            float endX;
            if (thumbPosition > startPosition) {
                startCutAnimPart2();
                return;
            } else {
                startX = getScrollX();
                endX = startBarX;
                animDuration = startPosition - thumbPosition;
            }

            advanceAnimator = ObjectAnimator.ofInt(this, "scrollX", (int) startX, (int) endX);

            if (animDuration > 0) {
                animDuration = animDuration + 150;
                advanceAnimator.setDuration(animDuration);
            } else {
                advanceAnimator.setDuration(50);
            }

            advanceAnimator.setInterpolator(new LinearInterpolator());
            advanceAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    advanceAnimator = null;
                    thumbPosition = startPosition;

                    startCutAnimPart2();
                    super.onAnimationEnd(animation);
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    onAnimationListener.animationStart();
                    userTouch = false;
                    super.onAnimationStart(animation);
                }
            });

            advanceAnimator.start();
        }
    }

    public void startCutAnimPart2() {
        if (advanceAnimator != null && advanceAnimator.isPaused()) {
            advanceAnimator.resume();
        } else {
            float startX;
            int endX;
            if (thumbPosition < endPosition) {
                startX = endBarX;
                endX = rootWidth;
                animDuration = VideoInfo.getInstance().totalVideoDuration - endPosition;
            } else {
                startX = getScrollX();
                endX = rootWidth;
                animDuration = VideoInfo.getInstance().totalVideoDuration - thumbPosition;
            }

            advanceAnimator = ObjectAnimator.ofInt(this, "scrollX", (int) startX, (int) endX);

            if (animDuration > 0) {
                animDuration = animDuration + 150;
                advanceAnimator.setDuration(animDuration);
            } else {
                advanceAnimator.setDuration(50);
            }

            advanceAnimator.setInterpolator(new LinearInterpolator());
            advanceAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    userTouch = false;
                    advanceAnimator = null;
                    smoothScrollTo(0,0);
                    onAnimationListener.animationEnd();
                    super.onAnimationEnd(animation);
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    onAnimationListener.animationStart();
                    super.onAnimationStart(animation);
                }
            });

            advanceAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    thumbPosition = mediaPlayer.getCurrentPosition();
                }
            });

            advanceAnimator.start();
        }
    }

    public void stopAnim() {
        userTouch = true;
        if(advanceAnimator == null) return;
        if (advanceAnimator.isRunning()) {
            advanceAnimator.pause();
        } else {
            advanceAnimator.cancel();
            advanceAnimator = null;
        }
    }

    public void setType(String type) {
        this.type = type;
        if (type.equals("trim")) {
            startBar.setRotation(180);
            endBar.setRotation(180);
        } else {
            startBar.setRotation(0);
            endBar.setRotation(0);
        }
        colorView();
    }

    public String getType() {
        return type;
    }

    public void setOnProgressChangeListener(OnProgressChangeListenerAdvance onProgressChangeListener) {
        this.onProgressChangeListener = onProgressChangeListener;
    }

    public void setMediaPlayer(SimpleExoPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }

    public void setOnAnimationListener(OnAnimationListener onAnimationListener) {
        this.onAnimationListener = onAnimationListener;
    }

    public long getStartPosition() {
        return startPosition;
    }

    public void setStartPosition() {
        startBar.setX(getCurrentVideoPosition() + scrubberMargin - markerWidth);
        startBarX = startBar.getX() + markerWidth - scrubberMargin;
        startPosition = mediaPlayer.getCurrentPosition();
        //thumbPosition = startPosition;
        colorView();
    }

    public void setEndPosition() {
        endBar.setX(getCurrentVideoPosition() + scrubberMargin);
        endBarX = endBar.getX() - scrubberMargin;
        endPosition = mediaPlayer.getCurrentPosition();
        //thumbPosition = endPosition;
        colorView();
    }

    public long getEndPosition() {
        return endPosition;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            onProgressChangeListener.onStartScrolling();
        } else if (ev.getAction() == MotionEvent.ACTION_UP) {
            onProgressChangeListener.onStopScrolling();
            advanceAnimator = null;
        }

        return super.onTouchEvent(ev);
    }

    public void moveScrollBar(){
        scrollTo((int) getCurrentVideoPosition(), 0);
    }

    private long getCurrentVideoPosition() {
        return rootWidth * mediaPlayer.getCurrentPosition() / mediaPlayer.getDuration();
    }

    public void release() {
        for(int i = 0; i< videoFrameContainer.getChildCount(); i++){
            if(((BitmapDrawable)((ImageView) videoFrameContainer.getChildAt(i)).getDrawable()).getBitmap().isRecycled()) continue;
            ((BitmapDrawable)((ImageView) videoFrameContainer.getChildAt(i)).getDrawable()).getBitmap().recycle();
        }
    }
}
