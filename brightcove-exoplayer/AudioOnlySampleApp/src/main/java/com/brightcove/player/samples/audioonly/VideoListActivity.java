package com.brightcove.player.samples.audioonly;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.brightcove.player.edge.Catalog;
import com.brightcove.player.edge.PlaylistListener;
import com.brightcove.player.event.EventEmitterImpl;
import com.brightcove.player.model.Playlist;
import com.brightcove.player.model.Video;
import com.brightcove.player.samples.adapters.AdapterView;

import java.util.List;

/**
 * This activity displays a list of media items retrieved from the Brightcove platform.
 * Tapping an item from that list, it opens the @{@link PlayerActivity} that loads
 * and plays the media item.
 * If you want to launch this activity, specify that in the AndroidManifest file.
 * <P>
 * For a different behavior, check the @{@link MainActivity} activity
 */
public class VideoListActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();

    private RecyclerView videoListView;
    private AdapterView adapterView;
    private String accountId;
    private String policyKey;
    private String playListReference;
    private Catalog catalog;
    private Context context;
    private List<Video> videoList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);
        context = this;
        accountId = getString(R.string.account);
        policyKey = getString(R.string.policy);
        playListReference = getString(R.string.trackPlaylistReference);

        ImageButton actionGitHubButton = findViewById(R.id.action_github);
        actionGitHubButton.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.GITHUB_URL)));
            startActivity(browserIntent);
        });

        videoListView = findViewById(R.id.video_list_view);
        adapterView = new AdapterView(v -> {
            int videoIndex = (int) v.findViewById(R.id.titleTextView).getTag();
            Intent intent = new Intent(context, PlayerActivity.class);
            intent.putExtra("videoID", videoList.get(videoIndex).getId());
            startActivity(intent);
        });
        videoListView.setAdapter(adapterView);
        getPlaylist();
    }

    private void getPlaylist() {
        catalog = new Catalog.Builder(new EventEmitterImpl(), accountId)
                .setBaseURL(Catalog.DEFAULT_EDGE_BASE_URL)
                .setPolicy(policyKey)
                .build();
        catalog.findPlaylistByReferenceID(playListReference, new PlaylistListener() {
            @Override
            public void onPlaylist(Playlist playlist) {
                videoList = playlist.getVideos();
                adapterView.setVideoList(videoList);
            }
        });
    }

    @Override
    protected void onDestroy() {
        videoListView.setAdapter(null);
        videoListView.removeAllViews();
        super.onDestroy();
    }
}
