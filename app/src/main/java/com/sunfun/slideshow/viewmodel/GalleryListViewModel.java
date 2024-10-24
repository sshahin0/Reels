package com.sunfun.slideshow.viewmodel;


import android.os.Handler;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.sunfun.slideshow.tools.SongDownloader;

public class GalleryListViewModel extends ViewModel implements SongDownloader.OnReadyToPlayListener  {

    MutableLiveData<SongDownloader> result;
    MutableLiveData<Integer> downloadProgress;

    @Override
    public void onReadyToPlay(SongDownloader songDownloader) {
        new Handler().post(() -> result.setValue(songDownloader));
    }

    @Override
    public void onFailed(SongDownloader songDownloader) {
        new Handler().post(() -> result.setValue(songDownloader));
    }

    @Override
    public void onProgress(int progress) {
        if(downloadProgress!=null)downloadProgress.setValue(progress);
    }

    public MutableLiveData<SongDownloader> downloadMusic(SongDownloader songDownloader) {
        result = new MutableLiveData<>();
        songDownloader.startDownload();
        songDownloader.setOnReadyToPlayListener(this);
        return result;
    }

    public MutableLiveData<Integer> getDownloadProgress() {
        downloadProgress = new MutableLiveData<>();
        return downloadProgress;
    }
}
