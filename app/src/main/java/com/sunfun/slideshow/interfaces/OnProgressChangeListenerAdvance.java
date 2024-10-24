package com.sunfun.slideshow.interfaces;

public interface OnProgressChangeListenerAdvance {
    void onVideoPositionChanged();

    void onRightMarkerPositionChanged();

    void onLeftMarkerPositionChanged();

    void onStartScrolling();

    void onStopScrolling();

    void onScrollPositionChange();

    void onScrollPositionChange(long l);
}
