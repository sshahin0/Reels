package com.sunfun.slideshow;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.flyco.tablayout.SegmentTabLayout;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.sunfun.slideshow.utils.Prefs;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    private Prefs prefs;
    private String[] viewGalleryOptions;
    private String[] videoQualityOptions;
    private String[] fileTypeOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        changeStatusBarColor();

        initSettingOptions();

        initSharedPrefs();

        initTabs();

        TextView doneBtn = findViewById(R.id.doneButton);
        doneBtn.setOnClickListener(v -> finish());

    }

    private void initTabs() {
        SegmentTabLayout viewGalleryTab = findViewById(R.id.viewGalleryTab);
        SegmentTabLayout videoQualityTab = findViewById(R.id.videoQualityTab);
        final SegmentTabLayout fileTypeTab = findViewById(R.id.fileTypeTab);

        viewGalleryTab.setTabData(viewGalleryOptions);
        videoQualityTab.setTabData(videoQualityOptions);
        fileTypeTab.setTabData(fileTypeOptions);

        if (prefs.getViewGalleryOption().equals(viewGalleryOptions[0])) {
            viewGalleryTab.setCurrentTab(0);
        } else {
            viewGalleryTab.setCurrentTab(1);
        }

        if (prefs.getVideoQuality().equals(videoQualityOptions[0])) {
            videoQualityTab.setCurrentTab(0);
        } else if (prefs.getVideoQuality().equals(videoQualityOptions[1])) {
            videoQualityTab.setCurrentTab(1);
        } else {
            videoQualityTab.setCurrentTab(2);
        }

        if (prefs.getFileType().equals(fileTypeOptions[0])) {
            fileTypeTab.setCurrentTab(0);
        } else {
            fileTypeTab.setCurrentTab(1);
        }

        viewGalleryTab.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                setResult(Activity.RESULT_OK);
                prefs.setViewGalleryOption(viewGalleryOptions[position]);
            }

            @Override
            public void onTabReselect(int position) {

            }
        });

        videoQualityTab.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                prefs.setVideoQuality(videoQualityOptions[position]);
            }

            @Override
            public void onTabReselect(int position) {

            }
        });

        fileTypeTab.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                prefs.setFileType(fileTypeOptions[position]);
            }

            @Override
            public void onTabReselect(int position) {

            }
        });
    }

    private void initSettingOptions() {
        viewGalleryOptions = getResources().getStringArray(R.array.view_gallery_options);
        videoQualityOptions = getResources().getStringArray(R.array.video_quality_options);
        fileTypeOptions = getResources().getStringArray(R.array.file_type_options);
    }

    private void initSharedPrefs() {
        prefs = new Prefs(this);
    }

    private void changeStatusBarColor() {
        Window window = getWindow();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.backgroundColor));
        }
    }

}
