package com.sunfun.slideshow;

import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.provider.Telephony;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import com.daasuu.mp4compose.player.GPUPlayerView;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SeekParameters;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.sunfun.slideshow.utils.DialogUtils;
import com.sunfun.slideshow.utils.ExoPlayerHolder;
import com.sunfun.slideshow.utils.StorageUtils;
import com.sunfun.slideshow.utils.VideoInfo;


public class ShareActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private GPUPlayerView videoPreview;
    private boolean isPreparing;
    private ImageButton playBtn;
    private ImageButton backBtn;
    private ImageButton saveBtn;
    private ImageButton instaBtn;
    private ImageButton smsBtn;
    private ImageButton emailBtn;
    private ImageButton moreBtn;
    private String TAG = ShareActivity.class.getName();
    private boolean isSaved;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        toolbar = findViewById(R.id.toolbar);
        initToolbar();
        changeStatusBarColor();
        findAllViews();
        initButtons();
        initPlayer();
    }

    private void initButtons() {
        playBtn.setTag("0");
        playBtn.setOnClickListener(v -> {
            if (playBtn.getTag().equals("1")) {
                pausePlayer();
            } else {
                resumePlayer();
            }
        });

        backBtn.setOnClickListener(v -> {
            pausePlayer();
            releasePlayer();
            if(!isSaved) StorageUtils.getInstance().deleteVideo(getIntent().getStringExtra("path"));
            finish();
        });

        saveBtn.setOnClickListener(v -> saveVideo());
        instaBtn.setOnClickListener(v -> shareVideo("com.instagram.android"));
        smsBtn.setOnClickListener(v -> shareVideo(Telephony.Sms.getDefaultSmsPackage(getBaseContext())));
        emailBtn.setOnClickListener(v -> shareVideo("com.google.android.gm"));
        moreBtn.setOnClickListener(v -> shareVideo(""));
    }

    private void saveVideo() {

        if(isSaved){
            new DialogUtils().createWarningDialog(this, null, R.string.already_saved, null, null, null);
            return;
        }

        isSaved = true;
        String outVideoPath = getIntent().getStringExtra("path");
        exposedVideoToMediaStore(outVideoPath);
        Toast.makeText(this, "Saved to photo album", Toast.LENGTH_SHORT).show();

    }

    private void shareVideo(String packageName) {

        new MediaScannerConnection.MediaScannerConnectionClient() {
            private MediaScannerConnection msc;

            {
                msc = new MediaScannerConnection(getApplicationContext(), this);
                msc.connect();
            }

            public void onMediaScannerConnected() {
                msc.scanFile(getIntent().getStringExtra("path"), null);
            }

            public void onScanCompleted(String path, Uri uri) {
                msc.disconnect();
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                if(!packageName.equals("")) sendIntent.setPackage(packageName);
                sendIntent.setType("video/*");
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Video created by Video Crop app");
                sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
                sendIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Enjoy the Video");
                startActivity(Intent.createChooser(sendIntent, "Share your video"));
            }
        };
    }

    private void findAllViews() {
        videoPreview = findViewById(R.id.video_view);
        playBtn = findViewById(R.id.playBtn);
        backBtn = findViewById(R.id.backBtn);
        saveBtn = findViewById(R.id.saveBtn);
        instaBtn = findViewById(R.id.instaBtn);
        smsBtn = findViewById(R.id.smsBtn);
        emailBtn = findViewById(R.id.emailBtn);
        moreBtn = findViewById(R.id.moreBtn);
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


    private void initPlayer() {
        videoPreview.adjustWidthHeight(VideoInfo.getInstance().getCroppedWidth(), VideoInfo.getInstance().getCroppedHeight(), VideoInfo.getInstance().getRotation());
        videoPreview.setSimpleExoPlayer(ExoPlayerFactory.newSimpleInstance(this));
        videoPreview.setVisibility(View.VISIBLE);
        setMediaSourceAndPrepare();
        videoPreview.getPlayer().setSeekParameters(SeekParameters.EXACT);
        videoPreview.getPlayer().addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (isPreparing && playbackState == ExoPlayer.STATE_READY) {
                    isPreparing = false;
                }

                if (playbackState == ExoPlayer.STATE_ENDED) {
                    videoPreview.getPlayer().seekTo(0);
                    pausePlayer();
                }
            }
        });
    }

    private void exposedVideoToMediaStore(String videoPath){
        MediaScannerConnection.scanFile(
                this, new String[] { videoPath }, null, null);
                //(path, uri) -> Log.d(TAG, "Finished scanning " + path + " New row: " + uri));
    }

    private void setMediaSourceAndPrepare() {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "videoediting"));
        MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(getIntent().getStringExtra("path")));
        isPreparing = true;
        videoPreview.getPlayer().prepare(videoSource);
    }

    protected void pausePlayer() {
        if (videoPreview == null) return;
        videoPreview.getPlayer().setPlayWhenReady(false);
        playBtn.setImageResource(R.drawable.ic_play_trans);
        playBtn.setTag("0");
    }

    private void resumePlayer() {
        videoPreview.getPlayer().setPlayWhenReady(true);
        playBtn.setImageResource(R.drawable.ic_play_full_trans);
        playBtn.setTag("1");
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
        if(!isSaved) StorageUtils.getInstance().deleteVideo(getIntent().getStringExtra("path"));
        super.onDestroy();
    }
}
