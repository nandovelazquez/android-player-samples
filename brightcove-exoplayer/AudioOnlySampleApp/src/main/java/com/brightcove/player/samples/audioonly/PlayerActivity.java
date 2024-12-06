package com.brightcove.player.samples.audioonly;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.brightcove.playback.notification.BackgroundPlaybackNotification;
import com.brightcove.player.display.ExoPlayerVideoDisplayComponent;
import com.brightcove.player.edge.Catalog;
import com.brightcove.player.edge.VideoListener;
import com.brightcove.player.logging.Log;
import com.brightcove.player.model.Video;
import com.brightcove.player.playback.PlaybackNotification;
import com.brightcove.player.playback.PlaybackNotificationConfig;
import com.brightcove.player.view.BrightcovePlayer;


public class PlayerActivity extends BrightcovePlayer {


    private String accountId;
    private String policyKey;
    private Catalog catalog;
    private Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        context = this;
        accountId = getString(R.string.account);
        policyKey = getString(R.string.policy);

        brightcoveVideoView = findViewById(R.id.brightcove_video_view);

        ExoPlayerVideoDisplayComponent videoDisplayComponent = (ExoPlayerVideoDisplayComponent) brightcoveVideoView.getVideoDisplay();
        if (videoDisplayComponent != null ) {
            if (videoDisplayComponent.getPlaybackNotification() == null) {
                videoDisplayComponent.setPlaybackNotification(createPlaybackNotification());
            }
        }

        getVideo();
    }

    private PlaybackNotification createPlaybackNotification() {
        ExoPlayerVideoDisplayComponent displayComponent = ((ExoPlayerVideoDisplayComponent) brightcoveVideoView.getVideoDisplay());
        PlaybackNotification notification = BackgroundPlaybackNotification.getInstance(this);
        notification.setConfig(new PlaybackNotificationConfig(this));
        notification.setPlayback(displayComponent.getPlayback());
        return notification;
    }

    private void getVideo() {
        Intent intent = getIntent();
        String videoId = intent.getStringExtra("videoID");

        catalog = new Catalog.Builder(brightcoveVideoView.getEventEmitter(), accountId)
                .setBaseURL(Catalog.DEFAULT_EDGE_BASE_URL)
                .setPolicy(policyKey)
                .build();
        catalog.findVideoByID(videoId, new VideoListener() {
            @Override
            public void onVideo(Video track) {
                Log.v(TAG, "onTrack: track = " + track);
                brightcoveVideoView.add(track);
                brightcoveVideoView.start();
            }
        });
    }
}