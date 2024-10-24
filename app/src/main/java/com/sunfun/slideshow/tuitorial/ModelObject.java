package com.sunfun.slideshow.tuitorial;

import com.sunfun.slideshow.R;

public enum  ModelObject {
    PAGE1(R.string.app_name, R.layout.tuitorial_page),
    PAGE2(R.string.app_name, R.layout.tuitorial_page),
    PAGE3(R.string.app_name, R.layout.tuitorial_page),
    PAGE4(R.string.app_name, R.layout.tuitorial_page),
    PAGE5(R.string.app_name, R.layout.tuitorial_page),
    PAGE6(R.string.app_name, R.layout.tuitorial_page);

    private int mTitleResId;
    private int mLayoutResId;

    ModelObject(int titleResId, int layoutResId) {
        mTitleResId = titleResId;
        mLayoutResId = layoutResId;
    }

    public int getTitleResId() {
        return mTitleResId;
    }

    public int getLayoutResId() {
        return mLayoutResId;
    }
}
