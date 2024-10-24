package com.sunfun.slideshow;

import android.os.Build;
import android.os.Bundle;
import com.daasuu.mp4compose.player.GPUPlayerView;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SeekParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.RawResourceDataSource;
import com.sunfun.slideshow.tuitorial.CustomPagerAdapter;
import com.sunfun.slideshow.utils.ExoPlayerHolder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

public class TutorialActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private GPUPlayerView videoPreview;
    private boolean isPreparing;
    private ImageButton backBtn;
    private SimpleExoPlayer simpleExoPlayer;
    private TextView leftBtn;
    private TextView centerBtn;
    private TextView rightBtn;
    private ViewPager viewPager;
    private int LAST_POSITION = 5;
    private final int[] array = {R.raw.io_addmusic_tutorial, R.raw.io_crop_tutorial, R.raw.io_filter_tutorial, R.raw.io_flip_tutorial, R.raw.io_share_tutorial, R.raw.io_trimcut_tutorial};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
        toolbar = findViewById(R.id.toolbar);
        initToolbar();
        changeStatusBarColor();
        findAllViews();
        initViewPager();
        initButtons();
    }

    private void initViewPager() {
        viewPager.setAdapter(new CustomPagerAdapter(this));
        ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                pausePlayer();
                releasePlayer();
                videoPreview = viewPager.findViewWithTag("view" + viewPager.getCurrentItem()).findViewById(R.id.video_view);
                initPlayer(position);

                if (position == LAST_POSITION) {
                    centerBtn.setText("Previous");
                } else if (position == 0) {
                    centerBtn.setText("Next");
                }

                if (position == LAST_POSITION || position == 0) {
                    leftBtn.setVisibility(View.INVISIBLE);
                    rightBtn.setVisibility(View.INVISIBLE);
                    centerBtn.setVisibility(View.VISIBLE);
                } else {
                    leftBtn.setVisibility(View.VISIBLE);
                    rightBtn.setVisibility(View.VISIBLE);
                    centerBtn.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        };
        viewPager.addOnPageChangeListener(onPageChangeListener);
        viewPager.post(() -> onPageChangeListener.onPageSelected(viewPager.getCurrentItem()));
    }

    private void initButtons() {
        backBtn.setOnClickListener(v -> {
            pausePlayer();
            releasePlayer();
            finish();
        });

        leftBtn.setOnClickListener(v -> movePage(leftBtn.getText().toString()));
        rightBtn.setOnClickListener(v -> movePage(rightBtn.getText().toString()));
        centerBtn.setOnClickListener(v -> movePage(centerBtn.getText().toString()));

    }

    private void movePage(String text) {
        if (text.equals("Next")) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
        } else {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
        }
    }

    private void findAllViews() {
        viewPager = findViewById(R.id.viewpager);
        backBtn = findViewById(R.id.backBtn);
        leftBtn = findViewById(R.id.leftBtn);
        centerBtn = findViewById(R.id.centerBtn);
        rightBtn = findViewById(R.id.rightBtn);
    }

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
    }

    private void changeStatusBarColor() {
        Window window = getWindow();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.backgroundColor));
        }
    }

    private void initPlayer(int position) {
        if (simpleExoPlayer == null) simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(this);
        videoPreview.setSimpleExoPlayer(ExoPlayerFactory.newSimpleInstance(this));
        videoPreview.getPlayer().setVolume(0);
        setMediaSourceAndPrepare(position);
        videoPreview.getPlayer().setSeekParameters(SeekParameters.EXACT);
        videoPreview.getPlayer().addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (isPreparing && playbackState == ExoPlayer.STATE_READY) {
                    videoPreview.adjustWidthHeight(videoPreview.getPlayer().getVideoFormat().width, videoPreview.getPlayer().getVideoFormat().height, videoPreview.getPlayer().getVideoFormat().rotationDegrees);
                    videoPreview.setVisibility(View.VISIBLE);
                    isPreparing = false;
                    resumePlayer();
                }

                if (playbackState == ExoPlayer.STATE_ENDED) {
                    videoPreview.getPlayer().seekTo(0);
                }
            }
        });
    }

    private void setMediaSourceAndPrepare(int position) {
        final RawResourceDataSource rawResourceDataSource = new RawResourceDataSource(this);
        DataSpec dataSpec = new DataSpec(RawResourceDataSource.buildRawResourceUri(array[position]));
        try {
            rawResourceDataSource.open(dataSpec);
            DataSource.Factory factory = () -> rawResourceDataSource;
            MediaSource videoSource = new ExtractorMediaSource.Factory(factory).createMediaSource(rawResourceDataSource.getUri());
            isPreparing = true;
            videoPreview.getPlayer().prepare(videoSource);
        } catch (RawResourceDataSource.RawResourceDataSourceException e) {
            e.printStackTrace();
        }
    }

    protected void pausePlayer() {
        if (videoPreview != null) videoPreview.getPlayer().setPlayWhenReady(false);
    }

    private void resumePlayer() {
        videoPreview.getPlayer().setPlayWhenReady(true);
    }

    private void releasePlayer() {
        if (videoPreview != null) {
            videoPreview.getPlayer().release();
            ExoPlayerHolder.removeInstance();
            videoPreview = null;
        }
    }

    @Override
    protected void onPause() {
        pausePlayer();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        pausePlayer();
        releasePlayer();
        super.onDestroy();
    }

}
