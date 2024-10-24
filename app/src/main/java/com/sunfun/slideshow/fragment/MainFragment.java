package com.sunfun.slideshow.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.daasuu.mp4compose.filter.GlFilter;
import com.daasuu.mp4compose.filter.GlFilterGroup;
import com.daasuu.mp4compose.filter.GlTransformFilter;
import com.daasuu.mp4compose.filter.Overlay.GlVideoOverlayFilter;
import com.daasuu.mp4compose.player.GPUPlayerView;
import com.daasuu.mp4compose.player.PlayerScaleType;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SeekParameters;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.sunfun.slideshow.MainActivity;
import com.sunfun.slideshow.R;
import com.sunfun.slideshow.ShareActivity;
import com.sunfun.slideshow.TutorialActivity;
import com.sunfun.slideshow.interfaces.OnAnimationListener;
import com.sunfun.slideshow.interfaces.OnVideoEditFinishListener;
import com.sunfun.slideshow.tools.AudioRecorder;
import com.sunfun.slideshow.tools.VideoEditor;
import com.sunfun.slideshow.utils.AudioExoPlayerHolder;
import com.sunfun.slideshow.utils.DialogUtils;
import com.sunfun.slideshow.utils.ExoPlayerErrorHandler;
import com.sunfun.slideshow.utils.ExoPlayerHolder;
import com.sunfun.slideshow.utils.OnOneOffClickListener;
import com.sunfun.slideshow.utils.PathUtils;
import com.sunfun.slideshow.utils.Prefs;
import com.sunfun.slideshow.utils.AnimationUtils;
import com.sunfun.slideshow.utils.VideoInfo;
import com.sunfun.slideshow.utils.VideoUtils;
import com.sunfun.slideshow.videopicker.VideoPickerActivity;
import com.sunfun.slideshow.view.dialog.IOSDialog;
import com.sunfun.slideshow.view.scrubber.VideoScrubBar;
import com.sunfun.slideshow.viewmodel.MainViewModel;
import com.timqi.sectorprogressview.SectorProgressView;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MainFragment extends Fragment {

    static final int CROP_REQUEST = 1002;
    static final int TRIM_CUT_REQUEST = 1000;
    static final int ADD_MUSIC_REQUEST = 1010;
    static final int AUDIO_PICKER_REQUEST = 1366;
    private static final int FLIP_ROTATE_REQUEST = 1001;
    private static final String TAG = MainFragment.class.getName();

    Fragment activeFragment;
    GPUPlayerView videoPreview;
    MusicGalleryFragment musicGalleryFragment;
    CoordinatorLayout fragmentContainer;
    VideoScrubBar videoScrubBar;
    HorizontalScrollView horizontalScrollView;
    AnimationUtils animationUtils;
    ConstraintLayout activePanel;

    protected Toolbar toolbar;
    protected ImageButton playBtn;

    private TrimCutFragment trimCutFragment;
    private FlipRotateFragment flipRotateFragment;
    private Handler mHandler;
    private ImageButton trimCutBtn;
    private MainViewModel mainViewModel;
    private TextView timeText;
    private Runnable updateTask;
    ImageButton backBtn;
    ImageButton refreshBtn;
    private VideoInfo videoInfo;
    ImageButton exportBtn;
    private SectorProgressView progressBar;
    private ImageButton flipRotateBtn;
    private ImageButton cropBtn;
    private CropFragment cropFragment;
    private ImageButton filterBtn;
    private ConstraintLayout filterBtnPanel;
    private ImageButton downBtnFilterPanel;
    private RelativeLayout videoViewContainer;
    private ImageButton tutorialBtn;
    private AddMusic addMusic;

    private boolean isVideoPaused = true;
    private boolean isVideoEdited = false;
    private boolean isPreparing = true;
    private boolean playFirst = false;
    private int cutExportProgress;
    private int cutExportProgress2;
    ImageButton buyBtn;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initToolBar();
        findAllViews();
        initVideoData();
        initSharedPrefs();
        initButtons();
        initViewModel();
        initAddMusicPanel();
        setUpAnimation();
    }

    private void initAddMusicPanel() {
        addMusic = new AddMusic(getActivity(), this);
    }

    private void setUpAnimation() {
        fragmentContainer.post(() -> {
            int totalHeight = fragmentContainer.getHeight() - toolbar.getHeight();
            animationUtils = new AnimationUtils(
                    getContext(),
                    totalHeight,
                    videoViewContainer,
                    videoPreview,
                    playBtn,
                    addMusic.quickVolumeBtn,
                    addMusic.quickLibraryBtn,
                    addMusic.quickStoragePanel);

            animationUtils.setOverScrollAnimation(horizontalScrollView);
            animationUtils.growAnimation(addMusic.quickLibraryBtn);
        });
    }

    private void initSharedPrefs() {
        Prefs prefs = new Prefs(getActivity());
        prefs.setSelectedPath(videoInfo.getVideoPath());

        if (prefs.getMusicJson().equals("")) {
            prefs.setMusicJson(getString(R.string.music_json));
        }
    }

    private void initVideoData() {
        String selectedVideoPath = getActivity().getIntent().getStringExtra("path");
        videoInfo = VideoInfo.getInstance();
        videoInfo.setVideoPath(selectedVideoPath);
    }

    private void initToolBar() {
        toolbar = getView().findViewById(R.id.toolbar);
        ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("");
    }

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    private void initViewModel() {
        if (getActivity() != null)
            mainViewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == AudioRecorder.AUDIO_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                addMusic.recordPermissionGrant();
            } else {
                addMusic.recordPermissionDenied();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AUDIO_PICKER_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = data.getData();
                VideoInfo.getInstance().setExtraAudioPath(PathUtils.getRealPathFromURI(getContext(), uri));
                addMusic.showEditPanelWithPlayerInit();
                return;
            } else return;
        }

        if (resultCode == Activity.RESULT_OK) {
            isVideoEdited = true;
            if (data.hasExtra("status") && data.getStringExtra("status").equals("done")) {
                addMusic.showEditPanelWithPlayerInit();
                isVideoEdited = false;
            }

            if (data.hasExtra("status") && data.getStringExtra("status").equals("back")) {
                isVideoEdited = false;
            }

            updateVideoPreview();
            updatePlayer();
        }

        getActivity().getSupportFragmentManager().beginTransaction().remove(activeFragment);
        trimCutFragment = null;
        cropFragment = null;
        flipRotateFragment = null;
        musicGalleryFragment = null;
    }

    void updatePlayer() {

        if (VideoInfo.getInstance().isTrimmed || VideoInfo.getInstance().isCut) {
            initVideoScrubBar();
        }

        videoScrubBar.isMoved = false;
        videoScrubBar.videoAnimator = null;
        videoScrubBar.thumbBar.setX(0);

        timeText.setText(R.string._00_00);
        timeText.setX(videoScrubBar.thumbBar.getX() + 8);

        videoPreview.getPlayer().addListener(new Player.EventListener() {
            @Override
            public void onSeekProcessed() {
                videoScrubBar.thumbPosition = videoPreview.getPlayer().getCurrentPosition();
                videoPreview.getPlayer().removeListener(this);
            }
        });

        if (videoInfo.isTrimmed) {
            videoPreview.getPlayer().seekTo(videoInfo.getStartMs());
        } else {
            videoPreview.getPlayer().seekTo(0);
        }

        if (isVideoEdited) {
            playBtn.performClick();
            isVideoEdited = false;
        }
    }

    private void resetVideoInfo() {
        videoInfo.resetVideoInfo(new Prefs(getContext()).getSelectedPath());
    }

    private void setMediaSourceAndPrepare() {
        if (getContext() == null) return;
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getContext(), Util.getUserAgent(getContext(), "videoediting"));
        MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(VideoInfo.getInstance().getVideoPath()));
        isPreparing = true;
        videoPreview.getPlayer().prepare(videoSource);
    }

    private void updateVideoInfo() {
        videoInfo.updateVideoInfo(videoPreview.getPlayer().getDuration(), videoPreview.getScaleX(), videoPreview.getScaleY(), videoPreview.getRotation());
    }

    private void initVideoScrubBar() {
        videoScrubBar.setMediaPlayer(videoPreview.getPlayer());

        if (videoInfo.isTrimmed) {
            videoPreview.getPlayer().seekTo((int) videoInfo.getStartMs());
        } else if (videoInfo.isCut) {
            videoPreview.getPlayer().seekTo(0);
        }

        videoScrubBar.videoAnimator = null;
        videoScrubBar.thumbBar.setX(0);
        videoScrubBar.videoFrameContainer.removeAllViews();
        mainViewModel.progressBarImageList = null;

        if (videoInfo.isTrimmed) {
            mainViewModel.getScrubBerBitmap("trim").observe(getActivity(), bitmap -> {
                videoScrubBar.setBitmap(bitmap);
            });
        } else if (videoInfo.isCut) {
            mainViewModel.getScrubBerBitmap("cut").observe(getActivity(), bitmap -> {
                videoScrubBar.setBitmap(bitmap);
            });
        } else {
            mainViewModel.getScrubBerBitmap("").observe(getActivity(), bitmap -> {
                if (videoScrubBar != null) videoScrubBar.setBitmap(bitmap);
            });
        }

        videoScrubBar.setOnSeekBarChangeListener(new VideoScrubBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(long progress, boolean fromUser) {
                if (VideoInfo.getInstance().isCut) {
                    timeText.setText(millisToMinute((long) ((float) progress / (float) VideoInfo.getInstance().getTotalVideoDuration() * VideoInfo.getInstance().getDuration())));
                } else {
                    timeText.setText(millisToMinute(progress));
                }
                if (addMusic != null) addMusic.musicTimeText.setText(timeText.getText());
                timeText.setX(videoScrubBar.thumbBar.getX() + 8);
            }

            @Override
            public void onStartTrackingTouch() {
                startTrackingTouch();
            }

            @Override
            public void onStopTrackingTouch() {
                stopTrackingTouch();
            }

        });

        timeText.setText(calculateCurrentTime());
        timeText.setX(videoScrubBar.thumbBar.getX() + 8);
        if (addMusic != null) addMusic.musicTimeText.setText(timeText.getText());


        if (mHandler == null) mHandler = new Handler();
        updateTask = new Runnable() {
            @Override
            public void run() {
                try {
                    if (videoScrubBar.getProgress() < videoPreview.getPlayer().getDuration() && !isVideoPaused) {
                        mHandler.post(this);
                    }
                    timeText.setText(calculateCurrentTime());
                    timeText.setX(videoScrubBar.thumbBar.getTranslationX() + 8);
                    if (addMusic != null) addMusic.musicTimeText.setText(timeText.getText());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };


        videoScrubBar.setOnAnimationListener(new OnAnimationListener() {
            @Override
            public void animationEnd() {
                playBtn.setImageResource(R.drawable.ic_play_trans);
                playBtn.setTag("0");
                isVideoPaused = true;
                if (videoScrubBar != null) videoScrubBar.videoAnimator = null;
                addMusic.audioScrubBarAdvance.smoothScrollTo(0, 0);
            }

            @Override
            public void animationStart() {

            }
        });

        if (!playFirst) {
            playBtn.performClick();
            playFirst = true;
        }
    }

    private String calculateCurrentTime() {
        String curTime;
        if (VideoInfo.getInstance().isCut) {
            long curTimeMillis = videoPreview.getPlayer().getCurrentPosition();
            if (curTimeMillis >= VideoInfo.getInstance().getEndMs()) {
                curTimeMillis = curTimeMillis - (VideoInfo.getInstance().getEndMs() - VideoInfo.getInstance().getStartMs());
            }
            curTime = millisToMinute(curTimeMillis);
        } else {
            curTime = millisToMinute(videoPreview.getPlayer().getCurrentPosition() - VideoInfo.getInstance().getStartMs());
        }
        return curTime;
    }

    void stopTrackingTouch() {
        pausePlayer();
        playBtn.setTag("0");
        playBtn.setImageResource(R.drawable.ic_play_trans);
        timeText.setX(videoScrubBar.thumbBar.getX() + 8);
        if (addMusic != null) addMusic.musicTimeText.setText(timeText.getText());
        videoPreview.getPlayer().setSeekParameters(SeekParameters.EXACT);
    }

    void startTrackingTouch() {
        pausePlayer();
        playBtn.setTag("0");
        playBtn.setImageResource(R.drawable.ic_play_trans);
        if (videoPreview.getPlayer().getDuration() > 60000) {
            videoPreview.getPlayer().setSeekParameters(SeekParameters.CLOSEST_SYNC);
        }
    }


    private void initButtons() {
        trimCutBtn.setOnClickListener(v -> {
            if (trimCutFragment != null) return;
            resetVideoPreview();
            trimCutFragment = TrimCutFragment.getInstance();
            trimCutFragment.setTargetFragment(MainFragment.this, TRIM_CUT_REQUEST);
            Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.bottom_up, android.R.anim.fade_out, android.R.anim.fade_out, R.anim.bottom_down)
                    .add(fragmentContainer.getId(), trimCutFragment)
                    .addToBackStack("trim_cut_fragment")
                    .commit();
            activeFragment = trimCutFragment;
            pausePlayer();
            animationUtils.setBtnPositionCenter(horizontalScrollView, trimCutBtn, 1);
        });

        flipRotateBtn.setOnClickListener(v -> {
            if (flipRotateFragment != null) return;
            resetVideoPreview();
            flipRotateFragment = FlipRotateFragment.getInstance();
            flipRotateFragment.setTargetFragment(MainFragment.this, FLIP_ROTATE_REQUEST);
            Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.bottom_up, android.R.anim.fade_out, android.R.anim.fade_out, R.anim.bottom_down)
                    .add(fragmentContainer.getId(), flipRotateFragment)
                    .addToBackStack("flip_rotate_fragment")
                    .commit();
            activeFragment = flipRotateFragment;
            pausePlayer();
            animationUtils.setBtnPositionCenter(horizontalScrollView, flipRotateBtn, 2);
        });

        cropBtn.setOnClickListener(v -> {
            if (cropFragment != null) return;
            resetVideoPreview();
            cropFragment = CropFragment.getInstance();
            cropFragment.setTargetFragment(MainFragment.this, CROP_REQUEST);
            Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.bottom_up, android.R.anim.fade_out, android.R.anim.fade_out, R.anim.bottom_down)
                    .add(fragmentContainer.getId(), cropFragment)
                    .addToBackStack("flip_rotate_fragment")
                    .commit();
            activeFragment = cropFragment;
            pausePlayer();
            animationUtils.setBtnPositionCenter(horizontalScrollView, cropBtn, 0);
        });

        filterBtn.setOnClickListener(v -> {
            /*float totalHeight = fragmentContainer.getHeight() - toolbar.getHeight();
            activePanel = filterBtnPanel;
            animationUtils.slideUp(totalHeight, filterBtnPanel, videoViewContainer, videoPreview, playBtn);*/
            animationUtils.setBtnPositionCenter(horizontalScrollView, filterBtn, 3);
        });
//        downBtnFilterPanel.setOnClickListener(v -> animationUtils.slideDown(filterBtnPanel, videoViewContainer, videoPreview, playBtn, addMusic.quickVolumeBtn, addMusic.quickLibraryBtn, addMusic.quickStoragePanel));

        playBtn.setTag("0");
        playBtn.setOnClickListener(v -> {
            if (isPreparing) {
                return;
            }
            if (playBtn.getTag().equals("1")) {
                pausePlayer();
            } else {
                resumePlayer();
            }
        });

        backBtn.setOnClickListener(new OnOneOffClickListener() {
            @Override
            public void onSingleClick(View v) {

                if (animationUtils.isSlideUp) {
                    if (activePanel != null) animationUtils.slideDown(activePanel);
                    return;
                }

                new DialogUtils().createWarningDialog(getContext(), "warning!", R.string.warning_message, "YES", "NO", getIOSDialogBackListener());
            }
        });


        refreshBtn.setOnClickListener(new OnOneOffClickListener() {
            @Override
            public void onSingleClick(View v) {
                new DialogUtils().createWarningDialog(getContext(), "warning!", R.string.warning_message, "OK", "Cancel",
                        iosDialog -> {
                            addMusic.resetAudioPanel();
                            resetAudioInfo();
                            pausePlayer();
                            iosDialog.dismiss();
                            resetVideoInfo();
                            resetVideoPreview();
                            updateVideoInfo();
                            updatePlayer();
                            updateVideoPreview();
                        }
                );
            }
        });

        exportBtn.setOnClickListener(v -> {
            if (playBtn.getTag().equals("1")) {
                pausePlayer();
            }
            if (videoInfo == null) {
                Toast.makeText(getActivity(), "There is nothing to export", Toast.LENGTH_SHORT).show();
                return;
            }
            if (videoInfo.isCut) {
                if (videoInfo.startMs == 0) {
                    videoInfo.startMs = videoInfo.endMs;
                    videoInfo.endMs = videoInfo.totalVideoDuration;
                    trimVideo();
                } else if (videoInfo.endMs == videoInfo.totalVideoDuration) {
                    videoInfo.endMs = videoInfo.startMs;
                    videoInfo.startMs = 0;
                    trimVideo();
                } else {
                    cutVideo();
                }
            } else {
                trimVideo();
            }
        });

        tutorialBtn.setOnClickListener(v -> {
            resetVideoPreview();
            pausePlayer();
            startActivity(new Intent(getActivity(), TutorialActivity.class));
            animationUtils.setBtnPositionCenter(horizontalScrollView, trimCutBtn, 6);
        });
    }

    private void resetAll() {
        VideoInfo.setInstanceNull();
        resetAudioInfo();
        releasePlayer();
        resetVideoPreview();
        startVideoPickerActivity();
    }

    private void startVideoPickerActivity() {
        Intent intent = new Intent(getActivity(), VideoPickerActivity.class);
        intent.putExtra("video_id", getActivity().getIntent().getIntExtra("video_id", -1));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        if (getActivity() != null)
            getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.fade_out);
    }

    private void resetAudioInfo() {
        if (addMusic == null) return;
        AudioExoPlayerHolder.removeInstance();
        VideoInfo.getInstance().setExtraAudioPath(null);
    }

    void resetVideoPreview() {
        GlFilter.resetFilter();
    }

    private void trimVideo() {
        final VideoEditor videoEditor = new VideoEditor(getActivity());
        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View progressDialogView = factory.inflate(R.layout.video_cutting_progress_dialog, null);
        final AlertDialog progressDialog = new AlertDialog.Builder(Objects.requireNonNull(getActivity()), R.style.AlertDialogTheme).create();
        progressDialog.setCancelable(false);
        progressDialog.setView(progressDialogView);
        progressDialogView.findViewById(R.id.cancelButton).setOnClickListener(v -> {
            videoEditor.stopProcess();
            progressDialog.dismiss();
        });
        progressBar = progressDialogView.findViewById(R.id.progressBar);
        progressDialog.show();

        videoEditor.setOnFinishListener(new OnVideoEditFinishListener() {

            @Override
            public void onProgress(final double progress) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    if (progressBar != null) {
                        progressBar.setStartAngle(0);
                        progressBar.setPercent((int) (progress * 100));
                        ((TextView) progressDialogView.findViewById(R.id.percentText)).setText((int) (progress * 100) + "%");
                    }
                });
            }

            @Override
            public void onFinishEdit(final String path) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    progressDialog.dismiss();
                    GlFilter.resetFilter();
                    startShareActivity(path);
                });
            }

            @Override
            public void onFailEdit(final String msg) {
                getActivity().runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(), "Video export error : " + msg, Toast.LENGTH_LONG).show();
                });
            }
        });
        videoEditor.executeTrimVideoCommand(VideoInfo.getInstance().getStartMs(), VideoInfo.getInstance().getEndMs());
    }

    private void startShareActivity(String path) {
        if (getActivity() == null) return;
        Intent shareIntent = new Intent(getActivity(), ShareActivity.class);
        shareIntent.putExtra("path", path);
        getActivity().startActivity(shareIntent);
    }

    private void cutVideo() {
        final VideoEditor videoEditor = new VideoEditor(getActivity());
        final String[] paths = {"", ""};

        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View progressDialogView = factory.inflate(R.layout.video_cutting_progress_dialog, null);
        final AlertDialog progressDialog = new AlertDialog.Builder(Objects.requireNonNull(getActivity()), R.style.AlertDialogTheme).create();
        progressDialog.setCancelable(true);
        progressDialog.setView(progressDialogView);
        progressDialogView.findViewById(R.id.cancelButton).setOnClickListener(v -> {
            videoEditor.stopProcess();
            progressDialog.dismiss();
        });

        progressBar = progressDialogView.findViewById(R.id.progressBar);
        progressBar.setStartAngle(0);
        progressDialog.show();

        final OnVideoEditFinishListener concatListener = new OnVideoEditFinishListener() {
            @Override
            public void onProgress(double progress) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    if (progressBar != null) {
                        int percent = (int) ((progress + cutExportProgress2 / 100f) * 100);
                        progressBar.setPercent(percent);
                        ((TextView) progressDialogView.findViewById(R.id.percentText)).setText(percent + "%");
                    }
                });
            }

            @Override
            public void onFinishEdit(final String path) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    progressDialog.dismiss();
                    GlFilter.resetFilter();
                    startShareActivity(path);
                });
            }

            @Override
            public void onFailEdit(String msg) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(), "Video export error : " + msg, Toast.LENGTH_LONG).show();
                });

            }
        };

        final int percentPartTwo = (int) (((float) (VideoInfo.getInstance().getTotalVideoDuration() - VideoInfo.getInstance().getEndMs()) / (float) VideoInfo.getInstance().getDuration()) * 100);
        final OnVideoEditFinishListener cutPart2Listener = new OnVideoEditFinishListener() {
            @Override
            public void onProgress(final double progress) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    if (progressBar != null) {
                        int percent = (int) ((progress * percentPartTwo) + cutExportProgress);
                        progressBar.setPercent(percent);
                        ((TextView) progressDialogView.findViewById(R.id.percentText)).setText(percent + "%");
                        cutExportProgress2 = percent;
                    }
                });
            }

            @Override
            public void onFinishEdit(final String path) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    if (path != null && !path.equals("")) {
                        paths[1] = path;
                    }
                    VideoEditor videoEditor1 = new VideoEditor(getActivity());
                    videoEditor1.concatenate(paths);
                    videoEditor1.setOnFinishListener(concatListener);
                });
            }

            @Override
            public void onFailEdit(final String msg) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(), "Video export error : " + msg, Toast.LENGTH_LONG).show();
                });
            }
        };

        final int percentPartOne = (int) ((VideoInfo.getInstance().getStartMs() / (float) VideoInfo.getInstance().getDuration()) * 100);
        OnVideoEditFinishListener cutPart1Listener = new OnVideoEditFinishListener() {
            @Override
            public void onProgress(final double progress) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    if (progressBar != null) {
                        progressBar.setStartAngle(0);
                        cutExportProgress = (int) (percentPartOne * progress);
                        progressBar.setPercent(cutExportProgress);
                        ((TextView) progressDialogView.findViewById(R.id.percentText)).setText(cutExportProgress + "%");
                    }
                });

            }

            @Override
            public void onFinishEdit(final String path) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    if (path != null || !path.equals("")) {
                        paths[0] = path;
                    }
                    VideoEditor videoEditor12 = new VideoEditor(getActivity());
                    videoEditor12.executeTrimVideoCommand(VideoInfo.getInstance().getEndMs(), VideoInfo.getInstance().getTotalVideoDuration());
                    videoEditor12.setOnFinishListener(cutPart2Listener);
                });
            }

            @Override
            public void onFailEdit(final String msg) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(), "Video export error : " + msg, Toast.LENGTH_LONG).show();
                });

            }
        };
        videoEditor.setOnFinishListener(cutPart1Listener);
        videoEditor.executeTrimVideoCommand(0, VideoInfo.getInstance().getStartMs());
    }

    private void findAllViews() {
        videoPreview = Objects.requireNonNull(getView()).findViewById(R.id.video_view);
        trimCutBtn = getView().findViewById(R.id.trimCutBtn);
        fragmentContainer = getView().findViewById(R.id.fragmentContainer);
        timeText = getView().findViewById(R.id.timeText);
        playBtn = getView().findViewById(R.id.playBtn);
        backBtn = getView().findViewById(R.id.backBtn);
        refreshBtn = getView().findViewById(R.id.refreshBtn);
        videoScrubBar = getView().findViewById(R.id.videoProgressBar);
        exportBtn = getView().findViewById(R.id.exportBtn);
        flipRotateBtn = getView().findViewById(R.id.flipRotateBtn);
        cropBtn = getView().findViewById(R.id.cropBtn);
        filterBtn = getView().findViewById(R.id.filterBtn);
        filterBtnPanel = getView().findViewById(R.id.filterBtnPanel);
        videoViewContainer = getView().findViewById(R.id.videoViewContainer);
        downBtnFilterPanel = getView().findViewById(R.id.downBtnFilterPanel);
        horizontalScrollView = getView().findViewById(R.id.horizontalScrollView);
        tutorialBtn = getView().findViewById(R.id.tutorialBtn);
        buyBtn = getView().findViewById(R.id.buyBtn);
    }

    private String millisToMinute(long currentPosition) {
        @SuppressLint("DefaultLocale") String time = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(currentPosition),
                TimeUnit.MILLISECONDS.toSeconds(currentPosition) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(currentPosition)));
        return time;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseRecorder();
        releasePlayer();
    }

    private void releaseRecorder() {
        if (addMusic != null) addMusic.releaseRecorder();
    }

    @Override
    public void onPause() {
        super.onPause();
        pausePlayer();
    }

    @Override
    public void onStart() {
        super.onStart();
        initPlayer();
    }

    private void initPlayer() {
        if (getContext() == null) return;
        videoPreview.adjustWidthHeight(VideoInfo.getInstance().getWidth(), VideoInfo.getInstance().getHeight(), VideoInfo.getInstance().getRotation());
        videoPreview.setSimpleExoPlayer(ExoPlayerFactory.newSimpleInstance(getContext()));
        videoPreview.setVisibility(View.VISIBLE);
        videoPreview.getPlayer().setVolume(VideoInfo.getInstance().getAudioVolume());
        videoPreview.getPlayer().setSeekParameters(SeekParameters.EXACT);


        final int[] x = {0};
        final int[] y = {0};

        GlVideoOverlayFilter videoOverlayFilter = new GlVideoOverlayFilter(getActivity(), R.raw.video_petals);

        /*GlOverlayFilter glOverlayFilter = new GlOverlayFilter() {
            @Override
            protected void drawCanvas(Canvas canvas) {
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.email);
                Paint paint = new Paint();
                paint.setAntiAlias(true);
                paint.setFilterBitmap(true);
                paint.setDither(true);
                Bitmap finalBitmap = bitmap;
                canvas.drawBitmap(finalBitmap, x[0] += 1, y[0] += 1, paint);
                releaseBitmap(bitmap);

            }

        };*/

        GlFilterGroup glFilterGroup = new GlFilterGroup(videoOverlayFilter);

        videoPreview.setGlFilter(glFilterGroup);

        initExoPlayerListener();
        setMediaSourceAndPrepare();
    }

    private void initExoPlayerListener() {
        videoPreview.getPlayer().addListener(new Player.EventListener() {
            @Override
            public void onPlayerError(ExoPlaybackException error) {
                new ExoPlayerErrorHandler(getContext(), getIOSDialogBackListener()).handle(error);
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (isPreparing && playbackState == ExoPlayer.STATE_READY) {
                    isPreparing = false;
                    updateVideoInfo();
                    initVideoScrubBar();
                    Log.d(TAG, " rotation " + VideoInfo.getInstance().getRotation() + " " + VideoUtils.getVideoRotation());
                }

                if (!videoPreview.getPlayer().getPlayWhenReady()) {
                    if (addMusic != null) addMusic.pausePlayer();
                }
            }
        });
    }

    private IOSDialog.Listener getIOSDialogBackListener() {
        return iosDialog -> {
            iosDialog.dismiss();
            new Handler().postDelayed(this::resetAll, AnimationUtils.ANIMATION_DURATION / 2);
        };
    }

    private void updateVideoPreview() {
        GlTransformFilter glFiler = new GlTransformFilter();
        glFiler.setRotateInAngle(0);
        glFiler.setTranslateOffset(VideoInfo.getInstance().getLeft() / VideoInfo.getInstance().getWidth(), (VideoInfo.getInstance().getTop()) / VideoInfo.getInstance().getHeight());
        float scaleX = VideoInfo.getInstance().getCroppedWidth() / VideoInfo.getInstance().getWidth();
        float scaleY = VideoInfo.getInstance().getCroppedHeight() / VideoInfo.getInstance().getHeight();
        if (VideoInfo.getInstance().isFlippedV) {
            scaleX = scaleX * -1;
        }
        if (VideoInfo.getInstance().isFlippedH) {
            scaleY = scaleY * -1;
        }
        glFiler.setScaleUnit(scaleX, scaleY);
        glFiler.setRotateInAngle(VideoInfo.getInstance().getRotation());
        videoPreview.setGlFilter(glFiler);

        if (VideoInfo.getInstance().getCroppedWidth() > VideoInfo.getInstance().getCroppedHeight()) {
            if (Math.abs(VideoInfo.getInstance().getRotation()) == 0 || Math.abs(VideoInfo.getInstance().getRotation()) == 180) {
                videoPreview.playerScaleType = PlayerScaleType.RESIZE_FIT_WIDTH;
                videoPreview.videoAspect = VideoInfo.getInstance().getCroppedWidth() / VideoInfo.getInstance().getCroppedHeight();
            } else {
                videoPreview.playerScaleType = PlayerScaleType.RESIZE_FIT_HEIGHT;
                videoPreview.videoAspect = VideoInfo.getInstance().getCroppedHeight() / VideoInfo.getInstance().getCroppedWidth();
            }
        } else if (VideoInfo.getInstance().getCroppedWidth() < VideoInfo.getInstance().getCroppedHeight()) {
            if (Math.abs(VideoInfo.getInstance().getRotation()) == 0 || Math.abs(VideoInfo.getInstance().getRotation()) == 180) {
                videoPreview.playerScaleType = PlayerScaleType.RESIZE_FIT_HEIGHT;
                videoPreview.videoAspect = VideoInfo.getInstance().getCroppedWidth() / VideoInfo.getInstance().getCroppedHeight();
            } else {
                videoPreview.playerScaleType = PlayerScaleType.RESIZE_FIT_WIDTH;
                videoPreview.videoAspect = VideoInfo.getInstance().getCroppedHeight() / VideoInfo.getInstance().getCroppedWidth();
            }
        } else {
            videoPreview.playerScaleType = PlayerScaleType.RESIZE_FIT_WIDTH;
            videoPreview.videoAspect = 1;
        }
        videoPreview.requestLayout();
    }

    void pausePlayer() {
        playBtn.setImageResource(R.drawable.ic_play_trans);
        isVideoPaused = true;
        try {
            videoScrubBar.stopAnim();
            playBtn.setTag("0");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void resumePlayer() {
        isVideoPaused = false;
        if (videoScrubBar != null) videoScrubBar.startAnimation();
        if (addMusic != null) {
            addMusic.resumePlayer();
        }
        playBtn.setImageResource(R.drawable.ic_play_full_trans);
        playBtn.setTag("1");
        new Handler().post(updateTask);
    }

    private void releasePlayer() {
        if (videoPreview != null) {
            videoScrubBar = null;
            videoPreview.getPlayer().release();
            ExoPlayerHolder.removeInstance();
            videoPreview = null;
            if (addMusic != null) addMusic.releasePlayer();
        }
    }

    public void onBackPressed() {
        if (activeFragment == null) return;
        if (activeFragment == trimCutFragment) ((TrimCutFragment) activeFragment).onBackPressed();
        else if (activeFragment == cropFragment) ((CropFragment) activeFragment).onBackPressed();
        else if (activeFragment == flipRotateFragment)
            ((FlipRotateFragment) activeFragment).onBackPressed();
        else if (activeFragment == musicGalleryFragment)
            ((MusicGalleryFragment) activeFragment).onBackPressed();
    }

}
