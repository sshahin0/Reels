package com.sunfun.slideshow.viewmodel;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Handler;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.sunfun.slideshow.utils.ImageUtils;
import com.sunfun.slideshow.utils.VideoInfo;
import com.sunfun.slideshow.utils.VideoUtils;


public class MainViewModel extends ViewModel {

    private int maxFrame = 60;
    private MutableLiveData<Bitmap> mutableResultAdvance;
    public MutableLiveData<Bitmap> progressBarImageList;


    @Override
    protected void onCleared() {
        super.onCleared();
    }

    public MutableLiveData<Bitmap> getAdvanceScrubBerBitmap() {
        mutableResultAdvance = new MutableLiveData<>();
        final Handler handler = new Handler();
        Thread thread = new Thread(() -> {
            MediaMetadataRetriever mediaMetadataRetriever = VideoUtils.getMediaMetadataRetriever();
            long duration = VideoUtils.getVideoDuration();

            if (duration / 1000 < maxFrame) {
                maxFrame = (int) (duration / 1000);
            }

            Long frameInterval = duration / maxFrame;
            VideoUtils.maxFrame = maxFrame;

            for (int i = 0; i < duration; i += frameInterval) {
                final Bitmap bitmap = ImageUtils.getScaledBitmap(mediaMetadataRetriever.getFrameAtTime((i + frameInterval / 2) * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)); //unit in microsecond
                if (bitmap == null) {
                    continue;
                }

                Runnable runnable = () -> mutableResultAdvance.setValue(ImageUtils.getScaledBitmap(bitmap));
                handler.post(runnable);
            }
            mediaMetadataRetriever.release();
        });
        thread.start();
        return mutableResultAdvance;
    }

    public synchronized MutableLiveData<Bitmap> getScrubBerBitmap(String type) {
        final Handler handler = new Handler();

        final MutableLiveData<Bitmap> result;
        final long[] frameTimes = new long[VideoUtils.SCRUBBER_BER_BACKGROUND_IMAGE_COUNT];

        if (type.equals("simple")) {
            MutableLiveData<Bitmap> mutableResultSimple = new MutableLiveData<>();
            result = mutableResultSimple;
            for (int i = 0; i < VideoUtils.SCRUBBER_BER_BACKGROUND_IMAGE_COUNT; i++) {
                frameTimes[i] = i * (VideoUtils.getVideoDuration() / VideoUtils.SCRUBBER_BER_BACKGROUND_IMAGE_COUNT);
            }
        } else if (type.equals("trim")) {
            progressBarImageList = new MutableLiveData<>();
            result = progressBarImageList;
            for (int i = 0; i < VideoUtils.SCRUBBER_BER_BACKGROUND_IMAGE_COUNT; i++) {
                frameTimes[i] = (i * (VideoInfo.getInstance().getDuration() / VideoUtils.SCRUBBER_BER_BACKGROUND_IMAGE_COUNT)) + VideoInfo.getInstance().startMs;
            }
        } else if (type.equals("cut")) {
            progressBarImageList = new MutableLiveData<>();
            result = progressBarImageList;
            if (VideoInfo.getInstance().startMs == 0) {
                for (int i = 0; i < VideoUtils.SCRUBBER_BER_BACKGROUND_IMAGE_COUNT; i++) {
                    frameTimes[i] = (i * (VideoInfo.getInstance().getDuration() / VideoUtils.SCRUBBER_BER_BACKGROUND_IMAGE_COUNT)) + VideoInfo.getInstance().endMs;
                }
            } else if (VideoInfo.getInstance().endMs == VideoUtils.getVideoDuration()) {

                for (int i = 0; i < VideoUtils.SCRUBBER_BER_BACKGROUND_IMAGE_COUNT; i++) {
                    frameTimes[i] = (i * (VideoInfo.getInstance().getDuration() / VideoUtils.SCRUBBER_BER_BACKGROUND_IMAGE_COUNT)) + VideoInfo.getInstance().startMs;
                }
            } else {
                for (int i = 0; i < VideoUtils.SCRUBBER_BER_BACKGROUND_IMAGE_COUNT / 2; i++) {
                    frameTimes[i] = i * (VideoInfo.getInstance().startMs) / VideoUtils.SCRUBBER_BER_BACKGROUND_IMAGE_COUNT;
                }

                for (int i = 3; i < VideoUtils.SCRUBBER_BER_BACKGROUND_IMAGE_COUNT; i++) {
                    frameTimes[i] = (i * ((VideoUtils.getVideoDuration() - VideoInfo.getInstance().endMs) / VideoUtils.SCRUBBER_BER_BACKGROUND_IMAGE_COUNT)) + VideoInfo.getInstance().endMs;
                }
            }

        } else {
            if (progressBarImageList == null)
                progressBarImageList = new MutableLiveData<>();
            else
                return progressBarImageList;

            result = progressBarImageList;
            for (int i = 0; i < VideoUtils.SCRUBBER_BER_BACKGROUND_IMAGE_COUNT; i++) {
                frameTimes[i] = i * (VideoUtils.getVideoDuration() / VideoUtils.SCRUBBER_BER_BACKGROUND_IMAGE_COUNT);
            }
        }


        new Thread(() -> {
            MediaMetadataRetriever mediaMetadataRetriever = VideoUtils.getMediaMetadataRetriever();
            long duration = VideoUtils.getVideoDuration();

            long frameInterval = duration / VideoUtils.SCRUBBER_BER_BACKGROUND_IMAGE_COUNT;
            for (int i = 0; i < VideoUtils.SCRUBBER_BER_BACKGROUND_IMAGE_COUNT; i++) {
                try {
                    final Bitmap bitmap = ImageUtils.getScaledBitmap(mediaMetadataRetriever.getFrameAtTime((frameTimes[i] + frameInterval / 2) * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)); //unit in microsecond
                    Runnable runnable = () -> result.setValue(bitmap);
                    handler.post(runnable);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            mediaMetadataRetriever.release();

        }).start();


        return result;
    }


}
