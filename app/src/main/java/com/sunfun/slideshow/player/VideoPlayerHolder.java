package com.sunfun.slideshow.player;

import android.content.Context;

import com.daasuu.mp4compose.player.GPUPlayerView;

public class VideoPlayerHolder {
    private static GPUPlayerView videoPlayer;

    VideoPlayerHolder(){
        if (videoPlayer != null){
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }

    public static GPUPlayerView getVideoPlayerInstance(Context context) {
        if (videoPlayer == null) { //if there is no instance available... create new one
            synchronized (VideoPlayerHolder.class) {
                if (videoPlayer == null) videoPlayer = new GPUPlayerView(context);
            }
        }
        return videoPlayer;
    }
}
