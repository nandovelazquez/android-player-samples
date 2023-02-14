package com.brightcove.player.samples.ima.basic.dai;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.brightcove.player.edge.Catalog;
import com.brightcove.player.edge.CatalogError;
import com.brightcove.player.edge.VideoListener;
import com.brightcove.player.model.Video;
import com.brightcove.player.view.BaseVideoView;
import com.brightcove.player.view.BrightcoveExoPlayerVideoView;
import com.google.ads.interactivemedia.v3.api.AdErrorEvent;
import com.google.ads.interactivemedia.v3.api.AdEvent;
import com.google.ads.interactivemedia.v3.api.AdsLoader;
import com.google.ads.interactivemedia.v3.api.AdsManagerLoadedEvent;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;
import com.google.ads.interactivemedia.v3.api.ImaSdkSettings;
import com.google.ads.interactivemedia.v3.api.StreamDisplayContainer;
import com.google.ads.interactivemedia.v3.api.StreamManager;
import com.google.ads.interactivemedia.v3.api.StreamRequest;
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate;
import com.google.ads.interactivemedia.v3.api.player.VideoStreamPlayer;

import java.util.HashMap;
import java.util.List;

public class DAIWrapper implements AdEvent.AdEventListener,
        AdErrorEvent.AdErrorListener,
        AdsLoader.AdsLoadedListener {

    private static final String TAG = "AdsWrapper";
    // Live stream asset key.
    private static final String TEST_ASSET_KEY = "PSzZMzAkSXCmlJOWDmRj8Q";

    // VOD content source and video IDs.
    private static final String TEST_CONTENT_SOURCE_ID = "2528370";
    private static final String TEST_VIDEO_ID = "tears-of-steel";

    // This video is got from Video Cloud and should be used instead of a fallback URL
    private static final String TEST_FALLBACK_VIDEO_ID = "1753980443013591663";

    private final Context context;
    private final ImaSdkFactory sdkFactory;
    private final StreamDisplayContainer displayContainer;
    private final BrightcoveExoPlayerVideoView brightcoveVideoView;
    private VideoStreamPlayer videoStreamPlayer;
    private AdsLoader adsLoader;

    public DAIWrapper(Context context, BaseVideoView videoPlayer,
                      ViewGroup adUiContainer) {
        this.brightcoveVideoView = (BrightcoveExoPlayerVideoView) videoPlayer;
        this.context = context;
        sdkFactory = ImaSdkFactory.getInstance();
        createVideoStreamPlayer();
        displayContainer = sdkFactory.createStreamDisplayContainer(
                adUiContainer,
                videoStreamPlayer
        );
        createAdsLoader();
    }

    private void createAdsLoader() {
        ImaSdkSettings settings = sdkFactory.createImaSdkSettings();
        adsLoader = sdkFactory.createAdsLoader(context, settings, displayContainer);
    }

    public void requestAndPlayAds() {
        adsLoader.addAdErrorListener(this);
        adsLoader.addAdsLoadedListener(this);
        adsLoader.requestStream(buildStreamRequest());
    }

    private StreamRequest buildStreamRequest() {
        // Live stream request.
//         StreamRequest request = sdkFactory.createLiveStreamRequest(TEST_ASSET_KEY, null);

        // VOD request. Comment the createLiveStreamRequest() line above and uncomment this
        // createVodStreamRequest() below to switch from a live stream to a VOD stream.
         StreamRequest request = sdkFactory.createVodStreamRequest(TEST_CONTENT_SOURCE_ID, TEST_VIDEO_ID, null);
        return request;
    }

    private void createVideoStreamPlayer() {
        videoStreamPlayer = new VideoStreamPlayer() {
            @Override
            public int getVolume() {
                return 0;
            }

            @Override
            public void loadUrl(String url, List<HashMap<String, String>> subtitles) {
                Log.i(TAG, "createVideoStreamPlayer loadURL");
                Video video = Video.createVideo(url);
                brightcoveVideoView.add(video);
                brightcoveVideoView.start();
            }

            @Override
            public void addCallback(VideoStreamPlayerCallback videoStreamPlayerCallback) {
            }

            @Override
            public void removeCallback(VideoStreamPlayerCallback videoStreamPlayerCallback) {
            }

            @Override
            public void resume() {

            }

            @Override
            public void seek(long l) {

            }

            @Override
            public void onAdBreakStarted() {
                Log.i(TAG, "Ad Break Started\n");
            }

            @Override
            public void onAdPeriodEnded() {

            }

            @Override
            public void onAdPeriodStarted() {

            }

            @Override
            public void pause() {

            }

            @Override
            public void onAdBreakEnded() {
                Log.i(TAG, "Ad Break Ended\n");
            }

            @Override
            public VideoProgressUpdate getContentProgress() {
                return new VideoProgressUpdate(brightcoveVideoView.getCurrentPosition(),
                        brightcoveVideoView.getDuration());
            }
        };
    }

    /** AdErrorListener implementation **/
    @Override
    public void onAdError(AdErrorEvent event) {
        // play fallback video.
        Log.i(TAG, "DAI failed on getting the stream. Using fallback video");
        getAndPlayFallbackVideo();
    }

    /** AdEventListener implementation **/
    @Override
    public void onAdEvent(AdEvent event) {
        switch (event.getType()) {
            case AD_PROGRESS:
                // Do nothing or else log are filled by these messages.
                break;
            default:
                Log.i(TAG, String.format("Event: %s\n", event.getType()));
                break;
        }
    }

    /** AdsLoadedListener implementation **/
    @Override
    public void onAdsManagerLoaded(AdsManagerLoadedEvent event) {
        StreamManager streamManager = event.getStreamManager();
        streamManager.addAdErrorListener(this);
        streamManager.addAdEventListener(this);
        streamManager.init();
    }

    private void getAndPlayFallbackVideo() {
        // Request fallback video and keep it in memory
        Catalog catalog = new Catalog.Builder(brightcoveVideoView.getEventEmitter(), "5434391461001")
                .setPolicy("BCpkADawqM0T8lW3nMChuAbrcunBBHmh4YkNl5e6ZrKQwPiK_Y83RAOF4DP5tyBF_ONBVgrEjqW6fbV0nKRuHvjRU3E8jdT9WMTOXfJODoPML6NUDCYTwTHxtNlr5YdyGYaCPLhMUZ3Xu61L")
                .build();
        catalog.findVideoByID(TEST_FALLBACK_VIDEO_ID, new VideoListener() {
            @Override
            public void onVideo(Video video) {
                brightcoveVideoView.add(video);
                brightcoveVideoView.start();
            }

            @Override
            public void onError(@NonNull List<CatalogError> errors) {
                Log.e(TAG, errors.toString());
            }
        });
    }

}
