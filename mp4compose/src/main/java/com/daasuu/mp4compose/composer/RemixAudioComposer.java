package com.daasuu.mp4compose.composer;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import com.daasuu.mp4compose.SampleType;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

// Refer:  https://github.com/ypresto/android-transcoder/blob/master/lib/src/main/java/net/ypresto/androidtranscoder/engine/AudioTrackTranscoder.java

/**
 * Created by sudamasayuki2 on 2018/02/22.
 */

class RemixAudioComposer implements IAudioComposer {
    private static final SampleType SAMPLE_TYPE = SampleType.AUDIO;

    private static final int DRAIN_STATE_NONE = 0;
    private static final int DRAIN_STATE_SHOULD_RETRY_IMMEDIATELY = 1;
    private static final int DRAIN_STATE_CONSUMED = 2;
    private static final int BUFFER_SIZE = 64 * 1024; // I have no idea whether this value is appropriate or not...

    private final MediaExtractor extractor;
    private final MuxRender muxer;
    private long writtenPresentationTimeUs;

    private final int trackIndex;
    private int muxCount = 1;

    private final MediaFormat outputFormat;

    private final MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
    private MediaCodec decoder;
    private MediaCodec encoder;
    private MediaFormat actualOutputFormat;

    private boolean isExtractorEOS;
    private boolean isDecoderEOS;
    private boolean isEncoderEOS;
    private boolean decoderStarted;
    private boolean encoderStarted;

    private AudioChannel audioChannel;
    private final int timeScale;

    private final long trimStartUs;
    private final long trimEndUs;

    // Used for AAC priming offset.
    private boolean addPrimingDelay;
    private int frameCounter;
    private long primingDelay;
    private long fadeInDurationMillis = 5000;

    public RemixAudioComposer(MediaExtractor extractor, int trackIndex,
                              MediaFormat outputFormat, MuxRender muxer, int timeScale,
                              long trimStartMs, long trimEndMs) {
        this.extractor = extractor;
        this.trackIndex = trackIndex;
        this.outputFormat = outputFormat;
        this.muxer = muxer;
        this.timeScale = timeScale;
        this.trimStartUs = TimeUnit.MILLISECONDS.toMicros(trimStartMs);
        this.trimEndUs = trimEndMs == -1 ? trimEndMs : TimeUnit.MILLISECONDS.toMicros(trimEndMs);
    }

    @Override
    public void setup() {
        extractor.selectTrack(trackIndex);
        try {
            encoder = MediaCodec.createEncoderByType(outputFormat.getString(MediaFormat.KEY_MIME));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        encoder.configure(outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        encoder.start();
        encoderStarted = true;

        final MediaFormat inputFormat = extractor.getTrackFormat(trackIndex);
        extractor.seekTo(trimStartUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);

        try {
            decoder = MediaCodec.createDecoderByType(inputFormat.getString(MediaFormat.KEY_MIME));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        decoder.configure(inputFormat, null, null, 0);
        decoder.start();
        decoderStarted = true;

        audioChannel = new AudioChannel(decoder, encoder, outputFormat);
    }

    @Override
    public boolean stepPipeline() {
        boolean busy = false;

        int status;
        while (drainEncoder(0) != DRAIN_STATE_NONE) busy = true;
        do {
            status = drainDecoder(0);
            if (status != DRAIN_STATE_NONE) busy = true;
            // NOTE: not repeating to keep from deadlock when encoder is full.
        } while (status == DRAIN_STATE_SHOULD_RETRY_IMMEDIATELY);

        while (audioChannel.feedEncoder(0)) busy = true;
        while (drainExtractor(0) != DRAIN_STATE_NONE) busy = true;

        return busy;
    }

    private int drainExtractor(long timeoutUs) {
        if (isExtractorEOS) return DRAIN_STATE_NONE;
        int trackIndex = extractor.getSampleTrackIndex();
        if (trackIndex >= 0 && trackIndex != this.trackIndex) {
            return DRAIN_STATE_NONE;
        }

        final int result = decoder.dequeueInputBuffer(timeoutUs);
        if (result < 0) return DRAIN_STATE_NONE;
        if (trackIndex < 0) {
            isExtractorEOS = true;
            decoder.queueInputBuffer(result, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            return DRAIN_STATE_NONE;
        }

        final int sampleSize = extractor.readSampleData(decoder.getInputBuffer(result), 0);
        final boolean isKeyFrame = (extractor.getSampleFlags() & MediaExtractor.SAMPLE_FLAG_SYNC) != 0;
        decoder.queueInputBuffer(result, 0, sampleSize, extractor.getSampleTime(), isKeyFrame ? MediaCodec.BUFFER_FLAG_KEY_FRAME : 0);
        extractor.advance();
        return DRAIN_STATE_CONSUMED;
    }

    private int drainDecoder(long timeoutUs) {
        if (isDecoderEOS) return DRAIN_STATE_NONE;

        int result = decoder.dequeueOutputBuffer(bufferInfo, timeoutUs);
        switch (result) {
            case MediaCodec.INFO_TRY_AGAIN_LATER:
                return DRAIN_STATE_NONE;
            case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                audioChannel.setActualDecodedFormat(decoder.getOutputFormat());
            case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                return DRAIN_STATE_SHOULD_RETRY_IMMEDIATELY;
        }

        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            isDecoderEOS = true;
            audioChannel.drainDecoderBufferAndQueue(AudioChannel.BUFFER_INDEX_END_OF_STREAM, 0);
        } else if (bufferInfo.size > 0) {
            audioChannel.drainDecoderBufferAndQueue(result, bufferInfo.presentationTimeUs / timeScale);
            if (trimEndUs != -1 && bufferInfo.presentationTimeUs >= trimEndUs) {
                isDecoderEOS = true;
                audioChannel.drainDecoderBufferAndQueue(AudioChannel.BUFFER_INDEX_END_OF_STREAM, 0);
            }
        }
        return DRAIN_STATE_CONSUMED;
    }

    private int drainEncoder(long timeoutUs) {
        if (isEncoderEOS) return DRAIN_STATE_NONE;

        int result = encoder.dequeueOutputBuffer(bufferInfo, timeoutUs);
        switch (result) {
            case MediaCodec.INFO_TRY_AGAIN_LATER:
                return DRAIN_STATE_NONE;
            case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                if (actualOutputFormat != null) {
                    throw new RuntimeException("Audio output format changed twice.");
                }
                actualOutputFormat = encoder.getOutputFormat();
                addPrimingDelay = MediaFormat.MIMETYPE_AUDIO_AAC.equals(actualOutputFormat.getString(MediaFormat.KEY_MIME));
                muxer.setOutputFormat(SAMPLE_TYPE, actualOutputFormat);
                muxer.onSetOutputFormat();
                return DRAIN_STATE_SHOULD_RETRY_IMMEDIATELY;
            case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                return DRAIN_STATE_SHOULD_RETRY_IMMEDIATELY;
        }

        if (actualOutputFormat == null) {
            throw new RuntimeException("Could not determine actual output format.");
        }

        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            isEncoderEOS = true;
            bufferInfo.set(0, 0, 0, bufferInfo.flags);
        }
        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            // SPS or PPS, which should be passed by MediaFormat.
            encoder.releaseOutputBuffer(result, false);
            return DRAIN_STATE_SHOULD_RETRY_IMMEDIATELY;
        }

        if (muxCount == 1) {
            muxer.writeSampleData(SAMPLE_TYPE, encoder.getOutputBuffer(result), bufferInfo);
        }
        if (muxCount < timeScale) {
            muxCount++;
        } else {
            muxCount = 1;
        }

        writtenPresentationTimeUs = bufferInfo.presentationTimeUs;
        encoder.releaseOutputBuffer(result, false);
        return DRAIN_STATE_CONSUMED;
    }


    @Override
    public long getWrittenPresentationTimeUs() {
        return writtenPresentationTimeUs;
    }

    @Override
    public boolean isFinished() {
        return isEncoderEOS;
    }

    @Override
    public void release() {
        if (decoder != null) {
            if (decoderStarted) decoder.stop();
            decoder.release();
            decoder = null;
        }
        if (encoder != null) {
            if (encoderStarted) encoder.stop();
            encoder.release();
            encoder = null;
        }
    }

//    public void fadeIn(int inBufferId, int outBufferId){
//        if (bufferInfo.presentationTimeUs < fadeInDurationMillis * 1000) {
//            ByteBuffer inBuffer = encoder.getInputBuffer(inBufferId);
//            ByteBuffer outBuffer = decoder.getOutputBuffer(outBufferId);
//            ShortBuffer shortSamples = outBuffer.order(ByteOrder.nativeOrder()).asShortBuffer();
//            MediaFormat format = decoder.getOutputFormat(outBufferId);
//            int channels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
//            int size = shortSamples.remaining();
//            double sampleDurationMillis = 1000L / format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
//            long elapsedMillis = 0L;
//            double totalElapsedMillis = bufferInfo.presentationTimeUs / 1000;
//
//            for (int i = 0; i < size; i += channels) {
//                for (int c = 0; c < channels; c++) {
//                    try {
//                        // Process the sample
//                        short sample = shortSamples.get();
//                        // Put processed sample into encoder's buffer
//                        inBuffer.putShort(sample);
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    }
//                }
//                elapsedMillis += sampleDurationMillis;
//            }
//        }
//    }
//
//    public double getFactor(long elapsedMillis, double totalElapsedMillis) {
//        // How much progress since the start of fade in effect?
//        double progress = (totalElapsedMillis + elapsedMillis) / fadeInDurationMillis;
//
//        // Using exponential factor to increase volume
//        return progress * progress;
//    }

}



