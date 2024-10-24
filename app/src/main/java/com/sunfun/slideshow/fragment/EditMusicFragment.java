package com.sunfun.slideshow.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.sunfun.slideshow.R;
import com.sunfun.slideshow.interfaces.OnProgressChangeListenerSimple;
import com.sunfun.slideshow.utils.AudioExoPlayerHolder;
import com.sunfun.slideshow.utils.OnOneOffClickListener;
import com.sunfun.slideshow.utils.VideoInfo;
import com.sunfun.slideshow.utils.VideoUtils;
import com.sunfun.slideshow.view.scrubber.AudioScrubBarSimple;

public class EditMusicFragment extends DialogFragment {


    private ImageButton doneBtn;
    private ImageButton backBtn;
    private ImageView playBtn;
    private SimpleExoPlayer simpleExoPlayer;
    private SeekBar audioSeekBar;
    private Handler mHandler;
    private Runnable updateTask;
    private boolean isAudioPaused;
    private SeekBar soundSeekBar;
    private TextView startTimeText;
    private TextView endTimeText;
    private TextView seekTimeText;
    private TextView seekTotalTimeText;
    private AudioScrubBarSimple audioScrubBarSimple;
    private TextView fadeInBtn;
    private TextView fadeOutBtn;
    private OnDeleteMusicListener onDeleteMusicListener;
    private ImageButton deleteBtn;

    public EditMusicFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    static EditMusicFragment newInstance() {
        EditMusicFragment frag = new EditMusicFragment();
        Bundle args = new Bundle();
        args.putString("title", "Edit music");
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_music_edit, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        findAllViews();
        initButtons();
        initScrubber();
        initMediaPlayer();
    }

    private void initScrubber() {
        setAllTimeText();
        audioScrubBarSimple.setDuration(simpleExoPlayer.getDuration());
        audioScrubBarSimple.setOnProgressChangeListener(new OnProgressChangeListenerSimple() {
            @Override
            public void onThumbPositionChanged() {

            }

            @Override
            public void onRightMarkerPositionChanged() {
                VideoInfo.getInstance().setExtraAudioEnd(audioScrubBarSimple.getEndPosition());
                setAllTimeText();
            }

            @Override
            public void onLeftMarkerPositionChanged() {
                VideoInfo.getInstance().setExtraAudioStart(audioScrubBarSimple.getStartPosition());
                setAllTimeText();
            }

            @Override
            public void onTouchMoved() {
                pausePlayer();
            }
        });
    }

    private void setAllTimeText() {
        startTimeText.setText(VideoUtils.millisToMinute(VideoInfo.getInstance().getExtraAudioStart()));
        endTimeText.setText(VideoUtils.millisToMinute(VideoInfo.getInstance().getExtraAudioEnd()));
        seekTotalTimeText.setText(VideoUtils.millisToMinute(VideoInfo.getInstance().getExtraAudioEnd() - VideoInfo.getInstance().getExtraAudioStart()));
    }

    private void initMediaPlayer() {
        playBtn.setTag("0");
        if (mHandler == null) mHandler = new Handler();
        updateTask = new Runnable() {
            @Override
            public void run() {
                try {
                    if (audioSeekBar.getProgress() < audioSeekBar.getMax() && !isAudioPaused) {
                        mHandler.post(this);
                    } else {
                        pausePlayer();
                    }
                    audioSeekBar.setProgress((int) (simpleExoPlayer.getCurrentPosition() - VideoInfo.getInstance().getExtraAudioStart()));
                    seekTimeText.setText(VideoUtils.millisToMinute(audioSeekBar.getProgress()));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
    }

    private void initButtons(){
        backBtn.setOnClickListener(view -> dismiss());
        playBtn.setOnClickListener(v ->{
            if(playBtn.getTag().equals("0")){
                resumePlayer();
            } else {
                pausePlayer();
            }
        });

        doneBtn.setOnClickListener(v -> dismiss());

        audioSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    simpleExoPlayer.seekTo(progress + VideoInfo.getInstance().getExtraAudioStart());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                pausePlayer();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        soundSeekBar.setProgress((int) (VideoInfo.getInstance().getExtraAudioVolume() * 100));
        soundSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                simpleExoPlayer.setVolume((1f/100f)* progress);
                VideoInfo.getInstance().setExtraAudioVolume(simpleExoPlayer.getVolume());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        if(!VideoInfo.getInstance().isFadeIn()){
            fadeInBtn.setBackground(getActivity().getDrawable(R.drawable.button_state_off));
        }

        if(!VideoInfo.getInstance().isFadeOut()){
            fadeOutBtn.setBackground(getActivity().getDrawable(R.drawable.button_state_off));
        }

        fadeInBtn.setOnClickListener(v -> {
            if(VideoInfo.getInstance().isFadeIn()){
                VideoInfo.getInstance().setFadeIn(false);
                fadeInBtn.setBackground(getActivity().getDrawable(R.drawable.button_state_off));
            } else {
                VideoInfo.getInstance().setFadeIn(true);
                fadeInBtn.setBackground(getActivity().getDrawable(R.drawable.fade_button_background));
            }
        });

        fadeOutBtn.setOnClickListener(v -> {
            if(VideoInfo.getInstance().isFadeOut()){
                fadeOutBtn.setBackground(getActivity().getDrawable(R.drawable.button_state_off));
                VideoInfo.getInstance().setFadeOut(false);
            } else {
                VideoInfo.getInstance().setFadeOut(true);
                fadeOutBtn.setBackground(getActivity().getDrawable(R.drawable.fade_button_background));
            }
        });

        deleteBtn.setOnClickListener(new OnOneOffClickListener() {
            @Override
            public void onSingleClick(View v) {
                onDeleteMusicListener.onDeleteBtnClicked();
            }
        });
    }

    private void pausePlayer() {
        simpleExoPlayer.setPlayWhenReady(false);
        simpleExoPlayer.seekTo(0);
        playBtn.setTag("0");
        playBtn.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_music_play));
        pauseSeekAnimation();
    }

    private void pauseSeekAnimation() {
        isAudioPaused = true;
    }

    private void resumeSeekAnimation() {
        isAudioPaused = false;
        mHandler.post(updateTask);
    }

    private void resumePlayer() {
        audioSeekBar.setMax((int) (VideoInfo.getInstance().getExtraAudioEnd() - VideoInfo.getInstance().getExtraAudioStart()));
        simpleExoPlayer.seekTo(VideoInfo.getInstance().getExtraAudioStart());
        audioSeekBar.setProgress(0);
        simpleExoPlayer.setPlayWhenReady(true);
        playBtn.setTag("1");
        playBtn.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_music_pause));
        resumeSeekAnimation();
    }

    private void findAllViews() {
        backBtn = getView().findViewById(R.id.backBtn);
        playBtn = getView().findViewById(R.id.playBtn);
        audioSeekBar = getView().findViewById(R.id.audioSeekBar);
        soundSeekBar = getView().findViewById(R.id.soundSeekBar);
        startTimeText = getView().findViewById(R.id.startTimeText);
        endTimeText = getView().findViewById(R.id.endTimeText);
        seekTimeText = getView().findViewById(R.id.seekTimeText);
        seekTotalTimeText = getView().findViewById(R.id.seekTotalTimeText);
        audioScrubBarSimple = getView().findViewById(R.id.audioScrubBarSimple);
        doneBtn = getView().findViewById(R.id.doneButton);
        deleteBtn = getView().findViewById(R.id.deleteBtn);
        fadeInBtn = getView().findViewById(R.id.fadeInBtn);
        fadeOutBtn = getView().findViewById(R.id.fadeOutBtn);
        simpleExoPlayer = AudioExoPlayerHolder.getInstance(getContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        Window window = getDialog().getWindow();
        window.setLayout(VideoUtils.getScreenWidth(getContext()) - (64*4), (int) (VideoUtils.getScreenHeight(getContext())/2.2f));
        window.setGravity(Gravity.CENTER);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new Dialog(getActivity(), getTheme()){
            @Override
            public void onBackPressed() {

            }
        };
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        pausePlayer();
        super.onDismiss(dialog);
    }


    @Override
    public void onPause() {
        pausePlayer();
        super.onPause();
    }

    public void setOnDeleteMusicListener(OnDeleteMusicListener onDeleteMusicListener) {
        this.onDeleteMusicListener = onDeleteMusicListener;
    }

    public interface OnDeleteMusicListener{
        void onDeleteBtnClicked();
    }
}