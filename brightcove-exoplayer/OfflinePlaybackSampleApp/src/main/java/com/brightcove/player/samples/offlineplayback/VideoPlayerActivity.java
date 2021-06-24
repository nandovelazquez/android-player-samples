package com.brightcove.player.samples.offlineplayback;

import android.os.Bundle;

import com.brightcove.player.edge.Catalog;
import com.brightcove.player.edge.OfflineCatalog;
import com.brightcove.player.edge.VideoListener;
import com.brightcove.player.model.Video;
import com.brightcove.player.samples.offlineplayback.utils.ViewUtil;
import com.brightcove.player.view.BaseVideoView;
import com.brightcove.player.view.BrightcovePlayer;

public class VideoPlayerActivity extends BrightcovePlayer {

    private BaseVideoView mBrightcoveView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        mBrightcoveView = ViewUtil.findView(this, R.id.brightcove_video_view);

        String videoId = (String) getIntent().getExtras().get("video");

        Catalog catalog = new OfflineCatalog(
                this,
                mBrightcoveView.getEventEmitter(),
                "4744899836001",
                "BCpkADawqM3O4rejQjSfnm8_C7CfzF3vWBrx_E1UY_gbfIel6Moo_qQC6HP1vjPt1TWxqNc4QW8bk2oBGgWfypaKVF8TjN_--UI2_WlFFIGTdKibkjIJRm3--fi7INarN-SFEpH88UGL-H0J");

        catalog.findVideoByID(videoId, new VideoListener() {
            @Override
            public void onVideo(Video video) {
                mBrightcoveView.add(video);
                mBrightcoveView.start();
            }
        });
    }

}
