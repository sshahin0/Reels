package com.daasuu.mp4compose.filter.Overlay;

import android.content.Context;

public class GlRainyGlassEffect extends GlVideoOverlayFilter {


    private static final String FRAGMENT_SHADER = OverlayShaderHelper.RAINY_GLASS_FRAGMENT;

    public GlRainyGlassEffect(String datasrc)
    {
        super(DEFAULT_VERTEX_SHADER,FRAGMENT_SHADER);
        this.context = null;
        this.datasrc = datasrc;

    }
    public GlRainyGlassEffect(Context context, int resId)
    {
        super(DEFAULT_VERTEX_SHADER,FRAGMENT_SHADER);
        this.context = context;
        this.dataSrcId = resId;
        this.datasrc = "";

    }
}
