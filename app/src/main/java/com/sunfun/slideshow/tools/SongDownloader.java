package com.sunfun.slideshow.tools;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.storage.StorageReference;
import com.sunfun.slideshow.utils.StorageUtils;
import com.sunfun.slideshow.utils.ZipUtil;

import java.io.File;
import java.util.concurrent.Executors;

public class SongDownloader {

    private String srcZipPath;
    private String outUnZipPath;
    private Context context;
    private OnReadyToPlayListener onReadyToPlayListener;
    private StorageReference storageReference;
    private String songTitle;
    private int position;
    private String downloadPath;
    private File moviesDir;
    private StorageReference songRef;
    private File dest;
    private AsyncTask<Void, Void, Boolean> unzipTask;
    private boolean isCancel = false;

    public SongDownloader(Context context, StorageReference storageReference, String songTitle, int pos) {
        this.context = context;
        this.storageReference = storageReference;
        this.songTitle = songTitle;
        this.position = pos;
    }

    public void startDownload() {
        songRef = storageReference.child(songTitle);
        moviesDir = StorageUtils.getInstance().dataFolder;
        dest = new File(moviesDir, songTitle);
        srcZipPath = dest.getAbsolutePath();

        outUnZipPath = moviesDir + "/";
        songRef.getFile(dest).addOnProgressListener(taskSnapshot -> {
            int progress = (int) (taskSnapshot.getBytesTransferred()/(double)taskSnapshot.getTotalByteCount() * 100);
            onReadyToPlayListener.onProgress(progress);
        });
        songRef.getFile(dest).addOnSuccessListener(taskSnapshot -> doUnZip()).addOnFailureListener(exception -> onReadyToPlayListener.onFailed(SongDownloader.this));
    }

    private void doUnZip() {
        unzipTask = new UnzipTask().executeOnExecutor(Executors.newSingleThreadExecutor());
    }

    public void setOnReadyToPlayListener(OnReadyToPlayListener onReadyToPlayListener) {
        this.onReadyToPlayListener = onReadyToPlayListener;
    }

    public int getPosition() {
        return position;
    }

    public String getDownloadPath() {
        return downloadPath;
    }

    private class UnzipTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            Log.d("simul123", "unziping is in progress");
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (TextUtils.isEmpty(srcZipPath)) {
                Toast.makeText(context, "Plz choose a zip file", Toast.LENGTH_SHORT).show();
                return false;
            }

            File srcFile = new File(srcZipPath);
            if (!srcFile.isFile()) {
                Toast.makeText(context, "Plz choose a file", Toast.LENGTH_SHORT).show();
                return false;
            }

            try {
                if (TextUtils.isEmpty(srcZipPath)) {
                    ZipUtil.unzip(srcFile);
                } else {
                    ZipUtil.unzip(srcFile, new File(outUnZipPath));
                }
                return true;
            } catch (Exception ex) {
                ex.printStackTrace();
                onReadyToPlayListener.onFailed(SongDownloader.this);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean bool) {
            if (bool) {
                File from = new File(outUnZipPath + "/" + songTitle.replace(".zip", ""));
                File to = new File(StorageUtils.getInstance().mainFolder, songTitle.replace(".zip", ""));
                from.renameTo(to);
                new File(srcZipPath).delete();
                downloadPath = to.getAbsolutePath();
                if(!isCancel)
                onReadyToPlayListener.onReadyToPlay(SongDownloader.this);
            } else {
                onReadyToPlayListener.onFailed(SongDownloader.this);
            }
            super.onPostExecute(bool);
        }
    }

    public void cancelDownload(){
        if(songRef == null) songRef.getFile(dest).cancel();
        if(unzipTask!=null)unzipTask.cancel(true);
        isCancel = true;
    }


    public interface OnReadyToPlayListener {
        void onReadyToPlay(SongDownloader songDownloader);
        void onFailed(SongDownloader songDownloader);
        void onProgress(int progress);
    }

}
