package com.sunfun.slideshow.videopicker;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crashlytics.android.Crashlytics;
import com.sunfun.slideshow.R;
import com.sunfun.slideshow.SettingsActivity;
import com.sunfun.slideshow.utils.AnimationUtils;
import com.sunfun.slideshow.utils.OnOneOffClickListener;
import com.sunfun.slideshow.utils.Prefs;
import com.sunfun.slideshow.utils.TopSheetBehavior;

import java.util.List;
import java.util.Objects;

import io.fabric.sdk.android.Fabric;


public class VideoPickerActivity extends AppCompatActivity {

    private static final int PERMISSION_WRITE_STORAGE = 1000;
    private static final int SETTINGS_ACTIVITY = 1001;

    public TextView allRollBtn;
    private View sheet;
    private TopSheetBehavior topSheet;
    private RecyclerView videoRecyclerView;
    private RecyclerView bucketRecyclerView;

    private static final int sColumnWidth = 100; // width of single video item view for recycler view.
    private String selectedFolder = "";
    private ImageButton settingsBtn;
    private TextView closeBtn;
    public VideoPickerViewModel videoPickerViewModel;
    private Prefs prefs;
    private String viewGalleryOption = "Newest First";
    private String folderName = "";
    private boolean folderSelection = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_video_picker);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        changeStatusBarColor();
        overridePendingTransition(R.anim.slide_in_left, R.anim.fade_out);

        if (!hasPermission()) {
            //Requesting Read and Write storage Permission
            requestPermissions();
        } else {
            setupAppUtility();
        }

    }

    private void setupAppUtility() {
        findAllViews();
        initSharedPrefs();
        initButtons();
        initTopSheet();
        initViewModel();
        setObserver();

        videoPickerViewModel.getBuckets(viewGalleryOption);
        videoPickerViewModel.getVideos(selectedFolder, viewGalleryOption);
    }

    private void initSharedPrefs() {
        prefs = new Prefs(this);

        if (prefs.getViewGalleryOption().equals(""))
            prefs.setViewGalleryOption(getResources().getStringArray(R.array.view_gallery_options)[0]);

        if (prefs.getVideoQuality().equals(""))
            prefs.setVideoQuality(getResources().getStringArray(R.array.video_quality_options)[0]);

        if (prefs.getFileType().equals(""))
            prefs.setFileType(getResources().getStringArray(R.array.file_type_options)[0]);

        if (prefs.getViewGalleryOption().equals("Newest First")) {
            viewGalleryOption = "DESC";
        } else viewGalleryOption = "ASC";
    }


    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_WRITE_STORAGE);
        }
    }


    private boolean hasPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }


    private void setObserver() {
        videoPickerViewModel.bucketsMutable.observe(this, buckets -> initBucketRecyclerView(buckets));
        videoPickerViewModel.videoCursorMutable.observe(this, cursor -> initVideoRecyclerView(cursor));
    }

    private void initViewModel() {
        videoPickerViewModel = ViewModelProviders.of(this).get(VideoPickerViewModel.class);
    }

    private void initVideoRecyclerView(Cursor cursor) {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 4);
        int preSelectedVideoId = getIntent().getIntExtra("video_id", -1);
        VideoListAdapter videoListAdapter = new VideoListAdapter(this, cursor, preSelectedVideoId);
        videoRecyclerView.setAdapter(videoListAdapter);
        videoRecyclerView.setLayoutManager(layoutManager);

        /*ViewTreeObserver viewTreeObserver = videoRecyclerView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                calculateCellSize();
            }
        });*/
    }

    private void initBucketRecyclerView(List<BucketInfo> buckets) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        BucketListAdapter bucketListAdapter = new BucketListAdapter(this, buckets);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, R.drawable.recycler_divider);
        bucketRecyclerView.addItemDecoration(dividerItemDecoration);
        bucketRecyclerView.hasFixedSize();
        bucketRecyclerView.setAdapter(bucketListAdapter);
        bucketRecyclerView.setLayoutManager(layoutManager);
        bucketRecyclerView.setItemAnimator(new DefaultItemAnimator());
        new AnimationUtils(this).setOverScrollAnimation(bucketRecyclerView);
    }

    private void calculateCellSize() {
        int spanCount = (int) Math.floor(videoRecyclerView.getWidth() / convertDPToPixels());
        ((GridLayoutManager) Objects.requireNonNull(videoRecyclerView.getLayoutManager())).setSpanCount(spanCount);
    }

    private float convertDPToPixels() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float logicalDensity = metrics.density;
        return VideoPickerActivity.sColumnWidth * logicalDensity;
    }

    private void initTopSheet() {
        topSheet = TopSheetBehavior.from(sheet);
        topSheet.setTopSheetCallback(new TopSheetBehavior.TopSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == TopSheetBehavior.STATE_EXPANDED) {
                    //Nothing to do here for now.
                } else {
                    getIntent().putExtra("video_id", -1);
                    settingsBtn.setVisibility(View.VISIBLE);
                    closeBtn.setVisibility(View.GONE);
                    allRollBtn.setTag("0");
                    allRollBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_arrow_down, 0, R.drawable.ic_arrow_down, 0);

                    if (!folderName.equals("") && folderSelection) {
                        folderSelection = false;
                        if (folderName.equals(getString(R.string.all))) {
                            setSelectedFolder("");
                            videoPickerViewModel.getVideos("", viewGalleryOption);
                        } else {
                            setSelectedFolder(folderName);
                            videoPickerViewModel.getVideos(folderName, viewGalleryOption);
                        }
                    }
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset, @Nullable Boolean isOpening) {

            }
        });
    }

    private void initButtons() {
        allRollBtn.setOnClickListener(v -> {
            if (v.getTag().equals("0")) {
                openTopSheet();
                settingsBtn.setVisibility(View.GONE);
                closeBtn.setVisibility(View.VISIBLE);
                allRollBtn.setTag("1");
                allRollBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_arrow_up, 0, R.drawable.ic_arrow_up, 0);
                setNavigationBarWhite();
            } else {
                closeTopSheet();
            }
        });

        closeBtn.setOnClickListener(v -> closeTopSheet());
        settingsBtn.setOnClickListener(new OnOneOffClickListener() {
            @Override
            public void onSingleClick(View v) {
                Intent intent = new Intent(VideoPickerActivity.this, SettingsActivity.class);
                startActivityForResult(intent, SETTINGS_ACTIVITY);
            }
        });
    }

    private void setNavigationBarBlack() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.backgroundColor));
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
    }

    private void setNavigationBarWhite() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, android.R.color.white));
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTINGS_ACTIVITY && resultCode == Activity.RESULT_OK) {
            if (prefs.getViewGalleryOption().equals("Newest First")) {
                videoPickerViewModel.getVideos(selectedFolder, "DESC");
            } else {
                videoPickerViewModel.getVideos(selectedFolder, "ASC");
            }
        }
    }

    private void findAllViews() {
        allRollBtn = findViewById(R.id.allBtn);
        sheet = findViewById(R.id.top_sheet);
        videoRecyclerView = findViewById(R.id.videoRecyclerView);
        bucketRecyclerView = findViewById(R.id.bucketRecyclerView);
        settingsBtn = findViewById(R.id.settingsBtn);
        closeBtn = findViewById(R.id.closeBtn);
    }

    private void changeStatusBarColor() {
        Window window = getWindow();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.backgroundColor));
        }
    }

    public void openTopSheet() {
        topSheet.setState(TopSheetBehavior.STATE_EXPANDED);
    }

    public void closeTopSheet() {
        topSheet.setState(TopSheetBehavior.STATE_COLLAPSED);
        setNavigationBarBlack();
    }

    public void closeTopSheet(String folderName) {
        setNavigationBarBlack();
        topSheet.setState(TopSheetBehavior.STATE_COLLAPSED);
        this.folderName = folderName;
        folderSelection = true;
    }

    public void setSelectedFolder(String selectedFolder) {
        this.selectedFolder = selectedFolder;
        if (selectedFolder.equals("")) {
            allRollBtn.setText(R.string.all);
        } else allRollBtn.setText(selectedFolder);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == PERMISSION_WRITE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupAppUtility();
            } else {
                Toast.makeText(this, "You need to grant write permission for using this app", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(closeBtn.getVisibility() == View.VISIBLE){
            closeBtn.performClick();
        }else super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
