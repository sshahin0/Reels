package com.daasuu.mp4compose.filter.time_filter;


import com.daasuu.mp4compose.filter.BlendFilters.GlGlitchFilter;
import com.daasuu.mp4compose.filter.GlFilter;
import com.daasuu.mp4compose.filter.GlFilterGroup;
import com.daasuu.mp4compose.filter.GlInvertFilter;
import com.daasuu.mp4compose.filter.group_filters.GlSobelEdgeDetectionFilter;
import com.daasuu.mp4compose.gl.GlFramebufferObject;

import java.util.ArrayList;

public class GlGroupTimeFilterSample extends GlFilterWithTime {

    private long time;
    private float unit;
    public float timeDuration;
    private GlFilter noFilter;
    private GlSobelEdgeDetectionFilter sobelEdge;
    private GlChromaticAbberationWithTime chromaticAbberation;
    private GlFilterGroup invertChromaticAbberation;
    private GlMirrorTileWithTime mirrorTileWithTime;
    private int numberOfFilters;
    private int currentIndex;
    private ArrayList<GlFilter> filterList = new ArrayList<GlFilter>();

    public GlGroupTimeFilterSample() {
        noFilter = new GlGlitchFilter();
        sobelEdge = new GlSobelEdgeDetectionFilter();
        chromaticAbberation = new GlChromaticAbberationWithTime();
        chromaticAbberation.setInputRadius(35);
        invertChromaticAbberation = new GlFilterGroup(new GlInvertFilter(), chromaticAbberation);
        mirrorTileWithTime = new GlMirrorTileWithTime();
        filterList.clear();
        filterList.add(noFilter);
        filterList.add(sobelEdge);
        filterList.add(chromaticAbberation);
        filterList.add(invertChromaticAbberation);
        filterList.add(mirrorTileWithTime);

    }

    @Override
    public void setup() {

        numberOfFilters = filterList.size();
        unit = 1f / numberOfFilters;
        for (int i = 0; i < filterList.size(); i++) {
            filterList.get(i).setup();
        }
    }

    @Override
    public void release() {

        for (int i = 0; i < filterList.size(); i++) {
            filterList.get(i).release();
        }
        filterList.clear();
    }

    @Override
    public void setFrameSize(int width, int height) {

        for (int i = 0; i < filterList.size(); i++) {
            filterList.get(i).setFrameSize(width, height);
        }

    }

    @Override
    public void draw(int texName, GlFramebufferObject fbo) {

        updateInfoByTime();

        filterList.get(currentIndex).draw(texName, fbo);
    }

    private void updateInfoByTime() {

        float interval = getInputTimePeriod();

        currentIndex = (int) Math.floor(getInputTimePeriod() / unit);

        for (int i = 0; i < filterList.size(); i++) {
            try {
                ((GlFilterWithTime) filterList.get(i)).setInputTimePeriod(interval);
            } catch (ClassCastException ex) {
            }
        }


    }

}
