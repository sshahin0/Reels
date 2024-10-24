package com.sunfun.slideshow.tools;

import android.content.Context;

import com.coremedia.iso.boxes.Container;
import com.daasuu.mp4compose.composer.Mp4Composer;
import com.daasuu.mp4compose.filter.GlFilter;
import com.daasuu.mp4compose.filter.GlFilterGroup;
import com.daasuu.mp4compose.filter.GlTransformFilter;
import com.daasuu.mp4compose.filter.Overlay.GlVideoOverlayFilter;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.sunfun.slideshow.R;
import com.sunfun.slideshow.interfaces.AudioEditFinishListener;
import com.sunfun.slideshow.interfaces.OnVideoEditFinishListener;
import com.sunfun.slideshow.utils.PathUtils;
import com.sunfun.slideshow.utils.VideoInfo;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;

public class VideoEditor {

    private OnVideoEditFinishListener videoEditFinishListener;
    private Mp4Composer mp4Composer;
    private Context context;
    private String TAG = VideoEditor.class.getName();
    private double totalProgress = 0;
    private AudioEditor audioEditor;
    private Thread appendVideoThread;


    public VideoEditor(Context context) {
        this.context = context;
    }

    public void executeTrimVideoCommand(long startMs, long endMs) {
        totalProgress = 0;
        GlFilter.resetFilter();
        String filePath;

        if (VideoInfo.getInstance().getExtraAudioPath() != null || VideoInfo.getInstance().isCut) {
            filePath = PathUtils.generateTempPath(System.currentTimeMillis() + "video", ".mp4");
        } else {
            filePath = PathUtils.generateRealPath(System.currentTimeMillis() + "finalVideo", ".mp4");
        }

        VideoInfo videoInfo = VideoInfo.getInstance();
        int finalWidth;
        int finalHeight;
        if (VideoInfo.getInstance().getRotation() == 90 || VideoInfo.getInstance().getRotation() == 270) {
            finalWidth = (int) VideoInfo.getInstance().getCroppedHeight();
            finalHeight = (int) VideoInfo.getInstance().getCroppedWidth();
        } else {
            finalWidth = (int) VideoInfo.getInstance().getCroppedWidth();
            finalHeight = (int) VideoInfo.getInstance().getCroppedHeight();
        }

        mp4Composer = new Mp4Composer(videoInfo.getVideoPath(), filePath);
        mp4Composer.size(finalWidth, finalHeight);

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


        /*final int[] x = {0};
        final int[] y = {0};
        GlOverlayFilter glOverlayFilter = new GlOverlayFilter() {
            @Override
            protected void drawCanvas(Canvas canvas) {
                Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.email);
                Paint paint = new Paint();
                paint.setAntiAlias(true);
                paint.setFilterBitmap(true);
                paint.setDither(true);
                Bitmap finalBitmap = bitmap;
                canvas.drawBitmap(finalBitmap, x[0] += 1, y[0] += 1, paint);
                releaseBitmap(bitmap);

            }
        };*/

        GlVideoOverlayFilter videoOverlayFilter = new GlVideoOverlayFilter(context, R.raw.video_petals);

        GlFilterGroup glFilterGroup = new GlFilterGroup(videoOverlayFilter);


        mp4Composer.filter(glFilterGroup);

        mp4Composer.trim(startMs, endMs);
        mp4Composer.listener(new Mp4Composer.Listener() {
            @Override
            public void onProgress(double progress) {
                if (VideoInfo.getInstance().getExtraAudioPath() != null) {
                    progress = progress / 2;
                }
                totalProgress = progress;
                if (videoEditFinishListener != null)
                    progress = progress + .01f;
                if (progress > 1) progress = 1;
                videoEditFinishListener.onProgress(progress);
            }

            @Override
            public void onCompleted() {
                if (VideoInfo.getInstance().getExtraAudioPath() == null || VideoInfo.getInstance().isCut) {
                    if (videoEditFinishListener == null) return;
                    videoEditFinishListener.onFinishEdit(filePath);
                    return;
                }

                editAudio(filePath);
            }

            @Override
            public void onCanceled() {
            }

            @Override
            public void onFailed(Exception exception) {
                exception.printStackTrace();
                videoEditFinishListener.onFailEdit(exception.getMessage());
            }
        });
        mp4Composer.start();
    }

    private void editAudio(String filePath) {
        audioEditor = new AudioEditor(context);
        audioEditor.videoPath = filePath;
        audioEditor.extractedAudioPath = PathUtils.generateTempPath(System.currentTimeMillis() + "extractedAudio", ".aac");
        audioEditor.trimAudioPath = PathUtils.generateTempPath(System.currentTimeMillis() + "trimAudio", ".aac");
        audioEditor.margeAudioPath = PathUtils.generateTempPath(System.currentTimeMillis() + "mergeAudio", ".aac");
        audioEditor.finalVideoPath = PathUtils.generateRealPath(System.currentTimeMillis() + "finalVideo", ".mp4");
        audioEditor.start();
        audioEditor.setAudioEditFinishListener(new AudioEditFinishListener() {
            @Override
            public void onProgress(double progress) {
                if (videoEditFinishListener != null)
                    progress = totalProgress + progress + .01f;
                if (progress > 1) progress = 1;
                videoEditFinishListener.onProgress(progress);
            }

            @Override
            public void onFinishEdit(String path) {
                if (videoEditFinishListener == null) return;
                videoEditFinishListener.onFinishEdit(path);
            }

            @Override
            public void onFailEdit(String message) {
                if (videoEditFinishListener == null) return;
                videoEditFinishListener.onFailEdit(message);
            }
        });
    }

    public void setOnFinishListener(OnVideoEditFinishListener onVideoEditFinishListener) {
        this.videoEditFinishListener = onVideoEditFinishListener;
    }

    public void stopProcess() {
        if (mp4Composer != null) mp4Composer.cancel();
        if (audioEditor != null) audioEditor.cancel();
        if (appendVideoThread != null) appendVideoThread.interrupt();
    }

    public void concatenate(String[] paths) {
        String filePath;
        if (VideoInfo.getInstance().getExtraAudioPath() != null) {
            filePath = PathUtils.generateTempPath(System.currentTimeMillis() + "video", ".mp4");
        } else {
            filePath = PathUtils.generateRealPath(System.currentTimeMillis() + "finalVideo", ".mp4");
        }
        appendVideo(paths, filePath);
    }

    private void appendVideo(String[] videos, String outPath) {
        appendVideoThread = new Thread(() -> {
            try {
                Movie[] inMovies = new Movie[videos.length];
                int index = 0;
                for (String video : videos) {
                    inMovies[index] = MovieCreator.build(video);
                    index++;
                }
                List<Track> videoTracks = new LinkedList<Track>();
                List<Track> audioTracks = new LinkedList<Track>();
                for (Movie m : inMovies) {
                    for (Track t : m.getTracks()) {
                        if (t.getHandler().equals("soun")) {
                            audioTracks.add(t);
                        }
                        if (t.getHandler().equals("vide")) {
                            videoTracks.add(t);
                        }
                    }
                }

                Movie result = new Movie();
                if (audioTracks.size() > 0) {
                    result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
                }
                if (videoTracks.size() > 0) {
                    result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));
                }

                Container out = new DefaultMp4Builder().build(result);
                FileChannel fc = new RandomAccessFile(outPath, "rw").getChannel();
                out.writeContainer(fc);
                fc.close();

                if (VideoInfo.getInstance().getExtraAudioPath() == null) {
                    if (videoEditFinishListener == null) return;
                    videoEditFinishListener.onFinishEdit(outPath);
                    return;
                }
                editAudio(outPath);

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        appendVideoThread.start();
    }
}