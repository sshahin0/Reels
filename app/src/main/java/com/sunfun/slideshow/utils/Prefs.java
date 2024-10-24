package com.sunfun.slideshow.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {
    private Context context;
    private final static String PREFS_NAME = "video_editing_prefs";
    private final String SELECTED_PATH = "selected_path";
    private final String VIEW_GALLERY_OPTION = "view_gallery_option";
    private final String VIDEO_QUALITY_OPTION = "video_quality_option";
    private final String FILE_TYPE = "file_type";
    private final String MUSIC_JSON = "music_json";
    private final String SHOW_SCREENSHOT_DIALOG = "show_screenshot_dialog";


    public Prefs(Context context){
        this.context = context;
    }

    private void saveToPref(String key, String value) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREFS_NAME,0);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.apply();

        String str = sharedPref.getString(key, "");
        String str1 = sharedPref.getString(key, "");

    }

    private String getFromPref(String key){
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getString(key, "");
    }

    public void setSelectedPath(String value) {
        saveToPref(SELECTED_PATH, value);
    }

    public String getSelectedPath() {
        return getFromPref(SELECTED_PATH);
    }

    public void setViewGalleryOption(String value) {
        saveToPref(VIEW_GALLERY_OPTION, value);
    }

    public String getViewGalleryOption() {
        return getFromPref(VIEW_GALLERY_OPTION);
    }


    public void setVideoQuality(String value) {
        saveToPref(VIDEO_QUALITY_OPTION, value);
    }

    public String getVideoQuality() {
        return getFromPref(VIDEO_QUALITY_OPTION);
    }

    public void setFileType(String value) {
        saveToPref(FILE_TYPE, value);
    }

    public String getFileType() {
        return getFromPref(FILE_TYPE);
    }


    public String getMusicJson() {
        return getFromPref(MUSIC_JSON);
    }

    public void setMusicJson(String value){
        saveToPref(MUSIC_JSON, value);
    }

    public void setShowScreenShotDialog(String value) {
        saveToPref(SHOW_SCREENSHOT_DIALOG, value);
    }

    public String getShowScreenShotDialog() {
        return getFromPref(SHOW_SCREENSHOT_DIALOG).equals("") ? "true" : getFromPref(SHOW_SCREENSHOT_DIALOG);
    }
}
