package com.sunfun.slideshow.view.scrubber;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.sunfun.slideshow.R;
import com.sunfun.slideshow.interfaces.OnProgressChangeListenerSimple;
import com.sunfun.slideshow.utils.VideoInfo;
import com.sunfun.slideshow.utils.VideoUtils;

public class AudioScrubBarSimple extends RelativeLayout {

    private View selectedView;
    public View startBar;
    public View endBar;
    private View selectionView1;
    private View selectionView2;

    private OnProgressChangeListenerSimple onProgressChangeListener;
    public LinearLayout videoFrameContainer;
    private int rootWidth;

    private long startPosition;
    private long endPosition;
    private View seekView;
    private int markerWidth;
    private long duration;

    public AudioScrubBarSimple(Context context) {
        super(context);
    }

    public AudioScrubBarSimple(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public AudioScrubBarSimple(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onSizeChanged(final int width, int height, int oldw, int oldh) {
        super.onSizeChanged(width, height, oldw, oldh);
        setBackgroundColor(ContextCompat.getColor(getContext(),R.color.background_border_color));
        LayoutInflater mInflater = LayoutInflater.from(getContext());
        markerWidth = (int) getResources().getDimension(R.dimen.music_marker_width);
        rootWidth = width;

        LayoutParams linearParams = new LayoutParams(width, height- VideoUtils.convertDpToPx(getContext(), 2));
        linearParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        videoFrameContainer = new LinearLayout(getContext());
        videoFrameContainer.setOrientation(LinearLayout.HORIZONTAL);
        videoFrameContainer.setLayoutParams(linearParams);
        addView(videoFrameContainer, linearParams);
        videoFrameContainer.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.solid_color));
        ImageView imageView = new ImageView(getContext());
        imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_audio_visual_simple));
        imageView.getDrawable().setTint(ContextCompat.getColor(getContext(), R.color.color_blue));
        imageView.setLayoutParams((new RelativeLayout.LayoutParams(width, height- VideoUtils.convertDpToPx(getContext(), 2))));
        ((RelativeLayout.LayoutParams)imageView.getLayoutParams()).addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        videoFrameContainer.addView(imageView);

        startBar = mInflater.inflate(R.layout.music_start_marker, this, false);
        ((ConstraintLayout.LayoutParams)startBar.findViewById(R.id.startMarker).getLayoutParams()).setMargins(0,0,0,0);
        startBar.setX(VideoInfo.getInstance().getExtraAudioStart()/(float)VideoInfo.getInstance().getExtraAudioDuration() * (rootWidth-markerWidth*2));
        startBar.setY(0);
        startBar.setElevation(6);
        addView(startBar);

        endBar = mInflater.inflate(R.layout.music_end_marker, this, false);
        ((ConstraintLayout.LayoutParams)endBar.findViewById(R.id.endMarker).getLayoutParams()).setMargins(0,0,0,0);
        endBar.getLayoutParams().height = height;
        endBar.setX((VideoInfo.getInstance().getExtraAudioEnd()/(float)VideoInfo.getInstance().getExtraAudioDuration() * (rootWidth-markerWidth*2)) + markerWidth);
        endBar.setY(0);
        endBar.setElevation(6);
        addView(endBar);

        LayoutParams viewParams1 = new LayoutParams(0, height);
        viewParams1.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        selectionView1 = new View(getContext());
        selectionView1.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorBlackTrans));
        addView(selectionView1, viewParams1);

        LayoutParams viewParams2 = new LayoutParams(0, height);
        viewParams2.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        selectionView2 = new View(getContext());
        selectionView2.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorBlackTrans));
        addView(selectionView2, viewParams2);

        colorView();

        setOnTouchListener((v, event) -> {
            float x = event.getX() - 8;
            float startBarPosition = AudioScrubBarSimple.this.getChildAt(1).getX();
            final float endBarPosition = AudioScrubBarSimple.this.getChildAt(2).getX();

            if (event.getAction() == MotionEvent.ACTION_DOWN && selectedView == null) {
                if (Math.abs(startBarPosition - x) < startBar.getWidth()) {
                    selectedView = AudioScrubBarSimple.this.getChildAt(1);
                } else if (Math.abs(endBarPosition - x) < endBar.getWidth()) {
                    selectedView = AudioScrubBarSimple.this.getChildAt(2);
                }
            }

            if (event.getAction() == MotionEvent.ACTION_MOVE && selectedView != null) {
                onProgressChangeListener.onTouchMoved();
                if (selectedView == AudioScrubBarSimple.this.getChildAt(1)) {
                    if (x > endBarPosition-markerWidth) {
                        x = endBarPosition-markerWidth;
                    }

                    if (x <= 0) {
                        x = 0;
                    }

                    startPosition = (long) Math.floor(((duration * (x)) /(rootWidth-markerWidth*2)));
                    if(VideoUtils.millisToMinute(startPosition).equals(VideoUtils.millisToMinute(endPosition)) && startPosition >= 1){
                        startPosition = (long) (startPosition - 1000f);
                    }

                    onProgressChangeListener.onLeftMarkerPositionChanged();
                    selectedView.setX(x);
                    colorView();

                } else if (selectedView == AudioScrubBarSimple.this.getChildAt(2)) {

                    if (x < startBarPosition + markerWidth) {
                        x = startBarPosition + markerWidth;
                    }

                    if (x > width - markerWidth) {
                        x = width - markerWidth;
                    }

                    endPosition = (long) Math.floor(((duration * (x-markerWidth)) /(rootWidth-markerWidth*2)));
                    if(VideoUtils.millisToMinute(startPosition).equals(VideoUtils.millisToMinute(endPosition)) && endPosition <= duration){
                        endPosition = (long) (endPosition + 1000f);
                    }

                    onProgressChangeListener.onRightMarkerPositionChanged();
                    selectedView.setX(x);
                    colorView();

                }
            }

            if (event.getAction() == MotionEvent.ACTION_UP) {
                selectedView = null;
            }
            return false;
        });
    }

    private void colorView() {
        selectionView1.setTranslationX(VideoUtils.convertDpToPx(getContext(), 0));
        selectionView1.getLayoutParams().width = Math.round(startBar.getX() ) - VideoUtils.convertDpToPx(getContext(), 0);
        selectionView2.setTranslationX(endBar.getX());
        selectionView2.getLayoutParams().width = Math.round(rootWidth - endBar.getX()) - VideoUtils.convertDpToPx(getContext(), 0);
        new Handler().post(this::requestLayout);
    }

    public void setOnProgressChangeListener(OnProgressChangeListenerSimple onProgressChangeListener) {
        this.onProgressChangeListener = onProgressChangeListener;
    }

    public long getStartPosition() {
        return startPosition;
    }

    public long getEndPosition() {
        return endPosition;
    }

    public void setDuration(long duration){
        this.duration = duration;
    }

}
