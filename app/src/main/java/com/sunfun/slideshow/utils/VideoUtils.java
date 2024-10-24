package com.sunfun.slideshow.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.util.DisplayMetrics;
import android.view.Display;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;


public class VideoUtils {
    public static int maxFrame = 60;
    public static final int SCRUBBER_BER_BACKGROUND_IMAGE_COUNT = 6;
    private static long DURATION_LIMIT = 6000;

    public static String getVideoRotation() {
        FileInputStream inputStream;
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        try {
            inputStream = new FileInputStream(VideoInfo.getInstance().getVideoPath());
            mediaMetadataRetriever.setDataSource(inputStream.getFD());
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String rotation = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
        mediaMetadataRetriever.release();
        return rotation;
    }

    public static int getScreenWidth(Context context) {
        Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    public static int getScreenHeight(Context context) {
        Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }

    public static MediaMetadataRetriever getMediaMetadataRetriever() {
        FileInputStream inputStream;
        final MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        try {
            inputStream = new FileInputStream(VideoInfo.getInstance().getVideoPath());
            mediaMetadataRetriever.setDataSource(inputStream.getFD());
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mediaMetadataRetriever;
    }


    public static long getVideoDuration(){
        MediaMetadataRetriever mediaMetadataRetriever = VideoUtils.getMediaMetadataRetriever();
        final long duration = Long.parseLong(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        mediaMetadataRetriever.release();
        return duration;
    }


    public static long getVideoFrameRate(){
        MediaExtractor extractor = new MediaExtractor();
        int frameRate = 24;
        try {
            FileInputStream inputStream = new FileInputStream(VideoInfo.getInstance().getVideoPath());
            extractor.setDataSource(inputStream.getFD());
            int numTracks = extractor.getTrackCount();
            for (int i = 0; i < numTracks; ++i) {
                MediaFormat format = extractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                if (mime.startsWith("video/")) {
                    if (format.containsKey(MediaFormat.KEY_FRAME_RATE)) {
                        frameRate = format.getInteger(MediaFormat.KEY_FRAME_RATE);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            extractor.release();
        }

        return frameRate;
    }


    public static int convertDpToPx(Context context, int dp){
        return Math.round(dp*(context.getResources().getDisplayMetrics().xdpi/ DisplayMetrics.DENSITY_DEFAULT));
    }

    public static long getMaxFrame() {
        long duration = getVideoDuration();
        if (duration / 1000 < maxFrame) {
            maxFrame = (int) (duration / 1000);
        }
        return maxFrame;
    }

    public static String millisToMinute(long currentPosition) {
        @SuppressLint("DefaultLocale") String time = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(currentPosition), // The change is in this line
                TimeUnit.MILLISECONDS.toSeconds(currentPosition) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(currentPosition)));

        return time;
    }


    public static MediaMetadataRetriever getMediaMetadataRetrieverWithPath(String videoPath) {
        FileInputStream inputStream;
        final MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        try {
            inputStream = new FileInputStream(videoPath);
            mediaMetadataRetriever.setDataSource(inputStream.getFD());
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mediaMetadataRetriever;
    }


    public static long getVideoDurationWithPath(String videoPath){
        MediaMetadataRetriever mediaMetadataRetriever = VideoUtils.getMediaMetadataRetrieverWithPath(videoPath);
        final long duration = Long.parseLong(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        mediaMetadataRetriever.release();
        return duration;
    }

}
