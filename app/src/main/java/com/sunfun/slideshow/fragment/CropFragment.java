package com.sunfun.slideshow.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.daasuu.mp4compose.filter.GlTransformFilter;
import com.daasuu.mp4compose.player.GPUPlayerView;
import com.edmodo.cropper.CropImageView;
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
import com.sunfun.slideshow.utils.DialogUtils;
import com.sunfun.slideshow.utils.ExoPlayerErrorHandler;
import com.sunfun.slideshow.utils.ExoPlayerHolder;
import com.sunfun.slideshow.utils.VideoInfo;
import com.sunfun.slideshow.utils.VideoUtils;
import com.sunfun.slideshow.view.dialog.IOSDialog;
import com.sunfun.slideshow.view.scrubber.VideoScrubBar;
import com.sunfun.slideshow.viewmodel.MainViewModel;

import java.util.Objects;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

public class CropFragment extends Fragment {

    private GPUPlayerView videoPreview;
    private ImageButton playBtn;
    private VideoScrubBar videoScrubBar;
    private MainViewModel mainViewModel;
    private Handler mHandler;
    private Runnable updateTask;
    private boolean isVideoPaused = false;
    private TextView backBtn;
    private CropImageView cropView;
    private TextView videoSizeText;
    private LinearLayout buttonsPanel;
    private ImageButton selectedBtn;
    private int aspectRatioX = 1;
    private int aspectRatioY = 1;
    private TextView activeTextView;
    private HorizontalScrollView horizontalScrollView;
    private TextView doneButton;
    private boolean isCropped = false;
    private boolean isPreparing = true;
    private FrameLayout blackMask;
    private String TAG = CropFragment.class.getName();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_crop, container, false);
    }

    @Override
    public Animation onCreateAnimation(int transit, final boolean enter, int nextAnim) {
        Animation anim = AnimationUtils.loadAnimation(getActivity(), nextAnim);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (enter) {
                    findAllViews();
                    generalInitialization();
                    initButtons();
                    initPlayer();
                } else {
                    videoPreview = null;
                }
            }
        });
        return anim;
    }

    private void initButtons() {
        TypedArray array = getResources().obtainTypedArray(R.array.crop_button_panel_resource);

        final int width = (int) (VideoUtils.getScreenWidth(getContext()) / 5f - VideoUtils.convertDpToPx(getContext(), 16));
        int height = width + VideoUtils.convertDpToPx(getContext(), 30);
        final int margin = VideoUtils.convertDpToPx(getContext(), 9);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
        layoutParams.setMargins(margin, 0, margin, 0);

        for (int i = 0; i < 11; i++) {
            final int index = i;
            final View buttons = getLayoutInflater().inflate(R.layout.custom_crops_button, buttonsPanel, false);
            buttons.setLayoutParams(layoutParams);
            final ImageButton fancyButton = buttons.findViewById(R.id.btn);
            final TextView textView = buttons.findViewById(R.id.text);

            textView.setTag("0");

            if (activeTextView == null) {
                activeText(textView);
            }

            fancyButton.setTag(String.valueOf(index));
            fancyButton.setImageResource(array.getResourceId(i, 0));
            fancyButton.setOnClickListener(v -> {

                if (selectedBtn != null && fancyButton == selectedBtn) {
                    return;
                }

                VideoInfo.getInstance().setActiveAspectRatioPos(index);

                if (index == 0) {
                    cropView.setFixedAspectRatio(true);
                    cropView.setAspectRatio(1, 1);
                    cropView.setFixedAspectRatio(false);
                    videoSizeText.setText(getVideoSize());
                } else {
                    isCropped = true;
                    cropView.setFixedAspectRatio(true);
                    aspectRatioX = Integer.parseInt(getResources().getStringArray(R.array.crop_button_panel_ratios)[index].split(":")[0]);
                    aspectRatioY = Integer.parseInt(getResources().getStringArray(R.array.crop_button_panel_ratios)[index].split(":")[1]);
                    cropView.setAspectRatio(aspectRatioX, aspectRatioY);
                }

                if (textView.getTag().equals("0")) {
                    inActiveText(activeTextView);
                    activeText(textView);
                }

                int screenWidth = VideoUtils.getScreenWidth(getContext());
                int left = ((margin * 2 + width) * Integer.parseInt(fancyButton.getTag().toString())) + (margin + width / 2 + margin);
                horizontalScrollView.smoothScrollTo(left - screenWidth / 2, 0);

                selectedBtn = fancyButton;
            });

//            if (VideoInfo.getInstance().getActiveAspectRatioPos() != -1 && VideoInfo.getInstance().getActiveAspectRatioPos() == i) {
//                fancyButton.performClick();
//            }

            ((TextView) buttons.findViewById(R.id.text)).setText(getResources().getStringArray(R.array.crop_button_panel_titles)[i]);
            buttonsPanel.addView(buttons);
        }
        array.recycle();

        playBtn.setOnClickListener(v -> {

            if(isPreparing){
                return;
            }

            if (playBtn.getTag().equals("1")) {
                pausePlayer();
                playBtn.setTag("0");
            } else if (videoPreview.getPlayer() != null) {
                resumePlayer();
                playBtn.setTag("1");
            }
        });

        backBtn.setOnClickListener(v -> finish());

        doneButton.setOnClickListener(v -> {
            cropView.cropImage();
            if(cropView.getCropHeight()<100 || cropView.getCropWidth()<100){
                new DialogUtils().createWarningDialog(getContext(), "Warning!", R.string.crop_warning, "Ok", "", IOSDialog::dismiss);
                return;
            } else {
                finishWithResult();
            }
        });
    }

    private String getVideoSize() {
        if (VideoInfo.getInstance().getRotation() == 0 || VideoInfo.getInstance().getRotation() == 180) {
            return (int) VideoInfo.getInstance().getCroppedWidth() + "x" + (int) VideoInfo.getInstance().getCroppedHeight();
        } else {
            return (int) VideoInfo.getInstance().getCroppedHeight() + "x" + (int) VideoInfo.getInstance().getCroppedWidth();
        }
    }

    private void updateFilter() {
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
    }

    private void finish() {
        pausePlayer();
        Objects.requireNonNull(getActivity()).getSupportFragmentManager();
        getActivity().getSupportFragmentManager().popBackStack();

        Intent intent = new Intent();
        assert getTargetFragment() != null;
        getTargetFragment().onActivityResult(
                getTargetRequestCode(),
                Activity.RESULT_CANCELED,
                intent
        );
    }

    private void finishWithResult() {

        Objects.requireNonNull(getActivity()).getSupportFragmentManager();
        getActivity().getSupportFragmentManager().popBackStack();

        if (isCropped) {
            cropView.cropImage();

            if(cropView.getCropHeight()<100 || cropView.getCropWidth()<100){
                new DialogUtils().createWarningDialog(getContext(), "Warning!", R.string.crop_warning, "Ok", "", IOSDialog::dismiss);
                return;
            }

            //-----------------------------
            if (VideoInfo.getInstance().getRotation() == 0 || VideoInfo.getInstance().getRotation() == 180) {
                double cropWidth = cropView.getCropWidth();
                double cropHeight = cropView.getCropHeight();
                double cropX1 = cropView.getCropX() + cropWidth / 2f;
                double cropY1 = cropView.getCropY() + cropHeight / 2f;

                Point point1 = rotatePoint(cropX1, cropY1, VideoInfo.getInstance().getCroppedWidth() / 2f, VideoInfo.getInstance().getCroppedHeight() / 2, -VideoInfo.getInstance().getRotation());


                if (VideoInfo.getInstance().isFlippedV) { point1.x = point1.x * -1; }
                if (VideoInfo.getInstance().isFlippedH) { point1.y = point1.y * -1; }

                float left = (point1.x + VideoInfo.getInstance().previousCropX);
                float top = (point1.y + VideoInfo.getInstance().previousCropY);



                VideoInfo.getInstance().setCropped(true);
                VideoInfo.getInstance().setLeft(left);
                VideoInfo.getInstance().setTop(top);
                VideoInfo.getInstance().setCroppedWidth((float) cropWidth);
                VideoInfo.getInstance().setCroppedHeight((float) cropHeight);


                VideoInfo.getInstance().previousCropX = VideoInfo.getInstance().getLeft();
                VideoInfo.getInstance().previousCropY = VideoInfo.getInstance().getTop();
                VideoInfo.getInstance().previousFlipV = VideoInfo.getInstance().isFlippedV;
                VideoInfo.getInstance().previousFlipH = VideoInfo.getInstance().isFlippedH;
            } else {
                double cropWidth = cropView.getCropWidth();
                double cropHeight = cropView.getCropHeight();
                double cropX1 = cropView.getCropX() + cropWidth / 2f;
                double cropY1 = cropView.getCropY() + cropHeight / 2f;

                Point point1 = rotatePoint(cropX1, cropY1, VideoInfo.getInstance().getCroppedHeight() / 2f, VideoInfo.getInstance().getCroppedWidth() / 2, -VideoInfo.getInstance().getRotation());


                if (VideoInfo.getInstance().isFlippedV) { point1.x = point1.x * -1; }
                if (VideoInfo.getInstance().isFlippedH) { point1.y = point1.y * -1; }

                float left = (point1.x + VideoInfo.getInstance().previousCropX);
                float top = (point1.y + VideoInfo.getInstance().previousCropY);


                VideoInfo.getInstance().setCropped(true);
                VideoInfo.getInstance().setLeft(left);
                VideoInfo.getInstance().setTop(top);
                VideoInfo.getInstance().setCroppedWidth((float) cropHeight);
                VideoInfo.getInstance().setCroppedHeight((float) cropWidth);

                VideoInfo.getInstance().previousCropX = VideoInfo.getInstance().getLeft();
                VideoInfo.getInstance().previousCropY = VideoInfo.getInstance().getTop();
            }
        }

        Intent intent = new Intent();
        assert getTargetFragment() != null;
        getTargetFragment().onActivityResult(
                MainFragment.CROP_REQUEST,
                Activity.RESULT_OK,
                intent
        );

        pausePlayer();
    }

    public Point rotatePoint(double pointX, double pointY, double originX, double originY, double angle) {
        angle = (angle * Math.PI / 180.0);
        double x = Math.cos(angle) * (pointX - originX) - Math.sin(angle) * (pointY - originY);
        double y = Math.sin(angle) * (pointX - originX) + Math.cos(angle) * (pointY - originY);
        return new Point((int) x, (int) y);
    }

//    private void finishWithResult() {
//
//        Objects.requireNonNull(getActivity()).getSupportFragmentManager();
//        getActivity().getSupportFragmentManager().popBackStack();
//
//        if (isCropped) {
//            cropView.cropImage();
//
//            //-----------------------------
//            float cropWidth = cropView.getCropWidth();
//            float cropHeight = cropView.getCropHeight();
//
//
//            // --------------------------- Correcting crop width and crop height according to new rotation
//            if (VideoInfo.getInstance().getRotation() == 90 || VideoInfo.getInstance().getRotation() == 270) {
//                float temp = cropWidth;
//                cropWidth = cropHeight;
//                cropHeight = temp;
//            }
//
//            //--------------------------- Correcting new crop value for specific rotation
//            float rotation = VideoInfo.getInstance().getRotation() * -1;
//            float rotatedLeft = (float) Math.abs((cropView.getCropX() * Math.cos(Math.toRadians(rotation)) - cropView.getCropY() * Math.sin(Math.toRadians(rotation))));
//            float rotatedTop = (float) Math.abs((cropView.getCropX() * Math.sin(Math.toRadians(rotation)) + cropView.getCropY() * Math.cos(Math.toRadians(rotation))));
//
//            //--------------------------- Correcting previous crop value according to old rotation
//            //if (VideoInfo.getInstance().previousRotation != VideoInfo.getInstance().getRotation()) {
//            switch ((int) VideoInfo.getInstance().getRotation()) {
//                case 0: {
//                    break;
//                }
//                case 270: {
//                    VideoInfo.getInstance().previousCropX = VideoInfo.getInstance().getWidth() - VideoInfo.getInstance().previousCropX1;
//                    break;
//                }
//                case 90: {
//                    VideoInfo.getInstance().previousCropY = VideoInfo.getInstance().getHeight() - VideoInfo.getInstance().previousCropY1;
//                    break;
//                }
//                case 180: {
//                    VideoInfo.getInstance().previousCropX = VideoInfo.getInstance().getWidth() - VideoInfo.getInstance().previousCropX1;
//                    VideoInfo.getInstance().previousCropY = VideoInfo.getInstance().getHeight() - VideoInfo.getInstance().previousCropY1;
//                    break;
//                }
//            }
//
//            VideoInfo.getInstance().previousCropX1 = VideoInfo.getInstance().previousCropX + VideoInfo.getInstance().getCroppedWidth();
//            VideoInfo.getInstance().previousCropY1 = VideoInfo.getInstance().previousCropY + VideoInfo.getInstance().getCroppedHeight();
//            //}
//
//            //---------------------------  Correcting previous crop value according to old flip
//
//            if (!VideoInfo.getInstance().previousFlipV && VideoInfo.getInstance().isFlippedV) {
//                if (VideoInfo.getInstance().getRotation() == 0 || VideoInfo.getInstance().getRotation() == 180) {
//                    VideoInfo.getInstance().previousCropY = VideoInfo.getInstance().getHeight() - VideoInfo.getInstance().previousCropY1;
//                } else if (VideoInfo.getInstance().getRotation() == 90 || VideoInfo.getInstance().getRotation() == 270) {
//                    VideoInfo.getInstance().previousCropX = VideoInfo.getInstance().getWidth() - VideoInfo.getInstance().previousCropX1;
//                }
//            }
//
//            if (!VideoInfo.getInstance().previousFlipH && VideoInfo.getInstance().isFlippedH) {
//                if (VideoInfo.getInstance().getRotation() == 0 || VideoInfo.getInstance().getRotation() == 180) {
//                    VideoInfo.getInstance().previousCropX = VideoInfo.getInstance().getWidth() - VideoInfo.getInstance().previousCropX1;
//                } else if (VideoInfo.getInstance().getRotation() == 90 || VideoInfo.getInstance().getRotation() == 270) {
//                    VideoInfo.getInstance().previousCropY = VideoInfo.getInstance().getHeight() - VideoInfo.getInstance().previousCropY1;
//                }
//            }
//
//
//            //--------------------------- Adding previous flip and rotated data to new crop value
//            float left = rotatedLeft + VideoInfo.getInstance().previousCropX;
//            float top = rotatedTop + VideoInfo.getInstance().previousCropY;
//            float right = left + cropWidth;
//            float bottom = top + cropHeight;
//
//
//            //--------------------------- Correcting new crop value according to new rotation
//            if (VideoInfo.getInstance().getRotation() == 0) {
//                //Nothing to do for now
//            } else if (VideoInfo.getInstance().getRotation() == 90) {
//                top = VideoInfo.getInstance().getHeight() - bottom;
//            } else if (VideoInfo.getInstance().getRotation() == 180) {
//                left = VideoInfo.getInstance().getWidth() - right;
//                top = VideoInfo.getInstance().getHeight() - bottom;
//            } else if (VideoInfo.getInstance().getRotation() == 270) {
//                left = VideoInfo.getInstance().getWidth() - right;
//            }
//            right = left + cropWidth;
//            bottom = top + cropHeight;
//
//
//            //--------------------------- Correcting new crop value according to new flip
//            if (VideoInfo.getInstance().getRotation() == 90 || VideoInfo.getInstance().getRotation() == 270) {
//                if (VideoInfo.getInstance().isFlippedH) {
//                    top = VideoInfo.getInstance().getHeight() - bottom;
//                }
//                if (VideoInfo.getInstance().isFlippedV) {
//                    left = VideoInfo.getInstance().getWidth() - right;
//                }
//            } else {
//                if (VideoInfo.getInstance().isFlippedH) {
//                    left = VideoInfo.getInstance().getWidth() - right;
//                }
//                if (VideoInfo.getInstance().isFlippedV) {
//                    top = VideoInfo.getInstance().getHeight() - bottom;
//                }
//            }
//
//            right = left + cropWidth;
//            bottom = top + cropHeight;
//
//            //--------------------------- Setting new crop value to videoinfo
//            VideoInfo.getInstance().setCropped(true);
//            VideoInfo.getInstance().setLeft(left);
//            VideoInfo.getInstance().setTop(top);
//            VideoInfo.getInstance().setRight(right);
//            VideoInfo.getInstance().setBottom(bottom);
//            VideoInfo.getInstance().setCroppedWidth(cropWidth);
//            VideoInfo.getInstance().setCroppedHeight(cropHeight);
//
//            //--------------------------- setting new crop value as previous crop value to video info
//            VideoInfo.getInstance().previousCropX = left;
//            VideoInfo.getInstance().previousCropY = top;
//            VideoInfo.getInstance().previousCropX1 = right;
//            VideoInfo.getInstance().previousCropY1 = bottom;
//            VideoInfo.getInstance().previousRotation = VideoInfo.getInstance().getRotation();
//            VideoInfo.getInstance().previousFlipV = VideoInfo.getInstance().isFlippedV;
//            VideoInfo.getInstance().previousFlipH = VideoInfo.getInstance().isFlippedH;
//
//        }
//
//        Intent intent = new Intent();
//        assert getTargetFragment() != null;
//        getTargetFragment().onActivityResult(
//                MainFragment.CROP_REQUEST,
//                Activity.RESULT_OK,
//                intent
//        );
//
//        pausePlayer();
//    }

    private void activeText(TextView textView) {
        textView.setTag("1");
        textView.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.button_text_bg));
        textView.setTextColor(ContextCompat.getColor(getActivity(), android.R.color.black));
        activeTextView = textView;
    }

    private void inActiveText(TextView textView) {
        textView.setTag("0");
        textView.setBackground(null);
        textView.setTextColor(ContextCompat.getColor(getActivity(), android.R.color.white));
    }

    private void resumePlayer() {
        isVideoPaused = false;
        videoScrubBar.startAnimation();
        playBtn.setImageResource(R.drawable.ic_pause);
        new Handler().post(updateTask);
    }

    private void generalInitialization() {
        mainViewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
        playBtn.setTag("0");
        OverScrollDecoratorHelper.setUpOverScroll(horizontalScrollView);
        videoSizeText.setText(getVideoSize());
    }

    private void findAllViews() {
        blackMask = getView().findViewById(R.id.black_mask);
        videoPreview = getView().findViewById(R.id.video_view);
        playBtn = getView().findViewById(R.id.playBtn);
        videoScrubBar = getView().findViewById(R.id.scrubBer);
        backBtn = getView().findViewById(R.id.backButton);
        cropView = getView().findViewById(R.id.cropView);
        videoSizeText = getView().findViewById(R.id.titleText);
        buttonsPanel = getView().findViewById(R.id.buttonsPanel);
        horizontalScrollView = getView().findViewById(R.id.horizontalScrollView);
        doneButton = getView().findViewById(R.id.doneButton);
    }

    public void initPlayer() {
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
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (isPreparing && playbackState == ExoPlayer.STATE_READY) {
                    isPreparing = false;
                    updateFilter();
                    initScrubBar();
                    initCropView();
                    blackMask.setVisibility(View.INVISIBLE);
                    cropView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                new ExoPlayerErrorHandler(getContext(), iosDialog -> {
                    iosDialog.dismiss();
                    finish();
                }).handle(error);
            }
        });
    }

    private void setMediaSourceAndPrepare() {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getContext(), Util.getUserAgent(getContext(), "videoediting"));
        MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(VideoInfo.getInstance().getVideoPath()));
        isPreparing = true;
        videoPreview.getPlayer().prepare(videoSource);
    }

    private void initScrubBar() {
        videoScrubBar.setMediaPlayer(videoPreview.getPlayer());
        mainViewModel.progressBarImageList = null;

        if (VideoInfo.getInstance().isTrimmed) {
            videoPreview.getPlayer().seekTo((int) VideoInfo.getInstance().getStartMs());
            mainViewModel.getScrubBerBitmap("trim").observe(getActivity(), bitmap -> videoScrubBar.setBitmap(bitmap));
        } else if (VideoInfo.getInstance().isCut) {
            mainViewModel.getScrubBerBitmap("cut").observe(getActivity(), bitmap -> videoScrubBar.setBitmap(bitmap));
        } else {
            mainViewModel.getScrubBerBitmap("").observe(getActivity(), bitmap -> videoScrubBar.setBitmap(bitmap));
        }

        if (mHandler == null) mHandler = new Handler();
        updateTask = new Runnable() {
            @Override
            public void run() {
                try {
                    if (videoScrubBar.getProgress() < videoPreview.getPlayer().getDuration() && !isVideoPaused) {
                        mHandler.post(this);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };

        videoScrubBar.setOnAnimationListener(new OnAnimationListener() {
            @Override
            public void animationEnd() {
                playBtn.setImageResource(R.drawable.play);
                isVideoPaused = true;
                videoScrubBar.videoAnimator = null;
            }

            @Override
            public void animationStart() {

            }
        });

        videoScrubBar.setOnSeekBarChangeListener(new VideoScrubBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(long progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch() {
                pausePlayer();
                playBtn.setTag("1");
            }

            @Override
            public void onStopTrackingTouch() {
                pausePlayer();
                playBtn.setTag("0");
            }
        });

        videoScrubBar.removeWhiteBorder();
    }

    private void initCropView() {
        cropView.setImageBitmap(getNewBitmap());
        cropView.setCropViewSizeChanged((x, y, mBitmapRect, mSnapRadius, touchPosition) -> {
            cropView.cropImage();
            isCropped = true;
            final String videoSize = Math.round(cropView.getCropWidth()) + "x" + Math.round(cropView.getCropHeight());
            getActivity().runOnUiThread(() -> videoSizeText.setText(videoSize));
        });
    }

    private Bitmap getNewBitmap() {
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = Bitmap.createBitmap((int) VideoInfo.getInstance().getCroppedWidth(), (int) VideoInfo.getInstance().getCroppedHeight(), conf); // this creates a MUTABLE bitmap
        return rotateBitmap(bitmap, VideoInfo.getInstance().getRotation());
    }

    public static Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public void pausePlayer() {
        playBtn.setImageResource(R.drawable.play);
        isVideoPaused = true;
        videoScrubBar.stopAnim();
    }

    public static CropFragment getInstance() {
        return new CropFragment();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (videoPreview.getPlayer() != null) {
            videoPreview.getPlayer().setPlayWhenReady(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        pausePlayer();
    }

    @Override
    public void onResume() {
        super.onResume();
        resumePreview();
    }

    private void resumePreview() {
        try {
            if (videoPreview.getPlayer() != null) {
                videoPreview.getPlayer().setPlayWhenReady(false);
                videoScrubBar.thumbBar.setX(0);
                videoScrubBar.videoAnimator = null;
                videoPreview.getPlayer().seekTo(0);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    public void onBackPressed() {
        finish();
    }
}
