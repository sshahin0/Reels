package com.daasuu.mp4compose.composer;

public class SpeedControlCallback implements MoviePlayer.FrameCallback {
    @Override
    public void preRender(long presentationTimeUsec) {
        //Log.d("SpeedControl", "preRender:  presentation time "+ presentationTimeUsec);
        try {
            Thread.sleep(30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void postRender() {

    }

    @Override
    public void loopReset() {

    }
}
