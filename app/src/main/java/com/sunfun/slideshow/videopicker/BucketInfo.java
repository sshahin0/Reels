package com.sunfun.slideshow.videopicker;

import android.graphics.Bitmap;

class BucketInfo {
    private String bucketName;
    private String videoCount = "0";
    private Bitmap bitmap;
    private Bitmap bitmap1;
    private Bitmap bitmap2;

    String getBucketName() {
        return bucketName;
    }

    void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    String getVideoCount() {
        return videoCount;
    }

    void setVideoCount(String videoCount) {
        this.videoCount = videoCount;
    }

    Bitmap getBitmap() {
        return bitmap;
    }

    void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Bitmap getBitmap1() {
        return bitmap1;
    }

    public void setBitmap1(Bitmap bitmap1) {
        this.bitmap1 = bitmap1;
    }

    public Bitmap getBitmap2() {
        return bitmap2;
    }

    public void setBitmap2(Bitmap bitmap2) {
        this.bitmap2 = bitmap2;
    }
}
