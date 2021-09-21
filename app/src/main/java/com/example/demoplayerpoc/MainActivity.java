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
        }
        /*goto player activity*/
        startActivity(intent);
    }
}