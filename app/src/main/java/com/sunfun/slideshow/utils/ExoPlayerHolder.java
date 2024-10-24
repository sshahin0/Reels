package com.sunfun.slideshow.utils;

import android.content.Context;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;

public class ExoPlayerHolder {

    private static volatile SimpleExoPlayer exoPlayerInstance;

    ExoPlayerHolder(){
        if (exoPlayerInstance != null){
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }

    public static SimpleExoPlayer getInstance(Context context) {
        if (exoPlayerInstance == null) { //if there is no instance available... create new one
            synchronized (ExoPlayerHolder.class) {
                if (exoPlayerInstance == null) exoPlayerInstance = ExoPlayerFactory.newSimpleInstance(context);
            }
        }
        return exoPlayerInstance;
    }

    public static void removeInstance() {
        if (exoPlayerInstance != null) { //if there is no instance available... create new one
            synchronized (ExoPlayerHolder.class) {
                if (exoPlayerInstance != null) {
                    exoPlayerInstance.release();
                    exoPlayerInstance = null;
                }
            }
        }
    }
}
