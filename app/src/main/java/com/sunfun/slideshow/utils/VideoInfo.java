package com.sunfun.slideshow.utils;
import android.media.MediaMetadataRetriever;
import java.io.FileInputStream;

public class VideoInfo {

    public boolean previousFlipH;
    public boolean previousFlipV;
    public float previousRotation;
    public long startMs;
    public long endMs;
    public long totalVideoDuration;
    public boolean isTrimmed;
    public boolean isCut;
    public boolean isFlippedV;
    public boolean isFlippedH;
    public int activeAspectRatioPos = -1;
    public boolean previousCrop = false;
    private int width;
    private int height;
    public boolean rotateFirst;
    private float scaleX;
    public float scaleY;
    public boolean isRotated;
    public float rotation;
    public boolean isCropped;
    public float left;
    public float top;
    public float right;
    public float bottom;
    public float croppedWidth;
    public float croppedHeight;
    private static volatile VideoInfo videoInfoInstance;
    private String videoPath;
    private InitialVideoData initialVideoData;
    public float previousCropX = 0.0f;
    public float previousCropY = 0.0f;
    public float previousCropX1 = 0.0f;
    public float previousCropY1 = 0.0f;
    public float translationX = 0.0f;
    public float translationY = 0.0f;
    private String extraAudioPath;
    private long extraAudioStart;
    private long extraAudioEnd;
    private long extraAudioDuration;
    private float extraAudioVolume = 1;
    private float audioVolume = 1;
    private boolean isFadeIn = true;
    private boolean isFadeOut = true;

    public float getTranslationX() {
        return translationX;
    }

    public void setTranslationX(float translationX) {
        this.translationX = translationX;
    }

    public float getTranslationY() {
        return translationY;
    }

    public void setTranslationY(float translationY) {
        this.translationY = translationY;
    }

    public void setFlippedV(boolean flippedV) {
        isFlippedV = flippedV;
    }


    public void setFlippedH(boolean flippedH) {
        isFlippedH = flippedH;
    }

    public static void setInstanceNull() {
        videoInfoInstance = null;
    }

    public InitialVideoData getInitialVideoData() {
        return initialVideoData;
    }

    public void setInitialVideoData(InitialVideoData initialVideoData) {
        this.initialVideoData = initialVideoData;
    }

    public void setTrimmed(boolean trimmed) {
        isTrimmed = trimmed;
    }

    public void setCut(boolean cut) {
        isCut = cut;
    }

    VideoInfo(){
        if (videoInfoInstance != null){
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }

    public static VideoInfo getInstance() {
        if (videoInfoInstance == null) { //if there is no instance available... create new one
            synchronized (VideoInfo.class) {
                if (videoInfoInstance == null) videoInfoInstance = new VideoInfo();
            }
        }
        return videoInfoInstance;
    }

    public long getDuration() {
        if(VideoInfo.getInstance().isTrimmed){
            return endMs - startMs;
        } else if(VideoInfo.getInstance().isCut) {
            return startMs + (totalVideoDuration - endMs);
        } else {
            return totalVideoDuration;
        }
    }

    public long getEndMs() {
        return endMs;
    }

    public long getStartMs() {
        return startMs;
    }

    public void setStartMs(long startMs) {
        this.startMs = startMs;
    }


    public void setEndMs(long endMs) {
        this.endMs = endMs;
    }

    public void setTotalVideoDuration(long totalVideoDuration) {
        this.totalVideoDuration = totalVideoDuration;
    }

    public void setScaleX(float scaleX) {
        this.scaleX = scaleX;
    }

    public void setScaleY(float scaleY) {
        this.scaleY = scaleY;
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }


    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
        FileInputStream inputStream;
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        try {
            inputStream = new FileInputStream(VideoInfo.getInstance().getVideoPath());
            mediaMetadataRetriever.setDataSource(inputStream.getFD());
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String rotation = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);

        if(rotation.equals("0") || rotation.equals("180")){
            width = Integer.valueOf(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
            height = Integer.valueOf(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
        } else {
            height = Integer.valueOf(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
            width = Integer.valueOf(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
        }

        croppedWidth = width;
        croppedHeight = height;
        right = width;
        bottom = height;
        previousCropX1 = width;
        previousCropY1 = height;
        mediaMetadataRetriever.release();
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getVideoPath(){
        return videoPath;
    }

    public long getTotalVideoDuration() {
        return totalVideoDuration;
    }

    public void setRotated(boolean rotated) {
        isRotated = rotated;
    }

    public void setCropped(boolean cropped) {
        isCropped = cropped;
    }

    public float getLeft() {
        return left;
    }

    public void setLeft(float left) {
        this.left = left;
    }

    public float getTop() {
        return top;
    }

    public float getRight() {
        return right;
    }

    public void setRight(float right) {
        this.right = right;
    }

    public float getBottom() {
        return bottom;
    }

    public void setBottom(float bottom) {
        this.bottom = bottom;
    }

    public void setTop(float top) {
        this.top = top;
    }

    public float getCroppedWidth() {
        return croppedWidth;
    }

    public void setCroppedWidth(float croppedWidth) {
        this.croppedWidth = croppedWidth;
    }

    public float getCroppedHeight() {
        return croppedHeight;
    }

    public void setCroppedHeight(float croppedHeight) {
        this.croppedHeight = croppedHeight;
    }

    public void updateVideoInfo(long duration, float scaleX, float scaleY, float rotation) {
        setVideoPath(getVideoPath());
        setTotalVideoDuration(duration);

        setLeft(0);
        setRight(0);
        setRight(getWidth());
        setBottom(getHeight());

        if (!isFlippedV && !isFlippedH && getInitialVideoData() != null) {
            setScaleX(getInitialVideoData().getInitialScaleX());
            setScaleY(getInitialVideoData().getInitialScaleX());
        }

        if (!isRotated && getInitialVideoData() != null) {
            setRotation(getInitialVideoData().getInitialRotation());
        }

        if (!isTrimmed && !isCut) {
            setStartMs(0);
            setEndMs(duration);
        }

        if (getInitialVideoData() == null) {
            setInitialVideoData(new InitialVideoData());
            getInitialVideoData().setInitialScaleX(scaleX);
            getInitialVideoData().setInitialScaleY(scaleY);
            getInitialVideoData().setInitialRotation(rotation);
        }
    }

    public void resetVideoInfo(String originalPath) {
        setVideoPath(originalPath);
        setLeft(0);
        setTop(0);
        setRight(getWidth());
        setBottom(getHeight());
        setCroppedWidth(getWidth());
        setCroppedHeight(getHeight());
        setRotation(0);
        previousCropX = 0;
        previousCropY = 0;
        previousCropX1 = 0;
        previousCropY1 = 0;

        previousFlipH = false;
        previousFlipV = false;

        isRotated = false;
        isFlippedV = false;
        isFlippedH = false;
        isTrimmed = false;
        isCut = false;
        isCropped = false;
    }

    public void setExtraAudioPath(String extraAudioPath) {
        this.extraAudioPath = extraAudioPath;
    }

    public String getExtraAudioPath() {
        return extraAudioPath;
    }

    public void setExtraAudioStart(long extraAudioStart) {
        this.extraAudioStart = extraAudioStart;
    }

    public long getExtraAudioStart() {
        return extraAudioStart;
    }

    public void setExtraAudioEnd(long extraAudioEnd) {
        this.extraAudioEnd = extraAudioEnd;
    }

    public long getExtraAudioEnd() {
        return extraAudioEnd;
    }

    public void setExtraAudioDuration(long extraAudioDuration) {
        this.extraAudioDuration = extraAudioDuration;
    }

    public long getExtraAudioDuration() {
        return extraAudioDuration;
    }

    public void setExtraAudioVolume(float extraAudioVolume) {
        this.extraAudioVolume = extraAudioVolume; //min 0 max 1
    }

    public float getExtraAudioVolume() {
        return extraAudioVolume;
    }

    public void setAudioVolume(float audioVolume) {
        this.audioVolume = audioVolume;
    }

    public float getAudioVolume() {
        return audioVolume;
    }

    public void setFadeIn(boolean bool) {
        isFadeIn = bool;
    }

    public boolean isFadeIn() {
        return isFadeIn;
    }

    public boolean isFadeOut() {
        return isFadeOut;
    }

    public void setFadeOut(boolean fadeOut) {
        isFadeOut = fadeOut;
    }

    public static class InitialVideoData {
        float initialScaleX;
        float initialScaleY;
        float initialRotation;

        float getInitialScaleX() { return initialScaleX; }

        void setInitialScaleX(float initialScaleX) { this.initialScaleX = initialScaleX; }

        void setInitialScaleY(float initialScaleY) { this.initialScaleY = initialScaleY; }

        public float getInitialRotation() {
            return initialRotation;
        }

        void setInitialRotation(float initialRotation) { this.initialRotation = initialRotation; }
    }

    public int getActiveAspectRatioPos() {
        return activeAspectRatioPos;
    }

    public void setActiveAspectRatioPos(int activeAspectRatioPos) {
        this.activeAspectRatioPos = activeAspectRatioPos;
    }
}
