package com.sunfun.slideshow.tools;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.sunfun.slideshow.utils.PathUtils;

import java.io.IOException;
import java.util.Objects;

public class AudioRecorder {

    private final Activity activity;
    private final Fragment fragment;

    private MediaRecorder recorder;
    private String filePath;
    private boolean isRecording = false;
    public static final int AUDIO_PERMISSION_REQUEST = 1090;

    public AudioRecorder(Activity activity, Fragment fragment) {
        this.activity = activity;
        this.fragment = fragment;
        if(recorder == null) recorder = new MediaRecorder();
    }

    public boolean hasPermission(){
        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(activity), Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            fragment.requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, AUDIO_PERMISSION_REQUEST);
            return false;
        }
        return true;
    }

    public void prepareRecorderAndStart() {
        prepareRecorder();
    }

    private void prepareRecorder() {
        filePath = PathUtils.generateTempPath(System.currentTimeMillis() + "record_audio", ".mp3");
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setOutputFile(filePath);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        try {
            recorder.prepare();
            startRecording();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startRecording() {
        if(recorder==null) return;
        recorder.start();
        isRecording = true;
    }

    public void stopRecording(){
        if(recorder==null) return;
        isRecording = false;
        try{
        recorder.stop();
        recorder.reset();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public String getFilePath() {
        return filePath;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public void releaseRecorder() {
        stopRecording();
        recorder.release();
    }
}
