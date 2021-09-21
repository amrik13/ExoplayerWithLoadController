package com.example.demoplayerpoc;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSourceFactory;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.ads.AdsLoader;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.util.ErrorMessageProvider;
import com.google.android.exoplayer2.util.MimeTypes;

public class PlayerActivity extends AppCompatActivity {
    private static final String TAG = PlayerActivity.class.getSimpleName();
    private StyledPlayerView playerView;
    private SimpleExoPlayer player;
    private Uri playBackURL;
    private String streamType, mimeType;
    private boolean isAdsAvailable = false;
    private Handler handler;

    private TextView contentBufferDurationView;
    private TextView isPlayingView;
    private TextView totalDurationView;
    private TextView currentPositionView;
    private TextView minMaxStartBufferDurationView;
    private TextView totalBufferPercentageView;
    private RelativeLayout logsParentLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        /*Get Stream Type from the intent. If empty return with toast message*/
        streamType = getStreamType();
        if (TextUtils.isEmpty(streamType)){
            Toast.makeText(this, "Stream Not Supported", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        /*Logs TextView Instances*/
        contentBufferDurationView = findViewById(R.id.bufferDurationView);
        isPlayingView = findViewById(R.id.isPlayingView);
        totalDurationView = findViewById(R.id.totalDurationView);
        currentPositionView = findViewById(R.id.currentPositionView);
        minMaxStartBufferDurationView = findViewById(R.id.minMaxStartBufferView);
        totalBufferPercentageView = findViewById(R.id.bufferPerctgView);
        logsParentLayout = findViewById(R.id.log_layout);

        /*Player View layout instance*/
        playerView = findViewById(R.id.styled_playerView);
        playerView .requestFocus();
        /*Interface for any playback exception in exoplayer*/
        playerView.setErrorMessageProvider(new MyPlayerErrorProvider());

        /*set mimeType and Constant Playback URL according to stream type received from Intent*/
        switch (streamType){
            case DemoConstants.DASH_TYPE:
                mimeType = MimeTypes.APPLICATION_MPD;
                playBackURL = Uri.parse(DemoConstants.DASH_PLAYBACK_URL);
                break;
            case DemoConstants.HLS_TYPE:
                mimeType = MimeTypes.APPLICATION_M3U8;
                playBackURL = Uri.parse(DemoConstants.HLS_PLAYBACK_URL);
                break;
        }

    }

    private String getStreamType(){
        if (getIntent() != null){
            return getIntent().getType();
        }
        return "";
    }

    private void initializePlayer(){
        /*Create MediaSourceFactory & set mediaItems for playback and pass it to the player*/
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this,
                        new DefaultHttpDataSource.Factory().setUserAgent("DemoExoplayer"));
        MediaSourceFactory mediaSourceFactory = new DefaultMediaSourceFactory(dataSourceFactory);
        MediaItem mediaItem = new MediaItem.Builder()
                .setUri(playBackURL)
                .setMimeType(mimeType)
                .build();

        RenderersFactory renderersFactory = new DefaultRenderersFactory(this).
                setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON);
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(this);

        LoadControl loadControl = setCustomLoadController();

        /*Create Player Instance with mediaSourceFactory and default track selector*/
        player = new SimpleExoPlayer.Builder(this, renderersFactory)
                .setMediaSourceFactory(mediaSourceFactory)
                .setLoadControl(loadControl)
                .setTrackSelector(trackSelector).build();

        /*Add Listener for playback events or state change*/
        player.addListener(new MyPlayerEventListener());
        player.setPlayWhenReady(true /*true for autoplay */);
        playerView.setPlayer(player);
        player.setMediaItem(mediaItem, true);
        player.prepare();

        handler = new Handler();
    }

    Runnable playerHandler = new Runnable() {
        @Override
        public void run() {
            float bufferDuration = ((float) player.getTotalBufferedDuration()) / 1000f;
            contentBufferDurationView.setText("Buffer:"+bufferDuration);
            float duration = ((float) player.getDuration()) / 1000f;
            totalDurationView.setText("Total Content Duration:"+duration);

            float currentPosition = ((float) player.getCurrentPosition()) / 1000f;
            currentPositionView.setText("Current Position:"+currentPosition);
            int bufferPercentage = player.getBufferedPercentage();
            totalBufferPercentageView.setText("Buffer Percentage:"+bufferPercentage);
            String minMaxStartBuffer = (DemoConstants.MIN_BUFFER_DURATION/1000)+"/"
                                        +(DemoConstants.MAX_BUFFER_DURATION/1000)+"/"
                                        +(DemoConstants.MIN_PLAYBACK_START_BUFFER/1000);
            minMaxStartBufferDurationView.setText("Min/Max/Start Buffer Time:"+minMaxStartBuffer);

            Log.i("Player_Handler","IsPlaying "+ player.isPlaying());
            Log.i("Player_Handler","BufferedPosition "+ player.getBufferedPosition()+"");
            Log.i("Player_Handler","BufferedPercentage "+ player.getBufferedPercentage()+"");
            Log.i("Player_Handler","TotalBufferedDuration "+ player.getTotalBufferedDuration()+"");
            Log.i("Player_Handler","ContentBufferedPosition "+ player.getContentBufferedPosition()+"");

            handler.postDelayed(playerHandler, 500);
        }
    };

    private LoadControl setCustomLoadController(){
        LoadControl customLoadController = new DefaultLoadControl.Builder()
                .setAllocator(new DefaultAllocator(true, 20))
                .setBufferDurationsMs(DemoConstants.MIN_BUFFER_DURATION,
                        DemoConstants.MAX_BUFFER_DURATION,
                        DemoConstants.MIN_PLAYBACK_START_BUFFER,
                        DemoConstants.MIN_PLAYBACK_RESUME_BUFFER).build();

        return customLoadController;
    }

    private AdsLoader adsLoader;
    private AdsLoader getAdsLoader(MediaItem.AdsConfiguration adsConfiguration){
        adsLoader = new ImaAdsLoader.Builder(this).build();
        adsLoader.setPlayer(player);
        return adsLoader;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (player == null)
            initializePlayer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player == null)
            initializePlayer();
        if (player!= null && !player.isPlaying())
            player.play();
        if (handler != null)
            handler.postDelayed(playerHandler, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        player.pause();
        handler.removeCallbacks(playerHandler);
    }

    private class MyPlayerEventListener implements Player.Listener{
        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            Log.d(TAG, "onTracksChanged: "+ trackSelections);
            Log.d(TAG, "onTracksChanged: "+ trackGroups);
        }

        @Override
        public void onIsPlayingChanged(boolean isPlaying) {
            if (!isPlaying){
                isPlayingView.setText("Status: Pause");
                handler.removeCallbacks(playerHandler);
                isPlayingView.setTextColor(getResources().getColor(R.color.black));
            } else{
                isPlayingView.setText("Status: Playing");
                handler.postDelayed(playerHandler, 1000);
                isPlayingView.setTextColor(getResources().getColor(R.color.teal_700));
            }
        }

        @Override
        public void onPlaybackStateChanged(int playbackState) {
            Log.d(TAG, "onPlaybackStateChanged: "+ playbackState);
            switch (playbackState) {
                case Player.STATE_BUFFERING:
                    isPlayingView.setText("Status: Buffering");
                    isPlayingView.setTextColor(getResources().getColor(R.color.purple_500));
                    break;
                case Player.STATE_READY:
                    isPlayingView.setText("Status: Playing");
                    isPlayingView.setTextColor(getResources().getColor(R.color.teal_700));
                    break;
                case Player.STATE_IDLE:

                    break;
            }
        }

        @Override
        public void onPlayerError(PlaybackException error) {
            Log.d(TAG, "onPlayerError: "+error.getMessage());
            isPlayingView.setText("Status: Error");
        }
    }

    private class MyPlayerErrorProvider implements ErrorMessageProvider<PlaybackException> {
        @Override
        public Pair<Integer, String> getErrorMessage(PlaybackException throwable) {
            Log.d(TAG, "getErrorMessage: "+ throwable);
            return Pair.create(0, throwable.getMessage());
        }
    }
}