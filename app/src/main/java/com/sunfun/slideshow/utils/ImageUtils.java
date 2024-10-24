package com.sunfun.slideshow.utils;

import android.graphics.Bitmap;

public class ImageUtils {

	private static final int SCALE_OEF = 3;

	public static Bitmap getScaledBitmap(Bitmap frame_orig) {
		int w_orig = frame_orig.getWidth();
    	int h_orig = frame_orig.getHeight();
    	float ratio_orig = (float) w_orig / h_orig;

    	int w_new = w_orig / SCALE_OEF;
    	int h_new = (int) ((float) w_new / ratio_orig);

    	return Bitmap.createScaledBitmap(frame_orig, w_new, h_new, true);
	}
}
