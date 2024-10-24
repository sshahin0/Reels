package com.sunfun.slideshow.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SeekParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.sunfun.slideshow.MainActivity;
import com.sunfun.slideshow.R;
import com.sunfun.slideshow.interfaces.OnProgressChangeListenerAdvance;
import com.sunfun.slideshow.tools.AudioRecorder;
import com.sunfun.slideshow.utils.AnimationUtils;
import com.sunfun.slideshow.utils.AudioExoPlayerHolder;
import com.sunfun.slideshow.utils.AudioUtils;
import com.sunfun.slideshow.utils.DialogUtils;
import com.sunfun.slideshow.utils.ExoPlayerHolder;
import com.sunfun.slideshow.utils.OnOneOffClickListener;
import com.sunfun.slideshow.utils.PathUtils;
import com.sunfun.slideshow.utils.VideoInfo;
import com.sunfun.slideshow.view.scrubber.AudioScrubBarAdvance;

class AddMusic {
    private final Activity activity;
    private static final String TAG = AddMusic.class.getName();

    private ConstraintLayout editMusicPanel;
    private ConstraintLayout addMusicBtnPanel;
    private ImageButton musicGalleryBtn;
    private ImageButton recorderPanelBtn;
    private ImageButton recordBtn;
    private AudioRecorder recorder;
    private ImageButton storageBtn;

    private ImageButton downBtnMusicPanel;
    private ConstraintLayout recorderPanel;

    private Handler mHandler;
    private Runnable updateTask;

    private ImageButton downBtnEditMusicPanel;
    private TextView storageBtnEditMusic;
    private TextView libraryBtnEditMusic;
    private TextView recorderBtnEditMusic;
    private TextView editBtn;
    private MainFragment mainFragment;
    private SimpleExoPlayer simpleExoPlayer;
    private TextView musicTitleText;
    private TextView deleteBtn;
    private ImageButton volumeBtn;
    private ConstraintLayout volumePanel;
    private ImageButton downBtnVolumePanel;
    private SeekBar volumeSeekBar;
    private ImageButton musicPlayBtn;
    private TextView quickStorageText;
    private ImageButton addMusicBtn;

    TextView musicTimeText;
    AudioScrubBarAdvance audioScrubBarAdvance;
    ImageButton quickVolumeBtn;
    ImageButton quickLibraryBtn;
    ConstraintLayout quickStoragePanel;

    private boolean isPreparing = true;
    private boolean isAudioPause;
    private long fadeTimeMillis = 2000;
    private long fadeStartTimeInMillis;
    private long fadeEndTimeInMillis;
    private EditMusicFragment editMusicFragment;
    private Handler recordHandler;
    private TextView recordingText;
    private ImageView zeroVolumeBtn;
    private ImageView fullVolumeBtn;


    AddMusic(Activity activity, MainFragment fragment) {
        this.activity = activity;
        this.mainFragment = fragment;
        findAllViews();
        generalInitialization();
        initButtons();
        initVolumePanel();
    }

    private void generalInitialization() {
        recordHandler = new Handler();
    }

    private void initVolumePanel() {
        volumeSeekBar.setProgress((int) (VideoInfo.getInstance().getAudioVolume() * 100));
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setAudioVolume((1f / 100f) * progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void initPlayer() {
        if (VideoInfo.getInstance().getExtraAudioPath() == null) {
            return;
        }
        VideoInfo.getInstance().setExtraAudioEnd(AudioUtils.getAudioDuration());
        VideoInfo.getInstance().setExtraAudioDuration(AudioUtils.getAudioDuration());
        if (simpleExoPlayer != null) AudioExoPlayerHolder.removeInstance();
        simpleExoPlayer = AudioExoPlayerHolder.getInstance(activity);
        setMediaSourceAndPrepare();
        simpleExoPlayer.setSeekParameters(SeekParameters.EXACT);
        simpleExoPlayer.addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (isPreparing && playbackState == ExoPlayer.STATE_READY) {
                    isPreparing = false;
                    initVideoScrubber();
                }

                if (playbackState == ExoPlayer.STATE_ENDED) {
                    simpleExoPlayer.seekTo(VideoInfo.getInstance().getExtraAudioStart());
                }
            }
        });

        if (mHandler == null) mHandler = new Handler();
        updateTask = new Runnable() {
            @Override
            public void run() {
                try {

                    audioScrubBarAdvance.moveScrollBar();

                    if (mainFragment.videoPreview.getPlayer().getCurrentPosition() < VideoInfo.getInstance().getEndMs() && !isAudioPause) {
                        mHandler.post(this);
                    } else if (VideoInfo.getInstance().isCut && mainFragment.videoPreview.getPlayer().getCurrentPosition() < VideoInfo.getInstance().getTotalVideoDuration() && !isAudioPause) {
                        mHandler.post(this);
                    } else {
                        isAudioPause = true;
                    }

                    if (VideoInfo.getInstance().isFadeIn()) {
                        fadeIn();
                    }
                    if (VideoInfo.getInstance().isFadeOut()) {
                        fadeOut();
                    }

                    if (simpleExoPlayer.getCurrentPosition() >= VideoInfo.getInstance().getExtraAudioEnd() && !isAudioPause) {
                        simpleExoPlayer.seekTo(VideoInfo.getInstance().getExtraAudioStart());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
    }

    private void fadeOut() {
        long fadeOutMillisEnd = VideoInfo.getInstance().getEndMs() - fadeTimeMillis;

        if (VideoInfo.getInstance().isCut) {
            fadeOutMillisEnd = VideoInfo.getInstance().getTotalVideoDuration() - fadeTimeMillis;
        }

        if (mainFragment.videoPreview.getPlayer().getCurrentPosition() > fadeOutMillisEnd) {
            float volume = 1f - ((System.currentTimeMillis() - fadeEndTimeInMillis) / (float) (fadeTimeMillis));
            volume = volume * volume;
            mainFragment.videoPreview.getPlayer().setVolume(volume > VideoInfo.getInstance().getAudioVolume() ? VideoInfo.getInstance().getAudioVolume() : volume);
            simpleExoPlayer.setVolume(volume > VideoInfo.getInstance().getExtraAudioVolume() ? VideoInfo.getInstance().getExtraAudioVolume() : volume);
        } else if (mainFragment.videoPreview.getPlayer().getCurrentPosition() > fadeTimeMillis) {
            fadeEndTimeInMillis = System.currentTimeMillis();
            mainFragment.videoPreview.getPlayer().setVolume(VideoInfo.getInstance().getAudioVolume());
            simpleExoPlayer.setVolume(VideoInfo.getInstance().getExtraAudioVolume());
        }
    }

    private void fadeIn() {
        long fadeInMillisStart = fadeTimeMillis;

        if (VideoInfo.getInstance().isCut) {
            fadeInMillisStart = VideoInfo.getInstance().getStartMs() + fadeTimeMillis;
        }

        if (mainFragment.videoPreview.getPlayer().getCurrentPosition() < fadeInMillisStart) {
            float volume = ((System.currentTimeMillis() - fadeStartTimeInMillis) / (float) fadeTimeMillis);
            volume = volume * volume;
            mainFragment.videoPreview.getPlayer().setVolume(volume > VideoInfo.getInstance().getAudioVolume() ? VideoInfo.getInstance().getAudioVolume() : volume);
            simpleExoPlayer.setVolume(volume > VideoInfo.getInstance().getExtraAudioVolume() ? VideoInfo.getInstance().getExtraAudioVolume() : volume);
        }

    }


    private void setMediaSourceAndPrepare() {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(activity, Util.getUserAgent(activity, "videoediting"));
        MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(VideoInfo.getInstance().getExtraAudioPath()));
        isPreparing = true;
        simpleExoPlayer.prepare(videoSource);
    }

    private void initButtons() {

        zeroVolumeBtn.setOnClickListener(v -> volumeSeekBar.setProgress(0));
        fullVolumeBtn.setOnClickListener(v -> volumeSeekBar.setProgress(100));

        addMusicBtn.setOnClickListener(new OnOneOffClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (VideoInfo.getInstance().getExtraAudioPath() != null) {
                    mainFragment.activePanel = editMusicPanel;
                    mainFragment.animationUtils.slideUp(mainFragment.activePanel);
                } else {
                    mainFragment.activePanel = addMusicBtnPanel;
                    mainFragment.animationUtils.slideUp(mainFragment.activePanel);
                }

                if (recorder != null && recorder.isRecording()) {
                    recorderPanel.setVisibility(View.VISIBLE);
                }

                mainFragment.animationUtils.setBtnPositionCenter(mainFragment.horizontalScrollView, addMusicBtn, 4);
            }
        });

        downBtnMusicPanel.setOnClickListener(new OnOneOffClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (recorderPanel.getVisibility() == View.VISIBLE) {
                    mainFragment.animationUtils.slideDown(recorderPanel, true);
                    if (VideoInfo.getInstance().getExtraAudioPath() != null) {
                        new Handler().postDelayed(() -> {
                            addMusicBtnPanel.setVisibility(View.INVISIBLE);
                            addMusicBtnPanel.setTranslationY(addMusicBtnPanel.getHeight());
                            mainFragment.activePanel = editMusicPanel;
                            editMusicPanel.setTranslationY(0);
                        }, AnimationUtils.ANIMATION_DURATION);
                    }
                    return;
                }
                mainFragment.animationUtils.slideDown(mainFragment.activePanel);
                mainFragment.activePanel = null;
            }
        });

        volumeBtn.setOnClickListener(new OnOneOffClickListener() {
            @Override
            public void onSingleClick(View v) {
                mainFragment.activePanel = volumePanel;
                mainFragment.animationUtils.slideUp(mainFragment.activePanel);
                mainFragment.animationUtils.setBtnPositionCenter(mainFragment.horizontalScrollView, addMusicBtn, 5);
            }
        });

        downBtnVolumePanel.setOnClickListener(new OnOneOffClickListener() {
            @Override
            public void onSingleClick(View v) {
                mainFragment.animationUtils.slideDown(mainFragment.activePanel);
                mainFragment.activePanel = null;
            }
        });

        quickVolumeBtn.setOnClickListener(new OnOneOffClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (mainFragment.activePanel == volumePanel) {
                    return;
                }
                mainFragment.animationUtils.setOnAnimationFinishListener(() -> {
                    mainFragment.animationUtils.removeListener();
                    mainFragment.activePanel = volumePanel;
                    mainFragment.animationUtils.slideUp(mainFragment.activePanel);
                });

                if (recorderPanel.getVisibility() == View.VISIBLE) {
                    recorderPanel.setVisibility(View.INVISIBLE);
                }
                mainFragment.animationUtils.slideDown(mainFragment.activePanel);
            }
        });

        quickLibraryBtn.setOnClickListener(new OnOneOffClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (mainFragment.playBtn.getTag().equals("1")) mainFragment.playBtn.performClick();
                if (mainFragment.activePanel == addMusicBtnPanel) {
                    startGalleryFragment();
                    return;
                }
                mainFragment.animationUtils.setOnAnimationFinishListener(() -> {
                    mainFragment.animationUtils.removeListener();
                    mainFragment.activePanel = addMusicBtnPanel;
                    mainFragment.animationUtils.slideUp(mainFragment.activePanel);
                    startGalleryFragment();
                });
                mainFragment.animationUtils.slideDown(mainFragment.activePanel);
            }
        });

        recorderPanelBtn.setOnClickListener(view -> mainFragment.animationUtils.slideUp(recorderPanel, true));

        downBtnEditMusicPanel.setOnClickListener(v -> {
            mainFragment.activePanel = editMusicPanel;
            mainFragment.animationUtils.slideDown(editMusicPanel);
        });

        recordBtn.setOnClickListener(new OnOneOffClickListener() {
            @Override
            public void onSingleClick(View v) {

                if (recorder == null) {
                    recorder = new AudioRecorder(activity, mainFragment);
                }

                if(!recorder.hasPermission()){
                    return;
                }

                if (recorder.isRecording()) {
                    mainFragment.animationUtils.stopRoundAnimation(recordBtn);
                    recorder.stopRecording();
                    if (mainFragment.playBtn.getTag().equals("1"))
                        mainFragment.playBtn.performClick();
                    VideoInfo.getInstance().setExtraAudioPath(recorder.getFilePath());
                    showEditPanelWithPlayerInit();
                    mainFragment.videoPreview.getPlayer().setVolume(VideoInfo.getInstance().getAudioVolume());
                    enableButtons();
                    recordBtn.setBackground(activity.getDrawable(R.drawable.button_selector_without_border_round));
                    recordBtn.setImageResource(R.drawable.ic_add_music_record);
                    recordingText.setText(R.string.record_text);
                    recordHandler.removeCallbacksAndMessages(null);
                } else {
                    mainFragment.animationUtils.startRoundAnimation(recordBtn);
                    VideoInfo.getInstance().setExtraAudioPath(null);
                    recorder.prepareRecorderAndStart();
                    mainFragment.pausePlayer();
                    mainFragment.updatePlayer();
                    mainFragment.playBtn.performClick();
                    mainFragment.videoPreview.getPlayer().setVolume(0);
                    disableButtons();
                    recordBtn.setBackground(activity.getDrawable(R.drawable.selected_btn_bg_without_border_round));
                    recordBtn.setImageResource(R.drawable.ic_stop_red);
                    recordingText.setText(R.string.recording);
                    recordHandler.postDelayed(() -> {
                        if (recorder.isRecording()) {
                            recordBtn.performClick();
                        }
                    }, VideoInfo.getInstance().getDuration());
                }
            }
        });

        storageBtn.setOnClickListener(new OnOneOffClickListener() {
            @Override
            public void onSingleClick(View v) {
                startStorageIntent();
            }
        });


        musicGalleryBtn.setOnClickListener(v -> {
            mainFragment.resetVideoPreview();
            startGalleryFragment();
            mainFragment.pausePlayer();
        });


        editBtn.setOnClickListener(new OnOneOffClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (mainFragment.playBtn.getTag().equals("1")) mainFragment.playBtn.performClick();
                editMusicFragment = EditMusicFragment.newInstance();
                editMusicFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.MusicEditDialogTheme);
                editMusicFragment.setCancelable(false);
                editMusicFragment.show(((MainActivity) activity).getSupportFragmentManager(), "fragment_download");
                editMusicFragment.setOnDeleteMusicListener(() -> deleteBtn.performClick());
            }
        });

        deleteBtn.setOnClickListener(new OnOneOffClickListener() {
            @Override
            public void onSingleClick(View v) {
                new DialogUtils().createWarningDialog(activity, "Warning!", R.string.delete_msg, "OK", "Cancel", iosDialog -> {
                    if (editMusicFragment != null && editMusicFragment.isVisible())
                        editMusicFragment.dismiss();
                    iosDialog.dismiss();
                    deleteVideoPath();
                    mainFragment.activePanel = addMusicBtnPanel;
                    mainFragment.animationUtils.slideDown(editMusicPanel, true);
                    mainFragment.animationUtils.slideUp(addMusicBtnPanel, true);
                    quickStoragePanel.setVisibility(View.INVISIBLE);
                    quickLibraryBtn.setVisibility(View.VISIBLE);
                    setAudioVolume(0);
                });
            }
        });


        storageBtnEditMusic.setOnClickListener(v -> {
            if (mainFragment.playBtn.getTag().equals("1")) mainFragment.playBtn.performClick();
            storageBtn.performClick();
        });

        libraryBtnEditMusic.setOnClickListener(v -> {
            if (mainFragment.playBtn.getTag().equals("1")) mainFragment.playBtn.performClick();
            musicGalleryBtn.performClick();
        });

        recorderBtnEditMusic.setOnClickListener(v -> {
            if (mainFragment.playBtn.getTag().equals("1")) mainFragment.playBtn.performClick();
            mainFragment.animationUtils.slideDown(mainFragment.activePanel);
            mainFragment.animationUtils.setOnAnimationFinishListener(() -> {
                mainFragment.animationUtils.removeListener();
                mainFragment.activePanel = addMusicBtnPanel;
                mainFragment.animationUtils.slideUp(mainFragment.activePanel);
                recorderPanelBtn.performClick();
            });
        });

        musicPlayBtn.setOnClickListener(v -> {
            if(isPreparing){
                return;
            }
            mainFragment.playBtn.performClick();
        });
    }

    private void disableButtons() {
        downBtnMusicPanel.setEnabled(false);
        mainFragment.playBtn.setEnabled(false);
        quickVolumeBtn.setEnabled(false);
        mainFragment.exportBtn.setEnabled(false);
        quickLibraryBtn.setEnabled(false);
        mainFragment.buyBtn.setEnabled(false);
        mainFragment.refreshBtn.setEnabled(false);
        mainFragment.backBtn.setEnabled(false);
    }

    private void enableButtons() {
        downBtnMusicPanel.setEnabled(true);
        mainFragment.playBtn.setEnabled(true);
        quickVolumeBtn.setEnabled(true);
        mainFragment.exportBtn.setEnabled(true);
        quickLibraryBtn.setEnabled(true);
        mainFragment.buyBtn.setEnabled(true);
        mainFragment.refreshBtn.setEnabled(true);
        mainFragment.backBtn.setEnabled(true);
    }

    private void startGalleryFragment() {
        mainFragment.musicGalleryFragment = MusicGalleryFragment.getInstance();
        mainFragment.musicGalleryFragment.setTargetFragment(mainFragment, MainFragment.ADD_MUSIC_REQUEST);
        ((MainActivity) activity).getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.bottom_up, android.R.anim.fade_out, android.R.anim.fade_out, R.anim.bottom_down)
                .add(mainFragment.fragmentContainer.getId(), mainFragment.musicGalleryFragment)
                .addToBackStack("music_gallery_fragment")
                .commit();
        mainFragment.activeFragment = mainFragment.musicGalleryFragment;
    }

    private void startStorageIntent() {
        if (mainFragment.playBtn.getTag().equals("1")) mainFragment.playBtn.performClick();
        Intent audioPickerIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        mainFragment.startActivityForResult(audioPickerIntent, MainFragment.AUDIO_PICKER_REQUEST);
    }

    private void deleteVideoPath() {
        if (mainFragment.playBtn.getTag().equals("1")) mainFragment.playBtn.performClick();
        VideoInfo.getInstance().setExtraAudioPath(null);
    }

    private void initVideoScrubber() {
        VideoInfo.getInstance().setExtraAudioStart(0);
        audioScrubBarAdvance.setMediaPlayer(mainFragment.videoPreview.getPlayer());
        audioScrubBarAdvance.setOnProgressChangeListener(new OnProgressChangeListenerAdvance() {
            @Override
            public void onVideoPositionChanged() {
            }

            @Override
            public void onRightMarkerPositionChanged() {
            }

            @Override
            public void onLeftMarkerPositionChanged() {
            }

            @Override
            public void onStartScrolling() {
                mainFragment.startTrackingTouch();
            }

            @Override
            public void onStopScrolling() {
                mainFragment.stopTrackingTouch();
            }

            @Override
            public void onScrollPositionChange() {

            }

            @Override
            public void onScrollPositionChange(long currentVideoPosition) {
                mainFragment.videoScrubBar.setThumbPosition(currentVideoPosition);
                long audioProgress = (mainFragment.videoPreview.getPlayer().getCurrentPosition()) % (VideoInfo.getInstance().getExtraAudioEnd()
                        - VideoInfo.getInstance().getExtraAudioStart()) + VideoInfo.getInstance().getExtraAudioStart();
                simpleExoPlayer.seekTo(audioProgress);
            }
        });
    }

    private void findAllViews() {
        addMusicBtnPanel = mainFragment.getView().findViewById(R.id.adMusicBtnPanel);
        editMusicPanel = mainFragment.getView().findViewById(R.id.editMusicPanel);
        downBtnEditMusicPanel = editMusicPanel.findViewById(R.id.downBtnEditMusicPanel);
        storageBtnEditMusic = editMusicPanel.findViewById(R.id.storageBtn);
        recorderBtnEditMusic = editMusicPanel.findViewById(R.id.recordBtn);
        libraryBtnEditMusic = editMusicPanel.findViewById(R.id.libraryBtn);
        recorderPanel = addMusicBtnPanel.findViewById(R.id.recorderPanel);
        downBtnMusicPanel = addMusicBtnPanel.findViewById(R.id.downBtnMusicPanel);
        musicGalleryBtn = addMusicBtnPanel.findViewById(R.id.musicGalleryBtn);
        recorderPanelBtn = addMusicBtnPanel.findViewById(R.id.recorderBtn);
        recordBtn = addMusicBtnPanel.findViewById(R.id.recordBtn);
        storageBtn = addMusicBtnPanel.findViewById(R.id.storageBtn);
        editBtn = editMusicPanel.findViewById(R.id.editBtn);
        audioScrubBarAdvance = editMusicPanel.findViewById(R.id.audioScrubBarAdvance);
        musicTimeText = editMusicPanel.findViewById(R.id.musicTimeText);
        musicTitleText = editMusicPanel.findViewById(R.id.musicTitle);
        deleteBtn = editMusicPanel.findViewById(R.id.deleteBtn);
        volumeBtn = mainFragment.getView().findViewById(R.id.volumeBtn);
        volumePanel = mainFragment.getView().findViewById(R.id.audioVolumePanel);
        downBtnVolumePanel = volumePanel.findViewById(R.id.downBtnVolumePanel);
        volumeSeekBar = volumePanel.findViewById(R.id.volumeSeekBar);
        quickLibraryBtn = mainFragment.getView().findViewById(R.id.quickLibraryBtn);
        quickVolumeBtn = mainFragment.getView().findViewById(R.id.quickVolumeBtn);
        zeroVolumeBtn = mainFragment.getView().findViewById(R.id.zeroVolumeBtn);
        fullVolumeBtn = mainFragment.getView().findViewById(R.id.fullVolumeBtn);
        musicPlayBtn = editMusicPanel.findViewById(R.id.musicPlayBtn);
        quickStoragePanel = mainFragment.getView().findViewById(R.id.quickStoragePanel);
        quickStorageText = quickStoragePanel.findViewById(R.id.songTitleText);
        addMusicBtn = mainFragment.getView().findViewById(R.id.addMusicBtn);
        recordingText = mainFragment.getView().findViewById(R.id.recordingText);
    }

    void resumePlayer() {
        isAudioPause = false;
        if (VideoInfo.getInstance().getExtraAudioPath() == null) return;
        long audioProgress = (mainFragment.videoPreview.getPlayer().getCurrentPosition()) % (VideoInfo.getInstance().getExtraAudioEnd()
                - VideoInfo.getInstance().getExtraAudioStart()) + VideoInfo.getInstance().getExtraAudioStart();
        simpleExoPlayer.addListener(new Player.EventListener() {
            @Override
            public void onSeekProcessed() {
                simpleExoPlayer.setPlayWhenReady(true);
                audioScrubBarAdvance.moveScrollBar();
                simpleExoPlayer.removeListener(this);
            }
        });
        if (VideoInfo.getInstance().isFadeIn()) simpleExoPlayer.setVolume(0);
        mainFragment.videoPreview.getPlayer().setVolume(VideoInfo.getInstance().getAudioVolume());
        fadeStartTimeInMillis = System.currentTimeMillis();
        fadeEndTimeInMillis = -1;
        simpleExoPlayer.seekTo(audioProgress);
        mHandler.post(updateTask);
        musicPlayBtn.setVisibility(View.INVISIBLE);
    }

    void pausePlayer() {
        isAudioPause = true;
        if (VideoInfo.getInstance().getExtraAudioPath() == null) return;
        simpleExoPlayer.setPlayWhenReady(false);
        musicPlayBtn.setVisibility(View.VISIBLE);
    }

    void releasePlayer() {
        if (simpleExoPlayer != null) {
            simpleExoPlayer.release();
            ExoPlayerHolder.removeInstance();
            simpleExoPlayer = null;
        }
    }

    void showEditPanelWithPlayerInit() {
        musicTitleText.setText(PathUtils.getTitleFromPath(VideoInfo.getInstance().getExtraAudioPath()));
        editMusicPanel.setVisibility(View.VISIBLE);
        editMusicPanel.setTranslationY(0);
        if (recorderPanel.getVisibility() == View.VISIBLE)
            recorderPanel.setVisibility(View.INVISIBLE);
        addMusicBtnPanel.setVisibility(View.INVISIBLE);
        mainFragment.activePanel = editMusicPanel;
        quickLibraryBtn.setVisibility(View.GONE);
        quickStorageText.setText(musicTitleText.getText());
        quickStoragePanel.setVisibility(View.VISIBLE);
        initPlayer();
        setAudioVolume(0);
    }

    private void setAudioVolume(float volume) {

        if (volume == 0) {
            quickVolumeBtn.setImageResource(R.drawable.ic_mute);
        } else if (volume > 0 && volume <= 25 / 100f) {
            quickVolumeBtn.setImageResource(R.drawable.ic_volume_quick_0);
        } else if (volume > 25 / 100f && volume <= 50 / 100f) {
            quickVolumeBtn.setImageResource(R.drawable.ic_volume_quick_1);
        } else if (volume > 50 / 100f && volume <= 75 / 100f) {
            quickVolumeBtn.setImageResource(R.drawable.ic_volume_quick_2);
        } else {
            quickVolumeBtn.setImageResource(R.drawable.ic_volume_quick_3);
        }

        VideoInfo.getInstance().setAudioVolume(volume);
        mainFragment.videoPreview.getPlayer().setVolume(VideoInfo.getInstance().getAudioVolume());
        volumeSeekBar.setProgress((int) (VideoInfo.getInstance().getAudioVolume() * 100));
    }

    void resetAudioPanel() {
        try {
            quickStoragePanel.setVisibility(View.INVISIBLE);
            quickLibraryBtn.setVisibility(View.VISIBLE);
            mainFragment.animationUtils.slideDown(mainFragment.activePanel);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void releaseRecorder() {
        if (recorder != null)
            recorder.releaseRecorder();
    }

    void recordPermissionGrant() {
        recordBtn.performClick();
    }

    void recordPermissionDenied() {
        Toast.makeText(activity, "You need to grant write permission for using this app", Toast.LENGTH_LONG).show();
    }
}
