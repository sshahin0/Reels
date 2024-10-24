package com.sunfun.slideshow.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;


public class CustomScrollView extends HorizontalScrollView {

    private static final int DELAY_MILLIS = 100;

    public interface OnFlingListener {
        public void onFlingStarted();
        public void onFlingStopped();
    }

    private OnFlingListener mFlingListener;
    private Runnable mScrollChecker;
    private int mPreviousPosition;

    public CustomScrollView(Context context) {
        this(context, null, 0);
    }

    public CustomScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mScrollChecker = new Runnable() {
            @Override
            public void run() {
                int position = getScrollX();
                if (mPreviousPosition - position == 0) {
                    mFlingListener.onFlingStopped();
                    removeCallbacks(mScrollChecker);
                } else {
                    mPreviousPosition = getScrollX();
                    postDelayed(mScrollChecker, DELAY_MILLIS);
                }
            }
        };
    }

    @Override
    public void fling(int velocityX) {
        super.fling(velocityX);

        if (mFlingListener != null) {
            mFlingListener.onFlingStarted();
            post(mScrollChecker);
        }
    }

    public OnFlingListener getOnFlingListener() {
        return mFlingListener;
    }

    public void setOnFlingListener(OnFlingListener mOnFlingListener) {
        this.mFlingListener = mOnFlingListener;
    }

}