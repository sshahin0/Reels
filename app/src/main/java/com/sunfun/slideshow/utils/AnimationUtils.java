package com.sunfun.slideshow.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;


import com.daasuu.mp4compose.player.GPUPlayerView;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

public class AnimationUtils {
    private final Context context;
    private int originalHPCHeight = 0;
    public static final long ANIMATION_DURATION = 200;
    public boolean isSlideUp = false;
    private int scrollOffset;
    private OnAnimationFinishListener onAnimationFinishListener;
    private int animationCount;
    private String TAG = AnimationUtils.class.getName();
    private float totalHeight = 0;
    private RelativeLayout heightPartContainer = null;
    private GPUPlayerView higherPart = null;
    private ImageButton btn1 = null;
    private ImageButton btn2 = null;
    private ImageButton btn3 = null;
    private ViewGroup panel1 = null;
    private ObjectAnimator roundAnimation;

    public AnimationUtils(Context context) {
        this.context = context;
    }

    public AnimationUtils(Context context, float totalHeight, final RelativeLayout heightPartContainer, final GPUPlayerView higherPart, ImageButton btn1, ImageButton btn2, ImageButton btn3, ViewGroup panel1) {
        this(context);
        this.originalHPCHeight = heightPartContainer.getHeight();
        this.totalHeight = totalHeight;
        this.heightPartContainer = heightPartContainer;
        this.higherPart = higherPart;
        this.btn1 = btn1;
        this.btn2 = btn2;
        this.btn3 = btn3;
        this.panel1 = panel1;
    }

    public void slideUp(ViewGroup viewGroup, boolean singleAnimation){
        viewGroup.setTranslationY(viewGroup.getHeight());
        viewGroup.setVisibility(View.VISIBLE);
        viewGroup.animate().translationY(0).setInterpolator(new LinearInterpolator()).setDuration(ANIMATION_DURATION).start();
        isSlideUp = true;
    }

    public void slideDown(ViewGroup viewGroup, boolean singleAnimation) {
        viewGroup.animate().translationY(viewGroup.getHeight())
                .setInterpolator(new LinearInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        viewGroup.setVisibility(View.INVISIBLE);
                        viewGroup.animate().setListener(null);
                    }
                })
                .setDuration(ANIMATION_DURATION).start();
        isSlideUp = false;
    }



    public void slideUp(ViewGroup viewGroup) {
        animationCount = 0;
        if (viewGroup == null) {
            if (onAnimationFinishListener != null) onAnimationFinishListener.onAnimationFinish();
            return;
        }



        float heightDiff = totalHeight - heightPartContainer.getHeight() - viewGroup.getHeight();
        final float newMainHeight = heightPartContainer.getHeight() + heightDiff;
        final float scale = (1f / (float) (heightPartContainer.getHeight())) * newMainHeight;
        viewGroup.setVisibility(View.VISIBLE);
        viewGroup.setTranslationY(viewGroup.getHeight());

        viewGroup.animate()
                .translationY(0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        onAnimationFinish();
                    }
                })
                .setInterpolator(new LinearInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                    }
                })
                .setDuration(ANIMATION_DURATION).start();

        if (higherPart.getHeight() - higherPart.getWidth() < 4) {
            higherPart.animate()
                    .translationY(heightDiff / 2)
                    .setDuration(ANIMATION_DURATION)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            onAnimationFinish();
                        }
                    })
                    .setInterpolator(new LinearInterpolator()).start();

            HeightAnimation heightAnim = new HeightAnimation(heightPartContainer, heightPartContainer.getHeight(), heightPartContainer.getHeight() + (int) heightDiff * 2);
            heightAnim.setDuration(ANIMATION_DURATION);
            heightAnim.setInterpolator(new LinearInterpolator());
            heightPartContainer.startAnimation(heightAnim);
            isSlideUp = true;
        } else {
            heightPartContainer.animate()
                    .scaleX(scale)
                    .scaleY(scale)
                    .translationY(heightDiff / 2)
                    .setDuration(ANIMATION_DURATION)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            onAnimationFinish();
                        }
                    })
                    .setInterpolator(new LinearInterpolator()).start();
        }

        btn1.animate()
                .translationY(heightDiff / 2)
                .setDuration(ANIMATION_DURATION)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        onAnimationFinish();
                    }
                })
                .setInterpolator(new LinearInterpolator()).start();

        btn2.animate()
                .translationY(heightDiff)
                .setDuration(ANIMATION_DURATION)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        onAnimationFinish();
                    }
                })
                .setInterpolator(new LinearInterpolator()).start();

        btn3.animate()
                .translationY(heightDiff)
                .setDuration(ANIMATION_DURATION)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        onAnimationFinish();
                    }
                })
                .setInterpolator(new LinearInterpolator()).start();

        panel1.animate()
                .translationY(heightDiff)
                .setDuration(ANIMATION_DURATION)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        onAnimationFinish();
                    }
                })
                .setInterpolator(new LinearInterpolator()).start();
        isSlideUp = true;
    }

    public void slideDown(ViewGroup viewGroup) {
        animationCount = 0;
        if (viewGroup == null) {
            if (onAnimationFinishListener != null) onAnimationFinishListener.onAnimationFinish();
            return;
        }

        viewGroup.animate()
                .translationY(viewGroup.getHeight())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        onAnimationFinish();
                    }
                })
                .setInterpolator(new LinearInterpolator())
                .setDuration(ANIMATION_DURATION).start();


        if (higherPart.getHeight() - higherPart.getWidth() < 4) {
            higherPart.animate()
                    .translationY(0)
                    .setDuration(ANIMATION_DURATION)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            onAnimationFinish();
                        }
                    })
                    .setInterpolator(new LinearInterpolator()).start();

            HeightAnimation heightAnim = new HeightAnimation(heightPartContainer, heightPartContainer.getHeight(), originalHPCHeight);
            heightAnim.setDuration(ANIMATION_DURATION);
            heightAnim.setInterpolator(new LinearInterpolator());
            heightPartContainer.startAnimation(heightAnim);
        } else {
            heightPartContainer.animate()
                    .scaleX(1)
                    .scaleY(1)
                    .translationY(0)
                    .setDuration(ANIMATION_DURATION)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            onAnimationFinish();
                        }
                    })
                    .setInterpolator(new LinearInterpolator()).start();
        }

        btn1.animate()
                .translationY(0)
                .setDuration(ANIMATION_DURATION)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        onAnimationFinish();
                    }
                })
                .setInterpolator(new LinearInterpolator()).start();

        btn2.animate()
                .translationY(0)
                .setDuration(ANIMATION_DURATION)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        onAnimationFinish();
                    }
                })
                .setInterpolator(new LinearInterpolator()).start();

        btn3.animate()
                .translationY(0)
                .setDuration(ANIMATION_DURATION)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        onAnimationFinish();
                    }
                })
                .setInterpolator(new LinearInterpolator()).start();

        panel1.animate()
                .translationY(0)
                .setDuration(ANIMATION_DURATION)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        onAnimationFinish();
                    }
                })
                .setInterpolator(new LinearInterpolator()).start();

        isSlideUp = false;
    }

    public void setOverScrollAnimation(ScrollView scrollView) {
        OverScrollDecoratorHelper.setUpOverScroll(scrollView);
    }

    public void setOverScrollAnimation(HorizontalScrollView scrollView) {
        OverScrollDecoratorHelper.setUpOverScroll(scrollView);
    }

    public void setOverScrollAnimation(RecyclerView musicListRecyclerView) {
        OverScrollDecoratorHelper.setUpOverScroll(musicListRecyclerView, 0);
    }

    public void setAlphaAnimation(float start, float end, int duration, View view) {
        view.setVisibility(View.VISIBLE);
        view.setAlpha(start);
        view.animate().alpha(end).setDuration(duration).start();
    }

    public void setScrollViewAnimation(final ScrollView scrollView, final ConstraintLayout titleBar, final View divider, final TextView titleText) {
        titleBar.post(() -> scrollOffset = titleBar.getHeight());

        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            int scrollY = scrollView.getScrollY(); // For ScrollView

            if (divider.getVisibility() == View.INVISIBLE && scrollY > scrollOffset) {
                divider.setVisibility(View.VISIBLE);
                setAlphaAnimation(0f, 1f, 300, titleText);
            }

            if (divider.getVisibility() == View.VISIBLE && scrollY < scrollOffset) {
                divider.setVisibility(View.INVISIBLE);
                setAlphaAnimation(1f, 0f, 300, titleText);
            }
        });
    }

    public void setOnAnimationFinishListener(OnAnimationFinishListener onAnimationFinishListener) {
        this.onAnimationFinishListener = onAnimationFinishListener;
    }

    public void removeListener() {
        onAnimationFinishListener = null;
    }

    public interface OnAnimationFinishListener {
        void onAnimationFinish();
    }

    public void setBtnPositionCenter(HorizontalScrollView horizontalScrollView, ImageButton btn, int index) {
        int screenWidth = VideoUtils.getScreenWidth(context);
        int left = (int) ((index * (btn.getWidth() + horizontalScrollView.getPaddingLeft())) + (horizontalScrollView.getPaddingLeft() + btn.getWidth() / 2f));
        horizontalScrollView.smoothScrollTo(left - screenWidth / 2, 0);
    }

    private void onAnimationFinish() {
        animationCount = animationCount + 1;
        if (animationCount == 6) {
            if (onAnimationFinishListener != null) onAnimationFinishListener.onAnimationFinish();
        }
    }


   public void growAnimation(View view){
       ObjectAnimator scaleDown = ObjectAnimator.ofPropertyValuesHolder(
               view,
               PropertyValuesHolder.ofFloat("scaleX", 0.8f),
               PropertyValuesHolder.ofFloat("scaleY", 0.8f)
       );
       scaleDown.setDuration(600);
       scaleDown.setRepeatMode(ValueAnimator.REVERSE);
       scaleDown.setRepeatCount(5);
       scaleDown.start();
   }

   public void startRoundAnimation(View view){
       roundAnimation = ObjectAnimator.ofFloat(view,"rotation",360);
       roundAnimation.setDuration(3000);
       roundAnimation.setRepeatCount(ValueAnimator.INFINITE);
       roundAnimation.start();
   }

   public void stopRoundAnimation(View view){
        if(roundAnimation!=null){
            roundAnimation.pause();
            view.setRotation(0);
            view.setAnimation(null);
        }
   }

}
