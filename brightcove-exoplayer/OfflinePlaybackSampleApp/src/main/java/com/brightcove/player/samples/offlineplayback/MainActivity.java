package com.brightcove.player.samples.offlineplayback;

import android.content.res.Configuration;
import android.net.NetworkInfo;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.brightcove.player.edge.OfflineCallback;
import com.brightcove.player.edge.OfflineCatalog;
import com.brightcove.player.edge.PlaylistListener;
import com.brightcove.player.edge.VideoListener;
import com.brightcove.player.event.Event;
import com.brightcove.player.event.EventEmitter;
import com.brightcove.player.event.EventListener;
import com.brightcove.player.event.EventType;
import com.brightcove.player.model.DeliveryType;
import com.brightcove.player.model.Playlist;
import com.brightcove.player.model.Video;
import com.brightcove.player.network.ConnectivityMonitor;
import com.brightcove.player.network.DownloadStatus;
import com.brightcove.player.network.HttpRequestConfig;
import com.brightcove.player.offline.MediaDownloadable;
import com.brightcove.player.samples.offlineplayback.utils.BrightcoveDownloadUtil;
import com.brightcove.player.samples.offlineplayback.utils.ViewUtil;
import com.brightcove.player.view.BrightcovePlayer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * An activity that displays a list of videos that can be downloaded.
 */
public class MainActivity extends BrightcovePlayer {
    /**
     * The name that will be used to tag the events generated by this class.
     */
    private static final String TAG = MainActivity.class.getSimpleName();

    /**
     * The video cloud account identifier.
     */
    private static final String ACCOUNT_ID = "5420904993001";

    /**
     * The policy key for the video cloud account.
     */
    private static final String POLICY_KEY = "BCpkADawqM1RJu5c_I13hBUAi4c8QNWO5QN2yrd_OgDjTCVsbILeGDxbYy6xhZESTFi68MiSUHzMbQbuLV3q-gvZkJFpym1qYbEwogOqKCXK622KNLPF92tX8AY9a1cVVYCgxSPN12pPAuIM";

    /**
     * Specifies how long the content can be consumed after the start of playback as total number
     * of milliseconds. The default value is forty-eight hours.
     */
    private static long DEFAULT_RENTAL_PLAY_DURATION = TimeUnit.HOURS.toMillis(48);

    /**
     * Reference to the video cloud catalog client.
     */
    private OfflineCatalog catalog;

    /**
     * Adapter for holding the video list details.
     */
    private VideoListAdapter videoListAdapter;

    /**
     * Text view that will be used to indicate the video list type (online or offline).
     */
    private TextView videoListLabel;

    /**
     * View that will be used to display video list (online or offline).
     */
    private RecyclerView videoListView;

    /**
     * Text view that will be used to show a message when the video list is empty.
     */
    private TextView emptyListMessage;

    /**
     * Network connectivity state change monitor.
     */
    private ConnectivityMonitor connectivityMonitor;

    /**
     *
     */
    private HttpRequestConfig httpRequestConfig;
    private String pasToken = "YOUR_PAS_TOKEN";
    private static final int PLAYDURATION_EXTENSION = 10000;

//    PlaylistModel playlist = PlaylistModel.byReferenceId("demo_odrm_widevine_dash", "Offline Playback List");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        onCreate();
    }

    @Override
    protected void onStart() {
        super.onStart();
        ConnectivityMonitor.getInstance(this).addListener(connectivityListener);
        catalog.addDownloadEventListener(downloadEventListener);
        updateVideoList();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (isFinishing()) {
            ConnectivityMonitor.getInstance(this).removeListener(connectivityListener);
            catalog.removeDownloadEventListener(downloadEventListener);
        }
    }

    /**
     * Sets up the view when created.
     */
    private void onCreate() {
        connectivityMonitor = ConnectivityMonitor.getInstance(this);
        videoListLabel = ViewUtil.findView(this, R.id.video_list_label);
        videoListView = ViewUtil.findView(this, R.id.video_list_view);
        emptyListMessage = ViewUtil.findView(this, R.id.empty_list_message);

        brightcoveVideoView = ViewUtil.findView(this, R.id.brightcove_video_view);
        EventEmitter eventEmitter = brightcoveVideoView.getEventEmitter();
        catalog = new OfflineCatalog(this, eventEmitter, "4744899836001",
                "BCpkADawqM3O4rejQjSfnm8_C7CfzF3vWBrx_E1UY_gbfIel6Moo_qQC6HP1vjPt1TWxqNc4QW8bk2oBGgWfypaKVF8TjN_--UI2_WlFFIGTdKibkjIJRm3--fi7INarN-SFEpH88UGL-H0J");

        //Configure downloads through the catalog.
        catalog.setMobileDownloadAllowed(true);
        catalog.setMeteredDownloadAllowed(false);
        catalog.setRoamingDownloadAllowed(false);

        videoListAdapter = new VideoListAdapter(catalog, videoListListener);

        // Connect the video list view to the adapter
        RecyclerView videoListView = ViewUtil.findView(this, R.id.video_list_view);
        videoListView.setAdapter(videoListAdapter);

        // Setup an adapter to render the playlist items in the spinner view Adapter that
        // will be used to bind the playlist spinner to the underlying data source.
        ArrayAdapter<PlaylistModel> playlistAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item/*, playlistNames*/);
        playlistAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    private void updateVideoList() {
        videoListAdapter.setVideoList(null);

        if (connectivityMonitor.isConnected()) {
            videoListLabel.setVisibility(View.GONE);
            videoListView.setVisibility(View.GONE);
            emptyListMessage.setText(R.string.fetching_playlist);
            emptyListMessage.setVisibility(View.VISIBLE);

            HttpRequestConfig.Builder httpRequestConfigBuilder = new HttpRequestConfig.Builder();
            httpRequestConfigBuilder.setBrightcoveAuthorizationToken(pasToken);
            httpRequestConfig = httpRequestConfigBuilder.build();

            List<Video> videoList = new ArrayList<>();
            String[] videoIDList = getResources().getStringArray(R.array.list_of_videos_upgrad);

            for (String id : videoIDList) {
                catalog.findVideoByID(id, new VideoListener() {
                    @Override
                    public void onVideo(Video video) {
                        videoList.add(video);

                        videoListAdapter.setVideoList(videoList);
                        onVideoListUpdated(false);
                        brightcoveVideoView.addAll(videoList);
                    }
                });
            }


//            playlist.findPlaylist(catalog, httpRequestConfig, new PlaylistListener() {
//                @Override
//                public void onPlaylist(Playlist playlist) {
//                    videoListAdapter.setVideoList(playlist.getVideos());
//                    onVideoListUpdated(false);
//                    brightcoveVideoView.addAll(playlist.getVideos());
//                }
//
//                @Override
//                public void onError(String error) {
//                    String message = showToast("Failed to find playlist[%s]: %s", playlist.displayName, error);
//                    Log.w(TAG, message);
//                    onVideoListUpdated(true);
//                }
//            });
        } else {
            videoListLabel.setVisibility(View.VISIBLE);
            videoListLabel.setText(R.string.offline_playlist);
            catalog.findAllVideoDownload(
                    DownloadStatus.STATUS_COMPLETE,
                    new OfflineCallback<List<Video>>() {
                        @Override
                        public void onSuccess(List<Video> videos) {
                            videoListAdapter.setVideoList(videos);
                            onVideoListUpdated(false);
                        }

                        @Override
                        public void onFailure(Throwable throwable) {
                            Log.e(TAG, "Error fetching all videos downloaded: ", throwable);
                        }
                    });
        }
    }

    private void onVideoListUpdated(boolean error) {
        if (videoListAdapter.getItemCount() == 0) {
            videoListView.setVisibility(View.GONE);
            if (connectivityMonitor.isConnected()) {
                if (error) {
                    emptyListMessage.setText(R.string.fetching_playlist_error);
                } else {
                    emptyListMessage.setText(R.string.fetching_playlist_no_videos);
                }
            } else {
                emptyListMessage.setText(R.string.offline_playlist_no_videos);
            }
            emptyListMessage.setVisibility(View.VISIBLE);
        } else {
            videoListView.setVisibility(View.VISIBLE);
            emptyListMessage.setVisibility(View.GONE);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);

        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (!brightcoveVideoView.isFullScreen()) {
                brightcoveVideoView.getEventEmitter().emit(EventType.ENTER_FULL_SCREEN);
            }
        } else if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (brightcoveVideoView.isFullScreen()) {
                brightcoveVideoView.getEventEmitter().emit(EventType.EXIT_FULL_SCREEN);
            }
        }
    }

    /**
     * Implements a {@link com.brightcove.player.offline.MediaDownloadable.DownloadEventListener} that
     * will show a toast message about the download status and refresh the video list display.
     */
    private final MediaDownloadable.DownloadEventListener downloadEventListener = new MediaDownloadable.DownloadEventListener() {

        @Override
        public void onDownloadRequested(@NonNull final Video video) {
            Log.i(TAG, String.format(
                    "Starting to process '%s' video download request", video.getName()));
            videoListAdapter.notifyVideoChanged(video);
        }

        @Override
        public void onDownloadStarted(@NonNull Video video, long estimatedSize, @NonNull Map<String, Serializable> mediaProperties) {
            videoListAdapter.notifyVideoChanged(video);
            String message = showToast(
                    "Started to download '%s' video. Estimated = %s, width = %s, height = %s, mimeType = %s",
                    video.getName(),
                    Formatter.formatFileSize(MainActivity.this, estimatedSize),
                    mediaProperties.get(Event.RENDITION_WIDTH),
                    mediaProperties.get(Event.RENDITION_HEIGHT),
                    mediaProperties.get(Event.RENDITION_MIME_TYPE)
            );
            Log.i(TAG, message);
        }

        @Override
        public void onDownloadProgress(@NonNull final Video video, @NonNull final DownloadStatus status) {
            Log.i(TAG, String.format(
                    "Downloaded %s out of %s of '%s' video. Progress %3.2f",
                    Formatter.formatFileSize(MainActivity.this, status.getBytesDownloaded()),
                    Formatter.formatFileSize(MainActivity.this, status.getMaxSize()),
                    video.getName(), status.getProgress()));
            videoListAdapter.notifyVideoChanged(video, status);
        }

        @Override
        public void onDownloadPaused(@NonNull final Video video, @NonNull final DownloadStatus status) {
            Log.i(TAG, String.format(
                    "Paused download of '%s' video: Reason #%d", video.getName(), status.getReason()));
            videoListAdapter.notifyVideoChanged(video, status);
        }

        @Override
        public void onDownloadCompleted(@NonNull final Video video, @NonNull final DownloadStatus status) {
            videoListAdapter.notifyVideoChanged(video, status);
            String message = showToast(
                    "Successfully saved '%s' video", video.getName());
            Log.i(TAG, message);
        }

        @Override
        public void onDownloadCanceled(@NonNull final Video video) {
            //No need to update UI here because it will be handled by the deleteVideo method.
            String message = showToast(
                    "Cancelled download of '%s' video removed", video.getName());
            Log.i(TAG, message);
            onDownloadRemoved(video);
        }

        @Override
        public void onDownloadDeleted(@NonNull final Video video) {
            //No need to update UI here because it will be handled by the deleteVideo method.
            String message = showToast(
                    "Offline copy of '%s' video removed", video.getName());
            Log.i(TAG, message);
            onDownloadRemoved(video);
        }

        @Override
        public void onDownloadFailed(@NonNull final Video video, @NonNull final DownloadStatus status) {
            videoListAdapter.notifyVideoChanged(video, status);
            String message = showToast(
                    "Failed to download '%s' video: Error #%d", video.getName(), status.getReason());
            Log.e(TAG, message);
        }
    };

    /**
     * Called when an offline copy of the video is either cancelled or deleted.
     *
     * @param video the video that was removed.
     */
    private void onDownloadRemoved(@NonNull final Video video) {
        if (connectivityMonitor.isConnected()) {
            // Fetch the video object again to avoid using the given video that may have been
            // tainted by previous download.
            catalog.findVideoByID(video.getId(), new FindVideoListener(video) {
                @Override
                public void onVideo(Video newVideo) {
                    videoListAdapter.notifyVideoChanged(newVideo);
                }
            });
        } else {
            videoListAdapter.removeVideo(video);
            onVideoListUpdated(false);
        }
    }

    /**
     * Implements a {@link com.brightcove.player.network.ConnectivityMonitor.Listener} that will
     * update the current video list based on network connectivity state.
     */
    private final ConnectivityMonitor.Listener connectivityListener = new ConnectivityMonitor.Listener() {
        public void onConnectivityChanged(boolean connected, @Nullable NetworkInfo networkInfo) {
            updateVideoList();
        }
    };

    /**
     * Implements a {@link VideoListListener} that responds to user interaction on the video list.
     */
    private final VideoListListener videoListListener = new VideoListListener() {
        @Override
        public void playVideo(@NonNull Video video) {
            MainActivity.this.playVideo(video);
        }

        @Override
        public void rentVideo(@NonNull final Video video) {
            // Fetch the video object again to avoid using the given video that may have been
            // changed by previous download.
            catalog.findVideoByID(video.getId(), new FindVideoListener(video) {
                @Override
                public void onVideo(Video newVideo) {
                    MainActivity.this.rentVideo(newVideo);
                }
            });
        }

        @Override
        public void buyVideo(@NonNull final Video video) {
            // Fetch the video object again to avoid using the given video that may have been
            // changed by previous download.
            catalog.findVideoByID(video.getId(), new FindVideoListener(video) {
                @Override
                public void onVideo(Video newVideo) {
                    HttpRequestConfig.Builder httpRequestConfigBuilder = new HttpRequestConfig.Builder();
                    httpRequestConfigBuilder.setBrightcoveAuthorizationToken(pasToken);
                    httpRequestConfig = httpRequestConfigBuilder.build();
                    catalog.requestPurchaseLicense(video, licenseEventListener, httpRequestConfig);
                }
            });
        }

        @Override
        public void pauseVideoDownload(@NonNull Video video) {
            Log.v(TAG,"Calling pauseVideoDownload.");
            catalog.pauseVideoDownload(video, new OfflineCallback<Integer>() {
                @Override
                public void onSuccess(Integer integer) {
                    // Video download was paused successfully
                }

                @Override
                public void onFailure(Throwable throwable) {
                    Log.e(TAG, "Error pausing video download: ", throwable);
                }
            });
        }

        @Override
        public void resumeVideoDownload(@NonNull Video video) {
            Log.v(TAG,"Calling resumeVideoDownload.");
            catalog.resumeVideoDownload(video, new OfflineCallback<Integer>() {
                @Override
                public void onSuccess(Integer integer) {
                    // Video download was resumed successfully
                }

                @Override
                public void onFailure(Throwable throwable) {
                    Log.e(TAG, "Error resuming video download: ", throwable);
                }
            });
        }

        @Override
        public void downloadVideo(@NonNull final Video video) {
            // bundle has all available captions and audio tracks
            catalog.getMediaFormatTracksAvailable(video, new MediaDownloadable.MediaFormatListener() {
                @Override
                public void onResult(MediaDownloadable mediaDownloadable, Bundle bundle) {
                    BrightcoveDownloadUtil.selectMediaFormatTracksAvailable(mediaDownloadable, bundle);
                    catalog.downloadVideo(video, new OfflineCallback<DownloadStatus>() {
                        @Override
                        public void onSuccess(DownloadStatus downloadStatus) {
                            // Video download started successfully
                            videoListAdapter.notifyVideoChanged(video);
                        }

                        @Override
                        public void onFailure(Throwable throwable) {
                            Log.e(TAG, "Error initializing video download: ", throwable);
                        }
                    });
                }
            });
        }

        @Override
        public void deleteVideo(@NonNull Video video) {
            catalog.deleteVideo(video, new OfflineCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean aBoolean) {

                }

                @Override
                public void onFailure(Throwable throwable) {
                    Log.e(TAG, "Error deleting video: ", throwable);
                }
            });
        }
    };

    /**
     * Plays the specified video. If the player is already playing a video, playback will be stopped
     * and the current video cleared before loading the new video into the player.
     *
     * @param video the video to be played.
     */
    private void playVideo(@NonNull Video video) {
        brightcoveVideoView.stopPlayback();
        brightcoveVideoView.clear();
        brightcoveVideoView.add(video);
        brightcoveVideoView.start();
    }

    private final EventListener licenseEventListener = new EventListener() {
        @Override
        public void processEvent(Event event) {

            final String type = event.getType();
            final Video video = (Video) event.properties.get(Event.VIDEO);

            switch (type) {
                case EventType.ODRM_LICENSE_ACQUIRED: {
                    videoListAdapter.notifyVideoChanged(video);
                    String message = showToast(
                            "Successfully downloaded license for '%s' video", video.getName());
                    Log.i(TAG, message);
                    break;
                }
                case EventType.ODRM_PLAYBACK_NOT_ALLOWED:
                case EventType.ODRM_SOURCE_NOT_FOUND: {
                    String message = showToast(
                            "Failed to downloaded license for '%s' video: %s", video.getName(), type);
                    Log.w(TAG, message);
                    break;
                }
                case EventType.ODRM_LICENSE_ERROR: {
                    String message = showToast(
                            "Error encountered while downloading license for '%s' video", video.getName());
                    Log.e(TAG, message, (Throwable) event.properties.get(Event.ERROR));
                    break;
                }
            }
        }
    };

    /**
     * Attempts to download an offline playback rental license for the specified video. Upon
     * successful acquisition of offline playback license, the video can be downloaded
     * into the host device.
     *
     * @param video the video for which the offline playback license is needed.
     */
    private void rentVideo(@NonNull final Video video) {
        new DatePickerFragment()
                .setTitle("Select Rental Expiry Date")
                .setListener(new DatePickerFragment.Listener() {
                    @Override
                    public void onDateSelected(@NonNull Date expiryDate) {
                        // Extend the playDuration value to the video duration plus an additional small amount to account for:
                        // - Loading the video into the player (which starts the playDuration clock)
                        // - Starting playback in a manual-start player
                        long playDuration = video.getDuration() + PLAYDURATION_EXTENSION;
                        if (playDuration == 0) {
                            playDuration = DEFAULT_RENTAL_PLAY_DURATION;
                        }

                        HttpRequestConfig.Builder httpRequestConfigBuilder = new HttpRequestConfig.Builder();
                        httpRequestConfigBuilder.setBrightcoveAuthorizationToken(pasToken);
                        httpRequestConfig = httpRequestConfigBuilder.build();
                        catalog.requestRentalLicense(video, expiryDate, playDuration, licenseEventListener, httpRequestConfig);
                    }
                })
                .show(getFragmentManager(), "rentalExpiryDatePicker");
    }

    /**
     * Shows a formatted toast message.
     *
     * @param message    the message to be shown. The message may include string format tokens.
     * @param parameters the parameters to be used for formatting the message.
     * @return the formatted message that was shown.
     * @see String#format(String, Object...)
     */
    private String showToast(@NonNull String message, @Nullable Object... parameters) {
        if (parameters != null) {
            message = String.format(message, parameters);
        }
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();

        return message;
    }

    /**
     * Find video listener extends the base implementation to show toast if the video is not found.
     */
    private abstract class FindVideoListener extends VideoListener {
        /**
         * The video being retrieved.
         */
        private final Video video;

        /**
         * Constructs a new find video listener
         *
         * @param video the video being searched.
         */
        FindVideoListener(Video video) {
            this.video = video;
        }

        @Override
        public void onError(String error) {
            String message = showToast(
                    "Cannot find '%s' video: %s", video.getName(), error);
            Log.e(TAG, message);
        }
    }

}
