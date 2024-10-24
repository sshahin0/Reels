package com.daasuu.mp4compose.composer;

import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaMuxer;
import android.os.Build;
import android.util.Log;
import android.util.Size;

import androidx.annotation.NonNull;

import com.daasuu.mp4compose.FillMode;
import com.daasuu.mp4compose.FillModeCustomItem;
import com.daasuu.mp4compose.Rotation;
import com.daasuu.mp4compose.filter.GlFilter;
import com.daasuu.mp4compose.logger.Logger;
import com.daasuu.mp4compose.source.DataSource;

import java.io.FileDescriptor;
import java.io.IOException;


// Refer: https://github.com/ypresto/android-transcoder/blob/master/lib/src/main/java/net/ypresto/androidtranscoder/engine/MediaTranscoderEngine.java

/**
 * Internal engine, do not use this directly.
 */
class Mp4ComposerAudioEngine {

    private static final String TAG = Mp4ComposerAudioEngine.class.getName();
    private static final String VIDEO_PREFIX = "video/";
    private static final double PROGRESS_UNKNOWN = -1.0;
    private static final long SLEEP_TO_WAIT_TRACK_TRANSCODERS = 10;
    private static final long PROGRESS_INTERVAL_STEPS = 10;
    private IAudioComposer audioComposer;
    private MediaExtractor mediaExtractor;
    private MediaMuxer mediaMuxer;
    private ProgressCallback progressCallback;
    private long durationUs;
    private MediaMetadataRetriever mediaMetadataRetriever;
    private final Logger logger;

    public Mp4ComposerAudioEngine(@NonNull final Logger logger) {
        this.logger = logger;
    }

    void setProgressCallback(ProgressCallback progressCallback) {
        this.progressCallback = progressCallback;
    }

    void compose(
            final DataSource srcDataSource,
            final String destSrc,
            final FileDescriptor destFileDescriptor,
            final boolean mute,
            final int timeScale,
            final long trimStartMs,
            final long trimEndMs
    ) throws IOException {
        try {
            mediaExtractor = new MediaExtractor();
            mediaExtractor.setDataSource(srcDataSource.getFileDescriptor());
            if (Build.VERSION.SDK_INT >= 26 && destSrc == null) {
                mediaMuxer = new MediaMuxer(destFileDescriptor, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            } else {
                mediaMuxer = new MediaMuxer(destSrc, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            }
            mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(srcDataSource.getFileDescriptor());
            try {
                durationUs = Long.parseLong(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) * 1000;
            } catch (NumberFormatException e) {
                durationUs = -1;
            }
            logger.debug(TAG, "Duration (us): " + durationUs);

            MuxRender muxRender = new MuxRender(mediaMuxer, logger);

            // identify track indices
            MediaFormat format = mediaExtractor.getTrackFormat(0);
            String mime = format.getString(MediaFormat.KEY_MIME);

            final int videoTrackIndex;
            final int audioTrackIndex;

            if (mime.startsWith(VIDEO_PREFIX)) {
                videoTrackIndex = 0;
                audioTrackIndex = 1;
            } else {
                videoTrackIndex = 1;
                audioTrackIndex = 0;
            }

            // setup audio if present and not muted
            if (mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO) != null && !mute) {
                // has Audio video
                final MediaFormat inputMediaFormat = mediaExtractor.getTrackFormat(audioTrackIndex);
                final MediaFormat outputMediaFormat = createAudioOutputFormat(inputMediaFormat);
                audioComposer = new RemixAudioComposer(mediaExtractor, audioTrackIndex, outputMediaFormat, muxRender, timeScale, trimStartMs, trimEndMs);


                /*if (timeScale < 2 && outputMediaFormat.equals(inputMediaFormat)) {
                    audioComposer = new AudioComposer(mediaExtractor, audioTrackIndex, muxRender, trimStartMs, trimEndMs, logger);
                } else {
                    audioComposer = new RemixAudioComposer(mediaExtractor, audioTrackIndex, outputMediaFormat, muxRender, timeScale, trimStartMs, trimEndMs);
                }*/

                audioComposer.setup();
                mediaExtractor.selectTrack(audioTrackIndex);
                runPipelines();
            }

            mediaMuxer.stop();

        } finally {
            try {
                if (audioComposer != null) {
                    audioComposer.release();
                    audioComposer = null;
                }
                if (mediaExtractor != null) {
                    mediaExtractor.release();
                    mediaExtractor = null;
                }
            } catch (RuntimeException e) {
                logger.error(TAG, "Could not shutdown mediaExtractor, codecs and mediaMuxer pipeline.", e);
            }
            try {
                if (mediaMuxer != null) {
                    mediaMuxer.release();
                    mediaMuxer = null;
                }
            } catch (RuntimeException e) {
                logger.error(TAG, "Failed to release mediaMuxer.", e);
            }
            try {
                if (mediaMetadataRetriever != null) {
                    mediaMetadataRetriever.release();
                    mediaMetadataRetriever = null;
                }
            } catch (RuntimeException e) {
                logger.error(TAG, "Failed to release mediaMetadataRetriever.", e);
            }
        }


    }

    @NonNull
    private static MediaFormat createVideoOutputFormatWithAvailableEncoders(final int bitrate,
                                                                            @NonNull final Size outputResolution) {
        final MediaCodecList mediaCodecList = new MediaCodecList(MediaCodecList.REGULAR_CODECS);

        final MediaFormat hevcMediaFormat = createVideoFormat(MediaFormat.MIMETYPE_VIDEO_HEVC, bitrate, outputResolution);
        if (mediaCodecList.findEncoderForFormat(hevcMediaFormat) != null) {
            return hevcMediaFormat;
        }

        final MediaFormat avcMediaFormat = createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, bitrate, outputResolution);
        if (mediaCodecList.findEncoderForFormat(avcMediaFormat) != null) {
            return avcMediaFormat;
        }

        final MediaFormat mp4vesMediaFormat = createVideoFormat(MediaFormat.MIMETYPE_VIDEO_MPEG4, bitrate, outputResolution);
        if (mediaCodecList.findEncoderForFormat(mp4vesMediaFormat) != null) {
            return mp4vesMediaFormat;
        }

        return createVideoFormat(MediaFormat.MIMETYPE_VIDEO_H263, bitrate, outputResolution);
    }

    @NonNull
    private static MediaFormat createAudioOutputFormat(@NonNull final MediaFormat inputFormat) {
        if (MediaFormat.MIMETYPE_AUDIO_AAC.equals(inputFormat.getString(MediaFormat.KEY_MIME))) {
            return inputFormat;
        } else {
            final MediaFormat outputFormat = new MediaFormat();
            outputFormat.setString(MediaFormat.KEY_MIME, "audio/mp4a-latm");
            outputFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectELD);
            outputFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, inputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE));
            outputFormat.setInteger(MediaFormat.KEY_BIT_RATE, inputFormat.getInteger(MediaFormat.KEY_BIT_RATE));
            outputFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, inputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT));
            outputFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE,1048576);
            return outputFormat;
        }
    }

    @NonNull
    private static MediaFormat createVideoFormat(@NonNull final String mimeType,
                                                 final int bitrate,
                                                 @NonNull final Size outputResolution) {
        final MediaFormat outputFormat =
                MediaFormat.createVideoFormat(mimeType,
                        outputResolution.getWidth(),
                        outputResolution.getHeight());

        outputFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
        // Required but ignored by the encoder
        outputFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
        outputFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        outputFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);

        return outputFormat;
    }


    private void runPipelines() {
        long loopCount = 0;
        if (durationUs <= 0) {
            if (progressCallback != null) {
                progressCallback.onProgress(PROGRESS_UNKNOWN);
            }// unknown
        }
        while (!(audioComposer.isFinished())) {

            if(loopCount == 8059){
                Log.d("simulaudio", loopCount + " loop count");
            }

            boolean stepped = audioComposer.stepPipeline();
            loopCount++;
            if (durationUs > 0 && loopCount % PROGRESS_INTERVAL_STEPS == 0) {
                double audioProgress = audioComposer.isFinished() ? 1.0 : Math.min(1.0, (double) audioComposer.getWrittenPresentationTimeUs() / durationUs);
                double progress = (audioProgress) / 2.0;
                if (progressCallback != null) {
                    progressCallback.onProgress(progress);
                }
            }
            if (!stepped) {
                try {
                    Thread.sleep(SLEEP_TO_WAIT_TRACK_TRANSCODERS);
                } catch (InterruptedException e) {
                    // nothing to do
                }
            }
        }
    }



    interface ProgressCallback {
        /**
         * Called to notify progress. Same thread which initiated transcode is used.
         *
         * @param progress Progress in [0.0, 1.0] range, or negative value if progress is unknown.
         */
        void onProgress(double progress);
    }
}
