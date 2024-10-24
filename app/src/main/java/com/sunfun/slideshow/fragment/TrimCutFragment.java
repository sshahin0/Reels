package com.sunfun.slideshow.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaActionSound;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.daasuu.mp4compose.filter.GlTransformFilter;
import com.daasuu.mp4compose.player.GPUPlayerView;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SeekParameters;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.sunfun.slideshow.R;
import com.sunfun.slideshow.interfaces.OnAnimationListener;
import com.sunfun.slideshow.interfaces.OnProgressChangeListenerAdvance;
import com.sunfun.slideshow.interfaces.OnProgressChangeListenerSimple;
import com.sunfun.slideshow.utils.DialogUtils;
import com.sunfun.slideshow.utils.ExoPlayerErrorHandler;
import com.sunfun.slideshow.utils.ExoPlayerHolder;
import com.sunfun.slideshow.utils.OnOneOffClickListener;
import com.sunfun.slideshow.utils.Prefs;
import com.sunfun.slideshow.utils.VideoInfo;
import com.sunfun.slideshow.utils.VideoUtils;
import com.sunfun.slideshow.view.scrubber.VideoScrubBarAdvance;
import com.sunfun.slideshow.view.scrubber.VideoScrubBarSimple;
import com.sunfun.slideshow.viewmodel.MainViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import mehdi.sakout.fancybuttons.FancyButton;

public class TrimCutFragment extends Fragment {

    public static final String TAG = TrimCutFragment.class.getName();
    private MainViewModel mainViewModel;
    private GPUPlayerView videoPreview;
    private ImageView startBtn;
    private ImageView endBtn;
    private ImageView timeSelectionLeft;
    private ImageView timeSelectionRight;
    private ImageButton doneBtn;
    private ImageButton playBtn;
    private FancyButton trimBtn;
    private FancyButton cutBtn;
    private ImageButton backBtn;
    private TextView timeMiddleText;
    private TextView startTimeText;
    private TextView endTimeText;
    private TextView simpleBtn;
    private TextView advanceBtn;
    private VideoScrubBarSimple videoScrubBarSimple;
    private VideoScrubBarAdvance videoScrubBarAdvance;
    private String scrubBarType = "simple";
    private ConstraintLayout advanceScrubBarGroup;
    private long startPosition = 1;
    private long endPosition = 2;
    private int startBtnResActive;
    private int startBtnResDisable;
    private int endBtnResActive;
    private int endBtnResDisable;
    private boolean isPreparing = true;
    private ConstraintLayout scrubberContainer;
    private FrameLayout blackMask;
    private ImageButton cameraBtn;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trim_cut, container, false);
    }


    @Override
    public Animation onCreateAnimation(int transit, final boolean enter, int nextAnim) {
        Animation anim = AnimationUtils.loadAnimation(getActivity(), nextAnim);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }

            @Override
            public void onAnimationRepeat(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation) {
                if(enter){
                    findAllViews();
                    generalInitialization();
                    initViewModel();
                    initButtons();
                    initPlayer();
                } else {
                    videoPreview = null;
                    videoScrubBarSimple.release();
                    videoScrubBarAdvance.release();
                }
            }
        });
        return anim;
    }


    private void findAllViews() {
        videoPreview = Objects.requireNonNull(getView()).findViewById(R.id.video_view);
        blackMask = getView().findViewById(R.id.black_mask);
        startBtn = getView().findViewById(R.id.startButton);
        endBtn = getView().findViewById(R.id.endButton);
        doneBtn = getView().findViewById(R.id.doneButton);
        playBtn = getView().findViewById(R.id.playBtn);
        trimBtn = getView().findViewById(R.id.trimButton);
        cutBtn = getView().findViewById(R.id.cutButton);
        backBtn = getView().findViewById(R.id.backButton);
        simpleBtn = getView().findViewById(R.id.simpleBtn);
        advanceBtn = getView().findViewById(R.id.advanceBtn);
        cameraBtn = getView().findViewById(R.id.cameraBtn);

        videoScrubBarSimple = getView().findViewById(R.id.fixedScrubBar);
        videoScrubBarSimple.setVisibility(View.VISIBLE);
        videoScrubBarAdvance = getView().findViewById(R.id.movableScrubBar);
        advanceScrubBarGroup = getView().findViewById(R.id.advanceScrubBarGroup);
        advanceScrubBarGroup.setVisibility(View.INVISIBLE);
        scrubberContainer = getView().findViewById(R.id.scrubberContainer);

        timeMiddleText = getView().findViewById(R.id.timeMiddleText);
        startTimeText = getView().findViewById(R.id.startTimeText);
        endTimeText = getView().findViewById(R.id.endTimeText);
        timeSelectionLeft = getView().findViewById(R.id.timeSelectionLeft);
        timeSelectionRight = getView().findViewById(R.id.timeSelectionRight);
    }


    private void generalInitialization() {
        trimBtn.setTag(1);
        cutBtn.setTag(0);
        startBtn.setEnabled(true);
        endBtn.setEnabled(true);
        videoPreview.setZOrderMediaOverlay(true);
        enableTrimResources();
    }

    private void initViewModel() {
        if (getActivity() != null) {
            mainViewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initButtons() {
        cameraBtn.setOnClickListener(new OnOneOffClickListener() {
            @Override
            public void onSingleClick(View v) {
                String isShowString = new Prefs(getContext()).getShowScreenShotDialog();
                boolean isShow = Boolean.parseBoolean(new Prefs(getContext()).getShowScreenShotDialog());
                if(isShow){
                    new DialogUtils().createWarningDialog(getActivity(),
                            "Warning!", R.string.screenshot_text,
                            "YES", "NO",
                            iosDialog -> {
                                iosDialog.dismiss();
                                captureBitmap();
                            },
                            true,
                            iosDialog -> new Prefs(getContext()).setShowScreenShotDialog(String.valueOf(!iosDialog.isChecked)));
                } else {
                    captureBitmap();
                }
            }
        });

        doneBtn.setOnClickListener(v -> {
            if (scrubBarType.equals("simple")) {
                startPosition = videoScrubBarSimple.getStartPosition();
                endPosition = videoScrubBarSimple.getEndPosition();
            } else {
                startPosition = videoScrubBarAdvance.getStartPosition();
                endPosition = videoScrubBarAdvance.getEndPosition();
            }

            Log.d("simul", startPosition + " position " + endPosition);
            finishWithResult(startPosition, endPosition);
        });

        playBtn.setTag("0");
        playBtn.setOnClickListener(v -> {

            if(isPreparing){
                return;
            }

            if (playBtn.getTag().equals("0")) {
                videoScrubBarSimple.startAnim();
                videoScrubBarAdvance.startAnim();
                playBtn.setTag("1");
                playBtn.setImageResource(R.drawable.ic_pause);
            } else {
                stopPlaying();
                playBtn.setTag("0");
            }
        });

        trimBtn.setOnClickListener(v -> {
            enableTrimResources();
            changeBtnSelection(trimBtn, cutBtn);
            videoScrubBarSimple.setType("trim");
            videoScrubBarAdvance.setType("trim");
            timeSelectionLeft.setRotation(180);
            timeSelectionRight.setRotation(0);

            stopPlaying();
            videoScrubBarSimple.simpleAnimator = null;
        });

        cutBtn.setOnClickListener(v -> {
            enableCutResources();
            changeBtnSelection(cutBtn, trimBtn);
            videoScrubBarSimple.setType("cut");
            videoScrubBarAdvance.setType("cut");
            timeSelectionLeft.setRotation(0);
            timeSelectionRight.setRotation(180);

            stopPlaying();
            videoScrubBarSimple.simpleAnimator = null;
        });

        backBtn.setOnClickListener(v -> {
            finish();
        });


        simpleBtn.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (scrubBarType.equals("simple")) {
                    simpleBtn.setTextColor(getResources().getColor(android.R.color.white));
                } else {
                    simpleBtn.setTextColor(getResources().getColor(R.color.dark_gray));
                }
            }
            return false;
        });


        advanceBtn.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (scrubBarType.equals("advance")) {
                    advanceBtn.setTextColor(getResources().getColor(android.R.color.white));
                } else {
                    advanceBtn.setTextColor(getResources().getColor(R.color.dark_gray));
                }
            }
            return false;
        });


        simpleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrubBarType = "simple";
                simpleBtn.setTextColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.activeColor));
                advanceBtn.setTextColor(ContextCompat.getColor(getContext(), android.R.color.white));

                int distance = 8000;
                float scale = getResources().getDisplayMetrics().density;
                scrubberContainer.setCameraDistance(distance * scale);

                final ObjectAnimator oa1 = ObjectAnimator.ofFloat(scrubberContainer, "rotationX", 0, 90).setDuration(100);
                final ObjectAnimator oa2 = ObjectAnimator.ofFloat(scrubberContainer, "rotationX", 90, 0).setDuration(100);
                final ObjectAnimator oa3 = ObjectAnimator.ofFloat(scrubberContainer, "scaleX", 1f, 0.9f).setDuration(100);
                final ObjectAnimator oa4 = ObjectAnimator.ofFloat(scrubberContainer, "scaleX", 0.9f, 1f).setDuration(100);
                oa1.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        videoScrubBarSimple.setVisibility(View.VISIBLE);
                        advanceScrubBarGroup.setVisibility(View.INVISIBLE);
                        oa2.start();
                        oa4.start();
                    }
                });
                oa1.start();
                oa3.start();
            }
        });


        advanceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                scrubBarType = "advance";

                startTimeText.setText(VideoUtils.millisToMinute(videoScrubBarAdvance.getStartPosition()));
                endTimeText.setText(VideoUtils.millisToMinute(videoScrubBarAdvance.getEndPosition()));
                simpleBtn.setTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
                advanceBtn.setTextColor(ContextCompat.getColor(getContext(), R.color.activeColor));

                int distance = 25000;
                float scale = getResources().getDisplayMetrics().density;
                scrubberContainer.setCameraDistance(distance * scale);


                final ObjectAnimator oa1 = ObjectAnimator.ofFloat(scrubberContainer, "rotationX", 0, 90).setDuration(100);
                final ObjectAnimator oa2 = ObjectAnimator.ofFloat(scrubberContainer, "rotationX", 90, 0).setDuration(100);
                final ObjectAnimator oa3 = ObjectAnimator.ofFloat(scrubberContainer, "scaleX", 1f, 0.9f).setDuration(100);
                final ObjectAnimator oa4 = ObjectAnimator.ofFloat(scrubberContainer, "scaleX", 0.9f, 1f).setDuration(100);
                oa1.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        videoScrubBarSimple.setVisibility(View.INVISIBLE);
                        advanceScrubBarGroup.setVisibility(View.VISIBLE);
                        oa2.start();
                        oa4.start();
                    }
                });
                oa1.start();
                oa3.start();
            }
        });
    }

    private void captureBitmap() {
        videoPreview.captureBitmap(getActivity(), bitmap -> {
            MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), bitmap, "capImage"+System.currentTimeMillis(), "");
            MediaActionSound sound = new MediaActionSound();
            sound.play(MediaActionSound.SHUTTER_CLICK);
            Toast.makeText(getContext(), "Current frame is saved in your photo gallery", Toast.LENGTH_SHORT).show();
        });
    }


    private void initPlayer() {
        videoPreview.adjustWidthHeight(VideoInfo.getInstance().getCroppedWidth(), VideoInfo.getInstance().getCroppedHeight(), VideoInfo.getInstance().getRotation());
        videoPreview.setSimpleExoPlayer(ExoPlayerHolder.getInstance(getContext()));
        blackMask.setVisibility(View.VISIBLE);
        blackMask.post(() -> videoPreview.setVisibility(View.VISIBLE));
        videoPreview.getPlayer().setSeekParameters(SeekParameters.EXACT);
        initExoPlayerListener();
        setMediaSourceAndPrepare();
    }

    private void initExoPlayerListener() {
        videoPreview.getPlayer().addListener(new Player.EventListener() {
            @Override
            public void onPlayerError(ExoPlaybackException error) {
                new ExoPlayerErrorHandler(getContext(), iosDialog -> {
                    iosDialog.dismiss();
                    finish();
                }).handle(error);
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (isPreparing && playbackState == ExoPlayer.STATE_READY) {
                    updateVideoPreview();
                    initSimpleScrubBer();
                    initAdvanceScrubBer();
                    blackMask.setVisibility(View.INVISIBLE);
                    isPreparing = false;
                }
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initSimpleScrubBer() {
        mainViewModel.getScrubBerBitmap("simple").observe(getActivity(), bitmap -> videoScrubBarSimple.setBitmap(bitmap));

        videoScrubBarSimple.setMediaPlayer(videoPreview.getPlayer());
        videoScrubBarSimple.setOnProgressChangeListener(new OnProgressChangeListenerSimple() {
            @Override
            public void onThumbPositionChanged() {
                if (advanceScrubBarGroup.getVisibility() == View.INVISIBLE) {
                    videoScrubBarAdvance.moveScrollBar();
                }
            }

            @Override
            public void onRightMarkerPositionChanged() {
                if (advanceScrubBarGroup.getVisibility() == View.INVISIBLE) {
                    videoScrubBarAdvance.setEndPosition();
                }
            }

            @Override
            public void onLeftMarkerPositionChanged() {
                if (advanceScrubBarGroup.getVisibility() == View.INVISIBLE) {
                    videoScrubBarAdvance.setStartPosition();
                }
            }

            @Override
            public void onTouchMoved() {
                stopPlaying();
            }

        });

        videoScrubBarSimple.setOnAnimationListener(new OnAnimationListener() {
            @Override
            public void animationEnd() {
                if (playBtn.getTag().equals("1")) {
                    playBtn.setTag("0");
                    playBtn.setImageResource(R.drawable.play);
                }
                videoScrubBarSimple.simpleAnimator = null;
            }

            @Override
            public void animationStart() {
                Log.d("simul", "animation start");
            }
        });
        videoScrubBarSimple.setUpSeekPosition();
    }


    private void initAdvanceScrubBer() {

        startBtn.setOnClickListener(v -> {
            stopPlaying();
            videoScrubBarAdvance.setStartPosition();
            videoScrubBarAdvance.onProgressChangeListener.onLeftMarkerPositionChanged();
            startTimeText.setText(VideoUtils.millisToMinute(videoScrubBarAdvance.getStartPosition()));

        });

        endBtn.setOnClickListener(v -> {
            stopPlaying();
            videoScrubBarAdvance.setEndPosition();
            videoScrubBarAdvance.onProgressChangeListener.onRightMarkerPositionChanged();
            endTimeText.setText(VideoUtils.millisToMinute(videoScrubBarAdvance.getEndPosition()));

        });

        mainViewModel.getAdvanceScrubBerBitmap().observe(getActivity(), bitmap -> videoScrubBarAdvance.setBitmap(bitmap));

        videoScrubBarAdvance.setMediaPlayer(videoPreview.getPlayer());
        videoScrubBarAdvance.setOnProgressChangeListener(new OnProgressChangeListenerAdvance() {
            @SuppressLint("StaticFieldLeak")

            @Override
            public void onVideoPositionChanged() {
                try {
                    timeMiddleText.setText(VideoUtils.millisToMinute(videoPreview.getPlayer().getCurrentPosition()));
                    videoScrubBarSimple.moveThumbBar();
                } catch (Exception ex){
                    ex.printStackTrace();
                }
            }

            @Override
            public void onRightMarkerPositionChanged() {
                if (videoScrubBarSimple.getVisibility() == View.INVISIBLE) {
                    videoScrubBarSimple.setEndPosition();
                }
            }

            @Override
            public void onLeftMarkerPositionChanged() {
                if (videoScrubBarSimple.getVisibility() == View.INVISIBLE) {
                    videoScrubBarSimple.setStartPosition();
                }
            }

            @Override
            public void onStartScrolling() {
                stopPlaying();
            }

            @Override
            public void onStopScrolling() {
                stopPlaying();
            }

            @Override
            public void onScrollPositionChange(){
                setButtonInability(videoScrubBarAdvance.getScrollX(), videoScrubBarAdvance.startBarX, videoScrubBarAdvance.endBarX);
            }

            @Override
            public void onScrollPositionChange(long l) {

            }

        });

        videoScrubBarAdvance.setOnAnimationListener(new OnAnimationListener() {
            @Override
            public void animationEnd() {
                if (playBtn.getTag().equals("1")) {
                    playBtn.setTag("0");
                    playBtn.setImageResource(R.drawable.play);
                }
                videoScrubBarAdvance.advanceAnimator = null;
            }

            @Override
            public void animationStart() {
            }
        });
    }

    private void stopPlaying() {
        videoScrubBarSimple.stopAnim();
        videoScrubBarAdvance.stopAnim();
        videoScrubBarSimple.simpleAnimator = null;
        videoScrubBarAdvance.advanceAnimator = null;
        if (playBtn.getTag().equals("1")) {
            playBtn.setTag("0");
            playBtn.setImageResource(R.drawable.play);
        }
    }

    private void updateVideoPreview() {
        GlTransformFilter glFiler = new GlTransformFilter();
        glFiler.setRotateInAngle(0);
        glFiler.setTranslateOffset( VideoInfo.getInstance().getLeft()/VideoInfo.getInstance().getWidth(),  (VideoInfo.getInstance().getTop())/VideoInfo.getInstance().getHeight());
        float scaleX = VideoInfo.getInstance().getCroppedWidth()/VideoInfo.getInstance().getWidth();
        float scaleY = VideoInfo.getInstance().getCroppedHeight()/VideoInfo.getInstance().getHeight();
        if(VideoInfo.getInstance().isFlippedV){ scaleX = scaleX * -1; }
        if(VideoInfo.getInstance().isFlippedH){ scaleY = scaleY * -1; }
        glFiler.setScaleUnit(scaleX, scaleY);
        glFiler.setRotateInAngle(VideoInfo.getInstance().getRotation());
        videoPreview.setGlFilter(glFiler);
    }

    private void setMediaSourceAndPrepare() {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getContext(), Util.getUserAgent(getContext(), "videoediting"));
        MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(VideoInfo.getInstance().getVideoPath()));
        isPreparing = true;
        videoPreview.getPlayer().prepare(videoSource);
    }

    private void enableCutResources() {
        startBtnResActive = R.drawable.start_here_cut;
        startBtnResDisable = R.drawable.start_here_after_press_cut;
        endBtnResActive = R.drawable.end_here_cut;
        endBtnResDisable = R.drawable.end_here_after_press_cut;
    }

    private void enableTrimResources() {
        startBtnResActive = R.drawable.start_here_trim;
        startBtnResDisable = R.drawable.start_here_after_press_trim;
        endBtnResActive = R.drawable.end_here_trim;
        endBtnResDisable = R.drawable.end_here_after_press_trim;
    }

    private void finish() {
        Objects.requireNonNull(getActivity()).getSupportFragmentManager();
        getActivity().getSupportFragmentManager().popBackStack();
        Intent intent = new Intent();
        assert getTargetFragment() != null;
        getTargetFragment().onActivityResult(
                getTargetRequestCode(),
                Activity.RESULT_CANCELED,
                intent
        );
        stopPlaying();
    }


    private void finishWithResult(long startTime, long endTime) {
        stopPlaying();
        Objects.requireNonNull(getActivity()).getSupportFragmentManager();
        getActivity().getSupportFragmentManager().popBackStack();
        Intent intent = new Intent();

        if (scrubBarType.equals("advance") && videoScrubBarAdvance.getType().equals("trim")) {
            VideoInfo.getInstance().setTrimmed(true);
            VideoInfo.getInstance().setCut(false);
        } else if (scrubBarType.equals("advance") && videoScrubBarAdvance.getType().equals("cut")) {
            VideoInfo.getInstance().setCut(true);
            VideoInfo.getInstance().setTrimmed(false);
        } else if (scrubBarType.equals("simple") && videoScrubBarSimple.getType().equals("trim")) {
            VideoInfo.getInstance().setTrimmed(true);
            VideoInfo.getInstance().setCut(false);
        } else if (scrubBarType.equals("simple") && videoScrubBarSimple.getType().equals("cut")) {
            VideoInfo.getInstance().setCut(true);
            VideoInfo.getInstance().setTrimmed(false);
        }

        if (VideoInfo.getInstance().isCut && startTime <= 33) {
            VideoInfo.getInstance().setTrimmed(true);
            VideoInfo.getInstance().setCut(false);

            VideoInfo.getInstance().setStartMs(endTime);
            VideoInfo.getInstance().setEndMs(VideoInfo.getInstance().totalVideoDuration);
        } else if (VideoInfo.getInstance().isCut && endTime >= VideoInfo.getInstance().totalVideoDuration) {
            VideoInfo.getInstance().setTrimmed(true);
            VideoInfo.getInstance().setCut(false);

            VideoInfo.getInstance().setStartMs(0);
            VideoInfo.getInstance().setEndMs(startTime);
        } else {
            VideoInfo.getInstance().setStartMs(startTime);
            VideoInfo.getInstance().setEndMs(endTime);
        }

        assert getTargetFragment() != null;
        getTargetFragment().onActivityResult(
                MainFragment.TRIM_CUT_REQUEST,
                Activity.RESULT_OK,
                intent
        );
    }

    private void setButtonInability(int progress, float startPosition, float endPosition) {

        progress = progress + 2;

        if (progress < endPosition) {
            startBtn.setEnabled(true);
            startBtn.setImageResource(startBtnResActive);
        } else {
            startBtn.setEnabled(false);
            startBtn.setImageResource(startBtnResDisable);
        }

        if (progress > startPosition) {
            endBtn.setEnabled(true);
            endBtn.setImageResource(endBtnResActive);
        } else {
            endBtn.setEnabled(false);
            endBtn.setImageResource(endBtnResDisable);
        }
    }

    private void changeBtnSelection(FancyButton btn1, FancyButton btn2) {
        btn1.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.activeColor));
        btn1.setTextColor(ContextCompat.getColor(getActivity(), android.R.color.white));
        btn1.setTag(1);

        btn2.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.disableColor));
        btn2.setTextColor(ContextCompat.getColor(getActivity(), R.color.activeColor));
        btn2.setTag(0);
    }

    private void pausePlayer() {
        stopPlaying();
        videoPreview.getPlayer().setPlayWhenReady(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        pausePlayer();
    }


    public static TrimCutFragment getInstance() {
        return new TrimCutFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void onBackPressed(){
        finish();
   }
}
