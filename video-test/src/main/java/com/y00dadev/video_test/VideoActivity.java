package com.y00dadev.video_test;


import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerControlView;
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

    private PlayerView mExoPlayerView;
    private SimpleExoPlayer mExoPlayer;
    private DefaultTrackSelector mDefaultTrackSelector;
    private boolean isShowingTrackSelectDialog;
    private DataSource.Factory mDataSourceFactory;
    private HlsMediaSource mHlsMediaSource;
    private DefaultTrackSelector.Parameters mTrackSelectorParameters;
    private boolean namesFetched;
    private ArrayList<String> mNamesList;

    private FrameLayout mFullScreenButton;
    private ImageView mFullScreenIcon;
    private Dialog mFullScreenDialog;
    private boolean isFullScreen = false;
    private ImageView mPlayIcon;
    private FrameLayout mPlayButton;
    private boolean isPlaying = true;
    private ImageView mQualitySelectorIcon;
    private FrameLayout mQualitySelectorButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        HttpDataSource.Factory httpDataSourceFactory = new DefaultHttpDataSourceFactory(Util.getUserAgent(this, "VideoTest"));
        mDataSourceFactory = new DefaultDataSourceFactory(this, httpDataSourceFactory);
        setContentView(R.layout.activity_video);
        mExoPlayerView = findViewById(R.id.vdPlayerView);
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
            initFullscreenDialog();
            initFullscreenButton();
            initControlButtons();
            if(mExoPlayerView != null) {
                mExoPlayerView.onResume();
            }
        }
    }

    private void initControlButtons() {
        PlayerControlView controlView = mExoPlayerView.findViewById(R.id.exo_controller);
        mPlayIcon = controlView.findViewById(R.id.exo_play_icon);
        mPlayButton = controlView.findViewById(R.id.exo_play_button);
        mPlayButton.setOnClickListener(view -> {
            if(!isPlaying) {
                onPlayerPlay();
            } else {
                onPlayerPause();
            }
        });
        mQualitySelectorIcon = controlView.findViewById(R.id.exo_quality_icon);
        mQualitySelectorButton = controlView.findViewById(R.id.exo_quality_button);
        mQualitySelectorButton.setOnClickListener(view -> {
            if (!isShowingTrackSelectDialog && TrackSelectionDialog.willHaveContent(mDefaultTrackSelector)) {
                isShowingTrackSelectDialog = true;
                TrackSelectionDialog trackSelectionDialog =
                    TrackSelectionDialog.createForTrackSelector(
                        mDefaultTrackSelector,
                        dismissedDialog -> isShowingTrackSelectDialog = false, mNamesList);
                trackSelectionDialog.show(getSupportFragmentManager(), /* tag= */ null);
            }
        });

    }

    private void onPlayerPause() {
        mExoPlayer.setPlayWhenReady(false);
        mPlayIcon.setImageDrawable(ContextCompat.getDrawable(VideoActivity.this, R.drawable.exo_controls_play));
        isPlaying = false;
    }

    private void onPlayerPlay() {
        mExoPlayer.seekTo( 0);
        mExoPlayer.setPlayWhenReady(true);
        mPlayIcon.setImageDrawable(ContextCompat.getDrawable(VideoActivity.this, R.drawable.exo_controls_pause));
        isPlaying = true;
    }

    private void initFullscreenDialog() {
        mFullScreenDialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
            public void onBackPressed() {
                if(isFullScreen) {
                    closeFullScreenDialog();
                }
                super.onBackPressed();
            }
        };
    }

    private void initFullscreenButton() {
        PlayerControlView controlView = mExoPlayerView.findViewById(R.id.exo_controller);
        mFullScreenIcon = controlView.findViewById(R.id.exo_fullscreen_icon);
        mFullScreenButton = controlView.findViewById(R.id.exo_fullscreen_button);
        mFullScreenButton.setOnClickListener(view -> {
            if(!isFullScreen) {
                openFullScreenDialog();
            } else {
                closeFullScreenDialog();
            }
        });

    }

    private void closeFullScreenDialog() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ((ViewGroup) mExoPlayerView.getParent()).removeView(mExoPlayerView);
        ((AspectRatioFrameLayout) findViewById(R.id.fixedFrameLayout)).addView(mExoPlayerView);
        isFullScreen = false;
        mFullScreenDialog.dismiss();
        mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(VideoActivity.this, R.drawable.exo_controls_fullscreen_enter));
    }

    private void openFullScreenDialog() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        ((ViewGroup) mExoPlayerView.getParent()).removeView(mExoPlayerView);

/*
        AspectRatioFrameLayout aspectRatioFrameLayout = new AspectRatioFrameLayout(this);
        aspectRatioFrameLayout.setAspectRatio(16f/9f);
        exoPlayerView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        aspectRatioFrameLayout.addView(exoPlayerView);
        FrameLayout frameLayout = new FrameLayout(this);
        AspectRatioFrameLayout.LayoutParams aspectRatioFLParams = new AspectRatioFrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        aspectRatioFrameLayout.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT);
        aspectRatioFrameLayout.setLayoutParams(aspectRatioFLParams);
        frameLayout.addView(aspectRatioFrameLayout);
*/
        View customFullscreenDialogView = getLayoutInflater().inflate(R.layout.custom_fullscreen_dialog, null);
        ViewGroup customFullscreenDialogViewGroup = (ViewGroup) customFullscreenDialogView;
        View aspectRatioFrameLayoutView = customFullscreenDialogViewGroup.getChildAt(0);
        AspectRatioFrameLayout aspectRatioFrameLayout = (AspectRatioFrameLayout) aspectRatioFrameLayoutView;
        aspectRatioFrameLayout.setAspectRatio(16f/9f);
        mExoPlayerView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        aspectRatioFrameLayout.addView(mExoPlayerView);
        mFullScreenDialog.addContentView(customFullscreenDialogView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(VideoActivity.this, R.drawable.exo_controls_fullscreen_exit));
        isFullScreen = true;
        mFullScreenDialog.show();

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
        mExoPlayer = new SimpleExoPlayer.Builder(this).setTrackSelector(mDefaultTrackSelector).build();
        mExoPlayer.setPlayWhenReady(true);
        mExoPlayerView.setPlayer(mExoPlayer);
//        exoPlayerView.setPlaybackPreparer((PlaybackPreparer) this);
        mExoPlayer.prepare(mHlsMediaSource);

    }

    @Override
    public void finishedGetManifestTask(ArrayList<String> namesList) {
        mNamesList = namesList;
    }
}