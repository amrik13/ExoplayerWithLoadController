package com.example.demoplayerpoc;

public class DemoConstants {

    public static final String HLS_TYPE = "HLS";
    public static final String DASH_TYPE = "DASH";
    public static final String DASH_PLAYBACK_URL = "https://storage.googleapis.com/wvmedia/clear/h264/tears/tears.mpd";
    public static final String HLS_PLAYBACK_URL = "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_4x3/bipbop_4x3_variant.m3u8";

    //Minimum Video you want to buffer while Playing
    public static final int MIN_BUFFER_DURATION = 25000;
    //Max Video you want to buffer during PlayBack
    public static final int MAX_BUFFER_DURATION = 30000;
    //Min Video you want to buffer before start Playing it
    public static final int MIN_PLAYBACK_START_BUFFER = 20000;
    //Min video You want to buffer when user resumes video
    public static final int MIN_PLAYBACK_RESUME_BUFFER = 10000;

    /* below field added for checking
    DefaultLoadControl.DEFAULT_TARGET_BUFFER_BYTES,
    DefaultLoadControl.DEFAULT_PRIORITIZE_TIME_OVER_SIZE_THRESHOLDS,
    DefaultLoadControl.DEFAULT_BACK_BUFFER_DURATION_MS,
    DefaultLoadControl.DEFAULT_RETAIN_BACK_BUFFER_FROM_KEYFRAME */

}
