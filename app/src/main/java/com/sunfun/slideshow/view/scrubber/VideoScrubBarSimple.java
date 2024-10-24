package com.sunfun.slideshow.view.scrubber;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.core.content.ContextCompat;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.sunfun.slideshow.R;
import com.sunfun.slideshow.interfaces.OnAnimationListener;
import com.sunfun.slideshow.interfaces.OnProgressChangeListenerSimple;
import com.sunfun.slideshow.utils.VideoInfo;
import com.sunfun.slideshow.utils.VideoUtils;

public class VideoScrubBarSimple extends RelativeLayout {

    private View selectedView;
    public View startBar;
    public View endBar;
    public View thumbBar;

    private View selectionView1;
    private View selectionView2;
    private String type = "trim";

    private OnProgressChangeListenerSimple onProgressChangeListener;
    public LinearLayout videoFrameContainer;

    public int markerWidth;
    private int rootWidth;
    private OnAnimationListener onAnimationListener;
    public ObjectAnimator simpleAnimator;
    private long animDuration;
    private SimpleExoPlayer mediaPlayer;

    private long startPosition;
    private long endPosition;
    private long thumbPosition;
    private float startBarX;
    private float endBarX;
    private View seekView;
    private LinearLayout.LayoutParams imageParams;

    public VideoScrubBarSimple(Context context) {
        super(context);
    }

    public VideoScrubBarSimple(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public VideoScrubBarSimple(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onSizeChanged(final int width, int height, int oldw, int oldh) {
        super.onSizeChanged(width, height, oldw, oldh);

        LayoutInflater mInflater = LayoutInflater.from(getContext());
        markerWidth = (int) getResources().getDimension(R.dimen.marker_width);
        rootWidth = (width - markerWidth * 2);

        height = height - 16;
        LayoutTransition layoutTransition = new LayoutTransition();
        setLayoutTransition(layoutTransition);
        LayoutParams linearParams = new LayoutParams(width, height);
        linearParams.leftMargin = markerWidth;
        linearParams.rightMargin = markerWidth;
        linearParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        videoFrameContainer = new LinearLayout(getContext());
        videoFrameContainer.setOrientation(LinearLayout.HORIZONTAL);
        videoFrameContainer.setLayoutParams(linearParams);
        videoFrameContainer.setBackground(getResources().getDrawable(R.drawable.imageview_border));
        videoFrameContainer.setLayoutTransition(layoutTransition);
        addView(videoFrameContainer, linearParams);

        startBar = mInflater.inflate(R.layout.start_marker, this, false);
        startBar.setX(rootWidth / 4f);
        startBar.setY(0);
        startBar.setElevation(6);
        addView(startBar);

        endBar = mInflater.inflate(R.layout.end_marker, this, false);
        endBar.setX(rootWidth - rootWidth / 4f + markerWidth);
        endBar.setY(0);
        endBar.setElevation(6);
        addView(endBar);

        if (type.equals("trim")) {
            startBar.setRotation(180);
            endBar.setRotation(180);
        }

        LayoutParams viewParams1 = new LayoutParams(0, height - VideoUtils.convertDpToPx(getContext(), 4));
        viewParams1.leftMargin = markerWidth;
        viewParams1.rightMargin = markerWidth;
        viewParams1.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        selectionView1 = new View(getContext());
        selectionView1.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorBlackTrans));
        addView(selectionView1, viewParams1);

        LayoutParams viewParams2 = new LayoutParams(0, height - VideoUtils.convertDpToPx(getContext(), 4));
        viewParams2.leftMargin = markerWidth;
        viewParams2.rightMargin = markerWidth;
        viewParams2.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        selectionView2 = new View(getContext());
        selectionView2.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorBlackTrans));
        addView(selectionView2, viewParams2);

        thumbBar = mInflater.inflate(R.layout.thumb_marker, this, false);
        thumbBar.setX(markerWidth - 18);
        thumbBar.setY(0);
        thumbBar.setElevation(10);
        addView(thumbBar);

        colorView();
        imageParams = new LinearLayout.LayoutParams(rootWidth / VideoUtils.SCRUBBER_BER_BACKGROUND_IMAGE_COUNT, height);

        setOnTouchListener((v, event) -> {
            int thumbBarNo = 4;

            if (VideoScrubBarSimple.this.getChildCount() == 6) {
                thumbBarNo = 5;
            }

            float x = event.getX() - 8;

            float startBarPosition = VideoScrubBarSimple.this.getChildAt(1).getX();
            final float endBarPosition = VideoScrubBarSimple.this.getChildAt(2).getX();
            float thumbBarPosition = VideoScrubBarSimple.this.getChildAt(thumbBarNo).getX();

            if (event.getAction() == MotionEvent.ACTION_DOWN && selectedView == null) {
                if (Math.abs(startBarPosition - x) < startBar.getWidth()) {
                    selectedView = VideoScrubBarSimple.this.getChildAt(1);
                } else if (Math.abs(endBarPosition - x) < endBar.getWidth()) {
                    selectedView = VideoScrubBarSimple.this.getChildAt(2);
                } else if (Math.abs(thumbBarPosition - x) < thumbBar.getLayoutParams().width * 2) {
                    selectedView = VideoScrubBarSimple.this.getChildAt(thumbBarNo);
                }
            }

            if (event.getAction() == MotionEvent.ACTION_MOVE && selectedView != null) {
                onProgressChangeListener.onTouchMoved();
                if (selectedView == VideoScrubBarSimple.this.getChildAt(1)) {
                    if (x > endBarPosition - markerWidth) {
                        x = endBarPosition - markerWidth;
                    }

                    if (x <= 0) {
                        x = 0;
                    }

                    seekTo((int) ((mediaPlayer.getDuration() * (x)) / (long) (rootWidth)), startBar);
                    selectedView.setX(x);
                    colorView();

                } else if (selectedView == VideoScrubBarSimple.this.getChildAt(2)) {

                    if (x < startBarPosition + markerWidth) {
                        x = startBarPosition + markerWidth;
                    }

                    if (x > width - markerWidth) {
                        x = width - markerWidth;
                    }

                    seekTo((int) ((mediaPlayer.getDuration() * (x - markerWidth)) / (long) (rootWidth)), endBar);
                    selectedView.setX(x);
                    colorView();

                } else if (selectedView == VideoScrubBarSimple.this.getChildAt(thumbBarNo)) {
                    if (x < markerWidth) {
                        x = markerWidth;
                    }

                    if (x > width - markerWidth) {
                        x = width - markerWidth;
                    }

                    seekTo((int) ((mediaPlayer.getDuration() * (x - markerWidth)) / (long) (rootWidth)), thumbBar);
                    selectedView.setX(x - thumbBar.getWidth() / 2f);
                }
            }

            if (event.getAction() == MotionEvent.ACTION_UP) {
                selectedView = null;
                simpleAnimator = null;
            }
            return false;
        });
    }

    private void seekTo(final int progress, final View view) {
        seekView = view;
        mediaPlayer.addListener(new Player.EventListener() {
            @Override
            public void onSeekProcessed() {
                if (seekView == startBar) {
                    startPosition = (int) mediaPlayer.getCurrentPosition();
                    startBarX = startBar.getX() + markerWidth - thumbBar.getWidth() / 2f;
                    onProgressChangeListener.onLeftMarkerPositionChanged();
                } else if (seekView == endBar) {
                    endPosition = (int) mediaPlayer.getCurrentPosition();
                    endBarX = endBar.getX() - thumbBar.getWidth() / 2f;
                    onProgressChangeListener.onRightMarkerPositionChanged();
                } else if (seekView == thumbBar) {
                    thumbPosition = (int) mediaPlayer.getCurrentPosition();
                    onProgressChangeListener.onThumbPositionChanged();
                }
                seekView = null;
                mediaPlayer.removeListener(this);
            }
        });
        mediaPlayer.seekTo(progress);
    }

    private void colorView() {
        if (type.equals("cut")) {
            selectionView1.setTranslationX(startBar.getX());
            selectionView1.getLayoutParams().width = Math.round(endBar.getX() - startBar.getX() - markerWidth);

            selectionView2.setTranslationX(0);
            selectionView2.getLayoutParams().width = 0;
        } else {
            selectionView1.setTranslationX(VideoUtils.convertDpToPx(getContext(), 3));
            selectionView1.getLayoutParams().width = Math.round(startBar.getX() - markerWidth) - VideoUtils.convertDpToPx(getContext(), 3);

            selectionView2.setTranslationX(endBar.getX());
            selectionView2.getLayoutParams().width = Math.round(rootWidth - endBar.getX()) + 1 - VideoUtils.convertDpToPx(getContext(), 3);
        }
        VideoScrubBarSimple.this.requestLayout();
    }

    public void setBitmap(Bitmap bitmap) {
        if(bitmap.isRecycled()) return;
        ImageView imageView = new ImageView(getContext());
        imageView.setLayoutParams(imageParams);
        imageView.setImageBitmap(bitmap);
        imageView.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.black));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        videoFrameContainer.addView(imageView);
        videoFrameContainer.requestLayout();
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

    public void setOnProgressChangeListener(OnProgressChangeListenerSimple onProgressChangeListener) {
        this.onProgressChangeListener = onProgressChangeListener;
    }

    public void startAnim() {
        if (type.equals("trim")) {
            startTrimAnimation();
        } else {
            startCutAnimPart1();
        }
    }

    private void startTrimAnimation() {
        if (simpleAnimator != null && simpleAnimator.isPaused()) {
            simpleAnimator.resume();
            mediaPlayer.setPlayWhenReady(true);
        } else {
            float startX;
            float endX;
            if (thumbPosition >= startPosition && thumbPosition <= endPosition) {
                startX = thumbBar.getX();
                endX = endBarX;
                animDuration = endPosition - thumbPosition;
                mediaPlayer.seekTo(thumbPosition);
            } else {
                startX = startBarX;
                endX = endBarX;
                animDuration = endPosition - startPosition;
                mediaPlayer.seekTo(startPosition);
            }

            simpleAnimator = ObjectAnimator.ofFloat(thumbBar, "x", startX, endX);

            if (animDuration > 0) {
                animDuration = animDuration + 150;
                simpleAnimator.setDuration(animDuration);
            } else {
                simpleAnimator.setDuration(50);
            }

            simpleAnimator.setInterpolator(new LinearInterpolator());
            simpleAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mediaPlayer.setPlayWhenReady(false);
                    mediaPlayer.seekTo(startPosition);
                    thumbPosition = startPosition;

                    moveWithAnimation(thumbBar.getX(), startBar.getX() + markerWidth - thumbBar.getWidth() / 2f).addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            onAnimationListener.animationEnd();
                        }
                    });

                    super.onAnimationEnd(animation);
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    mediaPlayer.setPlayWhenReady(true);
                    onAnimationListener.animationStart();
                    super.onAnimationStart(animation);
                }

                @Override
                public void onAnimationPause(Animator animation) {
                    super.onAnimationPause(animation);
                }
            });

            moveWithAnimation(thumbBar.getX(), startX).addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if(simpleAnimator!=null)simpleAnimator.start();
                }
            });
        }
    }

    private void startCutAnimPart1() {
        if (simpleAnimator != null && simpleAnimator.isPaused()) {
            simpleAnimator.resume();
            mediaPlayer.setPlayWhenReady(true);
        } else {
            float startX;
            float endX;
            if (thumbPosition > startPosition) {
                startCutAnimPart2();
                return;
            } else {
                startX = thumbBar.getX();
                endX = startBarX;
                animDuration = startPosition - thumbPosition;
                mediaPlayer.seekTo(thumbPosition);
            }

            simpleAnimator = ObjectAnimator.ofFloat(thumbBar, "x", startX, endX);

            if (animDuration > 0) {
                animDuration = animDuration + 150;
                simpleAnimator.setDuration(animDuration);
            } else {
                simpleAnimator.setDuration(50);
            }

            simpleAnimator.setInterpolator(new LinearInterpolator());
            simpleAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    simpleAnimator = null;
                    mediaPlayer.setPlayWhenReady(false);
                    mediaPlayer.seekTo(startPosition);
                    thumbPosition = startPosition;
                    startCutAnimPart2();
                    super.onAnimationEnd(animation);
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    mediaPlayer.setPlayWhenReady(true);
                    onAnimationListener.animationStart();
                    super.onAnimationStart(animation);
                }
            });
            simpleAnimator.start();
        }
    }

    public void startCutAnimPart2() {
        if (simpleAnimator != null && simpleAnimator.isPaused()) {
                simpleAnimator.resume();
                mediaPlayer.setPlayWhenReady(true);
        } else {
            float startX;
            float endX;
            if (thumbPosition < endPosition) {
                startX = endBarX;
                endX = rootWidth + thumbBar.getWidth() / 2;
                animDuration = VideoInfo.getInstance().totalVideoDuration - endPosition;
                mediaPlayer.seekTo(endPosition);
            } else {
                startX = thumbBar.getX();
                endX = rootWidth + thumbBar.getWidth() / 2;
                animDuration = VideoInfo.getInstance().totalVideoDuration - thumbPosition;
                mediaPlayer.seekTo(thumbPosition);
            }

            simpleAnimator = ObjectAnimator.ofFloat(thumbBar, "x", startX, endX);

            if (animDuration > 0) {
                animDuration = animDuration + 150;
                simpleAnimator.setDuration(animDuration);
            } else {
                simpleAnimator.setDuration(50);
            }

            simpleAnimator.setInterpolator(new LinearInterpolator());
            simpleAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    simpleAnimator = null;
                    mediaPlayer.setPlayWhenReady(false);
                    mediaPlayer.seekTo((int) ((mediaPlayer.getDuration() * (markerWidth)) / (long) (rootWidth)));
                    thumbPosition = mediaPlayer.getCurrentPosition();

                    moveWithAnimation(thumbBar.getX(), markerWidth - thumbBar.getWidth() / 2f).addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            thumbPosition = 0;
                            onAnimationListener.animationEnd();
                        }
                    });
                    super.onAnimationEnd(animation);
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    mediaPlayer.setPlayWhenReady(true);
                    onAnimationListener.animationStart();
                    super.onAnimationStart(animation);
                }
            });

            moveWithAnimation(thumbBar.getX(), startX).addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if(simpleAnimator!=null)simpleAnimator.start();
                }
            });
        }
    }

    public void stopAnim() {
        thumbPosition = mediaPlayer.getCurrentPosition();
        if(simpleAnimator == null) return;
        if (simpleAnimator.isRunning()) {
            simpleAnimator.pause();
            mediaPlayer.setPlayWhenReady(false);
        } else {
            simpleAnimator.cancel();
            simpleAnimator = null;
        }
    }

    private ObjectAnimator moveWithAnimation(float startX, float endX) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(thumbBar, "x", startX, endX).setDuration(200);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
        return animator;
    }

    public void setOnAnimationListener(OnAnimationListener onAnimationListener) {
        this.onAnimationListener = onAnimationListener;
    }

    public String getType() {
        return type;
    }

    public void setMediaPlayer(final SimpleExoPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }

    public void setUpSeekPosition() {
        mediaPlayer.seekTo((long) ((mediaPlayer.getDuration() * (startBar.getX())) / (long) (rootWidth)));
        mediaPlayer.addListener(new Player.EventListener() {
            @Override
            public void onSeekProcessed() {
                startBarX = startBar.getX() + markerWidth - thumbBar.getWidth() / 2f;
                startPosition = mediaPlayer.getCurrentPosition();
                onProgressChangeListener.onLeftMarkerPositionChanged();
                mediaPlayer.seekTo((long) ((mediaPlayer.getDuration() * (endBar.getX() - markerWidth)) / (long) (rootWidth)));
                mediaPlayer.removeListener(this);
                mediaPlayer.addListener(new Player.EventListener() {
                    @Override
                    public void onSeekProcessed() {
                        endBarX = endBar.getX() - thumbBar.getWidth() / 2f;
                        endPosition = mediaPlayer.getCurrentPosition();
                        onProgressChangeListener.onRightMarkerPositionChanged();
                        mediaPlayer.seekTo(0);
                        mediaPlayer.removeListener(this);
                        mediaPlayer.addListener(new Player.EventListener() {
                            @Override
                            public void onSeekProcessed() {
                                thumbPosition = mediaPlayer.getCurrentPosition();
                                onProgressChangeListener.onThumbPositionChanged();
                                mediaPlayer.removeListener(this);
                            }
                        });
                    }
                });
            }
        });
    }

    public long getStartPosition() {
        return startPosition;
    }

    public long getEndPosition() {
        return endPosition;
    }

    public void moveThumbBar() {
        thumbBar.setX(getCurrentVideoPosition() - 18 + markerWidth);
        thumbPosition = mediaPlayer.getCurrentPosition();
    }

    public void setEndPosition() {
        endBar.setX(getCurrentVideoPosition() + markerWidth);
        endBarX = endBar.getX() - thumbBar.getWidth() / 2f;
        endPosition = mediaPlayer.getCurrentPosition();
        colorView();
    }

    public void setStartPosition() {
        startBar.setX(getCurrentVideoPosition());
        startBarX = startBar.getX() + markerWidth - thumbBar.getWidth() / 2f;
        startPosition = mediaPlayer.getCurrentPosition();
        colorView();
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
