package com.sunfun.slideshow.view.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.sunfun.slideshow.R;
import com.sunfun.slideshow.utils.OnOneOffClickListener;
import com.sunfun.slideshow.utils.VideoUtils;

public class DownloadFragment extends DialogFragment {

    private SeekBar seekBar;
    private TextView percentTextView;
    private static final int SEEK_BAR_FULL_PROGRESS = 100;
    private TextView songTitle;
    private OnCancelListener onCancelListener;

    public DownloadFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static DownloadFragment newInstance(String title) {
        DownloadFragment frag = new DownloadFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.download_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ImageButton closeBtn = getView().findViewById(R.id.closeBtn);

        closeBtn.setOnClickListener(new OnOneOffClickListener() {
            @Override
            public void onSingleClick(View v) {
                onCancelListener.onCancel();
                dismiss();
            }
        });

        seekBar = getView().findViewById(R.id.progressSeekBar);
        seekBar.setProgress(SEEK_BAR_FULL_PROGRESS);
        seekBar.setEnabled(false);
        percentTextView = getView().findViewById(R.id.percentText);
        songTitle = getView().findViewById(R.id.songTitle);
        songTitle.setText(getArguments().getString("title"));

    }

    @Override
    public void onResume() {
        super.onResume();
        Window window = getDialog().getWindow();
        window.setLayout(VideoUtils.getScreenWidth(getContext()) - 64, VideoUtils.getScreenHeight(getContext())/4);
        window.setGravity(Gravity.BOTTOM);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new Dialog(getActivity(), getTheme()){
            @Override
            public void onBackPressed() {
            }
        };
    }

    public void setProgressToSeekBar(int progress){
        seekBar.setProgress(progress);
        percentTextView.setText(progress+"%");
    }

    public void setOnCancelListener(OnCancelListener onCancelListener){
        this.onCancelListener = onCancelListener;
    }

    public interface OnCancelListener {
        void onCancel();
    }

}