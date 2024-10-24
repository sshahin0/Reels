package com.sunfun.slideshow.utils;

import android.content.Context;
import android.util.Log;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.sunfun.slideshow.R;
import com.sunfun.slideshow.view.dialog.IOSDialog;

public class ExoPlayerErrorHandler {

    public static final String TAG = ExoPlayerErrorHandler.class.getName();
    private final Context context;
    private final IOSDialog.Listener iosDialogBackListener;

    public ExoPlayerErrorHandler(Context context, IOSDialog.Listener iosDialogBackListener) {
        this.context = context;
        this.iosDialogBackListener = iosDialogBackListener;
    }

    public void handle(ExoPlaybackException error) {

        if (error == null) {
            new DialogUtils().createWarningDialog(context, "Error!", R.string.video_error_msg, null, null, iosDialogBackListener);
            return;
        }

        switch (error.type) {
            case ExoPlaybackException.TYPE_SOURCE:
                Log.e(TAG, "TYPE_SOURCE: " + error.getSourceException().getMessage());
                new DialogUtils().createWarningDialog(context, "Error!", R.string.video_error_msg, null, null, iosDialogBackListener);
                break;

            case ExoPlaybackException.TYPE_RENDERER:
                Log.e(TAG, "TYPE_RENDERER: " + error.getRendererException().getMessage());
                new DialogUtils().createWarningDialog(context, "Error!", R.string.video_error_msg, null, null, iosDialogBackListener);
                break;

            case ExoPlaybackException.TYPE_OUT_OF_MEMORY:
                Log.e(TAG, "TYPE_OUT_OF_MEMORY: " + error.getUnexpectedException().getMessage());
                new DialogUtils().createWarningDialog(context, "Error!", R.string.video_error_msg, null, null, iosDialogBackListener);
                break;

            case ExoPlaybackException.TYPE_REMOTE:
                Log.e(TAG, "TYPE_OUT_OF_REMOTE: " + error.getUnexpectedException().getMessage());
                new DialogUtils().createWarningDialog(context, "Error!", R.string.video_error_msg, null, null, iosDialogBackListener);
                break;

            default:
                Log.e(TAG, "TYPE_UNEXPECTED: " + error.getUnexpectedException().getMessage());
                new DialogUtils().createWarningDialog(context, "Error!", R.string.video_error_msg, null, null, iosDialogBackListener);
                break;

        }
    }
}
