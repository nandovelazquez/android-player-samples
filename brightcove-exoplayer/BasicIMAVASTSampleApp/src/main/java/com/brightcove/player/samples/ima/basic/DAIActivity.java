package com.brightcove.player.samples.ima.basic;

import android.os.Bundle;

import com.brightcove.player.samples.ima.basic.dai.DAIWrapper;
import com.brightcove.player.view.BrightcoveExoPlayerVideoView;
import com.brightcove.player.view.BrightcovePlayer;

public class DAIActivity extends BrightcovePlayer {

    private final String TAG = "DAIActivity";
    private static final String DEFAULT_STREAM_URL = "https://storage.googleapis.com/testtopbox-public/video_content/bbb/master.m3u8";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.ima_activity_main);
        brightcoveVideoView = (BrightcoveExoPlayerVideoView) findViewById(R.id.brightcove_video_view);
        super.onCreate(savedInstanceState);

        DAIWrapper adsWrapper = new DAIWrapper(this, brightcoveVideoView, brightcoveVideoView);
        // We need to request the stream before the BC Player sets a video
        adsWrapper.requestAndPlayAds();
    }

}
