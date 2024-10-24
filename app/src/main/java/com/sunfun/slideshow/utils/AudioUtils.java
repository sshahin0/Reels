package com.sunfun.slideshow.utils;

import android.content.ContentResolver;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.FileInputStream;

public class AudioUtils {

    public static String getRealPathFromURI(Uri contentURI, ContentResolver contentResolver) {
        String result;
        Cursor cursor = contentResolver.query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }


    public static MediaMetadataRetriever getMediaMetadataRetriever() {
        FileInputStream inputStream;
        final MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        try {
            inputStream = new FileInputStream(VideoInfo.getInstance().getExtraAudioPath());
            mediaMetadataRetriever.setDataSource(inputStream.getFD());
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mediaMetadataRetriever;
    }


    public static long getAudioDuration(){
        MediaMetadataRetriever mediaMetadataRetriever = getMediaMetadataRetriever();
        final long duration = Long.parseLong(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        mediaMetadataRetriever.release();
        return duration;
    }

}
