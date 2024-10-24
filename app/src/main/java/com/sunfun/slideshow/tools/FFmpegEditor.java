package com.sunfun.slideshow.tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.sunfun.slideshow.interfaces.OnVideoEditFinishListener;
import com.sunfun.slideshow.utils.VideoInfo;
import com.sunfun.slideshow.utils.VideoUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

class FFmpegEditor {

    private FFmpeg fFmpeg;
    private OnVideoEditFinishListener videoEditFinishListener;
    private boolean isFailed = false;
    private String failedMsg = "";
    private String filePath;

    FFmpegEditor(Context context) {
        setUpFFmpeg(context);
    }

    private void setUpFFmpeg(Context context) {
        fFmpeg = FFmpeg.getInstance(context);
        try {
            fFmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onStart() {
                    Log.d("Event ", "onStart");
                }

                @Override
                public void onFailure() {
                    Log.d("Event ", "onFailure");
                }

                @Override
                public void onSuccess() {
                    Log.d("Event ", "onSuccess");
                }

                @Override
                public void onFinish() {
                    Log.d("Event ", "onFinish");
                }
            });
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }
    }


    void stopProcess() {
        fFmpeg.killRunningProcesses();
        videoEditFinishListener = null;
        clearErrorFiles(new String[]{filePath});
    }

    private void clearErrorFiles(String[] paths) {
        try {
            File file = new File(paths[0]);
            boolean isDeleted;
            if (file.exists())
                isDeleted = file.delete();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            File file = new File(paths[1]);
            boolean isDeleted;
            if (file.exists())
                isDeleted = file.delete();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            paths[0] = "";
            paths[1] = "";
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static String generateList(String[] inputs) {
        File list;
        Writer writer = null;
        try {
            list = File.createTempFile("fFmpegEditor-list", ".txt");
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(list)));
            for (String input : inputs) {
                writer.write("file '" + input + "'\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "/";
        } finally {
            try {
                if (writer != null)
                    writer.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return list.getAbsolutePath();
    }

    void setVideoEditFinishListener(OnVideoEditFinishListener videoEditFinishListener) {
        this.videoEditFinishListener = videoEditFinishListener;
    }

    void margeAudio(String audioPath1, String audioPath2, String outPath) {
        String[] complexCommand = {"-i", audioPath1, "-filter_complex", "amovie=" + audioPath2 + ":loop=1000,amix=duration=shortest,afade=in:st=0:d=2,afade=out:st="+ ((VideoInfo.getInstance().getDuration()-2000)/1000) +":d=2", "-preset", "ultrafast", "-b:a", "64k", outPath};
        filePath = outPath;
        try {
            fFmpeg.execute(complexCommand, new ExecuteBinaryResponseHandler() {
                @Override
                public void onStart() {

                }

                @Override
                public void onProgress(String message) {
                    if (message.startsWith("size")) {
                        int index = message.indexOf("time");
                        String time = message.substring(index + 5, index + 11 + 5);

                        Log.d("simul", time);

                        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                        Date date = null;
                        try {
                            date = dateFormat.parse(time.split("\\.")[0]);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        assert date != null;
                        long seconds = date.getTime();
                        long millis = seconds + Integer.parseInt(time.split("\\.")[1]);

                        if (videoEditFinishListener != null)
                            videoEditFinishListener.onProgress(millis/(float)(VideoUtils.getVideoDurationWithPath(audioPath1)));
                    }
                }

                @Override
                public void onFailure(String message) {
                    isFailed = true;
                    failedMsg = message;
                    Log.e("simul", message);
                }

                @Override
                public void onSuccess(String message) {
                    isFailed = false;
                }

                @Override
                public void onFinish() {
                    if (videoEditFinishListener == null) return;
                    if (isFailed)
                        videoEditFinishListener.onFailEdit(failedMsg);
                    else
                        videoEditFinishListener.onFinishEdit(filePath);
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
    }

}
