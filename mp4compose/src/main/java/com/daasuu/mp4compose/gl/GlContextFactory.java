package com.daasuu.mp4compose.gl;



public class GlContextFactory extends DefaultContextFactory {

    private static final int EGL_CONTEXT_CLIENT_VERSION = 2;

    public GlContextFactory() {
        super(EGL_CONTEXT_CLIENT_VERSION);
    }

}
