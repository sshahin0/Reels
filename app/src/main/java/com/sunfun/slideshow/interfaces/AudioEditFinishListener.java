package com.sunfun.slideshow.interfaces;

public interface AudioEditFinishListener {
    void onProgress(double progress);
    void onFinishEdit(String path);
    void onFailEdit(String message);
}
