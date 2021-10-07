package com.example.demoplayerpoc;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*button for hls stream playback*/
        findViewById(R.id.hls_btn).setOnClickListener(v -> {
            redirectToPlayer(DemoConstants.HLS_TYPE);
        });

        /*button for dash stream playback*/
        findViewById(R.id.dash_button).setOnClickListener(v -> {
            redirectToPlayer(DemoConstants.DASH_TYPE);
        });

        /*button for dash stream playback*/
        findViewById(R.id.live_btn).setOnClickListener(v -> {
            redirectToPlayer(DemoConstants.HLS_TYPE_LIVE);
        });

        /*button for IMA Ads with HLS playback*/
        findViewById(R.id.ads_button).setOnClickListener(v -> {
            redirectToPlayer(DemoConstants.HLS_WITH_ADS_TYPE);
        });

        /*button for IMA Ads with HLS playback*/
        findViewById(R.id.ads_buttonLive).setOnClickListener(v -> {
            redirectToPlayer(DemoConstants.LIVE_WITH_ADS_TYPE);
        });
    }

    private void redirectToPlayer(String streamType){
        Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
        switch (streamType){
            case DemoConstants.HLS_TYPE:
                intent.setType(DemoConstants.HLS_TYPE);
                break;
            case DemoConstants.DASH_TYPE:
                intent.setType(DemoConstants.DASH_TYPE);
                break;
            case DemoConstants.HLS_TYPE_LIVE:
                intent.setType(DemoConstants.HLS_TYPE_LIVE);
                break;
            case DemoConstants.HLS_WITH_ADS_TYPE:
                intent.setType(DemoConstants.HLS_WITH_ADS_TYPE);
                break;
                case DemoConstants.LIVE_WITH_ADS_TYPE:
                intent.setType(DemoConstants.LIVE_WITH_ADS_TYPE);
                break;
        }
        /*goto player activity*/
        startActivity(intent);
    }
}