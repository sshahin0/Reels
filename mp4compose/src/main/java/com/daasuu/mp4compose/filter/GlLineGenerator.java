package com.daasuu.mp4compose.filter;

import android.util.Pair;

import com.daasuu.mp4compose.filter.time_filter.GlFilterWithTime;
import com.daasuu.mp4compose.gl.GlFramebufferObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

public class GlLineGenerator extends GlFilterWithTime {

    private GlBandMaskGenerator bandMask;
    private HashMap<Float, Pair<Float, Float>> mapPositionIndexWithTime = new HashMap<Float, Pair<Float, Float>>();
    private float[] timeStamp;
    private boolean isHorizontal;

    public GlLineGenerator() {
        bandMask = new GlBandMaskGenerator();
    }

    @Override
    public void setup() {
        bandMask.setup();
    }

    @Override
    public void release() {
        bandMask.release();
    }

    @Override
    public void setFrameSize(int width, int height) {
        bandMask.setFrameSize(width, height);
    }

    @Override
    public void draw(int texName, GlFramebufferObject fbo) {
        UpdateMaskPostionWithTime();
        bandMask.draw(texName, fbo);
    }

    private void UpdateMaskPostionWithTime() {

        if (mapPositionIndexWithTime.isEmpty()) {
            return;
        }

        ArrayList<Float> timeStamp = new ArrayList<Float>();
        timeStamp.addAll(mapPositionIndexWithTime.keySet());
        Collections.sort(timeStamp);


        float inputTime = getInputTimePeriod();

        int prevPosition = -1;

        for (int i = 0; i < timeStamp.size(); i++) {
            if (timeStamp.get(i).floatValue() > inputTime) {
                break;
            }
            prevPosition = i;
        }

        int nextPosition = prevPosition + 1;

        nextPosition = (nextPosition + timeStamp.size()) % timeStamp.size();
        prevPosition = (prevPosition + timeStamp.size()) % timeStamp.size();

        float nextIntense = 0.0f;

        if (nextPosition < prevPosition) {
            float timeDiff = (float) (1.0f - timeStamp.get(prevPosition).floatValue() + timeStamp.get(nextPosition).floatValue() + 1e-6);

            if (inputTime > timeStamp.get(prevPosition).floatValue()) {

                nextIntense = (inputTime - timeStamp.get(prevPosition).floatValue()) / timeDiff;
            } else {

                nextIntense = (inputTime + 1.0f - timeStamp.get(prevPosition).floatValue()) / timeDiff;
            }

        } else {
            float timeDiff = timeStamp.get(nextPosition).floatValue() - timeStamp.get(prevPosition).floatValue();

            nextIntense = (inputTime - timeStamp.get(prevPosition).floatValue()) / timeDiff;
        }

        float prevIntense = 1f - nextIntense;

        float lowerPosition = (1f - mapPositionIndexWithTime.get(Float.valueOf(timeStamp.get(prevPosition).floatValue())).first.floatValue()) * prevIntense +
                (1f - mapPositionIndexWithTime.get(Float.valueOf(timeStamp.get(nextPosition).floatValue())).first.floatValue()) * nextIntense;

        float upperPosition = (1f - mapPositionIndexWithTime.get(Float.valueOf(timeStamp.get(prevPosition).floatValue())).second.floatValue()) * prevIntense +
                (1f - mapPositionIndexWithTime.get(Float.valueOf(timeStamp.get(nextPosition).floatValue())).second.floatValue()) * nextIntense;


        float x1, x2, y1, y2;

        if (isHorizontal) {
            x1 = 0f;
            x2 = 1f;
            y1 = Math.min(lowerPosition, upperPosition);
            y2 = Math.max(lowerPosition, upperPosition);
        } else {
            x1 = Math.min(lowerPosition, upperPosition);
            x2 = Math.max(lowerPosition, upperPosition);
            y1 = 0;
            y2 = 1f;
        }

        bandMask.setBandPosition(x1, y1, x2, y2);

    }

    public void setTimeStamp(float[] timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setHorizontalOrientation(boolean horizonal) {
        isHorizontal = horizonal;
    }

    public void setMapPositionIndexWithTime(HashMap<Float, Pair<Float, Float>> mapPositionIndexWithTime) {
        this.mapPositionIndexWithTime = mapPositionIndexWithTime;
        Set<Float> keySet = this.mapPositionIndexWithTime.keySet();

    }

    public void setLineGeneratorType(int index) {
        if (mapPositionIndexWithTime != null) {
            mapPositionIndexWithTime.clear();
        }

        if (index == 0) {
            setHorizontalOrientation(true);
            mapPositionIndexWithTime.put(0.0f, new Pair<Float, Float>(0.02f, 0.05f));
            mapPositionIndexWithTime.put(0.1f, new Pair<Float, Float>(0.15f, 0.15f));
            mapPositionIndexWithTime.put(0.2f, new Pair<Float, Float>(0.10f, 0.20f));
            mapPositionIndexWithTime.put(0.3f, new Pair<Float, Float>(0.01f, 0.02f));
            mapPositionIndexWithTime.put(0.4f, new Pair<Float, Float>(0.33f, 0.33f));
            mapPositionIndexWithTime.put(0.5f, new Pair<Float, Float>(0.44f, 0.44f));
            mapPositionIndexWithTime.put(0.6f, new Pair<Float, Float>(0.50f, 0.20f));
            mapPositionIndexWithTime.put(0.7f, new Pair<Float, Float>(0.70f, 0.70f));
            mapPositionIndexWithTime.put(0.8f, new Pair<Float, Float>(0.80f, 0.80f));
            mapPositionIndexWithTime.put(0.9f, new Pair<Float, Float>(0.50f, 0.55f));
            mapPositionIndexWithTime.put(1.0f, new Pair<Float, Float>(0.70f, 0.80f));
        } else if (index == 1) {
            isHorizontal = true;
            mapPositionIndexWithTime.put(0.0f, new Pair<Float, Float>(0.25f, 0.30f));
            mapPositionIndexWithTime.put(0.15f, new Pair<Float, Float>(0.03f, 0.03f));
            mapPositionIndexWithTime.put(0.22f, new Pair<Float, Float>(0.05f, 0.20f));
            mapPositionIndexWithTime.put(0.31f, new Pair<Float, Float>(0.20f, 0.20f));
            mapPositionIndexWithTime.put(0.45f, new Pair<Float, Float>(0.40f, 0.40f));
            mapPositionIndexWithTime.put(0.51f, new Pair<Float, Float>(0.52f, 0.59f));
            mapPositionIndexWithTime.put(0.63f, new Pair<Float, Float>(0.60f, 0.60f));
            mapPositionIndexWithTime.put(0.76f, new Pair<Float, Float>(0.75f, 0.75f));
            mapPositionIndexWithTime.put(0.81f, new Pair<Float, Float>(0.65f, 0.40f));
            mapPositionIndexWithTime.put(0.94f, new Pair<Float, Float>(0.45f, 0.50f));
            mapPositionIndexWithTime.put(1.00f, new Pair<Float, Float>(0.14f, 0.33f));

        } else if (index == 2) {
            isHorizontal = true;
            mapPositionIndexWithTime.put(0.00f, new Pair<Float, Float>(0.01f, 0.03f));
            mapPositionIndexWithTime.put(0.05f, new Pair<Float, Float>(0.10f, 0.09f));
            mapPositionIndexWithTime.put(0.10f, new Pair<Float, Float>(0.05f, 0.06f));
            mapPositionIndexWithTime.put(0.25f, new Pair<Float, Float>(0.20f, 0.20f));
            mapPositionIndexWithTime.put(0.27f, new Pair<Float, Float>(0.10f, 0.10f));
            mapPositionIndexWithTime.put(0.30f, new Pair<Float, Float>(0.30f, 0.25f));
            mapPositionIndexWithTime.put(0.33f, new Pair<Float, Float>(0.15f, 0.16f));
            mapPositionIndexWithTime.put(0.37f, new Pair<Float, Float>(0.40f, 0.39f));
            mapPositionIndexWithTime.put(0.40f, new Pair<Float, Float>(0.20f, 0.21f));
            mapPositionIndexWithTime.put(0.45f, new Pair<Float, Float>(0.60f, 0.55f));
            mapPositionIndexWithTime.put(0.50f, new Pair<Float, Float>(0.30f, 0.31f));
            mapPositionIndexWithTime.put(0.53f, new Pair<Float, Float>(0.70f, 0.69f));
            mapPositionIndexWithTime.put(0.57f, new Pair<Float, Float>(0.40f, 0.41f));
            mapPositionIndexWithTime.put(0.60f, new Pair<Float, Float>(0.80f, 0.75f));
            mapPositionIndexWithTime.put(0.65f, new Pair<Float, Float>(0.50f, 0.51f));
            mapPositionIndexWithTime.put(0.70f, new Pair<Float, Float>(0.90f, 0.90f));
            mapPositionIndexWithTime.put(0.73f, new Pair<Float, Float>(0.60f, 0.60f));
            mapPositionIndexWithTime.put(0.80f, new Pair<Float, Float>(1.0f, 0.99f));
            mapPositionIndexWithTime.put(1.00f, new Pair<Float, Float>(0.70f, 0.71f));

        } else if (index == 3) {
            //vertical bend
            isHorizontal = false;

            mapPositionIndexWithTime.put(0.0f, new Pair<Float, Float>(0.02f, 0.05f));
            mapPositionIndexWithTime.put(0.1f, new Pair<Float, Float>(0.15f, 0.15f));
            mapPositionIndexWithTime.put(0.2f, new Pair<Float, Float>(0.10f, 0.20f));
            mapPositionIndexWithTime.put(0.3f, new Pair<Float, Float>(0.01f, 0.02f));
            mapPositionIndexWithTime.put(0.4f, new Pair<Float, Float>(0.33f, 0.33f));
            mapPositionIndexWithTime.put(0.5f, new Pair<Float, Float>(0.44f, 0.44f));
            mapPositionIndexWithTime.put(0.6f, new Pair<Float, Float>(0.50f, 0.20f));
            mapPositionIndexWithTime.put(0.7f, new Pair<Float, Float>(0.70f, 0.70f));
            mapPositionIndexWithTime.put(0.8f, new Pair<Float, Float>(0.80f, 0.80f));
            mapPositionIndexWithTime.put(0.9f, new Pair<Float, Float>(0.50f, 0.55f));
            mapPositionIndexWithTime.put(1.0f, new Pair<Float, Float>(0.70f, 0.80f));

        } else if (index == 4) {
            //vertical bend
            isHorizontal = false;

            mapPositionIndexWithTime.put(0.0f, new Pair<Float, Float>(0.25f, 0.30f));
            mapPositionIndexWithTime.put(0.15f, new Pair<Float, Float>(0.03f, 0.03f));
            mapPositionIndexWithTime.put(0.22f, new Pair<Float, Float>(0.05f, 0.20f));
            mapPositionIndexWithTime.put(0.31f, new Pair<Float, Float>(0.20f, 0.20f));
            mapPositionIndexWithTime.put(0.45f, new Pair<Float, Float>(0.40f, 0.40f));
            mapPositionIndexWithTime.put(0.51f, new Pair<Float, Float>(0.52f, 0.59f));
            mapPositionIndexWithTime.put(0.63f, new Pair<Float, Float>(0.60f, 0.60f));
            mapPositionIndexWithTime.put(0.76f, new Pair<Float, Float>(0.75f, 0.75f));
            mapPositionIndexWithTime.put(0.81f, new Pair<Float, Float>(0.65f, 0.40f));
            mapPositionIndexWithTime.put(0.94f, new Pair<Float, Float>(0.45f, 0.50f));
            mapPositionIndexWithTime.put(1.00f, new Pair<Float, Float>(0.14f, 0.33f));

        } else if (index == 5) {
            isHorizontal = false;
            mapPositionIndexWithTime.put(0.00f, new Pair<Float, Float>(0.01f, 0.03f));
            mapPositionIndexWithTime.put(0.05f, new Pair<Float, Float>(0.10f, 0.09f));
            mapPositionIndexWithTime.put(0.10f, new Pair<Float, Float>(0.05f, 0.06f));
            mapPositionIndexWithTime.put(0.25f, new Pair<Float, Float>(0.20f, 0.20f));
            mapPositionIndexWithTime.put(0.27f, new Pair<Float, Float>(0.10f, 0.10f));
            mapPositionIndexWithTime.put(0.30f, new Pair<Float, Float>(0.30f, 0.25f));
            mapPositionIndexWithTime.put(0.33f, new Pair<Float, Float>(0.15f, 0.16f));
            mapPositionIndexWithTime.put(0.37f, new Pair<Float, Float>(0.40f, 0.39f));
            mapPositionIndexWithTime.put(0.40f, new Pair<Float, Float>(0.20f, 0.21f));
            mapPositionIndexWithTime.put(0.45f, new Pair<Float, Float>(0.60f, 0.55f));
            mapPositionIndexWithTime.put(0.50f, new Pair<Float, Float>(0.30f, 0.31f));
            mapPositionIndexWithTime.put(0.53f, new Pair<Float, Float>(0.70f, 0.69f));
            mapPositionIndexWithTime.put(0.57f, new Pair<Float, Float>(0.40f, 0.41f));
            mapPositionIndexWithTime.put(0.60f, new Pair<Float, Float>(0.80f, 0.75f));
            mapPositionIndexWithTime.put(0.65f, new Pair<Float, Float>(0.50f, 0.51f));
            mapPositionIndexWithTime.put(0.70f, new Pair<Float, Float>(0.90f, 0.90f));
            mapPositionIndexWithTime.put(0.73f, new Pair<Float, Float>(0.60f, 0.60f));
            mapPositionIndexWithTime.put(0.80f, new Pair<Float, Float>(1.0f, 0.99f));
            mapPositionIndexWithTime.put(1.00f, new Pair<Float, Float>(0.70f, 0.71f));
        }
    }


}
