package com.example.demoplayerpoc;

public class DemoConstants {

    public static final String HLS_TYPE = "HLS";
    public static final String HLS_TYPE_LIVE = "HLS_LIVE";
    public static final String DASH_TYPE = "DASH";
    public static final String HLS_WITH_ADS_TYPE = "HLS_ADS";
    public static final String LIVE_WITH_ADS_TYPE = "LIVE_ADS";
    public static final String DASH_PLAYBACK_URL = "https://storage.googleapis.com/wvmedia/clear/h264/tears/tears.mpd";
    public static final String HLS_PLAYBACK_URL = "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_4x3/bipbop_4x3_variant.m3u8";
    public static final String HLS_LIVE_PLAYBACK_URL = "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8";
    public static final String AD_TAG_URL = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp&output=vmap&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&cmsid=496&vid=short_onecue&correlator=";

    //Minimum Video you want to buffer while Playing
    public static final int MIN_BUFFER_DURATION = 8000;
    //Max Video you want to buffer during PlayBack
    public static final int MAX_BUFFER_DURATION = 10000;
    //Min Video you want to buffer before start Playing it
    public static final int MIN_PLAYBACK_START_BUFFER = 1500;
    //Min video You want to buffer when user resumes video
    public static final int MIN_PLAYBACK_RESUME_BUFFER = 1500;

    public static final String SUBTITLE_URL = "https://storage.googleapis.com/exoplayer-test-media-1/ssa/test-subs-position.ass";
    public static final String SUBTITLE_MIME_TYPE = "text/x-ssa";
    public static final String SUBTITLE_LANGUAGE = "en";

    /* below field added for checking
    DefaultLoadControl.DEFAULT_TARGET_BUFFER_BYTES,
    DefaultLoadControl.DEFAULT_PRIORITIZE_TIME_OVER_SIZE_THRESHOLDS,
    DefaultLoadControl.DEFAULT_BACK_BUFFER_DURATION_MS,
    DefaultLoadControl.DEFAULT_RETAIN_BACK_BUFFER_FROM_KEYFRAME */

}
