package com.sunfun.slideshow.tools;

import android.content.Context;
import com.coremedia.iso.boxes.Container;
import com.daasuu.mp4compose.composer.Mp4ComposerAudio;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AACTrackImpl;
import com.sunfun.slideshow.interfaces.AudioEditFinishListener;
import com.sunfun.slideshow.interfaces.OnVideoEditFinishListener;
import com.sunfun.slideshow.utils.VideoInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class AudioEditor {

    private Context context;
    String videoPath;
    String finalVideoPath;
    String extractedAudioPath;
    String trimAudioPath;
    String margeAudioPath;
    private AudioEditFinishListener audioEditFinishListener;
    private double currentProgress = 0;
    private Mp4ComposerAudio mp4ComposerAudio;
    private Thread extractedAudioThread;
    private Thread addAudioToVideoThread;
    private FFmpegEditor fFmpegEditor;

    public void cancel() {
        if(mp4ComposerAudio!=null)
            mp4ComposerAudio.cancel();
        if(fFmpegEditor!=null)
            fFmpegEditor.stopProcess();
        if(addAudioToVideoThread!=null)
            addAudioToVideoThread.interrupt();
        if(extractedAudioThread!=null)
            extractedAudioThread.interrupt();
        audioEditFinishListener.onFailEdit("process canceled");
        audioEditFinishListener = null;
    }

    enum Type {EXTRACTION,TRIM,ADD,MARGE}
    Type type;
    double totalProgress;

    AudioEditor(Context context) {
        this.context = context;
    }

    private void trimAudio() {
        mp4ComposerAudio = new Mp4ComposerAudio(VideoInfo.getInstance().getExtraAudioPath(), trimAudioPath);
        mp4ComposerAudio.trim(VideoInfo.getInstance().getExtraAudioStart(), VideoInfo.getInstance().getExtraAudioEnd());
        mp4ComposerAudio.start();
        mp4ComposerAudio.listener(new Mp4ComposerAudio.Listener() {
            @Override
            public void onProgress(double progress) {
                type = Type.TRIM;
                AudioEditor.this.onProgress(progress);
            }

            @Override
            public void onCompleted() {
                totalProgress = currentProgress;
                margeAudio();
            }

            @Override
            public void onCanceled() {
            }

            @Override
            public void onFailed(Exception exception) {
                exception.printStackTrace();
            }
        });
    }

    public void extractAudioFromVideo() {
       extractedAudioThread = new Thread(() -> {
            try {
                type = Type.EXTRACTION;
                Movie videoMovie = MovieCreator.build(videoPath);
                Track audioTracks = null;
                for (Track audioTrack : videoMovie.getTracks()) {
                    if ("soun".equals(audioTrack.getHandler())) {
                        audioTracks = audioTrack;
                    }
                }
                Movie resultMovie = new Movie();
                resultMovie.addTrack(audioTracks);
                Container out = new DefaultMp4Builder().build(resultMovie);
                File extractedAudioFile = new File(extractedAudioPath);
                FileOutputStream fos = new FileOutputStream(extractedAudioFile);

                long finalVideoFileSize = 0;
                for (int i = 0; i < out.getBoxes().size(); i++) {
                    finalVideoFileSize += out.getBoxes().get(i).getSize();
                }
                long finalVideoFileSize1 = finalVideoFileSize;
                Thread t=new Thread(() -> {
                    long currentFileSize = 0;
                    if (extractedAudioFile.exists())
                        currentFileSize = extractedAudioFile.length();
                    while (finalVideoFileSize1 !=currentFileSize) {
                        if (extractedAudioFile.exists()) {
                            currentFileSize = extractedAudioFile.length();
                            float progress = (currentFileSize * 100f / finalVideoFileSize1);
                            AudioEditor.this.onProgress(progress);
                        }
                    }
                    totalProgress = currentProgress;
                    trimAudio();
                });
                t.start();

                try {
                    out.writeContainer(fos.getChannel());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                fos.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
       extractedAudioThread.start();
    }

     public void addAudioToVideo() {
        addAudioToVideoThread = new Thread(() -> {
            type = Type.ADD;
            try {
                AACTrackImpl aacTrack = new AACTrackImpl(new FileDataSourceImpl(margeAudioPath));
                Movie videoMovie = MovieCreator.build(videoPath);
                Track videoTracks = null;// Get the video portion of the video

                //Track audioTracks = null;
                for (Track videoMovieTrack : videoMovie.getTracks()) {
                    if ("vide".equals(videoMovieTrack.getHandler())) {
                        videoTracks = videoMovieTrack;
                    }
                   /* if ("soun".equals(videoMovieTrack.getHandler())) {
                        audioTracks = videoMovieTrack;
                  }*/
                }

                Movie resultMovie = new Movie();
                resultMovie.addTrack(videoTracks);// video section
                resultMovie.addTrack(aacTrack);// audio section
                // resultMovie.addTrack(audioTracks);

                Container out = new DefaultMp4Builder().build(resultMovie);
                File finalVideoFile = new File(finalVideoPath);
                FileOutputStream fos = new FileOutputStream(finalVideoFile);

                long finalVideoFileSize = 0;
                for (int i = 0; i < out.getBoxes().size(); i++) {
                    finalVideoFileSize += out.getBoxes().get(i).getSize();
                }
                long finalVideoFileSize1 = finalVideoFileSize;
                Thread t=new Thread(() -> {
                    long currentFileSize = 0;
                    if (finalVideoFile.exists())
                        currentFileSize = finalVideoFile.length();
                    while (finalVideoFileSize1 !=currentFileSize) {
                        if (finalVideoFile.exists()) {
                            currentFileSize = finalVideoFile.length();
                            float progress = (currentFileSize * 100f / finalVideoFileSize1);
                            AudioEditor.this.onProgress(progress);
                        }
                    }
                    totalProgress = currentProgress;
                    audioEditFinishListener.onFinishEdit(finalVideoPath);
                });
                t.start();

                out.writeContainer(fos.getChannel());
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        addAudioToVideoThread.start();
    }

//    private void addAudioToVideo() {
//        new Thread(() -> {
//            type = Type.ADD;
//            try {
//                MediaExtractor videoExtractor = new MediaExtractor();
//                videoExtractor.setDataSource(videoPath);
//                MediaExtractor audioExtractor = new MediaExtractor();
//                audioExtractor.setDataSource(margeAudioPath);
//                MediaMuxer muxer = new MediaMuxer(finalVideoPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
//                videoExtractor.selectTrack(0);
//                MediaFormat videoFormat = videoExtractor.getTrackFormat(0);
//                int videoTrack = muxer.addTrack(videoFormat);
//
//                audioExtractor.selectTrack(0);
//                MediaFormat audioFormat = audioExtractor.getTrackFormat(0);
//                int audioTrack = muxer.addTrack(audioFormat);
//                boolean sawEOS = false;
//                int offset = 100;
//                int sampleSize = 1024 * 1024;
//                ByteBuffer videoBuf = ByteBuffer.allocate(sampleSize);
//                ByteBuffer audioBuf = ByteBuffer.allocate(sampleSize);
//                MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();
//                MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();
//                videoExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
//                audioExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
//                muxer.start();
//
//                while (!sawEOS) {
//                    videoBufferInfo.offset = offset;
//                    videoBufferInfo.size = videoExtractor.readSampleData(videoBuf, offset);
//                    if (videoBufferInfo.size < 0 || audioBufferInfo.size < 0) {
//                        Log.d("simul", "saw input EOS.");
//                        sawEOS = true;
//                        videoBufferInfo.size = 0;
//                    } else {
//                        videoBufferInfo.presentationTimeUs = videoExtractor.getSampleTime();
//                        videoBufferInfo.flags = videoExtractor.getSampleFlags();
//                        muxer.writeSampleData(videoTrack, videoBuf, videoBufferInfo);
//                        videoExtractor.advance();
//                    }
//                    AudioEditor.this.onProgress(videoBufferInfo.presentationTimeUs/ (float)(VideoUtils.getVideoDurationWithPath(videoPath) * 1000));
//                }
//                boolean sawEOS2 = false;
//                while (!sawEOS2) {
//                    audioBufferInfo.offset = offset;
//                    audioBufferInfo.size = audioExtractor.readSampleData(audioBuf, offset);
//                    if (videoBufferInfo.size < 0 || audioBufferInfo.size < 0) {
//                        //Log.d("simul", "saw input EOS.");
//                        sawEOS2 = true;
//                        audioBufferInfo.size = 0;
//                    } else {
//                        audioBufferInfo.presentationTimeUs = audioExtractor.getSampleTime();
//                        audioBufferInfo.flags = audioExtractor.getSampleFlags();
//                        muxer.writeSampleData(audioTrack, audioBuf, audioBufferInfo);
//                        audioExtractor.advance();
//                    }
//                }
//                muxer.stop();
//                muxer.release();
//            } catch (IOException e) {
//                e.printStackTrace();
//                //Log.d("simul", "Mixer Error 1 " + e.getMessage());
//            } catch (Exception e) {
//                e.printStackTrace();
//                //Log.d("simul", "Mixer Error 2 " + e.getMessage());
//            }
//            totalProgress = currentProgress;
//            audioEditFinishListener.onFinishEdit(finalVideoPath);
//        }).start();
//    }

    public void margeAudio() {
        type = Type.MARGE;
        fFmpegEditor = new FFmpegEditor(context);
        fFmpegEditor.margeAudio(extractedAudioPath, trimAudioPath, margeAudioPath);
        fFmpegEditor.setVideoEditFinishListener(new OnVideoEditFinishListener() {
            @Override
            public void onProgress(double progress) {
               AudioEditor.this.onProgress(progress);
            }

            @Override
            public void onFinishEdit(String path) {
                totalProgress = currentProgress;
                addAudioToVideo();
            }

            @Override
            public void onFailEdit(String message) {

            }
        });
    }

    public void start() {
        extractAudioFromVideo();
    }

    public void setAudioEditFinishListener(AudioEditFinishListener audioEditFinishListener) {
        this.audioEditFinishListener = audioEditFinishListener;
    }


    public void onProgress(double progress){
        if(progress>0.9d)progress = 0.9d;
        if(type == AudioEditor.Type.EXTRACTION){
            progress = progress/20;
        } else if(type == AudioEditor.Type.TRIM){
            progress = progress/7;
        } else if(type == AudioEditor.Type.MARGE){
            progress = progress/3;
        } else if(type == AudioEditor.Type.ADD){
            progress = progress/20;
        }
        currentProgress = progress + totalProgress;
        audioEditFinishListener.onProgress(totalProgress + progress);
    }

}