package com.y00dadev.video_test;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;
import java.util.ArrayList;


public class VideoActivity extends AppCompatActivity implements ExoPlayer.EventListener,
    GetManifestTask.GetManifestResponse {

    private static final String EXTRA_STREAM_URL = "extra_stream_url";

    private PlayerView exoPlayerView;
    private SimpleExoPlayer player;
    private DefaultTrackSelector mDefaultTrackSelector;
    private boolean isShowingTrackSelectDialog;
    private DataSource.Factory mDataSourceFactory;
    private HlsMediaSource mHlsMediaSource;
    private DefaultTrackSelector.Parameters mTrackSelectorParameters;
    private boolean namesFetched;
    private ArrayList<String> mNamesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        HttpDataSource.Factory httpDataSourceFactory = new DefaultHttpDataSourceFactory(Util.getUserAgent(this, "VideoTest"));
        mDataSourceFactory = new DefaultDataSourceFactory(this, httpDataSourceFactory);
        setContentView(R.layout.activity_video);
        exoPlayerView = findViewById(R.id.vdPlayerView);
        Button button = findViewById(R.id.trackSelectorButton);
        button.setOnClickListener(view -> {
            if (!isShowingTrackSelectDialog && TrackSelectionDialog.willHaveContent(mDefaultTrackSelector)) {
                isShowingTrackSelectDialog = true;
                TrackSelectionDialog trackSelectionDialog =
                    TrackSelectionDialog.createForTrackSelector(
                        mDefaultTrackSelector,
                        dismissedDialog -> isShowingTrackSelectDialog = false, mNamesList);
                trackSelectionDialog.show(getSupportFragmentManager(), /* tag= */ null);
            }
        });
        AspectRatioFrameLayout aspectRatioFrameLayout = findViewById(R.id.fixedFrameLayout);
        aspectRatioFrameLayout.setAspectRatio(16f/9f);

        DefaultTrackSelector.ParametersBuilder builder = new DefaultTrackSelector.ParametersBuilder(this);
        mTrackSelectorParameters = builder.build();

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(Util.SDK_INT > 23) {
            initializePlayer();
            if(exoPlayerView != null) {
                exoPlayerView.onResume();
            }
        }
    }

    private void initializePlayer() {
        Intent intent = getIntent();
        String url = intent.getExtras().getString(EXTRA_STREAM_URL);
        new GetManifestTask(this).execute(url);
        Uri uri = Uri.parse(url);
        mHlsMediaSource = new HlsMediaSource.Factory(mDataSourceFactory).createMediaSource(uri);
        TrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory();
        mDefaultTrackSelector = new DefaultTrackSelector(this, trackSelectionFactory);
        mDefaultTrackSelector.setParameters(mTrackSelectorParameters);
        player = new SimpleExoPlayer.Builder(this).setTrackSelector(mDefaultTrackSelector).build();
        player.setPlayWhenReady(true);
        exoPlayerView.setPlayer(player);
//        exoPlayerView.setPlaybackPreparer((PlaybackPreparer) this);
        player.prepare(mHlsMediaSource);

    }

    @Override
    public void finishedGetManifestTask(ArrayList<String> namesList) {
        mNamesList = namesList;
    }
}