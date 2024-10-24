package com.daasuu.mp4compose.filter.Overlay;

public class OverlayInfo {

    public int width;
    public int height;
    public float rotation;
    public float scale;
    public float duration;
    private int customRotaton;
    private boolean horizonatalFlip;
    private boolean verticalFilp;

    public OverlayInfo(){

        width = 0;
        height = 0;
        rotation = 0.0f;
        scale = 0.0f;
        duration = 0.0f;
        this.customRotaton = 0;
        this.horizonatalFlip = false;
        this.verticalFilp = false;
    }

    public float getRotation() {

        return rotation;
    }

    public float getScale() {
        return scale;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public float getDuration() {
        return duration;
    }

    public void setCustomRotaton(int customRotaton) {
        this.customRotaton = customRotaton;
    }

    public int getCustomRotaton() {
        return customRotaton;
    }

    public void setHorizonatalFlip(boolean horizonatalFlip) {
        this.horizonatalFlip = horizonatalFlip;
    }

    public void setVerticalFilp(boolean verticalFilp) {
        this.verticalFilp = verticalFilp;
    }

    public boolean isHorizonatalFlip() {
        return horizonatalFlip;
    }

    public boolean isVerticalFilp() {
        return verticalFilp;
    }
}
