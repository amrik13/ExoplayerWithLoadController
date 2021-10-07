package com.example.demoplayerpoc;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.C;
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
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.ads.AdsLoader;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.StyledPlayerControlView;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.util.ErrorMessageProvider;
import com.google.android.exoplayer2.util.MimeTypes;

import java.util.ArrayList;
import java.util.List;

public class PlayerActivity extends AppCompatActivity implements StyledPlayerControlView.VisibilityListener,
        QualityListAdapter.TrackClickListeners {
    private static final String TAG = PlayerActivity.class.getSimpleName();
    private StyledPlayerView playerView;
    private SimpleExoPlayer player;
    private Uri playBackURL;
    private String adTagURL;
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
    private LinearLayout qualityParentLayout;
    private ImageView audioTrackView, videoTrackView, subTitleView;
    private DefaultTrackSelector trackSelector;
    private CustomAdsLoader customAdsLoader;

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

        /*Setting Layout Instances*/
        qualityParentLayout = findViewById(R.id.quality_parent);
        audioTrackView = findViewById(R.id.audioTrackView);
        videoTrackView = findViewById(R.id.videoTrackView);
        subTitleView = findViewById(R.id.subTitleView);
        updateControllerVisibility(false);
        audioTrackView.setOnClickListener(v -> onAudioSettingClicked());
        videoTrackView.setOnClickListener(v -> onVideoSettingClicked());
        subTitleView.setOnClickListener(v -> onSubTitleSettingClicked());

        /*Player View layout instance*/
        playerView = findViewById(R.id.styled_playerView);
        playerView .requestFocus();
        /*Interface for any playback exception in exoplayer*/
        playerView.setErrorMessageProvider(new MyPlayerErrorProvider());
        playerView.setControllerVisibilityListener(this);

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
            case DemoConstants.HLS_TYPE_LIVE:
                mimeType = MimeTypes.APPLICATION_M3U8;
                playBackURL = Uri.parse(DemoConstants.HLS_LIVE_PLAYBACK_URL);
                break;

            case DemoConstants.HLS_WITH_ADS_TYPE:
                isAdsAvailable = true;
                adTagURL = DemoConstants.AD_TAG_URL;
                mimeType = MimeTypes.APPLICATION_M3U8;
                playBackURL = Uri.parse(DemoConstants.HLS_PLAYBACK_URL);
                break;

            case DemoConstants.LIVE_WITH_ADS_TYPE:
                isAdsAvailable = true;
                adTagURL = DemoConstants.AD_TAG_URL;
                mimeType = MimeTypes.APPLICATION_M3U8;
                playBackURL = Uri.parse(DemoConstants.HLS_LIVE_PLAYBACK_URL);
                break;
        }

    }
    private RecyclerView qualityRecyclerView;
    private AlertDialog settingDialog;
    private QualityListAdapter qualityListAdapter;
    private RadioButton autoRadio;

    private void resetDialog(){
        qualityRecyclerView = null;
        okBtn = null;
        qualityListAdapter = null;
        if (settingDialog != null && settingDialog.isShowing())
            settingDialog.dismiss();
        settingDialog = null;
    }

    private void onAudioSettingClicked(){
        resetDialog();
        if (trackSelector == null || !isSupportAudioTrackAvailable(trackSelector)) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.quality_list_layout, null);
        builder.setView(view);

        TextView title = view.findViewById(R.id.dialog_title);
        autoRadio = view.findViewById(R.id.item_radio_default);
        autoRadio.setChecked(!isAudioTrackSelected);
        autoRadio.setOnClickListener(new OnDefaultClicked());
        title.setText(R.string.audio_title);
        Button cancelBtn = view.findViewById(R.id.cancel_button);
        cancelBtn.setOnClickListener(v -> settingDialog.dismiss());
        okBtn = view.findViewById(R.id.okButton);
        okBtn.setOnClickListener(v -> {
            // On Setting Clicked
            settingDialog.dismiss();
            if (qualityListAdapter != null && selectedPositon >= 0){
                DefaultTrackSelector.ParametersBuilder paramBuilder = qualityListAdapter.onOkButtonClicked(parameters, selectedPositon);
                trackSelector.setParameters(paramBuilder);
            }
        });
        okBtn.setEnabled(false);
        qualityRecyclerView = view.findViewById(R.id.qualityRecycler);
        qualityListAdapter = new QualityListAdapter(this, parameters, trackInfo, audioTrackGroupArrayList, C.TRACK_TYPE_AUDIO);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        qualityRecyclerView.setLayoutManager(layoutManager);
        qualityRecyclerView.setAdapter(qualityListAdapter);
        qualityListAdapter.setTrackClickListeners(this);

        settingDialog = builder.create();
        settingDialog.show();

    }
    private Button okBtn;
    private List<TrackGroupArray> audioTrackGroupArrayList;
    private List<TrackGroupArray> videoTrackGroupArrayList;
    private MappingTrackSelector.MappedTrackInfo trackInfo;
    private DefaultTrackSelector.Parameters parameters;
    private boolean isAudioTrackSelected, isVideoTrackSelected;

    private boolean isSupportAudioTrackAvailable(DefaultTrackSelector defaultTrackSelector){
        trackInfo = defaultTrackSelector.getCurrentMappedTrackInfo();
        parameters = defaultTrackSelector.getParameters();
        if (trackInfo != null && trackInfo.getRendererCount() != 0){
            audioTrackGroupArrayList = new ArrayList<>();
            for (int i = 0 ; i < trackInfo.getRendererCount() ; i++){
                if (trackInfo.getTrackGroups(i).length > 0 && C.TRACK_TYPE_AUDIO == trackInfo.getRendererType(i)){
                    if (parameters != null && parameters.getSelectionOverride(i, trackInfo.getTrackGroups(i)) != null)
                        isAudioTrackSelected = parameters.getSelectionOverride(i, trackInfo.getTrackGroups(i)).length > 0;
                    audioTrackGroupArrayList.add(trackInfo.getTrackGroups(i));
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isSupportVideoTrackAvailable(DefaultTrackSelector defaultTrackSelector){
        trackInfo = defaultTrackSelector.getCurrentMappedTrackInfo();
        parameters = defaultTrackSelector.getParameters();
        if (trackInfo != null && trackInfo.getRendererCount() != 0){
            videoTrackGroupArrayList = new ArrayList<>();
            for (int i = 0 ; i < trackInfo.getRendererCount() ; i++){
                if (trackInfo.getTrackGroups(i).length > 0 && C.TRACK_TYPE_VIDEO == trackInfo.getRendererType(i)){
                    if (parameters != null && parameters.getSelectionOverride(i, trackInfo.getTrackGroups(i)) != null)
                        isVideoTrackSelected = parameters.getSelectionOverride(i, trackInfo.getTrackGroups(i)).length > 0;
                    videoTrackGroupArrayList.add(trackInfo.getTrackGroups(i));
                    return true;
                }
            }
        }
        return false;
    }

    private void onSubTitleSettingClicked(){

    }

    private void onVideoSettingClicked(){
        resetDialog();

        if (trackSelector == null || !isSupportVideoTrackAvailable(trackSelector)) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.quality_list_layout, null);
        builder.setView(view);

        TextView title = view.findViewById(R.id.dialog_title);
        autoRadio = view.findViewById(R.id.item_radio_default);
        autoRadio.setChecked(!isVideoTrackSelected);
        autoRadio.setOnClickListener(new OnDefaultClicked());
        title.setText(R.string.video_title);
        Button cancelBtn = view.findViewById(R.id.cancel_button);
        cancelBtn.setOnClickListener(v -> settingDialog.dismiss());
        okBtn = view.findViewById(R.id.okButton);
        okBtn.setOnClickListener(v -> {
            // On Setting Clicked
            settingDialog.dismiss();
            if (qualityListAdapter != null && selectedPositon >= 0){
                DefaultTrackSelector.ParametersBuilder paramBuilder = qualityListAdapter.onOkButtonClicked(parameters, selectedPositon);
                trackSelector.setParameters(paramBuilder);
            }
        });
        okBtn.setEnabled(false);
        qualityRecyclerView = view.findViewById(R.id.qualityRecycler);
        qualityListAdapter = new QualityListAdapter(this, parameters, trackInfo, videoTrackGroupArrayList, C.TRACK_TYPE_VIDEO);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        qualityRecyclerView.setLayoutManager(layoutManager);
        qualityRecyclerView.setAdapter(qualityListAdapter);
        qualityListAdapter.setTrackClickListeners(this);
        settingDialog = builder.create();
        settingDialog.show();
    }

    class OnDefaultClicked implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            selectedPositon = -1;
            if (qualityListAdapter != null){
                qualityListAdapter.isDefaultClicked(true);
                qualityListAdapter.notifyDataSetChanged();
            }
        }
    }
    private int selectedPositon = -1;
    @Override
    public void onTrackClicked(int position) {
        selectedPositon = position;
        if (okBtn != null){
            okBtn.setEnabled(true);
        }
        if (autoRadio != null){
            autoRadio.setChecked(false);
        }
        if (qualityListAdapter != null){
            qualityListAdapter.isDefaultClicked(false);
        }
    }

    private String getStreamType(){
        if (getIntent() != null){
            return getIntent().getType();
        }
        return "";
    }

    private MediaItem createMediaItem(){
        MediaItem mediaItem = new MediaItem.Builder()
                .setUri(playBackURL)
                .setMimeType(mimeType)
                .setAdTagUri(adTagURL)
                .setSubtitles(getSubtitleMediaList())
                .build();

        return mediaItem;
    }

    private List<MediaItem.Subtitle> getSubtitleMediaList(){
        MediaItem.Subtitle subtitle = new MediaItem.Subtitle(
            Uri.parse(DemoConstants.SUBTITLE_URL),
                DemoConstants.SUBTITLE_MIME_TYPE,
                DemoConstants.SUBTITLE_LANGUAGE,
                C.SELECTION_FLAG_DEFAULT
        );
        List<MediaItem.Subtitle> subtitlesList = new ArrayList<>();
        subtitlesList.add(subtitle);
        return subtitlesList;
    }

    private void initializePlayer(){
        /*Create MediaSourceFactory & set mediaItems for playback and pass it to the player*/
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this,
                        new DefaultHttpDataSource.Factory().setUserAgent("DemoExoplayer"));
        customAdsLoader = new CustomAdsLoader(this);

        MediaSourceFactory mediaSourceFactory = new DefaultMediaSourceFactory(dataSourceFactory)
                .setAdsLoaderProvider(adsConfiguration -> customAdsLoader.getAdsLoader())
                .setAdViewProvider(playerView);

        MediaItem mediaItem = createMediaItem();

        RenderersFactory renderersFactory = new DefaultRenderersFactory(this).
                setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON);
        trackSelector = new DefaultTrackSelector(this);

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
        if (isAdsAvailable) {
            customAdsLoader.getAdsLoader().setPlayer(player);
        } else {
            releaseAdsLoader();
        }
        player.setMediaItem(mediaItem, true);
        player.prepare();

        handler = new Handler();

        findViewById(R.id.exo_play).setOnClickListener(v -> {
            if (player != null && !player.isPlaying()){
                player.play();
                updatedPlayPauseIcon(true);
            }
        });

        findViewById(R.id.exo_pause).setOnClickListener(v -> {
            if (player != null && player.isPlaying()){
                player.pause();
                updatedPlayPauseIcon(true);
            }
        });

    }

    private void updateControllerVisibility(boolean isVisible){
        if (isVisible){
            qualityParentLayout.setVisibility(View.VISIBLE);
        } else {
            qualityParentLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onVisibilityChange(int visibility) {
        if (View.VISIBLE == visibility){
            updateControllerVisibility(true);
            updatedPlayPauseIcon(View.VISIBLE == visibility);
        } else {
            updateControllerVisibility(false);
        }
    }

    private void updatedPlayPauseIcon(boolean isVisible){
        if (player != null) {
            if (isVisible) {
                if (player.isPlaying()) {
                    findViewById(R.id.exo_pause).setVisibility(View.VISIBLE);
                    findViewById(R.id.exo_play).setVisibility(View.INVISIBLE);
                } else {
                    findViewById(R.id.exo_pause).setVisibility(View.INVISIBLE);
                    findViewById(R.id.exo_play).setVisibility(View.VISIBLE);
                }
            } else {
                if (player.isPlaying()) {
                    findViewById(R.id.exo_pause).setVisibility(View.INVISIBLE);
                    findViewById(R.id.exo_play).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.exo_play).setVisibility(View.INVISIBLE);
                    findViewById(R.id.exo_pause).setVisibility(View.VISIBLE);
                }
            }
        }
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

    private void releaseAdsLoader(){
        if (customAdsLoader != null){
            customAdsLoader.getAdsLoader().setPlayer(null);
            customAdsLoader.getAdsLoader().release();
            customAdsLoader = null;
            playerView.getOverlayFrameLayout().removeAllViews();
        }
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
        releaseAdsLoader();
    }

    @Override
    protected void onPause() {
        super.onPause();
        player.pause();
        handler.removeCallbacks(playerHandler);
    }

    private boolean isBuffering = false;

    private class MyPlayerEventListener implements Player.Listener{
        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            Log.d(TAG, "onTracksChanged: "+ trackSelections);
            Log.d(TAG, "onTracksChanged: "+ trackGroups);
        }

        @Override
        public void onIsPlayingChanged(boolean isPlaying) {
            if (!isPlaying){
                if (!isBuffering){
                    isPlayingView.setText("Status: Pause");
                    isPlayingView.setTextColor(getResources().getColor(R.color.black));
                }
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
                    isBuffering = true;
                    isPlayingView.setText("Status: Buffering");
                    isPlayingView.setTextColor(getResources().getColor(R.color.purple_500));
                    break;
                case Player.STATE_READY:
                    isBuffering = false;
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