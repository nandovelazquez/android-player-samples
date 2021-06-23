package com.brightcove.recyclervideoview;


import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.brightcove.player.edge.Catalog;
import com.brightcove.player.edge.OfflineCatalog;
import com.brightcove.player.edge.PlaylistListener;
import com.brightcove.player.edge.VideoListener;
import com.brightcove.player.event.EventEmitter;
import com.brightcove.player.event.EventEmitterImpl;
import com.brightcove.player.model.DeliveryType;
import com.brightcove.player.model.Playlist;
import com.brightcove.player.model.Video;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String ACCOUNT_ID = "3636334163001";
    private static final String POLICY_KEY = "BCpkADawqM1ZEczFURHXFgdyqvGPj4GWHEQka6QIs7hOwSFPffq-UI_ntgaa29FEMKY87rAd0jptOZHueqyDxjVBTGmfek97TLHSLPqJKWAmKx_YSiHdLdLp8uq9hhwTWs6qJIOkN9cA0qqp";
    private static final String PLAYLIST_REF = "Play2017";


    private Catalog catalog;
    private RecyclerView videoListView;
    private AdapterView adapterView;

    EventEmitter eventEmitter = new EventEmitterImpl();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        videoListView = (RecyclerView) findViewById(R.id.video_list_view);

        adapterView = new AdapterView();
        videoListView.setAdapter(adapterView);

//        catalog = new Catalog(eventEmitter, ACCOUNT_ID, POLICY_KEY);
        catalog = new Catalog( eventEmitter,
                "4744899836001",
                "BCpkADawqM3O4rejQjSfnm8_C7CfzF3vWBrx_E1UY_gbfIel6Moo_qQC6HP1vjPt1TWxqNc4QW8bk2oBGgWfypaKVF8TjN_--UI2_WlFFIGTdKibkjIJRm3--fi7INarN-SFEpH88UGL-H0J");

        List<Video> videoList = new ArrayList<>();
        String[] videoIDList = getResources().getStringArray(R.array.list_of_videos_upgrad);

        for (String id : videoIDList) {
            catalog.findVideoByID(id, new VideoListener() {
                @Override
                public void onVideo(Video video) {
                    Log.e("VideoListActivity", "video:" + video.toString());
                    videoList.add(video);
                    adapterView.setVideoList(videoList);
                }
            });
        }
//
//        catalog.findPlaylistByReferenceID(PLAYLIST_REF, new PlaylistListener() {
//            @Override
//            public void onPlaylist(Playlist playlist) {
//                adapterView.setVideoList(playlist.getVideos());
//            }
//        });

    }

    @Override
    protected void onDestroy() {
        videoListView.setAdapter(null);
        videoListView.removeAllViews();
        super.onDestroy();

    }
}
